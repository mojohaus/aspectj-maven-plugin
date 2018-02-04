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
import java.util.Collections;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Baseclass for AjcMojo testcases. Sets up the testproject, and cleans
 * up afterwards.
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public abstract class CompilerMojoTestBase
    extends AbstractMojoTestCase
{

    MavenProject project = new MavenProject( new Model() );

    AbstractAjcCompiler ajcMojo;

    String basedir = "";

    /**
     * 
     */
    protected void setUp()
        throws Exception
    {
        // prepare plexus environment
        super.setUp();
        
        ajcMojo.project = project;
        String temp = new File( "." ).getAbsolutePath();
        basedir = temp.substring( 0, temp.length() - 2 ) + "/src/test/projects/" + getProjectName() + "/";
        project.getBuild().setDirectory( basedir + "/target" );
        project.getBuild().setOutputDirectory( basedir + "/target/classes" );
        project.getBuild().setTestOutputDirectory( basedir + "/target/test-classes" );
        project.getBuild().setSourceDirectory( basedir + "/src/main/java" );
        project.getBuild().setTestSourceDirectory( basedir + "/src/test/java" );
        project.addCompileSourceRoot( project.getBuild().getSourceDirectory() );
        project.addTestCompileSourceRoot( project.getBuild().getTestSourceDirectory() );
        ajcMojo.basedir = new File( basedir );
        
        setVariableValueToObject( ajcMojo, "outputDirectory", new File( project.getBuild().getOutputDirectory() ) );
        setVariableValueToObject( ajcMojo, "generatedSourcesDirectory", new File( project.getBuild().getDirectory() + "/generated-sources/annotations" ) );

        ArtifactHandler artifactHandler = new MockArtifactHandler();
        Artifact artifact = new MockArtifact( "dill", "dall" );
        artifact.setArtifactHandler( artifactHandler );
        project.setArtifact( artifact );
        project.setDependencyArtifacts( Collections.emptySet() );

    }

    /**
     * Clean up targetarea after a testcase is run.
     * So we make shure, we don't get sideeffects between testruns.
     */
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        try
        {
            FileUtils.deleteDirectory( project.getBuild().getDirectory() );
        }
        catch ( Exception ex )
        {
            ;// Only a problem on windows. we really do not care.. if we cant delete the file
            // It is probably not there
        }
    }

    abstract String getProjectName();
}
