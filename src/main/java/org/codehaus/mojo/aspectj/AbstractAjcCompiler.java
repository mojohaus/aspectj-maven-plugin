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
import org.apache.maven.plugin.MojoExecutionException;
import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.Main;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for the two aspectJ compiletime weaving mojos.
 * <p/>
 * For all available options see {@link http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html}
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public abstract class AbstractAjcCompiler
    extends AbstractAjcMojo
{

    // Constants
    private static final List<String> XAJRUNTIMETARGET_SUPPORTED_VALUES = Arrays.asList( "1.2", "1.5" );

    /**
     * The source directory for the aspects.
     *
     * @parameter default-value="src/main/aspect"
     */
    protected String aspectDirectory = "src/main/aspect";

    /**
     * The source directory for the test aspects.
     *
     * @parameter default-value="src/test/aspect"
     */
    protected String testAspectDirectory = "src/test/aspect";

    /**
     * List of ant-style patterns used to specify the aspects that should be included when compiling. When none
     * specified all .java and .aj files in the project source directories, or directories specified by the ajdtDefFile
     * property are included.
     *
     * @parameter
     */
    protected String[] includes;

    /**
     * List of ant-style patterns used to specify the aspects that should be excluded when compiling. When none
     * specified all .java and .aj files in the project source directories, or directories specified by the ajdtDefFile
     * property are included.
     *
     * @parameter
     */
    protected String[] excludes;

    /**
     * Where to find the ajdt build definition file. <i>If set this will override the use of project sourcedirs</i>.
     *
     * @parameter
     */
    protected String ajdtBuildDefFile;

    /**
     * Generate aop.xml file for load-time weaving with default name (/META-INF/aop.xml).
     *
     * @parameter
     */
    protected boolean outxml;

    /**
     * Generate aop.xml file for load-time weaving with custom name.
     *
     * @parameter
     */
    protected String outxmlfile;

    /**
     * Generate .ajesym symbol files for emacs support.
     *
     * @parameter
     */
    protected boolean emacssym;

    /**
     * Allows the caller to provide additional arguments in a Map format. For example:
     * <pre>
     * &lt;configuration&gt;
     *   &lt;Xset&gt;
     *     &lt;overWeaving&gt;true&lt;/overWeaving&gt;
     *     &lt;avoidFinal&gt;false&lt;/avoidFinal&gt;
     *   &lt;/Xset&gt;
     * &lt;/configuration&gt;
     * </pre>
     *
     * @parameter
     * @since 1.5
     */
    protected Map<String, String> Xset;

    /**
     * Set default level for messages about potential programming mistakes in crosscutting code. {level} may be ignore,
     * warning, or error. This overrides entries in org/aspectj/weaver/XlintDefault.properties from aspectjtools.jar.
     *
     * @parameter
     */
    protected String Xlint;

    /**
     * Specify properties file to set levels for specific crosscutting messages.
     * PropertyFile is a path to a Java .properties file that takes the same property names and values as
     * org/aspectj/weaver/XlintDefault.properties from aspectjtools.jar, which it also overrides.
     *
     * @parameter
     */
    protected File Xlintfile;

    /**
     * Enables the compiler to support hasmethod(method_pattern) and hasfield(field_pattern) type patterns, but only
     * within declare statements. It's experimental and undocumented because it may change, and because it doesn't yet
     * take into account ITDs.
     *
     * @parameter
     * @since 1.3
     */
    protected boolean XhasMember;

    /**
     * Specify classfile target setting (1.1 to 1.7) default is 1.2
     *
     * @parameter default-value="${project.build.java.target}"
     */
    protected String target;

    /**
     * Toggle assertions (1.3, 1.4, 1.5, 1.6 or 1.7 - default is 1.4). When using -source 1.3, an assert() statement valid under
     * Java 1.4 will result in a compiler error. When using -source 1.4, treat assert as a keyword and implement
     * assertions according to the 1.4 language spec. When using -source 1.5 or higher, Java 5 language features are
     * permitted. With --source 1.7 or higher Java 7 features are supported.
     *
     * @parameter default-value="${mojo.java.target}"
     */
    protected String source;

    /**
     * Specify compiler compliance setting (1.3 to 1.7) default is 1.4
     *
     * @parameter
     */
    protected String complianceLevel;

    /**
     * Toggle warningmessages on deprecations
     *
     * @parameter
     */
    protected boolean deprecation;

    /**
     * Emit no errors for unresolved imports;
     *
     * @parameter
     */
    protected boolean noImportError;

    /**
     * Keep compiling after error, dumping class files with problem methods
     *
     * @parameter
     */
    protected boolean proceedOnError;

    /**
     * Preserve all local variables during code generation (to facilitate debugging).
     *
     * @parameter
     */
    protected boolean preserveAllLocals;

    /**
     * Compute reference information.
     *
     * @parameter
     */
    protected boolean referenceInfo;

    /**
     * Specify default source encoding format.
     *
     * @parameter property="project.build.sourceEncoding"
     */
    protected String encoding;

    /**
     * Emit messages about accessed/processed compilation units
     *
     * @parameter
     */
    protected boolean verbose;

    /**
     * Emit messages about weaving
     *
     * @parameter
     */
    protected boolean showWeaveInfo;

    /**
     * Repeat compilation process N times (typically to do performance analysis).
     *
     * @parameter
     */
    protected int repeat;

    /**
     * (Experimental) runs weaver in reweavable mode which causes it to create woven classes that can be rewoven,
     * subject to the restriction that on attempting a reweave all the types that advised the woven type must be
     * accessible.
     *
     * @parameter
     */
    protected boolean Xreweavable;

    /**
     * (Experimental) do not inline around advice
     *
     * @parameter
     */
    protected boolean XnoInline;

    /**
     * (Experimental) Normally it is an error to declare aspects {@link Serializable}. This option removes that restriction.
     *
     * @parameter
     */
    protected boolean XserializableAspects;

    /**
     * Causes the compiler to calculate and add the SerialVersionUID field to any type implementing {@link Serializable} that is
     * affected by an aspect. The field is calculated based on the class before weaving has taken place.
     *
     * @parameter
     */
    protected boolean XaddSerialVersionUID;

    /**
     * Causes compiler to terminate before weaving
     *
     * @parameter
     */
    protected boolean XterminateAfterCompilation;

    /**
     * (Experimental) Allows code to be generated that targets a 1.2 or a 1.5 level AspectJ runtime (default 1.5)
     *
     * @parameter
     */
    protected String Xajruntimetarget;

    /**
     * Override location of VM's bootclasspath for purposes of evaluating types when compiling. Path is a single
     * argument containing a list of paths to zip files or directories, delimited by the platform-specific path
     * delimiter.
     *
     * @parameter
     */
    protected String bootclasspath;

    /**
     * Emit warnings for any instances of the comma-delimited list of questionable code (e.g. 'unusedLocals,deprecation'):
     * see http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html#ajc for available settings
     *
     * @parameter
     */
    protected String warn;

    /**
     * The filename to store build configuration in. This file will be placed in the project build output directory, and
     * will contain all the arguments passed to the compiler in the last run, and also all the filenames included in the
     * build. Aspects as well as java files.
     *
     * @parameter default-value="builddef.lst"
     */
    protected String argumentFileName = "builddef.lst";

    /**
     * Forces re-compilation, regardless of whether the compiler arguments or the sources have changed.
     *
     * @parameter
     */
    protected boolean forceAjcCompile;

    /**
     * Holder for ajc compiler options
     */
    protected List<String> ajcOptions = new ArrayList<String>();

    /**
     * Holds all files found using the includes, excludes parameters.
     */
    protected Set<String> resolvedIncludes;

    /**
     * Abstract method used by child classes to specify the correct output directory for compiled classes.
     *
     * @return the directories containing compiled classes.
     */
    protected abstract List<String> getClasspathDirectories();

    /**
     * The directory where compiled classes go.
     *
     * @return the outputDirectory
     */
    protected abstract File getOutputDirectory();

    /**
     * Abstract method used by child classes to specify the correct source directory for classes.
     *
     * @return where sources may be found.
     */
    protected abstract List<String> getSourceDirectories();

    protected abstract Scanner[] getJavaSources();

    /**
     * Abstract method used by child classes to specify additional aspect paths.
     *
     * @return the additional aspect paths
     */
    protected abstract String getAdditionalAspectPaths();

    /**
     * Lock for the call to the AspectJ compiler to make it thread-safe.
     */
    private static final Object BIG_ASPECTJ_LOCK = new Object();

    /**
     * Do the AspectJ compiling.
     *
     * @throws MojoExecutionException
     */
    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException
    {
        if ( isSkip() )
        {
            if ( getLog().isInfoEnabled() )
            {
                getLog().info( "Skipping execution because of 'skip' option" );
            }
            return;
        }

        ArtifactHandler artifactHandler = project.getArtifact().getArtifactHandler();
        if ( !"java".equals( artifactHandler.getLanguage() ) )
        {
            getLog().warn( "Not executing aspectJ compiler as the project is not a Java classpath-capable package" );
            return;
        }

        // MASPECT-110:
        //
        // Only add the aspectSourcePathDir and testAspectSourcePathDir to their respective
        // compileSourceRoots if they actually exist and are directories... to avoid crashing
        // downstream plugins requiring/assuming that all entries within the compileSourceRoots
        // and testCompileSourceRoots are directories.
        //
        final File aspectSourcePathDir = FileUtils.resolveFile( basedir, aspectDirectory );
        final File testAspectSourcePathDir = FileUtils.resolveFile( basedir, testAspectDirectory );

        final String aspectSourcePath = aspectSourcePathDir.getAbsolutePath();
        final String testAspectSourcePath = testAspectSourcePathDir.getAbsolutePath();

        if ( aspectSourcePathDir.exists() && aspectSourcePathDir.isDirectory()
            && !project.getCompileSourceRoots().contains( aspectSourcePath ) )
        {
            getLog().debug( "Adding existing aspectSourcePathDir [" + aspectSourcePath + "] to compileSourceRoots." );
            project.getCompileSourceRoots().add( aspectSourcePath );
        }
        else
        {
            getLog().debug( "Not adding non-existent or already added aspectSourcePathDir [" + aspectSourcePath
                                + "] to compileSourceRoots." );
        }

        if(testAspectSourcePathDir.exists()
                && testAspectSourcePathDir.isDirectory()
                && !project.getTestCompileSourceRoots().contains(testAspectSourcePath))
        {
            getLog().debug( "Adding existing testAspectSourcePathDir [" + testAspectSourcePath
                    + "] to testCompileSourceRoots." );
            project.getTestCompileSourceRoots().add( testAspectSourcePath );
        }
        else
        {
            getLog().debug( "Not adding non-existent or already added testAspectSourcePathDir ["
                    + testAspectSourcePath + "] to testCompileSourceRoots." );
        }

        assembleArguments();

        if ( !forceAjcCompile && !hasSourcesToCompile() )
        {
            getLog().warn( "No sources found skipping aspectJ compile" );
            return;
        }

        if ( !forceAjcCompile && !isBuildNeeded() )
        {
            getLog().info( "No modifications found skipping aspectJ compile" );
            return;
        }

        if ( getLog().isDebugEnabled() )
        {
            StringBuilder command = new StringBuilder( "Running : ajc" );

            for ( String arg : ajcOptions )
            {
                command.append( ' ' ).append( arg );
            }
            getLog().debug( command );
        }
        try
        {
            getLog().debug(
                "Compiling and weaving " + resolvedIncludes.size() + " sources to " + getOutputDirectory() );
            AjcHelper.writeBuildConfigToFile( ajcOptions, argumentFileName, getOutputDirectory() );
            getLog().debug(
                "Argumentsfile written : " + new File( getOutputDirectory(), argumentFileName ).getAbsolutePath() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not write arguments file to the target area", e );
        }
        Main main = new Main();
        MavenMessageHandler mavenMessageHandler = new MavenMessageHandler( getLog() );
        main.setHolder( mavenMessageHandler );

        synchronized ( BIG_ASPECTJ_LOCK )
        {
            main.runMain( (String[]) ajcOptions.toArray( new String[ajcOptions.size()] ), false );
        }

        IMessage[] errors = mavenMessageHandler.getMessages( IMessage.ERROR, true );
        if ( !proceedOnError && errors.length > 0 )
        {
            throw new CompilationFailedException( errors );
        }
    }

    /**
     * Assembles a complete ajc compiler arguments list.
     *
     * @throws MojoExecutionException error in configuration
     */
    protected void assembleArguments()
        throws MojoExecutionException
    {
        if ( XhasMember )
        {
            ajcOptions.add( "-XhasMember" );
        }

        // Add classpath
        ajcOptions.add( "-classpath" );
        ajcOptions.add( AjcHelper.createClassPath( project, null, getClasspathDirectories() ) );

        // Add boot classpath
        if ( null != bootclasspath )
        {
            ajcOptions.add( "-bootclasspath" );
            ajcOptions.add( bootclasspath );
        }

        // Add warn option
        if ( null != warn )
        {
            ajcOptions.add( "-warn:" + warn );
        }

        if ( Xset != null && !Xset.isEmpty() )
        {
            StringBuilder sb = new StringBuilder( "-Xset:" );
            for ( Map.Entry<String, String> param : Xset.entrySet() )
            {
                sb.append( param.getKey() );
                sb.append( "=" );
                sb.append( param.getValue() );
                sb.append( ',' );
            }
            ajcOptions.add( sb.substring( 0, sb.length() - 1 ) );
        }

        // Add artifacts or directories to weave
        String joinedWeaveDirectories = null;
        if ( weaveDirectories != null )
        {
            joinedWeaveDirectories = StringUtils.join( weaveDirectories, File.pathSeparator );
        }
        addModulesArgument( "-inpath", ajcOptions, weaveDependencies, joinedWeaveDirectories,
                            "dependencies and/or directories to weave" );

        // Add library artifacts
        addModulesArgument( "-aspectpath", ajcOptions, aspectLibraries, getAdditionalAspectPaths(),
                            "an aspect library" );

        // Add xmlConfigured option and argument
        if ( null != xmlConfigured )
        {
            ajcOptions.add( "-xmlConfigured" );
            ajcOptions.add( xmlConfigured.getAbsolutePath() );
        }

        // add target dir argument
        ajcOptions.add( "-d" );
        ajcOptions.add( getOutputDirectory().getAbsolutePath() );

        // Add all the files to be included in the build,
        if ( null != ajdtBuildDefFile )
        {
            resolvedIncludes = AjcHelper.getBuildFilesForAjdtFile( ajdtBuildDefFile, basedir );
        }
        else
        {
            resolvedIncludes = getIncludedSources();
        }
        ajcOptions.addAll( resolvedIncludes );
    }

    protected Set<String> getIncludedSources()
        throws MojoExecutionException
    {
        Set<String> result = new HashSet<String>();
        if ( getJavaSources() == null )
        {
            result = AjcHelper.getBuildFilesForSourceDirs( getSourceDirectories(), this.includes, this.excludes );
        }
        else
        {
            for ( int scannerIndex = 0; scannerIndex < getJavaSources().length; scannerIndex++ )
            {
                Scanner scanner = getJavaSources()[scannerIndex];
                if ( scanner.getBasedir() == null )
                {
                    getLog().info( "Source without basedir, skipping it." );
                }
                else
                {
                    scanner.scan();
                    for ( int fileIndex = 0; fileIndex < scanner.getIncludedFiles().length; fileIndex++ )
                    {
                        result.add( FileUtils.resolveFile( scanner.getBasedir(),
                                                           scanner.getIncludedFiles()[fileIndex] ).getAbsolutePath() );
                    }
                }
            }
        }
        return result;
    }

    /**
     * Finds all artifacts in the weavemodule property, and adds them to the ajc options.
     *
     * @param argument
     * @param arguments
     * @param modules
     * @param aditionalpath
     * @param role
     * @throws MojoExecutionException
     */
    private void addModulesArgument( final String argument,
                                     final List<String> arguments,
                                     final Module[] modules,
                                     final String aditionalpath,
                                     final String role )
        throws MojoExecutionException
    {
        StringBuilder buf = new StringBuilder();

        if ( null != aditionalpath )
        {
            arguments.add( argument );
            buf.append( aditionalpath );
        }
        if ( modules != null && modules.length > 0 )
        {
            if ( !arguments.contains( argument ) )
            {
                arguments.add( argument );
            }

            for ( int i = 0; i < modules.length; ++i )
            {
                Module module = modules[i];
                // String key = ArtifactUtils.versionlessKey( module.getGroupId(), module.getArtifactId() );
                // Artifact artifact = (Artifact) project.getArtifactMap().get( key );
                Artifact artifact = null;
                @SuppressWarnings( "unchecked" ) Set<Artifact> allArtifacts = project.getArtifacts();
                for ( Artifact art : allArtifacts )
                {
                    if ( art.getGroupId().equals( module.getGroupId() ) && art.getArtifactId().equals(
                        module.getArtifactId() ) && StringUtils.defaultString( module.getClassifier() ).equals(
                        StringUtils.defaultString( art.getClassifier() ) ) && StringUtils.defaultString(
                        module.getType(), "jar" ).equals( StringUtils.defaultString( art.getType() ) ) )
                    {
                        artifact = art;
                        break;
                    }
                }
                if ( artifact == null )
                {
                    throw new MojoExecutionException(
                        "The artifact " + module.toString() + " referenced in aspectj plugin as " + role
                            + ", is not found the project dependencies" );
                }
                if ( buf.length() != 0 )
                {
                    buf.append( File.pathSeparatorChar );
                }
                buf.append( artifact.getFile().getPath() );
            }
        }
        if ( buf.length() > 0 )
        {
            String pathString = buf.toString();
            arguments.add( pathString );
            getLog().debug( "Adding " + argument + ": " + pathString );
        }
    }

    /**
     * Checks modifications that would make us need a build
     *
     * @return <code>true</code> if build is needed, otherwise <code>false</code>
     * @throws MojoExecutionException
     */
    protected boolean isBuildNeeded()
        throws MojoExecutionException
    {
        File outDir = getOutputDirectory();
        return hasNoPreviousBuild( outDir ) || hasArgumentsChanged( outDir ) ||
            hasSourcesChanged( outDir ) || hasNonWeavedClassesChanged( outDir );

    }

    private boolean hasNoPreviousBuild( File outDir )
    {
        return !FileUtils.resolveFile( outDir, argumentFileName ).exists();
    }

    private boolean hasArgumentsChanged( File outDir )
        throws MojoExecutionException
    {
        try
        {
            return ( !ajcOptions.equals( AjcHelper.readBuildConfigFile( argumentFileName, outDir ) ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error during reading of previous argumentsfile " );
        }
    }

    /**
     * Not entirely safe, assembleArguments() must be run
     */
    private boolean hasSourcesToCompile()
    {
        return resolvedIncludes.size() > 0;
    }

    private boolean hasSourcesChanged( File outDir )
    {
        long lastBuild = new File( outDir, argumentFileName ).lastModified();
        for ( String source : resolvedIncludes )
        {
            File sourceFile = new File( source );
            long sourceModified = sourceFile.lastModified();
            if ( sourceModified >= lastBuild )
            {
                return true;
            }

        }
        return false;
    }

    private boolean hasNonWeavedClassesChanged( File outDir )
        throws MojoExecutionException
    {
        if ( weaveDirectories != null && weaveDirectories.length > 0 )
        {
            Set<String> weaveSources = AjcHelper.getWeaveSourceFiles( weaveDirectories );
            long lastBuild = new File( outDir, argumentFileName ).lastModified();
            for ( String source : weaveSources )
            {
                File sourceFile = new File( source );
                long sourceModified = sourceFile.lastModified();
                if ( sourceModified >= lastBuild )
                {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * Setters which when called sets compiler arguments
     *
     * @param complianceLevel the complianceLevel
     */
    public void setComplianceLevel( String complianceLevel )
    {
        if ( complianceLevel.equals( "1.3" ) || complianceLevel.equals( "1.4" ) || complianceLevel.equals( "1.5" )
            || complianceLevel.equals( "1.6" ) || complianceLevel.equals( "1.7" ) )
        {
            ajcOptions.add( "-" + complianceLevel );
        }

    }

    public void setDeprecation( boolean deprecation )
    {
        if ( deprecation )
        {
            ajcOptions.add( "-deprecation" );
        }
    }

    public void setEmacssym( boolean emacssym )
    {
        if ( emacssym )
        {
            ajcOptions.add( "-emacssym" );
        }

    }

    public void setEncoding( String encoding )
    {
        ajcOptions.add( "-encoding" );
        ajcOptions.add( encoding );
    }

    public void setNoImportError( boolean noImportError )
    {
        if ( noImportError )
        {
            ajcOptions.add( "-noImportError" );
        }

    }

    public void setOutxml( boolean outxml )
    {
        if ( outxml )
        {
            ajcOptions.add( "-outxml" );
        }

    }

    public void setOutxmlfile( String outxmlfile )
    {
        ajcOptions.add( "-outxmlfile" );
        ajcOptions.add( outxmlfile );
    }

    public void setPreserveAllLocals( boolean preserveAllLocals )
    {
        if ( preserveAllLocals )
        {
            ajcOptions.add( "-preserveAllLocals" );
        }

    }

    public void setProceedOnError( boolean proceedOnError )
    {
        if ( proceedOnError )
        {
            ajcOptions.add( "-proceedOnError" );
        }
        this.proceedOnError = proceedOnError;
    }

    public void setReferenceInfo( boolean referenceInfo )
    {
        if ( referenceInfo )
        {
            ajcOptions.add( "-referenceInfo" );
        }

    }

    public void setRepeat( int repeat )
    {
        ajcOptions.add( "-repeat" );
        ajcOptions.add( "" + repeat );
    }

    public void setShowWeaveInfo( boolean showWeaveInfo )
    {
        if ( showWeaveInfo )
        {
            ajcOptions.add( "-showWeaveInfo" );
        }

    }

    public void setTarget( String target )
    {
        ajcOptions.add( "-target" );
        ajcOptions.add( target );
    }

    public void setSource( String source )
    {
        ajcOptions.add( "-source" );
        ajcOptions.add( source );
    }

    public void setVerbose( boolean verbose )
    {
        if ( verbose )
        {
            ajcOptions.add( "-verbose" );
        }
    }

    public void setXhasMember( boolean xhasMember )
    {
        XhasMember = xhasMember;
    }

    public void setXlint( String xlint )
    {
        ajcOptions.add( "-Xlint:" + xlint );
    }

    public void setXset( Map<String, String> xset )
    {
        this.Xset = xset;
    }

    public void setXlintfile( File xlintfile )
    {
        try
        {
            final String prefix = "Xlintfile parameter invalid: ";
            final String path = xlintfile.getCanonicalPath();
            if ( !xlintfile.exists() )
            {
                getLog().warn( prefix + " file [" + path + "] does not exist" );
            }
            else if ( xlintfile.isDirectory() )
            {
                getLog().warn( prefix + " given path [" + path + "] is a directory." );
            }
            else if ( !path.trim().toLowerCase().endsWith( ".properties" ) )
            {
                getLog().warn( prefix + " must be a .properties file" );
            }
            else
            {
                ajcOptions.add( "-Xlintfile" );
                ajcOptions.add( path );
            }
        }
        catch ( IOException e )
        {
            getLog().error( "IOException while setting Xlintfile option", e );
        }
    }

    public void setXnoInline( boolean xnoInline )
    {
        if ( xnoInline )
        {
            ajcOptions.add( "-XnoInline" );
        }

    }

    public void setXreweavable( boolean xreweavable )
    {
        if ( xreweavable )
        {
            ajcOptions.add( "-Xreweavable" );
        }

    }

    public void setXserializableAspects( boolean xserializableAspects )
    {
        if ( xserializableAspects )
        {
            ajcOptions.add( "-XserializableAspects" );
        }
    }

    public void setXaddSerialVersionUID( boolean xaddSerialVersionUID )
    {
        if ( xaddSerialVersionUID )
        {
            ajcOptions.add( "-XaddSerialVersionUID" );
        }
    }

    public void setXterminateAfterCompilation( boolean xterminateAfterCompilation )
    {
        if ( xterminateAfterCompilation )
        {
            ajcOptions.add( "-XterminateAfterCompilation" );
        }
    }

    public void setXajruntimetarget( String xajruntimetarget )
    {
        if ( XAJRUNTIMETARGET_SUPPORTED_VALUES.contains( xajruntimetarget ) )
        {
            ajcOptions.add( "-Xajruntimetarget:" + xajruntimetarget );
        }
        else
        {
            getLog().warn(
                "Incorrect Xajruntimetarget value specified. Supported: " + XAJRUNTIMETARGET_SUPPORTED_VALUES );
        }
    }

    public void setBootClassPath( String bootclasspath )
    {
        this.bootclasspath = bootclasspath;
    }

    public void setWarn( String warn )
    {
        this.warn = warn;
    }

    public void setArgumentFileName( String argumentFileName )
    {
        this.argumentFileName = argumentFileName;

    }
}
