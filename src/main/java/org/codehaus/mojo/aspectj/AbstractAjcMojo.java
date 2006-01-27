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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import org.codehaus.plexus.util.FileUtils;

/**
 * Base class for all AspectJ mojo's
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public abstract class AbstractAjcMojo
    extends AbstractMavenReport
    implements Mojo
{
    
    public static final String DEFAULT_INCLUDES = "**/*.java, **/*.aj";
    
    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required @readonly
     */
    protected MavenProject project;

    /**
     * The basedir of the project.
     * 
     * @parameter expression="${basedir}"
     * @required @readonly
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
     * Abstract method used by child classes to spesify the correct output directory for compiled classes.
     * 
     * @return where compiled classes should be put.
     */
    protected abstract String getOutputDirectory();

    /**
     * Abstract method used by child classes to spesify the correct source directory for classes.
     * 
     * @return where sources may be found.
     */
    protected abstract List getSourceDirectories();

    /**
     * Constructs AspectJ compiler classpath string
     * 
     * @return a os spesific classpath string
     */
    protected String createClassPath()
    {
        String cp = new String();
        Set classPathElements = Collections.synchronizedSet(new TreeSet(project.getDependencyArtifacts()));
        classPathElements.addAll(project.getArtifacts());
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
                        inclExlSet.addAll( FileUtils.getFileNames( new File( basedir, element ), DEFAULT_INCLUDES, "", true ) );
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

    /**
     * Get the maven project.
     */
    protected MavenProject getProject()
    {
        return project;
    }

    /**
     * Default report method. Ovverride this in a report mojo.
     */
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        throw new IllegalStateException( "Method not implemented. Must spesify implementation in a subclass." );
    }

    /**
     * Null report outputname. Ovverride this in a report mojo.
     */
    public String getOutputName()
    {
        return null;
    }

    /**
     * Null report name. Ovverride this in a report mojo.
     */
    public String getName( Locale arg0 )
    {
        return null;
    }

    /**
     * Null description. Ovverride this in a report mojo.
     */
    public String getDescription( Locale arg0 )
    {
        return null;
    }

    /**
     * Null siterenderer. Ovverride this in a report mojo.
     */
    protected SiteRenderer getSiteRenderer()
    {
        return null;
    }

    /**
     * Resolves the combination of all include and exclude statements
     * and returns a set of all the files to be compiled and weaved.
     * @return
     * @throws MojoExecutionException
     */
    protected Set getBuildFiles()
        throws MojoExecutionException
    {
        Set includes = new HashSet();
        
        try
        {
            if ( ( sourceDir == null ) && ( ajdtBuildDefFile == null ) )
            {
                Iterator it = getSourceDirectories().iterator();
                while ( it.hasNext() )
                {
                    includes.addAll( FileUtils.getFileNames( new File( ( String ) it.next() ), DEFAULT_INCLUDES, "", true ) );
                }
            }

            // Add all in the sourceDir property
            if ( null != sourceDir )
            {
                includes.addAll( FileUtils.getFileNames( new File( basedir, sourceDir ), DEFAULT_INCLUDES, "", true ) );
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
            return includes;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not resolve java or aspect sources to compile", e );
        }
    }

    /**
     * Checks all included files for modifications. If one of the files has changed, we need
     * to reweave everything, since a pointcut may have changed. 
     *
     */
    protected boolean checkModifications( Set files )
    {
        return true;
    }
}
