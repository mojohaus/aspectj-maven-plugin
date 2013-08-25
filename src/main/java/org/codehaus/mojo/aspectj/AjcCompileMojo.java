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
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;

/**
 * Weaves all main classes.
 * 
 * @goal compile
 * @requiresDependencyResolution compile
 * @phase compile
 * @description AspectJ Compiler Plugin.
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 * @threadSafe
 */
public class AjcCompileMojo
    extends AbstractAjcCompiler
{
    
    /**
     * The directory for compiled classes.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;
    
    /**
     * <p>
     * Set the java source folders to use, specifying the includes and excludes. 
     * </p>
     * <p>
     * If you don't specify this parameter, all java sources of the current project fill be used.
     * If you specify this parameter as an empty tag (i.e. &lt;sources/&gt;), all source folders will be ignored.
     * Otherwise specify the source folder(s) to use.
     * </p>
     * 
     * @parameter
     * @since 1.4
     * @see DirectoryScanner
     */
    private Scanner[] sources;
    
    protected File getOutputDirectory()
    {
        return outputDirectory;
    }
    
    /**
     * @return All directories matching {@code project.getBuild().getOutputDirectory()}.
     */
    protected List<String> getClasspathDirectories()
    {
        return Collections.singletonList( project.getBuild().getOutputDirectory() );
    }

    /**
     * @return All directories matching {@code project.getCompileSourceRoots()}.
     */
    @SuppressWarnings( "unchecked" )
    protected List<String> getSourceDirectories()
    {
        return project.getCompileSourceRoots();
    }
    
    protected Scanner[] getJavaSources()
    {
        return sources;
    }

    protected String getAdditionalAspectPaths()
    {
        return null;
    }
}
