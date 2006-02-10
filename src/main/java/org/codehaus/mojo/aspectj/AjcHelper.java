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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * A helper class for creating classpaths for the 
 * compilers and report mojos
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class AjcHelper
{
    public static final String DEFAULT_INCLUDES = "**/*.java, **/*.aj";

    public static final String DEFAULT_EXCLUDES = "";

    /**
     * Constructs AspectJ compiler classpath string
     * 
     * @return a os spesific classpath string
     */
    public static String createClassPath( MavenProject project, List outDirs )
    {
        String cp = new String();
        Set classPathElements = Collections.synchronizedSet( new TreeSet( project.getDependencyArtifacts() ) );
        classPathElements.addAll( project.getArtifacts() );
        Iterator iter = classPathElements.iterator();
        while ( iter.hasNext() )
        {
            Artifact classPathElement = (Artifact) iter.next();
            File artifact = classPathElement.getFile();
            if ( null != artifact )
            {
                cp += classPathElement.getFile().getAbsolutePath();
                cp += File.pathSeparatorChar;
            }
        }
        Iterator outIter = outDirs.iterator();
        while ( outIter.hasNext() )
        {
            cp += outIter.next();
            cp += File.pathSeparatorChar;
        }

        if ( cp.endsWith( "" + File.pathSeparatorChar ) )
        {
            cp = cp.substring( 0, cp.length() - 1 );
        }
        return cp;
    }

    /**
     * Based on a AJDT build properties file resolves the combination of all include and exclude statements
     * and returns a set of all the files to be compiled and weaved.
     * @return
     * @throws MojoExecutionException
     */
    public static Set getBuildFilesForAjdtFile( String ajdtBuildDefFile, File basedir )
        throws MojoExecutionException
    {
        Set result = new HashSet();

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
        result.addAll( resolveIncludeExcludeString( (String) ajdtBuildProperties.get( "src.includes" ), basedir ) );
        Set exludes = resolveIncludeExcludeString( (String) ajdtBuildProperties.get( "src.excludes" ), basedir );
        result.removeAll( exludes );

        return result;
    }

    /**
     * Based on a set of sourcedirs, apply include and exclude statements 
     * and returns a set of all the files to be compiled and weaved.
     * @return
     * @throws MojoExecutionException
     */
    public static Set getBuildFilesForSourceDirs( List sourceDirs, String[] includes, String[] excludes )
        throws MojoExecutionException
    {
        Set result = new HashSet();

        Iterator it = sourceDirs.iterator();
        while ( it.hasNext() )
        {
            try
            {
                result.addAll( FileUtils
                    .getFileNames( new File( (String) it.next() ),
                                   ( null == includes || 0 == includes.length ) ? DEFAULT_INCLUDES
                                                                               : getAsCsv( includes ),
                                   ( null == excludes || 0 == excludes.length ) ? DEFAULT_EXCLUDES
                                                                               : getAsCsv( excludes ), true ) );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "IO Error resolving sourcedirs", e );
            }
        }
        return result;
    }

    /**
     * Convert a string array to a comma seperated list
     * @param strings
     * @return
     */
    protected static String getAsCsv( String[] strings )
    {
        String csv = "";
        if ( null != strings )
        {
            for ( int i = 0; i < strings.length; i++ )
            {
                csv += strings[i];
                if ( i < ( strings.length - 1 ) )
                {
                    csv += ",";
                }
            }
        }
        return csv;
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
    protected static Set resolveIncludeExcludeString( String input, File basedir )
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
                        inclExlSet.addAll( FileUtils.getFileNames( new File( basedir, element ), DEFAULT_INCLUDES, "",
                                                                   true ) );
                    }
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not resolve java or aspect sources to compile", e );
        }
        return inclExlSet;
    }

}
