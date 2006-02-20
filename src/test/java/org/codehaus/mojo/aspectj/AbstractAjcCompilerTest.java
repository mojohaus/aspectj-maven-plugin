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
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Tests class {@link org.codehaus.mojo.aspectj.AbstractAjcCompiler}
 * 
 * @author <a href="mailto:tel@objectnet.no">Thor �ge Eldby</a>
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
     * Tests the artifact weave handling in
     * {@link AbstractAjcCompiler#execute()}
     * 
     * @throws Exception
     *             any
     */
    public void testGetAjcArguments_weaveArtifacts()
        throws Exception
    {
        // First no weave defined
        List args = ajcCompMojo.getAjcArguments();
        assertFalse( args.contains( "-inpath" ) );

        // ... then weave defined, but not member of project depencies
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        try
        {
            ajcCompMojo.weaveDependencies = new Module[1];
            ajcCompMojo.weaveDependencies[0] = module1;
            ajcCompMojo.getAjcArguments();
            fail( "Should fail quite miserably" );
        }
        catch ( MojoExecutionException e )
        {
            // good thing
        }

        // ... and now the weave is defined and a member
        ajcCompMojo.weaveDependencies = new Module[2];
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
        args = ajcCompMojo.getAjcArguments();
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
     * Tests the artifact weave handling in
     * {@link AbstractAjcCompiler#execute()}
     * 
     * @throws Exception
     *             any
     */
    public void testGetAjcArguments_libraryArtifacts()
        throws Exception
    {
        // First no weave defined
        List args = ajcCompMojo.getAjcArguments();
        assertFalse( args.contains( "-aspectpath" ) );

        // ... then weave defined, but not member of project depencies
        Module module1 = new Module();
        String mod1Group = "dill.group";
        module1.setGroupId( mod1Group );
        String mod1Artifact = "dall.artifact";
        module1.setArtifactId( mod1Artifact );
        try
        {
            ajcCompMojo.aspectLibraries= new Module[1];
            ajcCompMojo.aspectLibraries[0] = module1;
            ajcCompMojo.getAjcArguments();
            fail( "Should fail quite miserably" );
        }
        catch ( MojoExecutionException e )
        {
            // good thing
        }

        // ... and now the weave is defined and a member
        ajcCompMojo.aspectLibraries = new Module[2];
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
        args = ajcCompMojo.getAjcArguments();
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
