package org.codehaus.mojo.aspectj;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderConsoleLogger;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;

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

/**
 * Reporting testcases.
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 *
 */
public class AjcReportMojoTest
    extends TestCase
{
    MavenProject project = new MavenProject( new Model() );

    AjcReportMojo ajcMojo = new AjcReportMojo();;

    String basedir = "";

    /**
     * @throws MavenReportException 
     * 
     *
     */
    public void testCreateReport()
    {
        try
        {
            ajcMojo.ajdtBuildDefFile = basedir + "build-1-5.ajproperties";
            ajcMojo.setVerbose(true);
            ajcMojo.setPrivateScope(true);
            ajcMojo.setComplianceLevel("1.5");
            ajcMojo.executeReport( Locale.ENGLISH );
            assertTrue( FileUtils.fileExists( project.getBuild().getDirectory() + "/site/aspectj-report/index.html" ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Exception : " + e.toString() );
        }

    }

    /**
     * 
     */
    protected void setUp()
        throws Exception
    {
        MavenEmbedder embedder = new MavenEmbedder();

        embedder.setClassLoader( Thread.currentThread().getContextClassLoader() );
        embedder.setLogger( new MavenEmbedderConsoleLogger() );
        embedder.start();
        ArtifactRepository localRepository = embedder.getLocalRepository();

        ajcMojo.project = project;
        String temp = new File( "." ).getAbsolutePath();
        basedir = temp.substring( 0, temp.length() - 2 ) + "/src/test/projects/test-project/";
        project.getBuild().setDirectory( basedir + "/target" );
        project.getBuild().setOutputDirectory( basedir + "/target/classes" );
        project.getBuild().setTestOutputDirectory( basedir + "/target/test-classes" );
        project.getBuild().setSourceDirectory( basedir + "/src/main/java" );
        project.getBuild().setTestSourceDirectory( basedir + "/src/test/java" );
        project.addCompileSourceRoot(project.getBuild().getSourceDirectory());
        project.addTestCompileSourceRoot(project.getBuild().getTestSourceDirectory());
        ajcMojo.basedir = new File( basedir );

        Set artifacts = new HashSet();

        Artifact junit = new DefaultArtifact( "junit", "junit", VersionRange.createFromVersion( "3.8.1" ), "test",
                                              "jar", "", new DefaultArtifactHandler( "" ) );
        Artifact aspectJTools = new DefaultArtifact( "aspectj", "aspectjtools", VersionRange
            .createFromVersion( "1.5.0" ), "compile", "jar", "", new DefaultArtifactHandler( "" ) );
        Artifact aspectJTRt = new DefaultArtifact( "aspectj", "aspectjrt", VersionRange.createFromVersion( "1.5.0" ),
                                                   "compile", "jar", "", new DefaultArtifactHandler( "" ) );

        junit.setFile( new File( localRepository.getBasedir() + "/" + localRepository.pathOf( junit ) + ".jar" ) );
        aspectJTools.setFile( new File( localRepository.getBasedir() + "/" + localRepository.pathOf( aspectJTools )
            + ".jar" ) );
        aspectJTRt.setFile( new File( localRepository.getBasedir() + "/" + localRepository.pathOf( aspectJTRt )
            + ".jar" ) );

        artifacts.add( aspectJTools );
        artifacts.add( aspectJTRt );
        artifacts.add( junit );
        project.setDependencyArtifacts( artifacts );

    }

    /**
     * Clean up targetarea after a testcase is run.
     * So we make shure, we don't get sideeffects between testruns.
     */
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        FileUtils.deleteDirectory( project.getBuild().getDirectory() );
    }

}
