package org.codehaus.mojo.aspectj;

import java.util.Locale;

import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;

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

/**
 * Reporting testcases.
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 *
 */
public class AjcReportMojoTest
    extends AbstractAjcMojoTest
{
    /**
     * 
     */
    protected void setUp()
        throws Exception
    {
        ajcMojo = new AjcReportMojo();
        super.setUp();
    }

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
            ajcMojo.options = new String[] { "-verbose", "-private", "-source", "1.5" };
            ajcMojo.executeReport( Locale.ENGLISH );
            assertTrue( FileUtils.fileExists( project.getBuild().getDirectory() + "/site/aspectj-doc/index.html" ) );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "Exception : " + e.toString() );
        }

    }


}
