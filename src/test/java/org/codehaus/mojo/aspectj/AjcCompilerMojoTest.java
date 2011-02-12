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
    public void testModificationSet()
        throws Exception
    {
        try
        {
            ajcMojo.aspectDirectory = "src/main/aspect";
            final String[] includes = new String[]{"org/codehaus/mojo/aspectj/OldStyleAspect.aj"};
            ajcMojo.setArgumentFileName("builddef.lst");
            FileUtils.fileDelete(project.getBuild().getDirectory() + ajcMojo.argumentFileName);
            
            ajcMojo.includes= new String[]{"org/codehaus/mojo/aspectj/OldStyleAspect.aj"};
            ajcMojo.assembleArguments();
            assertTrue("Build should be needed when no previous files are found",ajcMojo.isBuildNeeded());
            
            ajcMojo.ajcOptions.clear();
            ajcMojo.includes = includes; 
            ajcMojo.execute();
            
            ajcMojo.ajcOptions.clear();
            ajcMojo.includes = includes;
            ajcMojo.assembleArguments();
            assertFalse("A build has compleeted. No modifications done. no new build needed",ajcMojo.isBuildNeeded());
            
            ajcMojo.ajcOptions.clear();
            ajcMojo.includes = includes;
            ajcMojo.setShowWeaveInfo(true);
            ajcMojo.assembleArguments();
            assertTrue("One of the arguments has changed, a new build is needed",ajcMojo.isBuildNeeded());
                        
            
            ajcMojo.ajcOptions.clear();
            ajcMojo.includes = includes;
            ajcMojo.assembleArguments();
            assertFalse("A build has compleeted. No modifications done. no new build needed",ajcMojo.isBuildNeeded());
            String currentDir = new File(".").getAbsolutePath();
            File aspect = new File(currentDir.substring(0,currentDir.length()-1)+"src/test/projects/test-project/src/main/aspect/org/codehaus/mojo/aspectj/OldStyleAspect.aj");
            long timeStamp = System.currentTimeMillis();
            assertTrue("Could not touch file: " + aspect.getAbsolutePath(), aspect.setLastModified(timeStamp));
            assertTrue("One of the included files has changed. a new build is needed",ajcMojo.isBuildNeeded());
            
  
            
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * @throws Exception
     */
    public void testCheckAspectDirectoryAddedToSourceDirs()
        throws Exception
    {
        try
        {
            ajcMojo.aspectDirectory = "src/main/aspect";
            ajcMojo.testAspectDirectory = "src/test/aspect";
            ajcMojo.setComplianceLevel( "1.5" );
            ajcMojo.execute();
            assertTrue( project.getCompileSourceRoots().contains(new File(basedir).getAbsolutePath()  + "/" +  "src/main/aspect") );
            assertTrue( project.getTestCompileSourceRoots().contains(new File(basedir).getAbsolutePath()  + "/" +  "src/test/aspect") );
        }
        catch ( Exception e )
        {
            fail();
        }
    }

    String getProjectName()
    {
        return "test-project";
    }
    



}
