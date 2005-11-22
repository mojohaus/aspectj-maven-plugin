package org.codehaus.mojo.aspectj;

/**
 * The MIT License
 *
 * Copyright (c) 2005, The Codehaus
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
import org.codehaus.mojo.aspectj.AbstractAjcCompiler;
import org.codehaus.mojo.aspectj.AjcCompileMojo;
import org.codehaus.plexus.util.FileUtils;
import org.springframework.core.io.ClassPathResource;

/**
 * Plugin testcases.
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 *
 */
public class AjcCompilerMojoTest
    extends TestCase
{
    MavenProject project = new MavenProject( new Model() );

    AbstractAjcCompiler ajcMojo = new AjcCompileMojo();

    String basedir = "";

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
        ClassPathResource cpr = new ClassPathResource( "test-project/pom.xml" );
        basedir = cpr.getFile().getAbsolutePath();
        String temp = cpr.getFile().getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath();
        basedir = temp + "/src/test/resources/test-project/";
        project.getBuild().setOutputDirectory( basedir + "/target/classes" );
        ajcMojo.basedir = new File( basedir );

        Set artifacts = new HashSet();

        Artifact aspectJTools = new DefaultArtifact( "aspectj", "aspectjtools", VersionRange
            .createFromVersion( "1.5.0_M5" ), "compile", "jar", "", new DefaultArtifactHandler( "" ) );
        Artifact aspectJTRt = new DefaultArtifact( "aspectj", "aspectjrt",
                                                   VersionRange.createFromVersion( "1.5.0_M5" ), "compile", "jar", "",
                                                   new DefaultArtifactHandler( "" ) );

        aspectJTools.setFile( new File( localRepository.getBasedir() + "/" + localRepository.pathOf( aspectJTools )
            + ".jar" ) );
        aspectJTRt.setFile( new File( localRepository.getBasedir() + "/" + localRepository.pathOf( aspectJTRt )
            + ".jar" ) );

        artifacts.add( aspectJTools );
        artifacts.add( aspectJTRt );
        project.setArtifacts( artifacts );
    }

    /**
     * @throws Exception
     */
    public void testUsingBuildConfigFileAndAspectJ5()
        throws Exception
    {
        try
        {
            ajcMojo.ajdtBuildDefFile = basedir + "build-1-5.ajproperties";
            ajcMojo.options = new String[] { "-1.5", "-verbose", "-showWeaveInfo" };
            ajcMojo.execute();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Exception : " + e.toString() );
        }
    }

    /**
     * @throws Exception
     */
    public void testUsingBuildConfigFileUsingOldStyle()
        throws Exception
    {
        try
        {
            ajcMojo.ajdtBuildDefFile = basedir + "build-1-2.ajproperties";
            ajcMojo.options = new String[] { "-1.4", "-verbose", "-showWeaveInfo" };
            ajcMojo.execute();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Exception : " + e.toString() );
        }
    }

    /**
     * @throws Exception
     */
    public void testUsingBuildConfigFileExclusionsUsingOldStyle()
        throws Exception
    {
        try
        {
            ajcMojo.ajdtBuildDefFile = basedir + "build-1-2-using-exclusions.ajproperties";
            ajcMojo.options = new String[] { "-1.4", "-verbose", "-showWeaveInfo" };
            ajcMojo.execute();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Exception : " + e.toString() );
        }
    }

    /**
     * @throws Exception
     */
    public void testWithWrongCompilerVersion()
        throws Exception
    {
        try
        {
            ajcMojo.ajdtBuildDefFile = basedir + "build-1-5.ajproperties";
            ajcMojo.options = new String[] { "-1.4", "-verbose", "-showWeaveInfo" };
            ajcMojo.execute();
            fail();
        }
        catch ( Exception e )
        {
            // success
        }
    }

    /**
     * @throws Exception
     */
    public void testWithUnsupportedCompilerOption()
        throws Exception
    {
        try
        {
            ajcMojo.ajdtBuildDefFile = basedir + "build-1-5.ajproperties";
            ajcMojo.options = new String[] { "-1.4", "-virbase", "-showWeaveInfo" };
            ajcMojo.execute();
            fail();
        }
        catch ( Exception e )
        {
            // success
        }
    }

    /**
     * @throws Exception
     */
    public void testWithSourceDir()
        throws Exception
    {
        try
        {
            ajcMojo.sourceDir = "src/main";
            ajcMojo.options = new String[] { "-1.5", "-verbose", "-showWeaveInfo", "-nowarn" };
            ajcMojo.execute();
        }
        catch ( Exception e )
        {
            fail();
        }
    }

    /**
     * @throws Exception
     */
    public void testCreateDefaultLoadTimeWeaveXml()
        throws Exception
    {
        try
        {
            ajcMojo.sourceDir = "src/main";
            ajcMojo.options = new String[] { "-1.5", "-verbose", "-showWeaveInfo", "-outxml" };
            ajcMojo.execute();
            assertTrue( FileUtils.fileExists( project.getBuild().getOutputDirectory() + "/META-INF/aop.xml" ) );
        }
        catch ( Exception e )
        {
            fail();
        }
    }

    /**
     * @throws Exception
     */
    public void testCreateCustomLoadTimeWeaveXml()
        throws Exception
    {
        try
        {
            ajcMojo.sourceDir = "src/main";
            ajcMojo.options = new String[] {
                "-1.5",
                "-verbose",
                "-showWeaveInfo",
                "-outxmlfile",
                "/META-INF/customaop.xml" };
            ajcMojo.execute();
            assertTrue( FileUtils.fileExists( project.getBuild().getOutputDirectory() + "/META-INF/customaop.xml" ) );
        }
        catch ( Exception e )
        {
            fail();
        }
    }

    /**
     * Clean up targetarea after a testcase is run.
     * So we make shure, we don't get sideeffects between testruns.
     */
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        FileUtils.deleteDirectory( basedir + "target" );
    }
}
