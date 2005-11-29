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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.reporting.MavenReportException;
import org.aspectj.tools.ajdoc.Main;
import org.codehaus.doxia.site.renderer.SiteRenderer;

/**
 * Creates a ajdoc report in html format.
 * 
 * @goal ajdoc-report
 *
 * @description A Maven 2.0 ajdoc report
 * @author       <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>  
 */
public class AjcReportMojo
    extends AbstractAjcMojo
{

    /**
     * 
     * @parameter expression="${component.org.codehaus.doxia.site.renderer.SiteRenderer}"
     * @required @readonly
     */
    private SiteRenderer siteRenderer;

    /**
     * Executes this ajdoc-report generation.
     * 
     */
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        getLog().info( "Starting generating ajdoc" );
        Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
        ArrayList arguments = new ArrayList();
        // Add classpath
        arguments.add( "-classpath" );
        arguments.add( createClassPath() );

        // Add all passthrough arguments
        if ( null != options )
        {
            List passthroughArgs = Arrays.asList( options );
            if ( passthroughArgs.contains( "-cp" ) || passthroughArgs.contains( "-classpath" ) )
            {
                passthroughArgs.remove( "-cp" );
                passthroughArgs.remove( "-classpath" );
                getLog()
                    .warn(
                           "-classpath argument is removed. If you need additional classpath entries, please add as dependencies in the pom" );
            }
            if ( passthroughArgs.contains( "-sourcedir" ) )
            {
                passthroughArgs.remove( "-sourcedir" );
                getLog()
                    .warn(
                           "-sourcedir argument is removed. To set this argument use the plugin property sourceDir instead." );

            }
            if ( passthroughArgs.contains( "-help" ) )
            {
                passthroughArgs.remove( "-help" );
                getLog().warn( "-help argument is removed" );

            }
            if ( passthroughArgs.contains( "-version" ) || passthroughArgs.contains( "-v" ) )
            {
                passthroughArgs.remove( "-version" );
                passthroughArgs.remove( "-v" );
                getLog().warn( "-version argument is removed. look in the pom ;)" );

            }
            if ( passthroughArgs.contains( "-d" ) )
            {
                passthroughArgs.remove( "-d" );
                getLog().warn( "-d argument is removed. This plugin are using the build output directories." );
            }
            arguments.addAll( passthroughArgs );
        }

        Set includes;
        try
        {
            includes = getBuildFiles();
        }
        catch ( MojoExecutionException e )
        {
            throw new MavenReportException( "AspectJ Report failed", e );
        }

        // add target dir argument
        arguments.add( "-d" );
        arguments.add( getOutputDirectory() );

        arguments.addAll( includes );

        if ( getLog().isDebugEnabled() )
        {
            String command = "Running : ajc ";
            Iterator iter = arguments.iterator();
            while ( iter.hasNext() )
            {
                command += ( iter.next() + " " );
            }
            getLog().debug( command );
        }

        Main.main( (String[]) arguments.toArray( new String[0] ) );

    }

    /**
     * Get the directories containg sources 
     */
    protected List getSourceDirectories()
    {
        List sourceDirectories = new ArrayList();
        sourceDirectories.addAll( project.getCompileSourceRoots() );
        sourceDirectories.addAll( project.getTestCompileSourceRoots() );
        return sourceDirectories;
    }

    /**
     * get report output directory.
     */
    protected String getOutputDirectory()
    {
        return project.getBuild().getDirectory() + "/site/aspectj-doc";
    }

    /**
     * 
     */
    public String getOutputName()
    {
        return "aspectj-doc/index";
    }

    /**
     * 
     */
    public String getName( Locale locale )
    {
        return "AspectJ";
    }

    /**
     * 
     */
    public String getDescription( Locale locale )
    {
        return " Similar to javadoc, Maven AspectJ Report renders HTML" + " documentation for "
            + "pointcuts, advice, and inter-type declarations, as well as the"
            + " Java constructs that Javadoc renders. Maven AspectJ Report also" + " links advice"
            + " from members affected by the advice and the inter-type "
            + "declaration for members declared from aspects. The aspect will"
            + " be fully documented, as will your target classes, including "
            + "links to any advice or declarations that affect the class. "
            + "That means, for example, that you can see everything affecting"
            + " a method when reading the documentation for the method.";
    }

    /**
     * Get the site renderer.
     */
    protected SiteRenderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * 
     */
    public boolean isExternalReport()
    {
        return true;
    }
}
