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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.Main;
import org.codehaus.plexus.util.FileUtils;

/**
 * Base class for aspectJ compiletime weaving of aspects.
 * 
 * 
 * @description AspectJ Weaver Plugin.
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public abstract class AbstractAjcCompiler
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    /**
     * The basedir of the project.
     * 
     * @parameter expression="${basedir}"
     * @required
     */
    protected File basedir;

    /**
     * Where to find all classes and aspects.
     * If not set ajdtBuildDefFile is required.
     * 
     * @parameter
     */
    protected String sourceDir;

    /**
     * Where to find the ajdt build definition file.
     * If not set sourceDir is required.
     * 
     * @parameter
     */
    protected String ajdtBuildDefFile;

    /**
     * Ajc compiler options. see ajc doc for valid arguments.
     * 
     * @parameter
     */
    protected String[] options;

    /**
     * Do the AspectJ compiling.
     * 
     * @throws MojoExecutionException
     */
    public void execute()
        throws MojoExecutionException
    {
        getLog().info( "Starting compiling aspects" );
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

        Set includes = new HashSet();

        // Add all in the sourceDir property
        if ( null != sourceDir )
        {
            includes.addAll( resolveIncludeExcludeString( sourceDir ) );
        }

        // read jbuild def
        if ( null != ajdtBuildDefFile )
        {
            Properties ajdtBuildProperties = new Properties();
            try
            {
                ajdtBuildProperties.load( new FileInputStream( ajdtBuildDefFile ) );
            }
            catch ( FileNotFoundException e )
            {
                throw new MojoExecutionException( "Build properties file spesified not found", e );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "IO Error reading build properties file spesified", e );
            }
            getLog().debug( "Include string : " + ajdtBuildProperties.get( "src.includes" ) );
            includes.addAll( resolveIncludeExcludeString( (String) ajdtBuildProperties.get( "src.includes" ) ) );
            Set exludes = resolveIncludeExcludeString( (String) ajdtBuildProperties.get( "src.excludes" ) );
            includes.removeAll( exludes );
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

    /**
     * Abstract method used by child classes to spesify the correct output directory for compiled classes.
     * 
     * @return where compiled classes should be put.
     */
    protected abstract String getOutputDirectory();

    /**
     * Constructs AspectJ compiler classpath string
     * 
     * @return a os spesific classpath string
     */
    private String createClassPath()
    {
        String cp = new String();
        Set classPathElements = project.getArtifacts();
        Iterator iter = classPathElements.iterator();
        while ( iter.hasNext() )
        {
            Artifact classPathElement = (Artifact) iter.next();
            cp += classPathElement.getFile().getAbsolutePath();
            cp += File.pathSeparatorChar;
        }
        cp += getOutputDirectory();

        return cp;
    }

    /**
     * Helper method to find all .java or .aj files spesified by the includeString. The includeString is a comma
     * seperated list over files, or directories relative to the spesified basedir. Examples of correct listings
     * 
     * <pre>
     *        src/main/java/
     *        src/main/java
     *        src/main/java/com/project/AClass.java
     *        src/main/java/com/project/AnAspect.aj
     *        src/main/java/com/project/AnAspect.java
     * </pre>
     * 
     * @param includeList
     * @return a list over all files inn the include string
     * @throws IOException
     */
    private Set resolveIncludeExcludeString( String input )
        throws MojoExecutionException
    {
        Set inclExlSet = new HashSet();
        try
        {
            if ( null == input || input.trim().equals( "" ) )
            {
                return inclExlSet;
            }
            String[] elements = input.split( "," );
            if ( null != elements )
            {

                for ( int i = 0; i < elements.length; i++ )
                {
                    String element = elements[i];
                    if ( element.endsWith( ".java" ) || element.endsWith( ".aj" ) )
                    {
                        inclExlSet.addAll( FileUtils.getFileNames( basedir, element, "", true ) );
                    }
                    else
                    {
                        if ( !element.endsWith( "/" ) )
                        {
                            element += "/";
                        }
                        element += "**/*.java" + "," + element + "**/*.aj";
                        inclExlSet.addAll( FileUtils.getFileNames( basedir, element, "", true ) );
                    }
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not resolve java or aspect classes to compile", e );
        }
        return inclExlSet;
    }
}
