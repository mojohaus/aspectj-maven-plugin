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
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.Main;

/**
 * Base class for the two aspectJ compiletime weaving mojos.
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public abstract class AbstractAjcCompiler
    extends AbstractAjcMojo
{

    /**
     * Do the AspectJ compiling.
     * 
     * @throws MojoExecutionException
     */
    public void execute()
        throws MojoExecutionException
    {
        getLog().info( "Starting compiling aspects" );
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
            if ( passthroughArgs.contains( "-version" ) )
            {
                passthroughArgs.remove( "-version" );
                getLog().warn( "-version argument is removed. look in the pom ;)" );

            }
            if ( passthroughArgs.contains( "-d" ) )
            {
                passthroughArgs.remove( "-d" );
                getLog().warn( "-d argument is removed. This plugin are using the build output directories." );
            }
            arguments.addAll( passthroughArgs );
        }

        Set includes = getBuildFiles();

        if ( checkModifications( includes ) )
        {

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
            Main main = new Main();
            MavenMessageHandler mavenMessageHandler = new MavenMessageHandler( getLog() );
            main.setHolder( mavenMessageHandler );

            main.runMain( (String[]) arguments.toArray( new String[0] ), false );
            IMessage[] errors = mavenMessageHandler.getErrors();
            if ( errors.length > 0 )
            {
                String errorMessage = "";
                for ( int i = 0; i < errors.length; i++ )
                {
                    errorMessage += errors[i] + "\n";
                }
                throw new MojoExecutionException( "Compiler errors : \n" + errorMessage );
            }
        }
    }
}
