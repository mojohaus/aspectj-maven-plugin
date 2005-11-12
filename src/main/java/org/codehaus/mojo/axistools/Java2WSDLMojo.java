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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * A Plugin for generating stubs for WSDL files using Axis WSDL2Java.
 * 
 * @goal java2wsdl
 * @phase generate-sources
 * @description Java2WSDL plugin
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id: WSDL2JavaMojo.java 495 2005-09-16 16:02:55Z jesse $
 */
public class Java2WSDLMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${project.build.directory}/generated-sources/axistools/java2wsdl"
     *
     */
    private File outputDirectory;

    /**
     * @parameter expression="${fileName}"
     * @required
     */
    private String filename;
    
    /**
     * @parameter expression="${classOfPortType}"
     */
    private String classOfPortType;
    
    /**
     * @parameter expression="${input}"
     */
    private String input;
    
    /**
     * @parameter expression="${location}"
     */
    private String location;
    
    /**
     * @parameter expression="${portTypeName}"
     */
    private String portTypeName;
    
    /**
     * @parameter expression="${bindingName}"
     */
    private String bindingName;
    
    /** 
     * @parameter expression="${serviceElementName}"
     */
    private String serviceElementName;
    
    /** 
     * @parameter expression="${servicePortName}"
     */
    private String servicePortName;
    
    /**
     * @parameter expression="${namespace}"
     */
    private String namespace;
    
    /**
     * @parameter expression="${PkgToNS}"
     */
    private String PkgToNS;
    
    /**
     * @parameter expression="${methods}"
     */
    private List methods;
    
    /** 
     * @parameter expression="false"
     */
    private boolean all;

    /**
     * @parameter expression="${outputWSDLMode}"
     */
    private String outputWSDLMode;
    
    /**
     * @parameter expression="${locationImport}"
     */
    private String locationImport;
    
    /**
     * @parameter expression="${namespaceImpl}"
     */
    private String namespaceImpl;
    
    /**
     * @parameter expression="${outputImpl}" 
     */
    private String outputImpl;
    
    /**
     * @parameter expression="${implClass}"
     */
    private String implClass;
    
    /**
     * @parameter expression="${exclude}"
     */
    private List excludes;
    
    /**
     * @parameter expression="${stopClasses}"
     */
    private List stopClasses;
    
    /**
     * @parameter expression="${typeMappingVersion}"
     */
    private String typeMappingVersion;
    
    /**
     * @parameter expression="${soapAction}"
     */
    private String soapAction;
    
    /**
     * @parameter expression="${style}"
     */
    private String style;
    
    /**
     * @parameter expression="${use}"
     */
    private String use;
    
    /**
     * @parameter expression="${extraClasses}"
     */
    private List extraClasses;
    
    /**
     * @parameter expression="${importSchema}"
     */
    private String importSchema;
    
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

        try 
        {
            MojoJava2WSDL mojo = new MojoJava2WSDL();
            mojo.execute( generateArgumentList() );
        } 
        catch (Throwable t)
        {
            throw new MojoExecutionException( "Java2WSDL execution failed", t);
        }
        
    }

  
    /**
     * generate the parameter String[] to be passed into the main method 
     * 
     * @param wsdl
     * @return
     */
    private String[] generateArgumentList() throws MojoExecutionException
    {

        ArrayList argsList = new ArrayList();
        argsList.add( "-o" );
        argsList.add( outputDirectory.getAbsolutePath() + File.separator + filename );

        if ( input != null )
        {
            argsList.add( "-I" );
            argsList.add( input );
        }
        
        if ( location != null )
        {
            argsList.add( "-l" );
            argsList.add( location );
        }
        
        if ( portTypeName != null )
        {
            argsList.add( "-P" );
            argsList.add( portTypeName );
        }
        
        if ( bindingName != null )
        {
            argsList.add( "-b" );
            argsList.add( bindingName );
        }
        
        if ( serviceElementName != null )
        {
            argsList.add( "-S" );
            argsList.add( serviceElementName );
        }
        
        if ( servicePortName != null )
        {
            argsList.add( "-s" );
            argsList.add( servicePortName );
        }

        if ( namespace != null )
        {
            argsList.add( "-n" );
            argsList.add( namespace );
        }

        if ( PkgToNS != null )
        {
            argsList.add( "-p" );
            argsList.add( PkgToNS );
        }
        
        if ( methods != null && methods.size() > 0)
        {
            argsList.add( "-m" );
            
            for (Iterator i = methods.iterator(); i.hasNext();)
            {    
                argsList.add( (String)i.next() );
            }
        }
        
        if ( all )
        { 
            argsList.add( "-a" );
        }
        
        if ( outputWSDLMode != null ) 
        {
            if ("All".equalsIgnoreCase(outputWSDLMode) 
                || "Interface".equalsIgnoreCase(outputWSDLMode) 
                || "Implementation".equalsIgnoreCase(outputWSDLMode))
            {
                argsList.add( "-w" );
                argsList.add( outputWSDLMode );
            } 
            else 
            {
                throw new MojoExecutionException("invalid outputWSDLMode setting");
            }
        }
        
        if ( locationImport != null )
        {
            argsList.add( "-L" );
            argsList.add( locationImport );
        }
        
        if ( namespaceImpl != null )
        {
            argsList.add( "-N" );
            argsList.add( namespaceImpl );
        }
        
        if ( outputImpl != null ) 
        {
            argsList.add( "-O" );
            argsList.add( outputImpl );
        }
        
        if ( implClass != null ) 
        {
            argsList.add( "-i" );
            argsList.add( implClass );
        }
        
        if ( excludes != null && excludes.size() > 0) 
        {
            argsList.add( "-x" );
            
            for (Iterator i = excludes.iterator(); i.hasNext();)
            {
                argsList.add( (String)i.next() );
            }
        }
        
        if ( stopClasses != null && stopClasses.size() > 0 ) 
        {
            argsList.add( "-c" );
            
            for (Iterator i = stopClasses.iterator(); i.hasNext();)
            {
                argsList.add( (String)i.next() );
            }
        }
        
        if ( typeMappingVersion != null ) 
        {
            if ("1.1".equals(typeMappingVersion) || "1.2".equals(typeMappingVersion) )
            {
                argsList.add( "-T" );
                argsList.add( typeMappingVersion );
            } 
            else 
            {
                throw new MojoExecutionException("invalid typeMappingVersion (1.1 or 1.2)");
            }
        }
        
        if ( soapAction != null )
        {
            if ( "DEFAULT".equalsIgnoreCase(soapAction) 
                || "OPERATION".equalsIgnoreCase(soapAction) 
                || "NONE".equalsIgnoreCase(soapAction) )
            {    
                argsList.add( "-A" );
                argsList.add( soapAction.toUpperCase() );
            }
        }
        
        if ( style != null )
        {
            if ("RPC".equalsIgnoreCase(style)
                || "DOCUMENT".equalsIgnoreCase(style)
                || "WRAPPED".equalsIgnoreCase(style))
            {
                argsList.add( "-y" );
                argsList.add( style.toUpperCase() );
            }
        }
        
        if ( use != null )
        {
            if ("LITERAL".equalsIgnoreCase(use)
                || "ENCODED".equalsIgnoreCase(use))
            {
                argsList.add( "-u" );
                argsList.add( use.toUpperCase() );
            }
        }
            
        if ( extraClasses != null && extraClasses.size() > 0 ) 
        {
            for (Iterator i = extraClasses.iterator(); i.hasNext();) 
            {
                argsList.add( "-e" );
                argsList.add( (String)i.next() );
            }
        }
        
        if ( importSchema != null )
        {
            argsList.add( "-C" );
            argsList.add( importSchema );   
        }
        
        if ( classOfPortType != null ) 
        {
            if ( portTypeName == null ) 
            {
                argsList.add ( classOfPortType );
            } 
            else
            {
                throw new MojoExecutionException("invalid parameters, can not use portTypeName and classOfPortType together");
            }
        }
        
        getLog().debug( "argslist: " + argsList.toString() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }
}
