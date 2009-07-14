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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

import junit.framework.TestCase;

/**
 * Tests class {@link org.codehaus.mojo.aspectj.AjcHelper}
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class AjcHelperTest
    extends TestCase
{
    /**
     * 
     * @throws Exception
     */
    public void testGetAsCsv()
        throws Exception
    {
        String[] tests = new String[] { "kaare", "java", "aspectJ" };
        assertEquals( "kaare,java,aspectJ", AjcHelper.getAsCsv( tests ) );
    }

    /**
     * 
     * @throws Exception
     */
    public void testGetSourcesEmptyBaseDir()
        throws Exception
    {
        List baseDirs = new ArrayList();
        baseDirs.add( "src/shouldNotExist" );
        HashSet sources = (HashSet) AjcHelper.getBuildFilesForSourceDirs( baseDirs,
            new String[] { AjcHelper.DEFAULT_INCLUDES },
            new String[] { AjcHelper.DEFAULT_EXCLUDES } );
        assertTrue( sources.isEmpty() );
    }

    /**
     * 
     * @throws Exception
     */
    public void testBuildConfigFile()
    {
        final File baseDir = new File(".");
        final String fileName = "test.lst";
        final String fileAbsolutePath = baseDir.getAbsolutePath() + File.separator + fileName;
        
        List args = new ArrayList();
        args.add("-classpath");
        args.add("a:b:c");
        args.add("-showWeaveInfo");
        args.add("/home/aspectj/AFile");
        args.add("/home/aspectj/AnotherFile");
        try
        {
            AjcHelper.writeBuildConfigToFile(args,fileName,baseDir);
            assertTrue("Config file not written to disk",FileUtils.fileExists(fileAbsolutePath));
            List readArgs = AjcHelper.readBuildConfigFile(fileName,baseDir);
            assertEquals(args,readArgs);
        } catch (Exception e)
        {
            fail("Unexpected exception: " + e.toString());
            if (FileUtils.fileExists(fileAbsolutePath))
            {
                FileUtils.fileDelete(fileAbsolutePath);
            }
        }
    }
}
