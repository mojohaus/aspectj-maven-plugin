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
import org.codehaus.plexus.util.FileUtils;

/**
 * Plugin testcases.
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 *
 */
public class AjcCompilerMojoTest
    extends CompilerMojoTestBase
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
            ajcMojo.setComplianceLevel( "1.5" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
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
            ajcMojo.setComplianceLevel( "1.4" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
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
            ajcMojo.setComplianceLevel( "1.4" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
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
            ajcMojo.setComplianceLevel( "1.4" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
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
            ajcMojo.setComplianceLevel( "1.5" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
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
    public void testWithInclutionsFullClassName()
        throws Exception
    {
        try
        {
            ajcMojo.setComplianceLevel( "1.4" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
            ajcMojo.includes= new String[]{"org/codehaus/mojo/aspectj/OldStyleAspect.aj"};
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
    public void testWithInclutionsAntStyle()
        throws Exception
    {
        try
        {
            ajcMojo.setComplianceLevel( "1.4" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
            ajcMojo.includes= new String[]{"**/Old*eAspect.aj"};
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
    public void testWithExclutionsFullClassName()
        throws Exception
    {
        try
        {
            ajcMojo.setComplianceLevel( "1.4" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
            ajcMojo.excludes= new String[]{"org/codehaus/mojo/aspectj/Azpect.java"};
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
    public void testWithExclutionsAntStyle()
        throws Exception
    {
        try
        {
            ajcMojo.setComplianceLevel( "1.4" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
            ajcMojo.excludes= new String[]{"**/Az*.*"};
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
            ajcMojo.setComplianceLevel( "1.5" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
            ajcMojo.setOutxml( true );
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
            ajcMojo.setComplianceLevel( "1.5" );
            ajcMojo.setVerbose( true );
            ajcMojo.setShowWeaveInfo( true );
            ajcMojo.setOutxmlfile( "/META-INF/customaop.xml" );
            ajcMojo.execute();
            assertTrue( FileUtils.fileExists( project.getBuild().getOutputDirectory() + "/META-INF/customaop.xml" ) );
        }
        catch ( Exception e )
        {
            fail();
        }
    }

}
