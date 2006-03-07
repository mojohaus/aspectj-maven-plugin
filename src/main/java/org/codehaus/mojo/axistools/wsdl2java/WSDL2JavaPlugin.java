package org.codehaus.mojo.axistools.wsdl2java;

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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.axistools.axis.AxisPluginException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: jesse
 * @version: $Id:$
 */
public interface WSDL2JavaPlugin
{
    String ROLE = WSDL2JavaPlugin.class.getName();

    void setUrls( ArrayList urls );

    void setSourceDependencies( ArrayList sourceDependencies );

    void setUrlDownloadDirectory( File urlDownloadDirectory );

    void setSourceDependencyDirectory( File sourceDependencyDirectory );

    void setUseEmitter( boolean useEmitter );

    void setMappings( ArrayList mappings );

    void setServerSide( boolean serverSide );

    void setPackageSpace( String packageSpace );

    void setVerbose( boolean verbose );

    void setTestCases( boolean testCases );

    void setRunTestCasesAsUnitTests( boolean runTestCasesAsUnitTests );

    void setAllElements( boolean allElements );

    void setDebug( boolean debug );

    void setTimeout( Integer timeout );

    void setNoImports( boolean noImports );

    void setNoWrapped( boolean noWrapped );

    void setSkeletonDeploy( boolean skeletonDeploy );

    void setNamespaceToPackage( String namespaceToPackage );

    void setFileNamespaceToPackage( String fileNamespaceToPackage );

    void setDeployScope( String deployScope );

    void setTypeMappingVersion( String typeMappingVersion );

    void setFactory( String factory );

    void setNsIncludes( ArrayList nsIncludes );

    void setNsExcludes( ArrayList nsExcludes );

    void setHelperGen( boolean helperGen );

    void setUsername( String username );

    void setPassword( String password );

    void setImplementationClassName( String implementationClassName );

    void setSubPackageByFileName( boolean subPackageByFileName );

    void setTestSourceDirectory( File testSourceDirectory );

     public void setPluginArtifacts( List pluginArtifacts );


    public void setSourceDirectory( File sourceDirectory );


    public void setOutputDirectory( File outputDirectory );


    public void setTimestampDirectory( File timestampDirectory );


    public void setStaleMillis( int staleMillis );


    public void setProject( MavenProject project );

    public void setLog( Log log );

    public void setLocalRepository( ArtifactRepository localRepository );

    public void setArtifactFactory( ArtifactFactory artifactFactory );

    void execute() throws AxisPluginException;
}
