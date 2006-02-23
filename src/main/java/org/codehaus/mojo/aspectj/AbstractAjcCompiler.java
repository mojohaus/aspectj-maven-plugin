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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.Main;
import org.codehaus.plexus.util.FileUtils;

/**
 * Base class for the two aspectJ compiletime weaving mojos.
 * 
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public abstract class AbstractAjcCompiler
    extends AbstractMojo
{
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
     * List of ant-style patterns used to specify the aspects that should be included when 
     * compiling. When none specified all .java and .aj files in the project source directories, or
     * directories spesified by the ajdtDefFile property are included.
     */
    protected String[] includes;

    /**
     * List of ant-style patterns used to specify the aspects that should be excluded when 
     * compiling. When none specified all .java and .aj files in the project source directories, or
     * directories spesified by the ajdtDefFile property are included.
     */
    protected String[] excludes;

    /**
     * Where to find the ajdt build definition file.
     * <i>If set this will override the use of project sourcedirs</i>.
     * 
     * @parameter
     */
    protected String ajdtBuildDefFile;

    /**
     * List of of modules to weave (into target directory). Corresponds to ajc
     * -inpath option (or -injars for pre-1.2 (which is not supported)).
     * 
     * @parameter
     */
    protected Module[] weaveDependencies;

    /**
     * Weave binary aspects from the jars. 
     * The aspects should have been output by the same version of the compiler. 
     * The modules must also be dependencies of the project.
     * Corresponds to ajc -aspectpath option
     * 
     * @parameter
     */
    protected Module[] aspectLibraries;

    /**
     * Generate aop.xml file for load-time weaving with default name.(/META-INF/aop.xml)
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
     * Generate .ajesym symbol files for emacs support
     * 
     *  @parameter
     */
    protected boolean emacssym;

    /**
     * Set default level for messages about potential 
     * programming mistakes in crosscutting code. 
     * {level} may be ignore, warning, or error. 
     * This overrides entries in org/aspectj/weaver/XlintDefault.properties 
     * from aspectjtools.jar.
     * 
     *  @parameter
     */
    protected String Xlint;

    /**
     * Specify classfile target setting (1.1 to 1.5) default is 1.2
     * 
     *  @parameter
     */
    protected String target;

    /**
     * Toggle assertions (1.3, 1.4, or 1.5 - default is 1.4). 
     * When using -source 1.3, an assert() statement valid under Java 1.4 
     * will result in a compiler error. When using -source 1.4, 
     * treat assert as a keyword and implement assertions 
     * according to the 1.4 language spec. 
     * When using -source 1.5, Java 5 language features are permitted.
     * 
     *  @parameter
     */
    protected String source;

    /**
     * Specify compiler compliance setting (1.3 to 1.5)
     * default is 1.4
     * 
     *  @parameter
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
     * @parameter
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
     * (Experimental) runs weaver in reweavable mode which causes it to create 
     * woven classes that can be rewoven, subject to the restriction 
     * that on attempting a reweave all the types that advised the woven 
     * type must be accessible.
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
     * (Experimental) Normally it is an error to declare aspects Serializable. This option removes that restriction.
     * 
     * @parameter
     */
    protected boolean XserializableAspects;

    /**
     * The filename to store build configuration in.
     * This file will be placed in the project build output
     * directory, and will contain all the arguments
     * passed to the compiler in the last run, and also
     * all the filenames included in the build. Aspects as
     * well as java files.
     * 
     * @parameter default-value="builddef.lst"
     */
    protected String argumentFileName;

    /**
     * Holder for ajc compiler options
     */
    protected List ajcOptions = new ArrayList();
    
    /**
     * Holds all files found using the includes, excludes parameters.
     */
    protected Set resolvedIncludes;

    /**
     * Abstract method used by child classes to spesify the correct output
     * directory for compiled classes.
     * 
     * @return where compiled classes should be put.
     */
    protected abstract List getOutputDirectories();

    /**
     * Abstract method used by child classes to spesify the correct source directory for classes.
     * 
     * @return where sources may be found.
     */
    protected abstract List getSourceDirectories();

    /**
     * Do the AspectJ compiling.
     * 
     * @throws MojoExecutionException
     */
    public void execute()
        throws MojoExecutionException
    {
        Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
        assembleArguments();

        if ( !isBuildNeeded() )
        {
            getLog().info( "No modifications found skipping aspectJ compile" );
            return;
        }

        getLog().info( "Starting compiling aspects" );
        if ( getLog().isDebugEnabled() )
        {
            String command = "Running : ajc ";
            Iterator iter = ajcOptions.iterator();
            while ( iter.hasNext() )
            {
                command += ( iter.next() + " " );
            }
            getLog().debug( command );
        }
        try
        {
            File outDir = new File( (String) getOutputDirectories().get( getOutputDirectories().size() - 1 ) );
            AjcHelper.writeBuildConfigToFile( ajcOptions, argumentFileName, outDir );
            getLog().info(
                           "Argumentsfile written : "
                               + new File( outDir.getAbsolutePath() + argumentFileName ).getAbsolutePath() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not write arguments file to the target area" );
        }
        Main main = new Main();
        MavenMessageHandler mavenMessageHandler = new MavenMessageHandler( getLog() );
        main.setHolder( mavenMessageHandler );

        main.runMain( (String[]) ajcOptions.toArray( new String[0] ), false );
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
     * Assembles a complete ajc compiler arguments list.
     * 
     * @return aspectj compiler arguments
     * @throws MojoExecutionException error in configuration
     */
    protected void assembleArguments()
        throws MojoExecutionException
    {
        // Add classpath
        ajcOptions.add( "-classpath" );
        ajcOptions.add( AjcHelper.createClassPath( project, getOutputDirectories() ) );

        // Add artifacts to weave
        addModulesArgument( "-inpath", ajcOptions, weaveDependencies, "a dependency to weave" );

        // Add library artifacts 
        addModulesArgument( "-aspectpath", ajcOptions, aspectLibraries, "an aspect library" );

        //add target dir argument
        ajcOptions.add( "-d" );
        ajcOptions.add( getOutputDirectories().get( getOutputDirectories().size() - 1 ) );

        // Add all the files to be included in the build,
        if ( null != ajdtBuildDefFile )
        {
            resolvedIncludes = AjcHelper.getBuildFilesForAjdtFile( ajdtBuildDefFile, basedir );
        }
        else
        {
            resolvedIncludes = AjcHelper.getBuildFilesForSourceDirs( getSourceDirectories(), this.includes, this.excludes );
        }
        ajcOptions.addAll( resolvedIncludes );
    }

    /**
     * Finds all artifacts in the weavemodule property,
     * and adds them to the ajc options.
     *  
     * @param arguments
     * @throws MojoExecutionException
     */
    private void addModulesArgument( String argument, List arguments, Module[] modules, String role )
        throws MojoExecutionException
    {
        if ( modules != null && modules.length > 0 )
        {
            arguments.add( argument );
            StringBuffer buf = new StringBuffer();
            for ( int i = 0; i < modules.length; ++i )
            {
                Module module = modules[i];
                String key = ArtifactUtils.versionlessKey( module.getGroupId(), module.getArtifactId() );
                Artifact artifact = (Artifact) project.getArtifactMap().get( key );
                if ( artifact == null )
                {
                    throw new MojoExecutionException( "The artifact " + key + " referenced in aspectj plugin as "
                        + role + ", is not found the project dependencies" );
                }
                if ( buf.length() != 0 )
                {
                    buf.append( File.pathSeparatorChar );
                }
                buf.append( artifact.getFile().getPath() );
            }
            String pathString = buf.toString();
            arguments.add( pathString );
            getLog().debug( "Adding " + argument + ": " + pathString );
        }
    }

    /**
     * Checks all included files for modifications. If one of the files has changed, we need
     * to reweave everything, since a pointcut may have changed.
     * 
     * The rules for doing a new build are :
     * 1) If no previous build definition file is found.
     * 2) If previous build arguments is not equal to the current args
     * 3) If Some of the files in the affected filelist is changed since last build
     * @throws MojoExecutionException 
     *
     */
    protected boolean isBuildNeeded()
        throws MojoExecutionException
    {
        File outDir = new File(getOutputDirectories().get( getOutputDirectories().size() - 1 ).toString());   
        // 1)
        if ( !FileUtils.fileExists( outDir.getAbsolutePath()
            + argumentFileName ) )
        {
            return true;
        }
        // 2)
        try
        {
            if ( !ajcOptions.equals( AjcHelper.readBuildConfigFile( argumentFileName, outDir ) ) )
            {
                return true;
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error during reading of previous argumentsfile " );
        }
        // 3)
        Iterator sourceIter = resolvedIncludes.iterator();
        long lastBuild = new File(outDir.getAbsolutePath()
                                  + argumentFileName ).lastModified();
        while (sourceIter.hasNext())
        {
            File sourceFile = new File((String) sourceIter.next());
            long sourceModified = sourceFile.lastModified(); 
            if ( sourceModified >= lastBuild)
            {
                return true;
            }
        }
        
        return false;
    }

    /** 
     * Setters which when called sets compiler arguments
     * @throws MojoExecutionException 
     */
    public void setComplianceLevel( String complianceLevel )
    {
        if ( complianceLevel.equals( "1.3" ) || complianceLevel.equals( "1.4" ) || complianceLevel.equals( "1.5" ) )
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

    public void setXlint( String xlint )
    {
        ajcOptions.add( "-Xlint" );
        ajcOptions.add( xlint );
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

    public void setArgumentFileName( String argumentFileName )
    {
        this.argumentFileName = argumentFileName;

    }

}
