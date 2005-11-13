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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
import org.codehaus.plexus.util.FileUtils;

/**
 * A Plugin for generating stubs for WSDL files using Axis WSDL2Java.
 * 
 * @goal wsdl2java
 * @phase generate-sources
 * @description WSDL2Java plugin
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id: WSDL2JavaMojo.java 495 2005-09-16 16:02:55Z jesse $
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
     * source directory that contains .wsdl files
     * 
     * @parameter expression="${basedir}/src/main/wsdl"
     */
    private File sourceDirectory;

    /**
     * @parameter expression="${project.build.directory}/generated-sources/axistools/wsdl2java"
     *
     */
    private File outputDirectory;

    /**
     * @parameter expression="${basedir}/target"
     *
     */
    private String timestampDirectory;

    /**
     * @parameter expression="${serverSide}"
     *
     */
    private boolean serverSide;

    /**
     * package to create the java files under
     * 
     * @parameter expression="${packageSpace}"
     * 
     */
    private String packageSpace;

    /**
     * @parameter expression="${verbose}"
     * 
     */
    private boolean verbose;

    /**
     * @parameter expression="${testCases}"
     * 
     */
    private boolean testCases;
    
    /**
     * @parameter expression="false"
     */
    private boolean runTestCasesAsUnitTests;

    /**
     * @parameter expression="${allElements}"
     * 
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
     * @parameter expression="${nsInclude}"
     */
    private String nsInclude;
    
    /**
     * @parameter expression="{$nsExclude}"
     */
    private String nsExclude;
    
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
     * @parameter expression="$subPackageByFileName"
     */
    private boolean subPackageByFileName;

    /**
     * location to place generated test source
     *
     * @parameter expression="${project.build.directory}/generated-test-sources/wsdl"
     */
    private File testSourceDirectory;

    /**
     * The granularity in milliseconds of the last modification
     *  date for testing whether a source needs recompilation
     *
     * @parameter expression="${lastModGranularityMs}" default-value="0"
     */
    private int staleMillis;

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    public void execute()
        throws MojoExecutionException
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

        // process urls if they are present, by their nature they 
        // will be regenerated every time.
        if ( urls != null )
        {

            for ( Iterator i = urls.iterator(); i.hasNext(); )
            {
                String url = (String) i.next();
                getLog().info( "processing wsdl location: " + url );

                try
                {
                    MojoWSDL2Java wsdlMojo = new MojoWSDL2Java();
                    wsdlMojo.execute( generateWSDLArgumentList( url ) );
                }
                catch ( Throwable t )
                {
                    throw new MojoExecutionException( "WSDL2Java execution failed", t );
                }
            }
        }
        else
        {

            Set wsdlSet = computeStaleWSDLs();

            for ( Iterator i = wsdlSet.iterator(); i.hasNext(); )
            {

                File wsdl = (File) i.next();

                getLog().info( "processing wsdl: " + wsdl.toString() );

                try
                {
                    MojoWSDL2Java wsdlMojo = new MojoWSDL2Java();
                    wsdlMojo.execute( generateWSDLArgumentList( wsdl.getAbsolutePath() ) );

                    FileUtils.copyFileToDirectory( wsdl, new File( timestampDirectory ) );
                }
                catch ( Throwable t )
                {
                    throw new MojoExecutionException( "WSDL2Java execution failed", t );
                }

            }
        }

        if (runTestCasesAsUnitTests) {
            migrateTestSource();
        }
    }

    /**
     * move the generated test cases to a suitable location for being picked up by the testing phase
     * 
     */
    private void migrateTestSource()
        throws MojoExecutionException
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
                throw new MojoExecutionException( "error copying test sources", ioe );
            }
        }

        project.addTestCompileSourceRoot( testSourceDirectory.getPath() );
    }

    /**
     * generate the parameter String[] to be passed into the main method 
     * 
     * @param wsdl
     * @return
     */
    private String[] generateWSDLArgumentList( String wsdl ) throws MojoExecutionException
    {

        ArrayList argsList = new ArrayList();
        argsList.add( "-o" );
        argsList.add( outputDirectory.getAbsolutePath() );

        if ( serverSide )
        {
            argsList.add( "-s" );
        }

        if ( verbose )
        {
            argsList.add( "-v" );
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
        
        if ( debug ) 
        {
            argsList.add( "-D" );
        }
        
        if ( noWrapped ) 
        {
            argsList.add( "-W" );
        }
        
        if ( skeletonDeploy  )
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
                throw new MojoExecutionException("NStoPkg and packageSpace can not be used together");
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
            if ("1.1".equals(typeMappingVersion) || "1.2".equals(typeMappingVersion) )
            {
                argsList.add( "-T" );
                argsList.add( typeMappingVersion );
            } else {
                throw new MojoExecutionException("invalid typeMappingVersion (1.1 or 1.2)");
            }
        }
        
        if ( factory != null ) 
        {
            argsList.add( "-F" );
            argsList.add( factory );
        }
        
        if ( nsInclude != null ) 
        {
            argsList.add( "-i" );
            argsList.add( nsInclude );
        }
        
        if ( nsExclude != null ) 
        {
            argsList.add( "-x" );
            argsList.add( nsExclude );
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
     * @throws MojoExecutionException
     */
    private Set locateTestSources()
        throws MojoExecutionException
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
            throw new MojoExecutionException( "Error scanning source root: \'" + outputDirectory
                + "\' for stale wsdls to reprocess.", e );
        }

        return testSources;
    }

    private Set computeStaleWSDLs()
        throws MojoExecutionException
    {
        SuffixMapping mapping = new SuffixMapping( ".wsdl", ".wsdl" );

        SourceInclusionScanner scanner = new StaleSourceScanner( staleMillis );

        scanner.addSourceMapping( mapping );

        File outDir = new File( timestampDirectory );

        Set staleSources = new HashSet();

        try
        {
            staleSources.addAll( scanner.getIncludedSources( sourceDirectory, outDir ) );
        }
        catch ( InclusionScanException e )
        {
            throw new MojoExecutionException( "Error scanning source root: \'" + sourceDirectory
                + "\' for stale wsdls to reprocess.", e );
        }

        return staleSources;
    }

}
