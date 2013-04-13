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
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;

/**
 * Weaves all test classes.
 * 
 * @goal test-compile
 * @requiresDependencyResolution test
 * @phase test-compile
 * @description AspectJ Compiler Plugin.
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * @threadSafe
 */
public class AjcTestCompileMojo
    extends AbstractAjcCompiler
{
    /**
     * Flag to indicate if the main source dirs
     * should be a part of the compile process. Note 
     * this will make all classes in main source dir
     * appare in the test output dir also, 
     * potentially overwriting test resources.
     * @parameter default-value="false"
     */
    protected boolean weaveMainSourceFolder = false;

    /**
     * Flag to indicate if aspects in the the main source dirs
     * should be a part of the compile process
     * @parameter default-value="true"
     */
    protected boolean weaveWithAspectsInMainSourceFolder = true;
    
    /**
     * The directory where compiled test classes go.
     *
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;
    
    /**
     * <p>
     * Set the java test source folders to use, specifying the includes and excludes.
     * </p> 
     * <p>
     * If you don't specify this parameter, all java test sources of the current project fill be used.
     * If you specify this parameter as an empty tag (i.e. &lt;testSources/&gt;), all test source folders will be ignored.
     * Otherwise specify the test source folder(s) to use.
     * <p>
     * 
     * @parameter
     * @since 1.4
     * @see DirectoryScanner
     */
    private Scanner[] testSources;

    @Override
    protected List<String> getClasspathDirectories()
    {
        List<String> outputDirectories = new ArrayList<String>();
        outputDirectories.add( project.getBuild().getTestOutputDirectory() );
        outputDirectories.add( project.getBuild().getOutputDirectory() );
        return outputDirectories;
    }
    
    @Override
    protected File getOutputDirectory()
    {
        return outputDirectory;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected List<String> getSourceDirectories()
    {
        List<String> sourceDirs = new ArrayList<String>();
        sourceDirs.addAll( project.getTestCompileSourceRoots() );
        if ( weaveMainSourceFolder )
        {
            sourceDirs.addAll( project.getCompileSourceRoots() );
        }
        return sourceDirs;
    }

    protected Scanner[] getJavaSources()
    {
        return testSources;
    }

    protected String getAdditionalAspectPaths()
    {
        String additionalPath = null;
        if ( weaveWithAspectsInMainSourceFolder )
        {
            additionalPath = project.getBuild().getOutputDirectory();
        }
        return additionalPath;
    }
}
