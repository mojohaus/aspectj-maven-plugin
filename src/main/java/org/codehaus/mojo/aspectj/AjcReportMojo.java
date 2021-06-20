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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.aspectj.tools.ajdoc.Main;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Creates an AspectJ HTML report using the {@code ajdoc} tool and format.
 *
 * A Maven 2.0 ajdoc report
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
@Mojo( name="aspectj-report", requiresDependencyResolution = ResolutionScope.COMPILE )
public class AjcReportMojo
    extends AbstractMavenReport
{
    /**
     * The source directory for the aspects
     *
     */
    @Parameter( defaultValue = "src/main/aspect" )
    private String aspectDirectory = "src/main/aspect";

    /**
     * The source directory for the test aspects
     *
     */
    @Parameter( defaultValue = "src/test/aspect" )
    private String testAspectDirectory = "src/test/aspect";

    /**
     * The maven project.
     *
     */
    @Parameter( readonly = true, required = true, defaultValue = "${project}" )
    private MavenProject project;

    /**
     * The basedir of the project.
     *
     */
    @Parameter( readonly = true, required = true, defaultValue = "${basedir}" )
    private File basedir;

    /**
     * The output directory for the report.
     *
     */
    @Parameter( required = true, defaultValue = "${project.reporting.outputDirectory}/aspectj-report" )
    private File outputDirectory;

    /**
     * The build directory (normally "${basedir}/target").
     *
     */
    @Parameter( required = true, readonly = true, defaultValue = "${project.build.directory}" )
    private File buildDirectory;

    /**
     * List of ant-style patterns used to specify the aspects that should be included when compiling. When none
     * specified all .java and .aj files in the project source directories, or directories spesified by the ajdtDefFile
     * property are included.
     */
    private String[] includes;

    /**
     * List of ant-style patterns used to specify the aspects that should be excluded when compiling. When none
     * specified all .java and .aj files in the project source directories, or directories spesified by the ajdtDefFile
     * property are included.
     */
    private String[] excludes;

    /**
     * Where to find the ajdt build definition file. <i>If set this will override the use of project sourcedirs</i>.
     *
     */
    @Parameter
    private String ajdtBuildDefFile;

    /**
     * Doxia Site Renderer.
     *
     */
    @Component
    private Renderer siteRenderer;

    /**
     * Shows only package, protected, and public classes and members.
     *
     */
    @Parameter
    protected boolean packageScope;

    /**
     * Shows only protected and public classes and members. This is the default.
     *
     */
    @Parameter
    protected boolean protectedScope;

    /**
     * Shows all classes and members.
     *
     */
    @Parameter
    protected boolean privateScope;

    /**
     * Shows only public classes and members.
     *
     */
    @Parameter
    protected boolean publicScope;

    /**
     * Specifies that javadoc should retrieve the text for the overview documentation from the "source" file specified
     * by path/filename and place it on the Overview page (overview-summary.html). The path/filename is relative to the
     * ${basedir}. While you can use any name you want for filename and place it anywhere you want for path, a typical
     * thing to do is to name it overview.html and place it in the source tree at the directory that contains the
     * topmost package directories. In this location, no path is needed when documenting packages, since -sourcepath
     * will point to this file. For example, if the source tree for the java.lang package is /src/classes/java/lang/,
     * then you could place the overview file at /src/classes/overview.html. See Real World Example. For information
     * about the file specified by path/filename, see overview comment file.Note that the overview page is created only
     * if you pass into javadoc two or more package names. For further explanation, see HTML Frames.) The title on the
     * overview page is set by -doctitle.
     *
     */
    @Parameter
    protected String overview;

    /**
     * Specifies the title to be placed near the top of the overview summary file. The title will be placed as a
     * centered, level-one heading directly beneath the upper navigation bar. The title may contain html tags and white
     * space, though if it does, it must be enclosed in quotes. Any internal quotation marks within title may have to be
     * escaped.
     *
     */
    @Parameter
    protected String doctitle;

    /**
     * Provides more detailed messages while javadoc is running. Without the verbose option, messages appear for loading
     * the source files, generating the documentation (one message per source file), and sorting. The verbose option
     * causes the printing of additional messages specifying the number of milliseconds to parse each java source file.
     *
     */
    @Parameter
    protected boolean verbose;

    /**
     * Specify compiler compliance setting (1.3 to 1.8, default is 1.5)
     *
     */
    @Parameter( defaultValue = "${mojo.java.target}" )
    protected String complianceLevel;

    /**
     * Holder for all options passed
     */
    private List<String> ajcOptions = new ArrayList<>();

    /**
     */
    @Parameter( readonly = true, required = true, defaultValue = "${plugin.artifacts}" )
    private List<Artifact> pluginArtifacts;

    /**
     * Executes this ajdoc-report generation.
     */
    @SuppressWarnings( "unchecked" )
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        getLog().info( "Starting generating ajdoc" );

        project.getCompileSourceRoots().add( basedir.getAbsolutePath() + "/" + aspectDirectory );
        project.getTestCompileSourceRoots().add( basedir.getAbsolutePath() + "/" + testAspectDirectory );

        List<String> arguments = new ArrayList<>();
        // Add classpath
        arguments.add( "-classpath" );
        arguments.add( AjcHelper.createClassPath( project, pluginArtifacts, getClasspathDirectories() ) );

        arguments.addAll( ajcOptions );

        Set<String> includes;
        try
        {
            if ( null != ajdtBuildDefFile )
            {
                includes = AjcHelper.getBuildFilesForAjdtFile( ajdtBuildDefFile, basedir );
            }
            else
            {
                includes = AjcHelper.getBuildFilesForSourceDirs( getSourceDirectories(), this.includes, this.excludes );
            }
        }
        catch ( MojoExecutionException e )
        {
            throw new MavenReportException( "AspectJ Report failed", e );
        }

        // add target dir argument
        arguments.add( "-d" );
        arguments.add( StringUtils.replace( getOutputDirectory(), "//", "/" ) );

        arguments.addAll( includes );

        if ( getLog().isDebugEnabled() )
        {
            StringBuilder command = new StringBuilder( "Running : ajdoc " );
            for ( String argument : arguments )
            {
                command.append( ' ' ).append( argument );
            }
            getLog().debug( command );
        }

        // There seems to be a difference in classloading when calling 'mvn site' or 'mvn aspectj:aspectj-report'.
        // When calling mvn site, without the contextClassLoader set, you might see the next message:
        // javadoc: error - Cannot find doclet class com.sun.tools.doclets.standard.Standard
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );

            // MASPECTJ-11: Make the ajdoc use the ${project.build.directory} directory for its intermediate folder.
            // The argument should be the absolute path to the parent directory of the "ajdocworkingdir" folder.
            Main.setOutputWorkingDir( buildDirectory.getAbsolutePath() );

            // Now produce the JavaDoc.
            Main.main( arguments.toArray( new String[0] ) );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldContextClassLoader );
        }

    }

    /**
     * @return list of source directories
     */
    @SuppressWarnings( "unchecked" )
    protected List<String> getSourceDirectories()
    {
        List<String> sourceDirectories = new ArrayList<>();
        sourceDirectories.addAll( project.getCompileSourceRoots() );
        sourceDirectories.addAll( project.getTestCompileSourceRoots() );
        return sourceDirectories;
    }

    /**
     * get report output directory.
     */
    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    /**
     * @return list of classpath directories
     */
    protected List<String> getClasspathDirectories()
    {
        return Arrays.asList( project.getBuild().getOutputDirectory(),
            project.getBuild().getTestOutputDirectory() );
    }

    /**
     *
     */
    public String getOutputName()
    {
        return "index";
    }

    /**
     *
     */
    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.aspectj.name" );
    }

    /**
     *
     */
    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.aspectj.description" );
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#isExternalReport()
     */
    public boolean isExternalReport()
    {
        return true;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#canGenerateReport()
     */
    public boolean canGenerateReport()
    {
        // Only execute reports for java projects
        ArtifactHandler artifactHandler = this.project.getArtifact().getArtifactHandler();
        return "java".equals( artifactHandler.getLanguage() );
    }

    /**
     * Get the site renderer.
     */
    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /**
     * Get the maven project.
     */
    protected MavenProject getProject()
    {
        return project;
    }

    public void setOverview( String overview )
    {
        ajcOptions.add( "-overview" );
        ajcOptions.add( overview );
    }

    public void setDoctitle( String doctitle )
    {
        ajcOptions.add( "-doctitle" );
        ajcOptions.add( doctitle );
    }

    public void setPackageScope( boolean packageScope )
    {
        if ( packageScope )
        {
            ajcOptions.add( "-package" );
        }
    }

    public void setPrivateScope( boolean privateScope )
    {
        if ( privateScope )
        {
            ajcOptions.add( "-private" );
        }
    }

    public void setProtectedScope( boolean protectedScope )
    {
        if ( protectedScope )
        {
            ajcOptions.add( "-protected" );
        }
    }

    public void setPublicScope( boolean publicScope )
    {
        if ( publicScope )
        {
            ajcOptions.add( "-public" );
        }
    }

    public void setVerbose( boolean verbose )
    {
        if ( verbose )
        {
            ajcOptions.add( "-verbose" );
        }
    }

    /**
     * Set source compliance level
     * 
     * @param complianceLevel compliance level
     */
    public void setComplianceLevel( String complianceLevel )
    {
        if ( AjcHelper.isValidComplianceLevel( complianceLevel ) )
        {
            ajcOptions.add( "-source" );
            ajcOptions.add( complianceLevel );
        }
    }

    public void setPluginArtifacts( List<Artifact> pluginArtifacts )
    {
        this.pluginArtifacts = pluginArtifacts;
    }

    /**
     * Gets the resource bundle for the report text.
     *
     * @param locale The locale for the report, must not be <code>null</code>.
     * @return The resource bundle for the requested locale.
     */
    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "aspectj-report", locale );
    }
}
