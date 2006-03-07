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

import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.maven.artifact.Artifact;
import org.codehaus.mojo.axistools.axis.AbstractAxisPlugin;
import org.codehaus.mojo.axistools.axis.AxisPluginException;
import org.codehaus.mojo.axistools.axis.Mapping;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;

/**
 * @author: jesse
 * @version: $Id:$
 */
public class DefaultWSDL2JavaPlugin
    extends AbstractAxisPlugin
    implements WSDL2JavaPlugin
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


    public void execute()
        throws AxisPluginException
    {

        if ( !outputDirectory.exists() )
        {
            outputDirectory.mkdirs();
        }

        getLog().info( "about to add compile source root" );

        if ( project != null )
        {
            project.addCompileSourceRoot( outputDirectory.getAbsolutePath() );
        }

        // Get WSDL files

        if ( urls != null )
        {
            for ( Iterator i = urls.iterator(); i.hasNext(); )
            {
                downloadWSDLFromUrl( (String) i.next() );
            }
        }

        if ( sourceDependencies != null )
        {
            for ( Iterator i = sourceDependencies.iterator(); i.hasNext(); )
            {
                extractWSDLFromSourceDependency( (String) i.next() );
            }
        }

        Set wsdlSet = computeStaleWSDLs();

        if ( wsdlSet.isEmpty() )
        {
            getLog().info( "Nothing to generate. All WSDL files are up to date." );
        }
        else
        {
            for ( Iterator i = wsdlSet.iterator(); i.hasNext(); )
            {
                File wsdl = (File) i.next();

                getLog().info( "processing wsdl: " + wsdl.toString() );

                try
                {
                    if ( !useEmitter )
                    {
                        WSDL2JavaWrapper wsdlWrapper = new WSDL2JavaWrapper();
                        wsdlWrapper.execute( generateWSDLArgumentList( wsdl.getAbsolutePath() ) );
                    }
                    else
                    {
                        runEmitter( wsdl );
                    }

                    FileUtils.copyFileToDirectory( wsdl, timestampDirectory );
                }
                catch ( Throwable t )
                {
                    throw new AxisPluginException( "WSDL2Java execution failed", t );
                }
            }
        }

        if ( runTestCasesAsUnitTests )
        {
            migrateTestSource();
        }

    }

    /**
     * Downloads a missing or stale WSDL from the given URL to the directory
     * {@link #urlDownloadDirectory}.
     *
     * @param urlStr the WSDL URL
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          <li>if the syntax of a URL is invalid
     *          <li>if the URL cannot be opened to check the modification
     *          timestamp
     *          <li>if the URL cannot be downloaded
     */
    private void downloadWSDLFromUrl( String urlStr )
        throws AxisPluginException
    {

        URLConnection urlConnection;
        try
        {
            URL url = new URL( urlStr );
            urlConnection = url.openConnection();
        }
        catch ( Exception e )
        {
            throw new AxisPluginException( "unable to open connection for download of WSDL file from URL " + urlStr +
                ". Reason: " + e.getClass().getName() + ": " + e.getMessage(), e );
        }

        File localWsdl = new File( urlDownloadDirectory, createSafeFileName( urlStr ) );
// Compare modification timestamp of the URL against
// that of the local copy.
        if ( localWsdl.exists() && localWsdl.lastModified() == urlConnection.getLastModified() )
        {
            getLog().debug( "local copy of WSDL file from URL " + urlStr + " is up to date." );
            return;
        }

        // The local copy does not exist or it is outdated.
        // Copy the file from the URL to disk

        if ( !urlDownloadDirectory.exists() )
        {
            urlDownloadDirectory.mkdirs();
        }

        InputStream urlInput = null;
        OutputStream localWsdlOutput = null;
        try
        {
            urlInput = urlConnection.getInputStream();
            localWsdlOutput = new FileOutputStream( localWsdl );

            IOUtil.copy( urlInput, localWsdlOutput );
            localWsdlOutput.flush();

            getLog().info( "downloaded WSDL from URL " + urlStr + " (" + localWsdl.length() + " Bytes)." );

        }
        catch ( Exception e )
        {
            throw new AxisPluginException( "unable to download WSDL file from " + urlStr + " to " +
                localWsdl.getAbsolutePath() + ". Reason: " + e.getClass().getName() + ": " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( urlInput );
            IOUtil.close( localWsdlOutput );
        }

        localWsdl.setLastModified( urlConnection.getLastModified() );
    }

    /**
     * Extracts a stale or missing WSDL from the artifact referenced via the
     * given source dependency name to the directory
     * {@link #sourceDependencyDirectory}.
     *
     * @param sourceDependencyString the source dependency (format should be
     *                               <code>groupId:artifactId:version:file</code>)
     * @throws org.apache.maven.plugin.MojoExecutionException
     *          <li>if the sourceDependency format is invalid
     *          <li>if the referenced artifact JAR file cannot be opened
     *          <li>if the referenced WSDL file cannot be found or retrieved
     *          from the referenced artifact
     */
    private void extractWSDLFromSourceDependency( String sourceDependencyString )
        throws AxisPluginException
    {

        StringTokenizer strtok = new StringTokenizer( sourceDependencyString, ":" );

        if ( strtok.countTokens() != 4 )
        {
            throw new AxisPluginException( "invalid sourceDependency: " + sourceDependencyString +
                ". Expected format: groupId:artifactId:version:file" );
        }

        String groupId = strtok.nextToken();
        String artifactId = strtok.nextToken();
        String version = strtok.nextToken();
        String wsdlFileString = strtok.nextToken();

        JarEntry entry;
        JarURLConnection jarConnection;
        try
        {
            Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version, null, "jar" );

            URL url = new URL( "jar:file:" + localRepository.getBasedir() + File.separator +
                localRepository.pathOf( artifact ) + "!" + wsdlFileString );

            jarConnection = (JarURLConnection) url.openConnection();

            entry = jarConnection.getJarEntry();
            if ( entry == null )
            {
                throw new AxisPluginException( "unable to find " + wsdlFileString +
                    " in artifact of sourceDependency " + sourceDependencyString + "." );
            }
        }
        catch ( Exception e )
        {
            throw new AxisPluginException( "unable to open JAR URL connection for extraction of " +
                "WSDL file from artifact of sourceDependency " + sourceDependencyString + ". Reason: " +
                e.getClass().getName() + ": " + e.getMessage(), e );
        }

        File localWsdl = new File( sourceDependencyDirectory, createSafeFileName( sourceDependencyString ) );

// Compare modification timestamp of the jar entry against
// that of the local copy
        if ( localWsdl.exists() && entry.getTime() == localWsdl.lastModified() )
        {
            getLog().debug( "local copy of WSDL file from artifact of sourceDependency " + sourceDependencyString +
                " is up to date." );
            return;
        }

        // The local copy does not exist or it is outdated.
        // Copy the file from the JAR entry to disk.

        if ( !sourceDependencyDirectory.exists() )
        {
            sourceDependencyDirectory.mkdirs();
        }

        InputStream jarWsdlInput = null;
        FileOutputStream localWsdlOutput = null;
        try
        {
            jarWsdlInput = jarConnection.getInputStream();
            localWsdlOutput = new FileOutputStream( localWsdl );

            IOUtil.copy( jarWsdlInput, localWsdlOutput );
            localWsdlOutput.flush();

            getLog().info( "extracted WSDL from sourceDependency " + sourceDependencyString + " (" +
                localWsdl.length() + " Bytes)." );
        }
        catch ( Exception e )
        {
            throw new AxisPluginException( "unable to retrieve " + wsdlFileString +
                " from artifact of sourceDependency " + sourceDependencyString + ".", e );
        }
        finally
        {
            IOUtil.close( jarWsdlInput );
            IOUtil.close( localWsdlOutput );
        }

        localWsdl.setLastModified( entry.getTime() );
    }

    /**
     * move the generated test cases to a suitable location for being picked up by the testing phase
     */
    private void migrateTestSource()
        throws AxisPluginException
    {

        if ( !testSourceDirectory.exists() )
        {
            testSourceDirectory.mkdirs();
        }

        Set testSources = locateTestSources();

        for ( Iterator iter = testSources.iterator(); iter.hasNext(); )
        {
            File source = (File) iter.next();

            try
            {
                FileUtils.copyFileToDirectory( source, testSourceDirectory );
                FileUtils.fileDelete( source.getAbsolutePath() );
            }
            catch ( IOException ioe )
            {
                throw new AxisPluginException( "error copying test sources", ioe );
            }
        }

        project.addTestCompileSourceRoot( testSourceDirectory.getPath() );
    }

    /**
     * generate the parameter String[] to be passed into the main method
     *
     * @param wsdl path of the wsdl file to process
     * @return argument array for the invocation of {@link org.codehaus.mojo.axistools.WSDL2JavaMojo}
     */
    private String[] generateWSDLArgumentList( String wsdl )
        throws AxisPluginException
    {

        ArrayList argsList = new ArrayList();

        if ( debug )
        {
            argsList.add( "--Debug" );
        }

        if ( verbose )
        {
            argsList.add( "-v" );
        }

        argsList.add( "-o" );
        argsList.add( outputDirectory.getAbsolutePath() );

        if ( serverSide )
        {
            argsList.add( "-s" );
        }

        if ( testCases )
        {
            argsList.add( "-t" );
        }

        if ( allElements )
        {
            argsList.add( "-a" );
        }

        if ( noImports )
        {
            argsList.add( "-n" );
        }

        if ( timeout != null )
        {
            argsList.add( "-O" );
            argsList.add( timeout );
        }

        if ( noWrapped )
        {
            argsList.add( "-W" );
        }

        if ( skeletonDeploy )
        {
            argsList.add( "-S" );
        }

        if ( namespaceToPackage != null )
        {
            if ( packageSpace == null )
            {
                argsList.add( "-N" );
                argsList.add( namespaceToPackage );
            }
            else
            {
                throw new AxisPluginException( "NStoPkg and packageSpace can not be used together" );
            }
        }

        if ( fileNamespaceToPackage != null )
        {
            argsList.add( "-f" );
            argsList.add( fileNamespaceToPackage );
        }

        if ( deployScope != null )
        {
            argsList.add( "-d" );
            argsList.add( deployScope );
        }

        if ( typeMappingVersion != null )
        {
            if ( "1.1".equals( typeMappingVersion ) || "1.2".equals( typeMappingVersion ) )
            {
                argsList.add( "-T" );
                argsList.add( typeMappingVersion );
            }
            else
            {
                throw new AxisPluginException( "invalid typeMappingVersion (1.1 or 1.2)" );
            }
        }

        if ( factory != null )
        {
            argsList.add( "-F" );
            argsList.add( factory );
        }

        if ( nsIncludes != null )
        {
            argsList.add( "-i" );
            argsList.add( listToCommaDelimitedString( nsIncludes ) );
        }

        if ( nsExcludes != null )
        {
            argsList.add( "-x" );
            argsList.add( listToCommaDelimitedString( nsExcludes ) );
        }

        if ( helperGen )
        {
            argsList.add( "-H" );
        }

        if ( username != null )
        {
            argsList.add( "-U" );
            argsList.add( username );
        }

        if ( password != null )
        {
            argsList.add( "-P" );
            argsList.add( password );
        }

        if ( implementationClassName != null )
        {
            argsList.add( "c" );
            argsList.add( implementationClassName );
        }

        if ( packageSpace != null && !subPackageByFileName )
        {
            argsList.add( "-p" );
            argsList.add( packageSpace );
        }
        else if ( packageSpace != null && subPackageByFileName )
        {
            argsList.add( "-p" );
            argsList.add( packageSpace + "." + FileUtils.basename( wsdl, ".wsdl" ) );
        }

        argsList.add( wsdl );

        getLog().debug( "argslist: " + argsList.toString() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

    /**
     * scans for the test cases that might have been generated by the call to wsdl2java
     *
     * @return Set of test case File objects
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    private Set locateTestSources()
        throws AxisPluginException
    {
        SuffixMapping mapping = new SuffixMapping( "TestCase.java", "TestCase.class" );

        SourceInclusionScanner scanner = new StaleSourceScanner();

        scanner.addSourceMapping( mapping );

        Set testSources = new HashSet();

        try
        {
            testSources.addAll( scanner.getIncludedSources( outputDirectory, testSourceDirectory ) );
        }
        catch ( InclusionScanException e )
        {
            throw new AxisPluginException(
                "Error scanning source root: \'" + outputDirectory + "\' for stale wsdls to reprocess.", e );
        }

        return testSources;
    }

    private Set computeStaleWSDLs()
        throws AxisPluginException
    {
        SuffixMapping mapping = new SuffixMapping( ".wsdl", ".wsdl" );

        SourceInclusionScanner scanner = new StaleSourceScanner( staleMillis );

        scanner.addSourceMapping( mapping );

        Set staleSources = new HashSet();

        try
        {
            if ( sourceDirectory.exists() )
            {
                staleSources.addAll( scanner.getIncludedSources( sourceDirectory, timestampDirectory ) );
            }

            if ( urlDownloadDirectory.exists() )
            {
                staleSources.addAll( scanner.getIncludedSources( urlDownloadDirectory, timestampDirectory ) );
            }

            if ( sourceDependencyDirectory.exists() )
            {
                staleSources.addAll( scanner.getIncludedSources( sourceDependencyDirectory, timestampDirectory ) );
            }
        }
        catch ( InclusionScanException e )
        {
            throw new AxisPluginException(
                "Error scanning source root: \'" + sourceDirectory + "\' for stale wsdls to reprocess.", e );
        }

        return staleSources;
    }

    private void runEmitter( File wsdl )
        throws AxisPluginException
    {
        Emitter emitter = new Emitter();
        if ( mappings != null )
        {
            getLog().debug( "mappings size : " + mappings.size() );
            HashMap mappingMap = this.getNamespaceMap( mappings );
            getLog().debug( "mappingMap size : " + mappingMap.size() );
            emitter.setNamespaceMap( mappingMap );
        }

        URL wsdlUrl = null;

        try
        {
            wsdlUrl = new URL( "file:///" + wsdl.getAbsolutePath() );
        }
        catch ( MalformedURLException e )
        {
            throw new AxisPluginException( "error processing " + wsdl.getAbsolutePath(), e );
        }

        getLog().debug( "wsdlUrl.toExternalForm() " + wsdlUrl.toExternalForm() );

//emitter.setAllowInvalidURL(mojo.is)
        emitter.setAllWanted( allElements );
// not exists in the mojo
//emitter.setBuildFileWanted(true);
        emitter.setDebug( debug );
// not in the mojo
//emitter.setDefaultTypeMapping(mojo.gett)
//emitter.setDeploy(mojo.is)
        if ( StringUtils.isNotEmpty( factory ) )
        {
            emitter.setFactory( factory );
        }
        emitter.setHelperWanted( helperGen );

        if ( StringUtils.isNotEmpty( implementationClassName ) )
        {
            emitter.setImplementationClassName( implementationClassName );
        }
        // ?? is it correct ?
        emitter.setImports( !noImports );
// TODO:  is it comma separated in the mojo -> no documentation provided
        emitter.setNamespaceExcludes( nsExcludes );
// TODO:  is it comma separated in the mojo -> no documentation provided
        emitter.setNamespaceIncludes( nsIncludes );
        emitter.setNowrap( noWrapped );

        if ( StringUtils.isNotEmpty( namespaceToPackage ) )
        {
            emitter.setNStoPkg( namespaceToPackage );
        }
        emitter.setOutputDir( outputDirectory.getPath() );
// TODO: is it the right mojo parameter certainly yes ;-)
        if ( StringUtils.isNotEmpty( packageSpace ) )
        {
            emitter.setPackageName( packageSpace );
        }
        emitter.setPassword( password );
// not in the mojo properties for custom JavaGeneratorFactories
//emitter.setProperties(mojo.getp)
// not in the mojo but needed ??
//emitter.setQName2ClassMap();
//emitter.setQuiet(mojo.is)
//emitter.setScope(mojo.get)
        emitter.setServerSide( serverSide );
// not in the mojo but needed ??
//emitter.setServiceDesc(mojo.gets)
        emitter.setSkeletonWanted( skeletonDeploy );
        emitter.setTestCaseWanted( testCases );

// not in the mojo but needed ?
//emitter.setTypeCollisionProtection(mojo.is)
        emitter.setTypeMappingVersion( typeMappingVersion );
        emitter.setUsername( username );
        emitter.setVerbose( verbose );
// not in the mojo but needed ?
//emitter.setWrapArrays(mojo.is)
        try
        {
            emitter.run( wsdlUrl.toExternalForm() );
        }
        catch ( Exception e )
        {
            throw new AxisPluginException( "error running " + wsdlUrl.toExternalForm(), e );
        }
    }

    protected HashMap getNamespaceMap( List mappings )
    {
        HashMap namespaceMap = new HashMap( mappings.size() );
        for ( int i = 0, size = mappings.size(); i < size; i++ )
        {
            Mapping mapping = (Mapping) mappings.get( i );
            getLog().debug( "mapping " + mappings.toString() );
            namespaceMap.put( mapping.getNamespace(), mapping.getTargetPackage() );
        }
        return namespaceMap;

    }

    public void setUrls( ArrayList urls )
    {
        this.urls = urls;
    }

    public void setSourceDependencies( ArrayList sourceDependencies )
    {
        this.sourceDependencies = sourceDependencies;
    }

    public void setUrlDownloadDirectory( File urlDownloadDirectory )
    {
        this.urlDownloadDirectory = urlDownloadDirectory;
    }

    public void setSourceDependencyDirectory( File sourceDependencyDirectory )
    {
        this.sourceDependencyDirectory = sourceDependencyDirectory;
    }

    public void setUseEmitter( boolean useEmitter )
    {
        this.useEmitter = useEmitter;
    }

    public void setMappings( ArrayList mappings )
    {
        this.mappings = mappings;
    }

    public void setServerSide( boolean serverSide )
    {
        this.serverSide = serverSide;
    }

    public void setPackageSpace( String packageSpace )
    {
        this.packageSpace = packageSpace;
    }

    public void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
    }

    public void setTestCases( boolean testCases )
    {
        this.testCases = testCases;
    }

    public void setRunTestCasesAsUnitTests( boolean runTestCasesAsUnitTests )
    {
        this.runTestCasesAsUnitTests = runTestCasesAsUnitTests;
    }

    public void setAllElements( boolean allElements )
    {
        this.allElements = allElements;
    }

    public void setDebug( boolean debug )
    {
        this.debug = debug;
    }

    public void setTimeout( Integer timeout )
    {
        this.timeout = timeout;
    }

    public void setNoImports( boolean noImports )
    {
        this.noImports = noImports;
    }

    public void setNoWrapped( boolean noWrapped )
    {
        this.noWrapped = noWrapped;
    }

    public void setSkeletonDeploy( boolean skeletonDeploy )
    {
        this.skeletonDeploy = skeletonDeploy;
    }

    public void setNamespaceToPackage( String namespaceToPackage )
    {
        this.namespaceToPackage = namespaceToPackage;
    }

    public void setFileNamespaceToPackage( String fileNamespaceToPackage )
    {
        this.fileNamespaceToPackage = fileNamespaceToPackage;
    }

    public void setDeployScope( String deployScope )
    {
        this.deployScope = deployScope;
    }

    public void setTypeMappingVersion( String typeMappingVersion )
    {
        this.typeMappingVersion = typeMappingVersion;
    }

    public void setFactory( String factory )
    {
        this.factory = factory;
    }

    public void setNsIncludes( ArrayList nsIncludes )
    {
        this.nsIncludes = nsIncludes;
    }

    public void setNsExcludes( ArrayList nsExcludes )
    {
        this.nsExcludes = nsExcludes;
    }

    public void setHelperGen( boolean helperGen )
    {
        this.helperGen = helperGen;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public void setImplementationClassName( String implementationClassName )
    {
        this.implementationClassName = implementationClassName;
    }

    public void setSubPackageByFileName( boolean subPackageByFileName )
    {
        this.subPackageByFileName = subPackageByFileName;
    }

    public void setTestSourceDirectory( File testSourceDirectory )
    {
        this.testSourceDirectory = testSourceDirectory;
    }


}

