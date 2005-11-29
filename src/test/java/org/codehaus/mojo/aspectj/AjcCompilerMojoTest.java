package org.codehaus.mojo.aspectj;

/**
 * The MIT License
 *
 * Copyright (c) 2005, Kaare Nilsen
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

import org.codehaus.plexus.util.FileUtils;

/**
 * Plugin testcases.
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 *
 */
public class AjcCompilerMojoTest
    extends AbstractAjcMojoTest
{
    
    /**
     * 
     */
    protected void setUp()
        throws Exception
    {
        ajcMojo = new AjcCompileMojo();
        super.setUp();
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
     * @throws Exception
     */
    public void _testCheckModifications()
        throws Exception
    {
        try
        {
            ajcMojo.sourceDir = "src/main";
            ajcMojo.options = new String[] { "-1.5", "-verbose", "-showWeaveInfo" };
            assertTrue( ajcMojo.checkModifications( ajcMojo.getBuildFiles() ) );
            ajcMojo.execute();
            assertFalse( ajcMojo.checkModifications( ajcMojo.getBuildFiles() ) );
            File aSourceFile = FileUtils.getFile(project.getBuild().getSourceDirectory()+"/org/codehaus/mojo/aspectj/Azpect.java");
            aSourceFile.setLastModified(System.currentTimeMillis());
            assertTrue( ajcMojo.checkModifications( ajcMojo.getBuildFiles() ) );
        }
        catch ( Exception e )
        {
            fail();
        }
    }

}
