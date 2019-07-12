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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * A helper class for creating classpaths for the compilers and report mojos
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public class AjcHelper
{
    public static final String DEFAULT_INCLUDES = "**/*.java, **/*.aj";

    public static final String DEFAULT_EXCLUDES = "";

    /**
     * List holding all accepted values for the {@code complianceLevel} parameter.
     */
    public static final List<String> ACCEPTED_COMPLIANCE_LEVEL_VALUES =
            Arrays.asList("1.3", "1.4", "1.5", "1.6", "1.7", "1.8", "1.9", "9", "10", "11");

    /**
     * Checks if the given complianceLevel value is valid.
     *
     * @param complianceLevel A complianceLevel
     * @return {@code true} if the supplied complianceLevel is valid, implying that it is defined within the
     * {@code ACCEPTED_COMPLIANCE_LEVEL_VALUES} List.
     * @see #ACCEPTED_COMPLIANCE_LEVEL_VALUES
     */
    public static boolean isValidComplianceLevel( String complianceLevel )
    {
        return ACCEPTED_COMPLIANCE_LEVEL_VALUES.contains(complianceLevel);
    }

    /**
     * Constructs AspectJ compiler classpath string
     *
     * @param project the Maven Project
     * @param pluginArtifacts the plugin Artifacts
     * @param outDirs the outputDirectories
     * @return a os spesific classpath string
     */
    @SuppressWarnings( "unchecked" )
    public static String createClassPath( MavenProject project, List<Artifact> pluginArtifacts, List<String> outDirs )
    {
        String cp = new String();
        Set<Artifact> classPathElements = Collections.synchronizedSet( new LinkedHashSet<Artifact>() );
        Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();
        classPathElements.addAll( dependencyArtifacts == null ? Collections.<Artifact>emptySet() : dependencyArtifacts );
        classPathElements.addAll( project.getArtifacts() );
        classPathElements.addAll( pluginArtifacts == null ? Collections.<Artifact>emptySet() : pluginArtifacts );
        for ( Artifact classPathElement  : classPathElements )
        {
            File artifact = classPathElement.getFile();
            if ( null != artifact )
            {
              String type = classPathElement.getType();
              if (!type.equals("pom")){
                cp += classPathElement.getFile().getAbsolutePath();
                cp += File.pathSeparatorChar;
              }
            }
        }
        Iterator<String> outIter = outDirs.iterator();
        while ( outIter.hasNext() )
        {
            cp += outIter.next();
            cp += File.pathSeparatorChar;
        }

        if ( cp.endsWith( "" + File.pathSeparatorChar ) )
        {
            cp = cp.substring( 0, cp.length() - 1 );
        }

        cp = StringUtils.replace( cp, "//", "/" );
        return cp;
    }

    /**
     * Based on a AJDT build properties file resolves the combination of all
     * include and exclude statements and returns a set of all the files to be
     * compiled and weaved.
     *
     * @param ajdtBuildDefFile the ajdtBuildDefFile
     * @param basedir the baseDirectory
     * @return Set of Build Files
     * @throws MojoExecutionException
     */
    public static Set<String> getBuildFilesForAjdtFile( String ajdtBuildDefFile, File basedir )
        throws MojoExecutionException
    {
        Set<String> result = new LinkedHashSet<String>();

        Properties ajdtBuildProperties = new Properties();
        try
        {
            ajdtBuildProperties.load( new FileInputStream( new File( basedir, ajdtBuildDefFile ) ) );
        }
        catch ( FileNotFoundException e )
        {
            throw new MojoExecutionException( "Build properties file specified not found", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "IO Error reading build properties file specified", e );
        }
        result.addAll( resolveIncludeExcludeString( ajdtBuildProperties.getProperty( "src.includes" ), basedir ) );
        Set<String> exludes = resolveIncludeExcludeString( ajdtBuildProperties.getProperty( "src.excludes" ), basedir );
        result.removeAll( exludes );

        return result;
    }

    /**
     * Based on a set of sourcedirs, apply include and exclude statements and
     * returns a set of all the files to be compiled and weaved.
     *
     * @param sourceDirs
     * @param includes
     * @param excludes
     * @return Set of Build Files for Source Dirs
     * @throws MojoExecutionException
     */
    public static Set<String> getBuildFilesForSourceDirs( List<String> sourceDirs, String[] includes, String[] excludes )
        throws MojoExecutionException
    {
        Set<String> result = new LinkedHashSet<String>();

        for ( String sourceDir : sourceDirs )
        {
            try
            {
                if ( FileUtils.fileExists( sourceDir ) )
                {
                    result.addAll( FileUtils
                        .getFileNames( new File( sourceDir ),
                                       ( null == includes || 0 == includes.length ) ? DEFAULT_INCLUDES
                                                                                   : getAsCsv( includes ),
                                       ( null == excludes || 0 == excludes.length ) ? DEFAULT_EXCLUDES
                                                                                   : getAsCsv( excludes ), true ) );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "IO Error resolving sourcedirs", e );
            }
        }
        // We might need to check if some of these files are already included through the weaveDirectories.

        return result;
    }

    /**
     * Based on a set of weavedirs returns a set of all the files to be weaved.
     *
     * @return a set of all the files to be weaved
     * @throws MojoExecutionException
     */
    public static Set<String> getWeaveSourceFiles( String[] weaveDirs )
        throws MojoExecutionException
    {
        Set<String> result = new HashSet<String>();

        for ( int i = 0; i < weaveDirs.length; i++ )
        {
            String weaveDir = weaveDirs[i];
            if ( FileUtils.fileExists( weaveDir ) )
            {
                try
                {
                    result.addAll( FileUtils.getFileNames( new File( weaveDir ), "**/*.class", DEFAULT_EXCLUDES, true ) );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "IO Error resolving weavedirs", e );
                }
            }
        }
        return result;
    }

    /**
     * Creates a file that can be used as input to the ajc compiler using the -argdfile flag.
     * Each line in these files should contain one option or filename.
     * Comments, as in Java, start with // and extend to the end of the line.
     *
     * @param arguments All arguments passed to ajc in this run
     * @param fileName the filename of the argfile
     * @param outputDir the build output area.
     * @throws IOException
     */
    public static void writeBuildConfigToFile( List<String> arguments, String fileName, File outputDir )
        throws IOException
    {
        FileUtils.forceMkdir( outputDir );
        File argFile = new File( outputDir, fileName );
        argFile.getParentFile().mkdirs();
        argFile.createNewFile();
        BufferedWriter writer = new BufferedWriter( new FileWriter( argFile ) );
        for ( String argument : arguments )
        {
            writer.write( argument );
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    /**
     * Reads a build config file, and returns the List of all compiler arguments.
     *
     * @param fileName the filename of the argfile
     * @param outputDir the build output area
     * @return the List of all compiler arguments.
     * @throws IOException
     */
    public static List<String> readBuildConfigFile( String fileName, File outputDir )
        throws IOException
    {
        List<String> arguments = new ArrayList<String>();
        File argFile = new File( outputDir, fileName );
        if ( FileUtils.fileExists( argFile.getAbsolutePath() ) )
        {
            FileReader reader = new FileReader( argFile );
            BufferedReader bufRead = new BufferedReader( reader );
            String line = null;
            do
            {
                line = bufRead.readLine();
                if ( null != line )
                {
                    arguments.add( line );
                }
            }
            while ( null != line );
        }
        return arguments;
    }

    /**
     * Convert a string array to a comma separated list
     *
     * @param strings
     * @return A comma separated list of Strings
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
     * Helper method to find all .java or .aj files specified by the
     * includeString. The includeString is a comma separated list over files, or
     * directories relative to the specified basedir. Examples of correct
     * listings
     *
     * <pre>
     *         src/main/java/
     *         src/main/java
     *         src/main/java/com/project/AClass.java
     *         src/main/java/com/project/AnAspect.aj
     *         src/main/java/com/project/AnAspect.java
     * </pre>
     *
     * @param input
     * @param basedir the baseDirectory
     * @return a list over all files inn the include string
     * @throws IOException
     */
    protected static Set<String> resolveIncludeExcludeString( String input, File basedir )
        throws MojoExecutionException
    {
        Set<String> inclExlSet = new LinkedHashSet<String>();
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
                        File lookupBaseDir = new File( basedir, element );
                        if ( FileUtils.fileExists( lookupBaseDir.getAbsolutePath() ) )
                        {
                            inclExlSet.addAll( FileUtils.getFileNames( lookupBaseDir, DEFAULT_INCLUDES, "",
                                                                   true ) );
                        }
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
