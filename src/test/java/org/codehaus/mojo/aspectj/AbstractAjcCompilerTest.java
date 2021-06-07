package org.codehaus.mojo.aspectj;

/**
 * The MIT License
 *
 * Copyright 2005-2006 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;

/**
 * Tests class {@link org.codehaus.mojo.aspectj.AbstractAjcCompiler}
 * 
 * @author <a href="mailto:tel@objectnet.no">Thor Age Eldby</a>
 */
public class AbstractAjcCompilerTest
    extends AbstractMojoTestCase
{

    /** Compiler mojo instance */
    private AjcTestCompileMojo ajcCompMojo;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        ajcCompMojo = new AjcTestCompileMojo();
        MavenProject project = new MavenProject( new Model() );
        ajcCompMojo.project = project;
        setVariableValueToObject( ajcCompMojo, "outputDirectory", getTestFile( "/target/test-classes" ) );
        setVariableValueToObject( ajcCompMojo, "generatedTestSourcesDirectory", getTestFile( "/target/generated-test-sources/test-annotations" ) );
        project.setDependencyArtifacts( new HashSet() );
    }

    /**
     * Verifies that if not stated no -inpath argument should be found in the ajc arguments
     * {@link AbstractAjcCompiler#execute()}
     *
     * @throws MojoExecutionException if the mojo fails to execute
     */
    public void testGetAjcArguments_noWeaveArtifacts() throws MojoExecutionException
    {
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertFalse( args.contains( "-inpath" ) );
    }

    /**
     * Tests that the compiler fails as it should if told to weave an artifact not listed in the project dependencies.
     */
    public void testGetAjcArguments_weaveArtifactsNotProjectDependecy()
    {
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        try
        {
            ajcCompMojo.weaveDependencies = new Module[1];
            ajcCompMojo.weaveDependencies[0] = module1;
            ajcCompMojo.assembleArguments();
            fail( "Should fail quite miserably" );
        }
        catch ( MojoExecutionException e )
        {
            // good thing
        }
    }

    /**
     * Tests if modules told to weave that are found in the project dependencies actually are found in the .inpath ajc
     * argument,.
     *
     * @throws MojoExecutionException if the mojo fails to execute
     */
    public void testGetAjcArguments_weaveArtifacts() throws MojoExecutionException
    {
        ajcCompMojo.weaveDependencies = new Module[2];
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        ajcCompMojo.weaveDependencies[0] = module1;
        Module module2 = new Module();
        String mod2Group = "foooup";
        module2.setGroupId( mod2Group );
        String mod2Artifact = "bartifact";
        module2.setArtifactId( mod2Artifact );
        ajcCompMojo.weaveDependencies[1] = module2;
        // Modify project to include depencies
        Set artifacts = new HashSet();
        artifacts.add( new MockArtifact( mod1Group, mod1Artifact ) );
        artifacts.add( new MockArtifact( mod2Group, mod2Artifact ) );
        ajcCompMojo.project.setArtifacts( artifacts );
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue( args.contains( "-inpath" ) );
        Iterator it = args.iterator();
        while ( !it.next().equals( "-inpath" ) )
        {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue( weavePath.indexOf( File.pathSeparator ) != -1 );
        assertTrue( weavePath.indexOf( mod1Artifact ) != -1 );
        assertTrue( weavePath.indexOf( mod2Artifact ) != -1 );
    }

    /**
     * Tests if modules told to weave that are found in the project dependencies actually are found in the .inpath ajc
     * argument, considering classifiers.
     *
     * @throws MojoExecutionException if the mojo fails to execute
     */
    public void testGetAjcArguments_weaveArtifactsWithClassifier() throws MojoExecutionException
    {
        ajcCompMojo.weaveDependencies = new Module[1];
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        String mod1classifier = "dev";
        module1.setClassifier( mod1classifier );
        ajcCompMojo.weaveDependencies[0] = module1;
        String mod2classifier = "stable";
        // Modify project to include dependencies
        Set artifacts = new HashSet();
        artifacts.add( new MockArtifact( mod1Group, mod1Artifact, mod1classifier, "jar" ) );
        artifacts.add( new MockArtifact( mod1Group, mod1Artifact, mod2classifier, "jar" ) );
        ajcCompMojo.project.setArtifacts( artifacts );
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue( args.contains( "-inpath" ) );
        Iterator it = args.iterator();
        while ( !it.next().equals( "-inpath" ) )
        {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue( weavePath.indexOf( mod1Artifact ) != -1 );
        assertTrue( weavePath.indexOf( mod1classifier ) != -1 );
        assertFalse( weavePath.indexOf( mod2classifier ) != -1 );
    }

    /**
     * Tests if modules told to weave that are found in the project dependencies actually are found in the .inpath ajc
     * argument, considering type.
     *
     * @throws MojoExecutionException if the mojo fails to execute
     */
    public void testGetAjcArguments_weaveArtifactsWithType() throws MojoExecutionException
    {
        ajcCompMojo.weaveDependencies = new Module[1];
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        String mod1type = "foo";
        module1.setType( mod1type );
        ajcCompMojo.weaveDependencies[0] = module1;
        String mod2type = "bar";
        // Modify project to include dependencies
        Set artifacts = new LinkedHashSet();
        artifacts.add( new MockArtifact( mod1Group, mod1Artifact, null, mod1type ) );
        artifacts.add( new MockArtifact( mod1Group, mod1Artifact, null, mod2type ) );
        ajcCompMojo.project.setArtifacts( artifacts );
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue( args.contains( "-inpath" ) );
        Iterator it = args.iterator();
        while ( !it.next().equals( "-inpath" ) )
        {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue( weavePath.indexOf( mod1Artifact ) != -1 );
        assertTrue( weavePath.indexOf( "." + mod1type ) != -1 );
        assertFalse( weavePath.indexOf( mod2type ) != -1 );
    }

    /**
     * Tests if modules told to weave classes that are found in directories.
     *
     * @throws Exception on test error
     */
    public void testGetAjcArguments_weaveDirectories()
        throws Exception
    {
        String dir1 = "target/classes1";
        String dir2 = "target/classes2";
        ajcCompMojo.weaveDirectories = new String[] { dir1, dir2 };
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue( args.contains( "-inpath" ) );
        Iterator it = args.iterator();
        while ( !it.next().equals( "-inpath" ) )
        {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue( weavePath.indexOf( File.pathSeparator ) != -1 );
        assertTrue( weavePath.indexOf( dir1 ) != -1 );
        assertTrue( weavePath.indexOf( dir2 ) != -1 );
    }
    
    /**
     * Verifies that if not stated no -aspectpath argument should
     * be found in the ajc arguments
     * {@link AbstractAjcCompiler#execute()}
     *
     * @throws Exception on test error
     */
    public void testGetAjcArguments_noLibraryArtifacts()
        throws Exception
    {
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertFalse( args.contains( "-aspectpath" ) );
    }

    /**
     * Tests that the compiler fails as it should if told to weave an library artifact not listed in the project
     * dependencies.
     */
    public void testGetAjcArguments_libraryArtifactsNotProjectDependency()
    {
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        try
        {
            ajcCompMojo.aspectLibraries = new Module[1];
            ajcCompMojo.aspectLibraries[0] = module1;
            ajcCompMojo.assembleArguments();
            fail( "Should fail quite miserably" );
        }
        catch ( MojoExecutionException e )
        {
            // good thing
        }
    }

    /**
     * Tests if modules told to weave that are found in the project dependencies actually are found in the .inpath ajc
     * argument,.
     *
     * @throws Exception on test error
     */
    public void testGetAjc_libraryArtifacts()
        throws Exception
    {
        ajcCompMojo.aspectLibraries = new Module[2];
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        ajcCompMojo.aspectLibraries[0] = module1;
        Module module2 = new Module();
        String mod2Group = "foooup";
        module2.setGroupId( mod2Group );
        String mod2Artifact = "bartifact";
        module2.setArtifactId( mod2Artifact );
        ajcCompMojo.aspectLibraries[1] = module2;
        // Modify project to include depencies
        Set artifacts = new HashSet();
        artifacts.add( new MockArtifact( mod1Group, mod1Artifact ) );
        artifacts.add( new MockArtifact( mod2Group, mod2Artifact ) );
        ajcCompMojo.project.setArtifacts( artifacts );
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue( args.contains( "-aspectpath" ) );
        Iterator it = args.iterator();
        while ( !it.next().equals( "-aspectpath" ) )
        {
            // don't do nothing
        }
        String weavePath = (String) it.next();
        assertTrue( weavePath.indexOf( File.pathSeparator ) != -1 );
        assertTrue( weavePath.indexOf( mod1Artifact ) != -1 );
        assertTrue( weavePath.indexOf( mod2Artifact ) != -1 );
    }

    /**
     * Verifies that if not stated no --module-path or -p argument should
     * be found in the ajc arguments
     * {@link AbstractAjcCompiler#execute()}
     *
     * @throws Exception on test error
     */
    public void testGetAjcArguments_noModulePath()
        throws Exception
    {
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertFalse( args.contains( "--module-path" ) );
        assertFalse( args.contains( "-p" ) );
    }

    /**
     * Tests that the compiler fails as it should if told to weave a module artifact not listed in the project
     * dependencies.
     */
    public void testGetAjcArguments_moduleArtifactsNotProjectDependency()
    {
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        try
        {
            ajcCompMojo.javaModules = new Module[1];
            ajcCompMojo.javaModules[0] = module1;
            ajcCompMojo.assembleArguments();
            fail( "Should fail quite miserably" );
        }
        catch ( MojoExecutionException e )
        {
            // good thing
        }
    }

    /**
     * Tests that Java 9+ module path works as expected if listed modules also exist as dependencies
     *
     * @throws MojoExecutionException if the mojo fails to execute
     */
    public void testGetAjc_moduleArtifacts() throws MojoExecutionException
    {
        ajcCompMojo.javaModules = new Module[2];
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        ajcCompMojo.javaModules[0] = module1;
        Module module2 = new Module();
        String mod2Group = "foooup";
        module2.setGroupId( mod2Group );
        String mod2Artifact = "bartifact";
        module2.setArtifactId( mod2Artifact );
        ajcCompMojo.javaModules[1] = module2;
        // Modify project to include depencies
        Set artifacts = new HashSet();
        artifacts.add( new MockArtifact( mod1Group, mod1Artifact ) );
        artifacts.add( new MockArtifact( mod2Group, mod2Artifact ) );
        ajcCompMojo.project.setArtifacts( artifacts );
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertTrue( args.contains( "--module-path" ) );
        Iterator it = args.iterator();
        while ( !it.next().equals( "--module-path" ) )
        {
            // don't do nothing
        }
        String modulePath = (String) it.next();
        assertTrue( modulePath.indexOf( File.pathSeparator ) != -1 );
        assertTrue( modulePath.indexOf( mod1Artifact ) != -1 );
        assertTrue( modulePath.indexOf( mod2Artifact ) != -1 );
    }

    // MASPECTJ-103
    public void testGetAJc_EmptyClassifier() throws Exception
    {
        String groupId = "groupId";
        String artifactId = "artifactId";
        String classifier = ""; //could be result of filtering properties

        Module module = new Module();
        module.setGroupId( groupId );
        module.setArtifactId( artifactId );
        module.setClassifier( classifier );
        
        ajcCompMojo.aspectLibraries = new Module[] { module };
        
        Set artifacts = new HashSet();
        artifacts.add( new MockArtifact( groupId, artifactId ) );
        ajcCompMojo.project.setArtifacts( artifacts );
        ajcCompMojo.assembleArguments();
        // should not fail
    }

}
