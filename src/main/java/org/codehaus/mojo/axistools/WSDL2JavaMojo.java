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
     * list of urls to process
     *
     * @parameter expression=""
     */
    private ArrayList urls;

    /**
     * list of source dependencies in the format groupId:artifactId:version:file
     *
     * @parameter expression=""
     */
    private ArrayList sourceDependencies;

    /**
     * Cache directory for WSDLs from URLs
     *
     * @parameter expression="${project.build.directory}/axistools/wsdl2java/urlDownloads"
     */
    private File urlDownloadDirectory;

    /**
     * Cache directory for WSDLs from sourceDependencies
     *
     * @parameter expression="${project.build.directory}/axistools/wsdl2java/sourceDependencies"
     */
    private File sourceDependencyDirectory;

    /**
     * use the Emitter for generating the java files as opposed to the commandline wsdl2java tool
     *
     * @parameter expression="false"
     */
    private boolean useEmitter;

    /**
     * mappings are only used when useEmitter is set to true
     *
     * @parameter expression=""
     */
    private ArrayList mappings;

    /**
     * @parameter expression="${serverSide}"
     */
    private boolean serverSide;

    /**
     * package to create the java files under
     *
     * @parameter expression="${packageSpace}"
     */
    private String packageSpace;

    /**
     * @parameter expression="${verbose}"
     */
    private boolean verbose;

    /**
     * generate the test cases
     *
     * @parameter expression="${testCases}"
     */
    private boolean testCases;

    /**
     * copy the generated test cases to a generated-sources test directory to be compiled and run as normal surefire unit tests
     *
     * @parameter expression="false"
     */
    private boolean runTestCasesAsUnitTests;

    /**
     * @parameter expression="${allElements}"
     */
    private boolean allElements;

    /**
     * @parameter expression="false"
     */
    private boolean debug;

    /**
     * @parameter expression="${timeout}"
     */
    private Integer timeout;

    /**
     * @parameter expression="false"
     */
    private boolean noImports;

    /**
     * @parameter expression="false"
     */
    private boolean noWrapped;

    /**
     * @parameter expression="false"
     */
    private boolean skeletonDeploy;

    /**
     * @parameter expression="${namespaceToPackage}"
     */
    private String namespaceToPackage;

    /**
     * @parameter expression="${fileNamespaceToPackage}"
     */
    private String fileNamespaceToPackage;

    /**
     * @parameter expression="${deployScope}"
     */
    private String deployScope;

    /**
     * @parameter expression="${typeMappingVersion}"
     */
    private String typeMappingVersion;

    /**
     * @parameter expression="${factory}"
     */
    private String factory;

    /**
     * @parameter expression=""
     */
    private ArrayList nsIncludes;

    /**
     * @parameter expression=""
     */
    private ArrayList nsExcludes;

    /**
     * @parameter expression="false"
     */
    private boolean helperGen;

    /**
     * @parameter expression="${username}"
     */
    private String username;

    /**
     * @parameter expression="${password}"
     */
    private String password;

    /**
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
     * location to place generated test source
     *
     * @parameter expression="${project.build.directory}/generated-test-sources/wsdl"
     */
    private File testSourceDirectory;

    /**
     * source directory that contains .wsdl files
     *
     * @parameter expression="${basedir}/src/main/wsdl"
     */
    private File sourceDirectory;

    /**
     * @parameter expression="${project.build.directory}/generated-sources/axistools/wsdl2java"
     */
    private File outputDirectory;

    /**
     * @parameter expression="${basedir}/target"
     */
    private File timestampDirectory;

    /**
     * The granularity in milliseconds of the last modification
     * date for testing whether a source needs recompilation
     *
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * @parameter expression="${project}"
     * @required
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
        catch ( AxisPluginException e)
        {
            throw new MojoExecutionException("error executing plugin", e);
        }

    }
}
