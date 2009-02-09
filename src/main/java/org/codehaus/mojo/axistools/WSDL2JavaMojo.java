package org.codehaus.mojo.axistools;

/*
 * Copyright 2005 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.axistools.axis.AxisPluginException;
import org.codehaus.mojo.axistools.wsdl2java.DefaultWSDL2JavaPlugin;
import org.codehaus.mojo.axistools.wsdl2java.WSDL2JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A Plugin for generating stubs for WSDL files using Axis WSDL2Java.
 *
 * @author jesse <jesse.mcconnell@gmail.com>
 * @author Christoph Schoenfeld <christophster@gmail.com>
 * @version $Id$
 * @requiresDependencyResolution test
 * @goal wsdl2java
 * @phase generate-sources
 * @description WSDL2Java plugin
 */
public class WSDL2JavaMojo
    extends AbstractMojo
{
    /**
     * List of URLs to process.
     *
     * @parameter expression=""
     */
    private ArrayList urls;

    /**
     * List of WSDL files from {@link #sourceDirectory} to process.
     *
     * @parameter expression=""
     */
    private ArrayList wsdlFiles;

    /**
     * List of source dependencies in the format groupId:artifactId:version:file.
     *
     * @parameter expression=""
     */
    private ArrayList sourceDependencies;

    /**
     * Cache directory for WSDLs from URLs.
     *
     * @parameter default-value="${project.build.directory}/axistools/wsdl2java/urlDownloads"
     */
    private File urlDownloadDirectory;

    /**
     * Cache directory for WSDLs from sourceDependencies.
     *
     * @parameter default-value="${project.build.directory}/axistools/wsdl2java/sourceDependencies"
     */
    private File sourceDependencyDirectory;

    /**
     * Use the Emitter for generating the java files as opposed to the commandline wsdl2java tool.
     *
     * @parameter default-value="false"
     */
    private boolean useEmitter;

    /**
     * Mappings are only used when useEmitter is set to true.
     *
     * @parameter expression=""
     */
    private ArrayList mappings;

    /**
     * Emit server-side bindings for web service.
     *
     * @parameter expression="${serverSide}"
     */
    private boolean serverSide;

    /**
     * Package to create the java files under.
     *
     * @parameter expression="${packageSpace}"
     */
    private String packageSpace;

    /**
     * See what the tool is generating as it is generating it.
     *
     * @parameter expression="${verbose}"
     */
    private boolean verbose;

    /**
     * Generate the test cases.
     *
     * @parameter expression="${testCases}"
     */
    private boolean testCases;

    /**
     * Copy the generated test cases to a generated-sources test directory to be
     * compiled and run as normal surefire unit tests.
     *
     * @parameter default-value="false"
     */
    private boolean runTestCasesAsUnitTests;

    /**
     * Generate code for all elements, even unreferenced ones.
     * By default, WSDL2Java only generates code for those elements in the WSDL file that are referenced.
     * A note about what it means to be referenced.
     * We cannot simply say: start with the services, generate all bindings referenced by the service,
     * generate all portTypes referenced by the referenced bindings, etc.
     * What if we're generating code from a WSDL file that only contains portTypes, messages, and types?
     * If WSDL2Java used service as an anchor, and there's no service in the file, then nothing will be generated.
     * So the anchor is the lowest element that exists in the WSDL file in the order:
     * <ol>
     *   <li>types
     *   <li>portTypes
     *   <li>bindings
     *   <li>services
     * </ol>
     * For example, if a WSDL file only contained types, then all the listed types would be generated.
     * But if a WSDL file contained types and a portType,
     * then that portType will be generated and only those types that are referenced by that portType.
     * Note that the anchor is searched for in the WSDL file appearing on the command line, not in imported WSDL files.
     * This allows one WSDL file to import constructs defined in another WSDL file without the nuisance of having all the imported WSDL file's constructs generated.
     *
     * @parameter expression="${allElements}"
     */
    private boolean allElements;

    /**
     * Print debug information, which currently is WSDL2Java's symbol table.
     * Note that this is only printed after the symbol table is complete, ie., after the WSDL is parsed successfully.
     *
     * @parameter default-value="false"
     */
    private boolean debug;

    /**
     * Timeout in seconds (default is 45, specify -1 to disable).
     *
     * @parameter expression="${timeout}"
     */
    private Integer timeout;

    /**
     * Only generate code for the immediate WSDL document.
     *
     * @parameter default-value="false"
     */
    private boolean noImports;

    /**
     * Turn off support for "wrapped" document/literal.
     *
     * @parameter default-value="false"
     */
    private boolean noWrapped;

    /**
     * @parameter default-value="true"
     * NJS 6 July 2006
     */
    private boolean wrapArrays;

    /**
     * Deploy skeleton (true) or implementation (false) in deploy.wsdd.
     *
     * @parameter default-value="false"
     */
    private boolean skeletonDeploy;

    /**
     * Mapping of namespace to package.
     *
     * @parameter expression="${namespaceToPackage}"
     */
    private String namespaceToPackage;

    /**
     * File containing NStoPkg mappings.
     * @parameter expression="${fileNamespaceToPackage}"
     */
    private File fileNamespaceToPackage;

    /**
     * Add scope to deploy.xml: "Application", "Request", "Session".
     *
     * @parameter expression="${deployScope}"
     */
    private String deployScope;

    /**
     * Indicate 1.1 or 1.2. The default is 1.1 (SOAP 1.1 JAX-RPC compliant.
     * 1.2 indicates SOAP 1.1 encoded.).
     *
     * @parameter expression="${typeMappingVersion}"
     */
    private String typeMappingVersion;

    /**
     * Name of a custom class that implements GeneratorFactory interface
     * (for extending Java generation functions).
     *
     * @parameter expression="${factory}"
     */
    private String factory;

    /**
     * Namescape to specifically include in the generated code (defaults to
     * all namespaces unless specifically excluded with the {@linkplain #nsExcludes} option).
     *
     * @parameter
     */
    private ArrayList nsIncludes;

    /**
     * Namespace to specifically exclude from the generated code (defaults to
     * none excluded until first namespace included with {@linkplain #nsIncludes} option).
     *
     * @parameter
     */
    private ArrayList nsExcludes;

    /**
     * Emits separate Helper classes for meta data.
     *
     * @parameter expression="false"
     */
    private boolean helperGen;

    /**
     * Username to access the WSDL-URI.
     *
     * @parameter expression="${username}"
     */
    private String username;

    /**
     * Password to access the WSDL-URI.
     *
     * @parameter expression="${password}"
     */
    private String password;

    /**
     * Use this as the implementation class.
     *
     * @parameter expression="${implementationClassName}"
     */
    private String implementationClassName;

    /**
     * load.wsdl would further subpackage into load.*
     *
     * @parameter expression="${subPackageByFileName}"
     */
    private boolean subPackageByFileName;

    /**
     * Location to place generated test source files.
     *
     * @parameter default-value="${project.build.directory}/generated-test-sources/wsdl"
     */
    private File testSourceDirectory;

    /**
     * Source directory that contains .wsdl files.
     *
     * @parameter default-value="${basedir}/src/main/wsdl"
     */
    private File sourceDirectory;

    /**
     * Location to place generated java source files.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/axistools/wsdl2java"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    private File timestampDirectory;

    /**
     * The granularity in milliseconds of the last modification
     * date for testing whether a source needs recompilation.
     *
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    private List pluginArtifacts;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        WSDL2JavaPlugin plugin = new DefaultWSDL2JavaPlugin();

        plugin.setAllElements( allElements );
        plugin.setDebug( debug );
        plugin.setDeployScope( deployScope );
        plugin.setFactory( factory );
        plugin.setFileNamespaceToPackage( fileNamespaceToPackage );
        plugin.setHelperGen( helperGen );
        plugin.setImplementationClassName( implementationClassName );
        plugin.setMappings( mappings );
        plugin.setNamespaceToPackage( namespaceToPackage );
        plugin.setNoImports( noImports );
        plugin.setNoWrapped( noWrapped );
        plugin.setWrapArrays( wrapArrays );
        plugin.setNsExcludes( nsExcludes );
        plugin.setNsIncludes( nsIncludes );
        plugin.setPackageSpace( packageSpace );
        plugin.setPassword( password );
        plugin.setRunTestCasesAsUnitTests( runTestCasesAsUnitTests );
        plugin.setServerSide( serverSide );
        plugin.setSkeletonDeploy( skeletonDeploy );
        plugin.setSourceDependencies( sourceDependencies );
        plugin.setSourceDependencyDirectory( sourceDependencyDirectory );
        plugin.setSubPackageByFileName( subPackageByFileName );
        plugin.setTestCases( testCases );
        plugin.setTestSourceDirectory( testSourceDirectory );
        plugin.setTimeout( timeout );
        plugin.setTypeMappingVersion( typeMappingVersion );
        plugin.setUrlDownloadDirectory( urlDownloadDirectory );
        plugin.setUrls( urls );
        plugin.setWsdlFiles( wsdlFiles );
        plugin.setUseEmitter( useEmitter );
        plugin.setUsername( username );
        plugin.setVerbose( verbose );
        plugin.setProject( project );
        plugin.setOutputDirectory( outputDirectory );
        plugin.setSourceDirectory( sourceDirectory );
        plugin.setTimestampDirectory( timestampDirectory );
        plugin.setLocalRepository( localRepository );
        plugin.setArtifactFactory( artifactFactory );
        plugin.setPluginArtifacts( pluginArtifacts );
        plugin.setLog( getLog() );

        try
        {
            plugin.execute();
        }
        catch ( AxisPluginException e )
        {
            throw new MojoExecutionException( "Error generating Java code from WSDL.", e );
        }
    }
}
