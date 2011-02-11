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

import junit.framework.TestCase;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Tests class {@link org.codehaus.mojo.aspectj.AbstractAjcCompiler}
 * 
 * @author <a href="mailto:tel@objectnet.no">Thor Age Eldby</a>
 */
public class AbstractAjcCompilerTest
    extends TestCase
{

    /** Compiler mojo instance */
    private AjcTestCompileMojo ajcCompMojo;

    /**
     * @inheritDoc
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();
        ajcCompMojo = new AjcTestCompileMojo();
        MavenProject project = new MavenProject( new Model() );
        ajcCompMojo.project = project;
        project.setDependencyArtifacts( new HashSet() );
    }

    /**
     * Verifies that if not stated no -inpath argument should be found in the ajc arguments
     * {@link AbstractAjcCompiler#execute()}
     * 
     * @throws Exception
     */
    public void testGetAjcArguments_noWeaveArtifacts()
        throws Exception
    {
        ajcCompMojo.assembleArguments();
        List args = ajcCompMojo.ajcOptions;
        assertFalse( args.contains( "-inpath" ) );
    }

    /**
     * Tests that the compiler fails as it should if told to weave an artifact not listed in the project dependencies.
     * 
     * @throws Exception
     */
    public void testGetAjcArguments_weaveArtifactsNotProjectDependecy()
        throws Exception
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
     * @throws Exception
     */
    public void testGetAjcArguments_weaveArtifacts()
        throws Exception
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
     * @throws Exception
     */
    public void testGetAjcArguments_weaveArtifactsWithClassifier()
        throws Exception
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
     * @throws Exception
     */
    public void testGetAjcArguments_weaveArtifactsWithType()
        throws Exception
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
     * Verifies that if not stated no -aspectpath argument should be found in the ajc arguments
     * {@link AbstractAjcCompiler#execute()}
     * 
     * @throws Exception
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
     * 
     * @throws Exception
     */
    public void testGetAjcArguments_libraryArtifactsNotProjectDependecy()
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
     * @throws Exception
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

}
