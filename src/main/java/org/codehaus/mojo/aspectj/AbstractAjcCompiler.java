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

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
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
 * Base class for the two aspectJ compile-time weaving mojos.
 * <p>
 * For all available options see <a href="http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html">ajc-ref</a>
 *
 * @author <a href="mailto:kaare.nilsen@gmail.com">Kaare Nilsen</a>
 */
public abstract class AbstractAjcCompiler extends AbstractAjcMojo {

    // Constants

    /**
     * List holding all accepted values for the {@code Xajruntimetarget} parameter.
     */
    public static final List<String> XAJRUNTIMETARGET_SUPPORTED_VALUES = Arrays.asList("1.2", "1.5");

    /**
     * The source directory for the aspects.
     *
     */
    @Parameter( defaultValue = "src/main/aspect" )
    protected String aspectDirectory = "src/main/aspect";

    /**
     * The source directory for the test aspects.
     *
     */
    @Parameter( defaultValue = "src/test/aspect" )
    protected String testAspectDirectory = "src/test/aspect";

    /**
     * List of ant-style patterns used to specify the aspects that should be included when compiling. When none
     * specified all .java and .aj files in the project source directories, or directories specified by the ajdtDefFile
     * property are included.
     *
     */
    @Parameter
    protected String[] includes;

    /**
     * List of ant-style patterns used to specify the aspects that should be excluded when compiling. When none
     * specified all .java and .aj files in the project source directories, or directories specified by the ajdtDefFile
     * property are included.
     *
     */
    @Parameter
    protected String[] excludes;

    /**
     * Where to find the ajdt build definition file. <i>If set this will override the use of project sourcedirs</i>.
     *
     */
    @Parameter
    protected String ajdtBuildDefFile;

    /**
     * Generate aop.xml file for load-time weaving with default name (/META-INF/aop.xml).
     *
     */
    @Parameter
    protected boolean outxml;

    /**
     * Generate aop.xml file for load-time weaving with custom name.
     *
     */
    @Parameter
    protected String outxmlfile;

    /**
     * Generate .ajesym symbol files for emacs support.
     *
     */
    @Parameter
    protected boolean emacssym;

    /**
    * Set the compiler "proc" argument.
    * Aspectj supports Annotation processing since 1.8.2, it can been disabled by <code>proc:none</code>.
    *
    * @see <a href="https://www.eclipse.org/aspectj/doc/released/README-182.html">AspectJ 1.8.2 Release notes</a>
    * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html#processing">Annotation Processing</a>
    */
    @Parameter
    protected String proc;

    /**
    * Set the compiler "parameters" argument.
    *
    */
    @Parameter
    protected boolean parameters;


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
     * @since 1.5
     */
    @Parameter
    protected Map<String, String> Xset;

    /**
     * generate .ajsym file into the output directory
     *
     */
    @Parameter
    protected boolean crossrefs;

    /**
     * Set default level for messages about potential programming mistakes in crosscutting code. {level} may be ignore,
     * warning, or error. This overrides entries in org/aspectj/weaver/XlintDefault.properties from aspectjtools.jar.
     *
     */
    @Parameter
    protected String Xlint;

    /**
     * Specify properties file to set levels for specific crosscutting messages.
     * PropertyFile is a path to a Java .properties file that takes the same property names and values as
     * org/aspectj/weaver/XlintDefault.properties from aspectjtools.jar, which it also overrides.
     *
     */
    @Parameter
    protected File Xlintfile;

    /**
     * Enables the compiler to support hasmethod(method_pattern) and hasfield(field_pattern) type patterns, but only
     * within declare statements. It's experimental and undocumented because it may change, and because it doesn't yet
     * take into account ITDs.
     *
     * @since 1.3
     */
    @Parameter
    protected boolean XhasMember;

    /**
     * Specify bytecode target setting (1.3 to 1.9, 10 to 16). See 'complianceLevel' for details. 
     *
     * @see org.codehaus.mojo.aspectj.AjcHelper#ACCEPTED_COMPLIANCE_LEVEL_VALUES
     */
    @Parameter( defaultValue = "${project.build.java.target}" )
    protected String target;

    /**
     * Specify source code language level (1.3 to 1.9, 10 to 16). See 'complianceLevel' for details. 
     *
     * @see org.codehaus.mojo.aspectj.AjcHelper#ACCEPTED_COMPLIANCE_LEVEL_VALUES
     */
    @Parameter( defaultValue = "${mojo.java.target}" )
    protected String source;

    /**
     * Specify compiler compliance setting (same as setting 'source' and 'target' to the same level).
     * Permitted values: 1.3, 1.4, 1.5, 5, 5.0, 1.6, 6, 6.0, 1.7, 7, 7.0, 1.8, 8, 8.0,
     * 1.9, 9, 9.0, 10, 10.0, 11, 11.0, 12, 12.0, 13, 13.0, 14, 14.0, 15, 15.0, 16, 16.0.
     *
     * @see org.codehaus.mojo.aspectj.AjcHelper#ACCEPTED_COMPLIANCE_LEVEL_VALUES
     */
    @Parameter( defaultValue = "1.4" )
    protected String complianceLevel;

    /**
     * Toggle warning messages on deprecations
     *
     */
    @Parameter
    protected boolean deprecation;

    /**
     * Emit no errors for unresolved imports;
     *
     */
    @Parameter
    protected boolean noImportError;

    /**
     * Keep compiling after error, dumping class files with problem methods
     *
     */
    @Parameter
    protected boolean proceedOnError;

    /**
     * Preserve all local variables during code generation (to facilitate debugging).
     *
     */
    @Parameter
    protected boolean preserveAllLocals;

    /**
     * Compute reference information.
     *
     */
    @Parameter
    protected boolean referenceInfo;

    /**
     * Specify default source encoding format.
     *
     */
    @Parameter( property = "project.build.sourceEncoding" )
    protected String encoding;

    /**
     * Emit messages about accessed/processed compilation units
     *
     */
    @Parameter
    protected boolean verbose;

    /**
     * Emit messages about weaving
     *
     */
    @Parameter
    protected boolean showWeaveInfo;

    /**
     * Repeat compilation process N times (typically to do performance analysis).
     *
     */
    @Parameter
    protected int repeat;

    /**
     * (Experimental) runs weaver in reweavable mode which causes it to create woven classes that can be rewoven,
     * subject to the restriction that on attempting a reweave all the types that advised the woven type must be
     * accessible.
     *
     */
    @Parameter
    protected boolean Xreweavable;

    /**
     * (Experimental) Create class files that can't be subsequently rewoven by AspectJ.
     *
     */
    @Parameter
    protected boolean XnotReweavable;

    /**
     * (Experimental) do not inline around advice
     *
     */
    @Parameter
    protected boolean XnoInline;

    /**
     * (Experimental) Normally it is an error to declare aspects {@link Serializable}. This option removes that restriction.
     *
     */
    @Parameter
    protected boolean XserializableAspects;

    /**
     * Causes the compiler to calculate and add the SerialVersionUID field to any type implementing {@link Serializable} that is
     * affected by an aspect. The field is calculated based on the class before weaving has taken place.
     *
     */
    @Parameter
    protected boolean XaddSerialVersionUID;

    /**
     * Causes compiler to terminate before weaving
     *
     */
    @Parameter
    protected boolean XterminateAfterCompilation;

    /**
     * (Experimental) Allows code to be generated that targets a 1.2 or a 1.5 level AspectJ runtime (default 1.5)
     *
     */
    @Parameter( defaultValue = "1.5" )
    protected String Xajruntimetarget;

    /**
     * supply a comma separated list of new joinpoints
     * that can be identified by pointcuts.  Values are:
     * arrayconstruction, synchronization
     *
     */
    @Parameter
    protected String Xjoinpoints;

    /**
     * Override location of VM's bootclasspath for purposes of evaluating types when compiling. Path is a single
     * argument containing a list of paths to zip files or directories, delimited by the platform-specific path
     * delimiter.
     *
     */
    @Parameter
    protected String bootclasspath;

    /**
     * Emit warnings for any instances of the comma-delimited list of questionable code.
     * Supported values are shown in the list below, with their respective explanations - as copied
     * directly from the AJC reference.
     * <dl>
     *   <dt>constructorName</dt>
     *   <dd>method with constructor name</dd>
     *   <dt>packageDefaultMethod</dt>
     *   <dd>attempt to override package-default method</dd>
     *   <dt>deprecation</dt>
     *   <dd>usage of deprecated type or member</dd>
     *   <dt>maskedCatchBlocks</dt>
     *   <dd>hidden catch block</dd>
     *   <dt>unusedLocals</dt>
     *   <dd>local variable never read</dd>
     *   <dt>unusedArguments</dt>
     *   <dd>method argument never read</dd>
     *   <dt>unusedImports</dt>
     *   <dd>import statement not used by code in file</dd>
     *   <dt>none</dt>
     *   <dd>suppress all compiler warnings</dd>
     * </dl>
     *
     * @see <a href="http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html#ajc">Eclipse AJC reference</a>
     */
    @Parameter
    protected String warn;

    /**
     * The filename holding AJC build arguments.
     * The file will be placed in the project build output directory, and will contain all the arguments passed to
     * the AJC compiler in the last run, and also all the files included in the AJC build.
     * <p>
     * Sample content shown below to illustrate typical content within the builddef.lst file:
     * <pre><code>
     * -1.6
     * -encoding
     * UTF-8
     * -classpath
     * /my/library/lib.jar:/somewhere/aspectjrt.jar:/my/project/target/classes
     * -d
     * /my/project/target/classes
     * /my/project/src/main/java/org/acme/ValidationAspect.java
     * </code></pre>
     */
    @Parameter( defaultValue = "builddef.lst" )
    protected String argumentFileName = "builddef.lst";

    /**
     * Forces re-compilation, regardless of whether the compiler arguments or the sources have changed.
     *
     */
    @Parameter( defaultValue = "false" )
    protected boolean forceAjcCompile;

  /**
   * Sets additional compiler arguments, e.g.
   * <pre>{@code
   * <compilerArgs>
   *   <arg>-Xmaxerrs=1000</arg>
   *   <arg>-Xlint</arg>
   *   <arg>-J-Duser.language=en_us</arg> 
   * </compilerArgs>
   * }</pre>
   * This option can be used in case you want to use AJC options not (yet) supported by this plugin.
   * <p>
   * <b>Caveat:</b> Be careful when using this option and select the additional compiler arguments wisely, because
   * behaviour is undefined if you add arguments which have already been added by the plugin using regular parameters
   * or their default values. The resulting compiler command line will in that case contain duplicate arguments, which
   * might be illegal depending on the specific argument. Do not expect to be able to manually override existing
   * arguments using this option or to replace whole argument lists.
   *
   * @since 1.13
   */
  @Parameter
  protected List<String> additionalCompilerArgs = new ArrayList<>();

    /**
     * Activates compiler preview features (e.g. sealed classes in Java 16) when used with a suitable JDK version.
     * <p>
     * <b>Please note:</b> You cannot run code compiled with preview features on any other JDK than the one used for
     * compilation. For example, records compiled with preview on JDK 15 cannot be used on JDK 16 without recompilation.
     * This is a JVM limitation unrelated to AspectJ. Also, e.g. sealed classes are preview-1 on JDK 15 and preview-2 on
     * JDK 16. You still need to recompile, no matter what. 
     *
     * @since 1.13
     */
    // TODO:
    //   Create tickets for at least Eclipse IDE and IntelliJ IDEA to recognise this switch and import it as a compiler
    //   and possibly runtime setting. As for AJDT, maybe we have to implement it ourselves, but actually I found no
    //   references to the Maven module there, so I guess the import is implemented somewhere else.
    @Parameter( defaultValue = "false" )
    protected boolean enablePreview;

    /**
     * Holder for ajc compiler options
     */
    protected List<String> ajcOptions = new ArrayList<>();

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
     * The directory for sources generated by annotation processing.
     *
     * @return the generatedSourcesDirectory
     */
    protected abstract File getGeneratedSourcesDirectory();

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
     * @throws MojoExecutionException if arguments file cannot be written
     */
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException {

        if (isSkip()) {
            if (getLog().isInfoEnabled()) {
                getLog().info("Skipping execution because of 'skip' option");
            }
            return;
        }

        ArtifactHandler artifactHandler = project.getArtifact().getArtifactHandler();
        if (!forceAjcCompile && !"java".equalsIgnoreCase(artifactHandler.getLanguage())) {
            getLog().warn("Not executing aspectJ compiler as the project is not a Java classpath-capable package");
            return;
        }

        // MASPECT-110:
        //
        // Only add the aspectSourcePathDir and testAspectSourcePathDir to their respective
        // compileSourceRoots if they actually exist and are directories... to avoid crashing
        // downstream plugins requiring/assuming that all entries within the compileSourceRoots
        // and testCompileSourceRoots are directories.
        //
        final File aspectSourcePathDir = FileUtils.resolveFile(basedir, aspectDirectory);
        final File testAspectSourcePathDir = FileUtils.resolveFile(basedir, testAspectDirectory);

        final String aspectSourcePath = aspectSourcePathDir.getAbsolutePath();
        final String testAspectSourcePath = testAspectSourcePathDir.getAbsolutePath();

        if (aspectSourcePathDir.exists() && aspectSourcePathDir.isDirectory()
                && !project.getCompileSourceRoots().contains(aspectSourcePath)) {
            getLog().debug("Adding existing aspectSourcePathDir [" + aspectSourcePath + "] to compileSourceRoots.");
            project.getCompileSourceRoots().add(aspectSourcePath);
        } else {
            getLog().debug("Not adding non-existent or already added aspectSourcePathDir [" + aspectSourcePath
                    + "] to compileSourceRoots.");
        }

        if (testAspectSourcePathDir.exists() && testAspectSourcePathDir.isDirectory()
                && !project.getTestCompileSourceRoots().contains(testAspectSourcePath)) {
            getLog().debug(
                    "Adding existing testAspectSourcePathDir [" + testAspectSourcePath + "] to testCompileSourceRoots.");
            project.getTestCompileSourceRoots().add(testAspectSourcePath);
        } else {
            getLog().debug("Not adding non-existent or already added testAspectSourcePathDir [" + testAspectSourcePath
                    + "] to testCompileSourceRoots.");
        }

        assembleArguments();

        if (!forceAjcCompile && !hasSourcesToCompile()) {
            getLog().warn("No sources found skipping aspectJ compile");
            return;
        }

        if (!forceAjcCompile && !isBuildNeeded()) {
            getLog().info("No modifications found skipping aspectJ compile");
            return;
        }

        if (getLog().isDebugEnabled()) {
            StringBuilder command = new StringBuilder("Running : ajc");

            for (String arg : ajcOptions) {
                command.append(' ').append(arg);
            }
            getLog().debug(command);
        }
        try {
            getLog().debug(
                    "Compiling and weaving " + resolvedIncludes.size() + " sources to " + getOutputDirectory());
            AjcHelper.writeBuildConfigToFile(ajcOptions, argumentFileName, getOutputDirectory());
            getLog().debug(
                    "Arguments file written : " + new File(getOutputDirectory(), argumentFileName).getAbsolutePath());
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write arguments file to the target area", e);
        }

        final Main ajcMain = new Main();
        MavenMessageHandler mavenMessageHandler = new MavenMessageHandler(getLog());
        ajcMain.setHolder(mavenMessageHandler);

        synchronized (BIG_ASPECTJ_LOCK) {
            ajcMain.runMain(ajcOptions.toArray(new String[0]), false);
        }

        IMessage[] errors = mavenMessageHandler.getMessages(IMessage.ERROR, true);
        if (!proceedOnError && errors.length > 0) {
            throw CompilationFailedException.create(errors);
        }
    }

    /**
     * Assembles a complete ajc compiler arguments list.
     *
     * @throws MojoExecutionException error in configuration
     */
    protected void assembleArguments()
            throws MojoExecutionException {
        if (XhasMember) {
            ajcOptions.add("-XhasMember");
        }

        // Add classpath
        ajcOptions.add("-classpath");
        ajcOptions.add(AjcHelper.createClassPath(project, null, getClasspathDirectories()));

        // Add boot classpath
        if (null != bootclasspath) {
            ajcOptions.add("-bootclasspath");
            ajcOptions.add(bootclasspath);
        }

        if (null != Xjoinpoints) {
            ajcOptions.add("-Xjoinpoints:" + Xjoinpoints);
        }

        // Add warn option
        if (null != warn) {
            ajcOptions.add("-warn:" + warn);
        }

        if (null != proc) {
            ajcOptions.add("-proc:" + proc);
        }

        if (Xset != null && !Xset.isEmpty()) {
            StringBuilder sb = new StringBuilder("-Xset:");
            for (Map.Entry<String, String> param : Xset.entrySet()) {
                sb.append(param.getKey());
                sb.append("=");
                sb.append(param.getValue());
                sb.append(',');
            }
            ajcOptions.add(sb.substring(0, sb.length() - 1));
        }

        // Add artifacts or directories to weave
        String joinedWeaveDirectories = null;
        if (weaveDirectories != null) {
            joinedWeaveDirectories = StringUtils.join(weaveDirectories, File.pathSeparator);
        }
        addModulesArgument("-inpath", ajcOptions, weaveDependencies, joinedWeaveDirectories,
                "dependencies and/or directories to weave");

        // Add library artifacts
        addModulesArgument("-aspectpath", ajcOptions, aspectLibraries, getAdditionalAspectPaths(),
                "an aspect library");

        // Add Java 9+ modules needed for compilation
        addModulesArgument("--module-path", ajcOptions, javaModules, null,
                "Java module");

        // Add xmlConfigured option and argument
        if (null != xmlConfigured) {
            ajcOptions.add("-xmlConfigured");
            ajcOptions.add(xmlConfigured.getAbsolutePath());
        }

        // add target dir argument
        ajcOptions.add("-d");
        ajcOptions.add(getOutputDirectory().getAbsolutePath());

        ajcOptions.add("-s");
        ajcOptions.add(getGeneratedSourcesDirectory().getAbsolutePath());

        // Add all the files to be included in the build,
        if (null != ajdtBuildDefFile) {
            resolvedIncludes = AjcHelper.getBuildFilesForAjdtFile(ajdtBuildDefFile, basedir);
        } else {
            resolvedIncludes = getIncludedSources();
        }
        ajcOptions.addAll(resolvedIncludes);

        if (CollectionUtils.isNotEmpty(additionalCompilerArgs)) {
            ajcOptions.addAll(additionalCompilerArgs);
        }
    }

    protected Set<String> getIncludedSources()
            throws MojoExecutionException {
        Set<String> result = new HashSet<>();
        if (getJavaSources() == null) {
            result = AjcHelper.getBuildFilesForSourceDirs(getSourceDirectories(), this.includes, this.excludes);
        } else {
            for (int scannerIndex = 0; scannerIndex < getJavaSources().length; scannerIndex++) {
                Scanner scanner = getJavaSources()[scannerIndex];
                if (scanner.getBasedir() == null) {
                    getLog().info("Source without basedir, skipping it.");
                } else {
                    scanner.scan();
                    for (int fileIndex = 0; fileIndex < scanner.getIncludedFiles().length; fileIndex++) {
                        result.add(FileUtils.resolveFile(scanner.getBasedir(),
                                scanner.getIncludedFiles()[fileIndex]).getAbsolutePath());
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
    private void addModulesArgument(final String argument, final List<String> arguments, final Module[] modules,
                                    final String aditionalpath, final String role)
            throws MojoExecutionException {
        StringBuilder buf = new StringBuilder();

        if (null != aditionalpath) {
            arguments.add(argument);
            buf.append(aditionalpath);
        }
        if (modules != null && modules.length > 0) {
            if (!arguments.contains(argument)) {
                arguments.add(argument);
            }

            for (Module module : modules) {
                // String key = ArtifactUtils.versionlessKey( module.getGroupId(), module.getArtifactId() );
                // Artifact artifact = (Artifact) project.getArtifactMap().get( key );
                Artifact artifact = null;
                @SuppressWarnings("unchecked") Set<Artifact> allArtifacts = project.getArtifacts();
                for (Artifact art : allArtifacts) {
                    if (art.getGroupId().equals(module.getGroupId()) && art.getArtifactId().equals(
                            module.getArtifactId()) && StringUtils.defaultString(module.getClassifier()).equals(
                            StringUtils.defaultString(art.getClassifier())) && StringUtils.defaultString(
                            module.getType(), "jar").equals(StringUtils.defaultString(art.getType()))) {
                        artifact = art;
                        break;
                    }
                }
                if (artifact == null) {
                    throw new MojoExecutionException(
                            "The artifact " + module.toString() + " referenced in aspectj plugin as " + role
                                    + ", is not found the project dependencies");
                }
                if (buf.length() != 0) {
                    buf.append(File.pathSeparatorChar);
                }
                buf.append(artifact.getFile().getPath());
            }
        }
        if (buf.length() > 0) {
            String pathString = buf.toString();
            arguments.add(pathString);
            getLog().debug("Adding " + argument + ": " + pathString);
        }
    }

    /**
     * Checks modifications that would make us need a build
     *
     * @return <code>true</code> if build is needed, otherwise <code>false</code>
     * @throws MojoExecutionException if an unexpected error occurs, e.g. weave directories cannot be resolved 
     */
    protected boolean isBuildNeeded()
            throws MojoExecutionException {
        File outDir = getOutputDirectory();
        return hasNoPreviousBuild(outDir) || hasArgumentsChanged(outDir) ||
                hasSourcesChanged(outDir) || hasNonWeavedClassesChanged(outDir);

    }

    private boolean hasNoPreviousBuild(File outDir) {
        return !FileUtils.resolveFile(outDir, argumentFileName).exists();
    }

    private boolean hasArgumentsChanged(File outDir)
            throws MojoExecutionException {
        try {
            return (!ajcOptions.equals(AjcHelper.readBuildConfigFile(argumentFileName, outDir)));
        } catch (IOException e) {
            throw new MojoExecutionException("Error during reading of previous argumentsfile ");
        }
    }

    /**
     * Not entirely safe, assembleArguments() must be run
     */
    private boolean hasSourcesToCompile() {
        return resolvedIncludes.size() > 0;
    }

    private boolean hasSourcesChanged(File outDir) {
        long lastBuild = new File(outDir, argumentFileName).lastModified();
        for (String source : resolvedIncludes) {
            File sourceFile = new File(source);
            long sourceModified = sourceFile.lastModified();
            if (sourceModified >= lastBuild) {
                return true;
            }

        }
        return false;
    }

    private boolean hasNonWeavedClassesChanged(File outDir)
            throws MojoExecutionException {
        if (weaveDirectories != null && weaveDirectories.length > 0) {
            Set<String> weaveSources = AjcHelper.getWeaveSourceFiles(weaveDirectories);
            long lastBuild = new File(outDir, argumentFileName).lastModified();
            for (String source : weaveSources) {
                File sourceFile = new File(source);
                long sourceModified = sourceFile.lastModified();
                if (sourceModified >= lastBuild) {
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
    public void setComplianceLevel(String complianceLevel) {
        if (AjcHelper.isValidComplianceLevel(complianceLevel)) {
            ajcOptions.add("-" + complianceLevel);
        }
    }

    public void setDeprecation(boolean deprecation) {
        if (deprecation) {
            ajcOptions.add("-deprecation");
        }
    }

    public void setEmacssym(boolean emacssym) {
        if (emacssym) {
            ajcOptions.add("-emacssym");
        }

    }

    public void setParameters(boolean parameters) {
        if (parameters) {
            ajcOptions.add("-parameters");
        }
    }

    public void setCrossrefs(boolean crossrefs) {
        if (crossrefs) {
            ajcOptions.add("-crossrefs");
        }
    }

    public void setEncoding(String encoding) {
        ajcOptions.add("-encoding");
        ajcOptions.add(encoding);
    }

    public void setNoImportError(boolean noImportError) {
        if (noImportError) {
            ajcOptions.add("-noImportError");
        }

    }

    public void setOutxml(boolean outxml) {
        if (outxml) {
            ajcOptions.add("-outxml");
        }
    }

    public void setOutxmlfile(String outxmlfile) {
        ajcOptions.add("-outxmlfile");
        ajcOptions.add(outxmlfile);
    }

    public void setPreserveAllLocals(boolean preserveAllLocals) {
        if (preserveAllLocals) {
            ajcOptions.add("-preserveAllLocals");
        }

    }

    public void setProceedOnError(boolean proceedOnError) {
        if (proceedOnError) {
            ajcOptions.add("-proceedOnError");
        }
        this.proceedOnError = proceedOnError;
    }

    public void setReferenceInfo(boolean referenceInfo) {
        if (referenceInfo) {
            ajcOptions.add("-referenceInfo");
        }
    }

    public void setRepeat(int repeat) {
        ajcOptions.add("-repeat");
        ajcOptions.add("" + repeat);
    }

    public void setShowWeaveInfo(boolean showWeaveInfo) {
        if (showWeaveInfo) {
            ajcOptions.add("-showWeaveInfo");
        }
    }

    public void setTarget(String target) {
        if (AjcHelper.isValidComplianceLevel(target)) {
            ajcOptions.add("-target");
            ajcOptions.add(target);
        }
    }

    public void setSource(String source) {
        if (AjcHelper.isValidComplianceLevel(source)) {
            ajcOptions.add("-source");
            ajcOptions.add(source);
        }
    }

    public void setEnablePreview(boolean enablePreview) {
        if (enablePreview) {
            ajcOptions.add("--enable-preview");
        }
    }

    public void setVerbose(boolean verbose) {
        if (verbose) {
            ajcOptions.add("-verbose");
        }
    }

    public void setXhasMember(boolean xhasMember) {
        XhasMember = xhasMember;
    }

    public void setXlint(String xlint) {
        ajcOptions.add("-Xlint:" + xlint);
    }

    public void setXset(Map<String, String> xset) {
        this.Xset = xset;
    }

    public void setXlintfile(File xlintfile) {
        try {
            final String prefix = "Xlintfile parameter invalid: ";
            final String path = xlintfile.getCanonicalPath();
            if (!xlintfile.exists()) {
                getLog().warn(prefix + " file [" + path + "] does not exist");
            } else if (xlintfile.isDirectory()) {
                getLog().warn(prefix + " given path [" + path + "] is a directory.");
            } else if (!path.trim().toLowerCase().endsWith(".properties")) {
                getLog().warn(prefix + " must be a .properties file");
            } else {
                ajcOptions.add("-Xlintfile");
                ajcOptions.add(path);
            }
        } catch (IOException e) {
            getLog().error("IOException while setting Xlintfile option", e);
        }
    }

    public void setXnoInline(boolean xnoInline) {
        if (xnoInline) {
            ajcOptions.add("-XnoInline");
        }
    }

    public void setXreweavable(boolean xreweavable) {
        if (xreweavable) {
            ajcOptions.add("-Xreweavable");
        }
    }

    public void setXnotReweavable(boolean xnotReweavable) {
        if (xnotReweavable) {
            ajcOptions.add("-XnotReweavable");
        }
    }

    public void setXserializableAspects(boolean xserializableAspects) {
        if (xserializableAspects) {
            ajcOptions.add("-XserializableAspects");
        }
    }

    public void setXaddSerialVersionUID(boolean xaddSerialVersionUID) {
        if (xaddSerialVersionUID) {
            ajcOptions.add("-XaddSerialVersionUID");
        }
    }

    public void setXterminateAfterCompilation(boolean xterminateAfterCompilation) {
        if (xterminateAfterCompilation) {
            ajcOptions.add("-XterminateAfterCompilation");
        }
    }

    public void setXajruntimetarget(String xajruntimetarget) {
        if (XAJRUNTIMETARGET_SUPPORTED_VALUES.contains(xajruntimetarget)) {
            ajcOptions.add("-Xajruntimetarget:" + xajruntimetarget);
        } else {
            getLog().warn(
                    "Incorrect Xajruntimetarget value specified. Supported: " + XAJRUNTIMETARGET_SUPPORTED_VALUES);
        }
    }

    public void setBootClassPath(String bootclasspath) {
        this.bootclasspath = bootclasspath;
    }

    public void setXjoinpoints(String xjoinpoints) {
        this.Xjoinpoints = xjoinpoints;
    }

    public void setWarn(String warn) {
        this.warn = warn;
    }

    public void setArgumentFileName(String argumentFileName) {
        this.argumentFileName = argumentFileName;
    }
}
