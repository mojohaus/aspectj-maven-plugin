package org.codehaus.mojo.axistools.java2wsdl;

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

import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.axistools.axis.AbstractAxisPlugin;
import org.codehaus.mojo.axistools.axis.AxisPluginException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * User: jesse
 * Date: Feb 21, 2006
 * Time: 9:52:39 AM
 */
public class DefaultJava2WSDLPlugin
    extends AbstractAxisPlugin
    implements Java2WSDLPlugin
{

    /**
     * the directory the compile objects will be located for java2wsdl to source from
     *
     */
    private File classesDirectory;

    /**
     * @parameter expression="${project.build.directory}/generated-sources/axistools/java2wsdl"
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
     * @parameter expression="${packageToNamespace}"
     */
    private String packageToNamespace;

    /**
     * @parameter expression="${methods}"
     */
    private ArrayList methods;

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
    private ArrayList excludes;

    /**
     * @parameter expression="${stopClasses}"
     */
    private ArrayList stopClasses;

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
    private ArrayList extraClasses;

    /**
     * @parameter expression="${importSchema}"
     */
    private String importSchema;

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;


    /**
     * @component
     */
    private MavenProjectHelper projectHelper;


    public void execute()
        throws AxisPluginException
    {

        if ( !outputDirectory.exists() )
        {
            outputDirectory.mkdirs();
        }
        try
        {
            Java2WSDLWrapper wrapper = new Java2WSDLWrapper();
            wrapper.execute( generateArgumentList() );
        }
        catch ( Throwable t )
        {
            throw new AxisPluginException( "Java2WSDL execution failed", t );
        }

        projectHelper.addResource( project, outputDirectory.getAbsolutePath(), Collections.singletonList( "**/*.wsdl" ),
                                   Collections.EMPTY_LIST );

    }


    /**
     * generate the parameter String[] to be passed into the main method
     *
     * @return argument array for the invocation of {@link Java2WSDLWrapper}
     */
    private String[] generateArgumentList()
        throws AxisPluginException
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

        if ( packageToNamespace != null )
        {
            argsList.add( "-p" );
            argsList.add( packageToNamespace );
        }

        if ( methods != null && methods.size() > 0 )
        {
            argsList.add( "-m" );

            for ( Iterator i = methods.iterator(); i.hasNext(); )
            {
                argsList.add( (String) i.next() );
            }
        }

        if ( all )
        {
            argsList.add( "-a" );
        }

        if ( outputWSDLMode != null )
        {
            if ( "All".equalsIgnoreCase( outputWSDLMode ) || "Interface".equalsIgnoreCase( outputWSDLMode ) ||
                "Implementation".equalsIgnoreCase( outputWSDLMode ) )
            {
                argsList.add( "-w" );
                argsList.add( outputWSDLMode );
            }
            else
            {
                throw new AxisPluginException( "invalid outputWSDLMode setting" );
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

        if ( excludes != null && excludes.size() > 0 )
        {
            argsList.add( "-x" );

            for ( Iterator i = excludes.iterator(); i.hasNext(); )
            {
                argsList.add( (String) i.next() );
            }
        }

        if ( stopClasses != null && stopClasses.size() > 0 )
        {
            argsList.add( "-c" );

            for ( Iterator i = stopClasses.iterator(); i.hasNext(); )
            {
                argsList.add( (String) i.next() );
            }
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

        if ( soapAction != null )
        {
            if ( "DEFAULT".equalsIgnoreCase( soapAction ) || "OPERATION".equalsIgnoreCase( soapAction ) ||
                "NONE".equalsIgnoreCase( soapAction ) )
            {
                argsList.add( "-A" );
                argsList.add( soapAction.toUpperCase() );
            }
        }

        if ( style != null )
        {
            if ( "RPC".equalsIgnoreCase( style ) || "DOCUMENT".equalsIgnoreCase( style ) ||
                "WRAPPED".equalsIgnoreCase( style ) )
            {
                argsList.add( "-y" );
                argsList.add( style.toUpperCase() );
            }
        }

        if ( use != null )
        {
            if ( "LITERAL".equalsIgnoreCase( use ) || "ENCODED".equalsIgnoreCase( use ) )
            {
                argsList.add( "-u" );
                argsList.add( use.toUpperCase() );
            }
        }

        if ( extraClasses != null && extraClasses.size() > 0 )
        {
            for ( Iterator i = extraClasses.iterator(); i.hasNext(); )
            {
                argsList.add( "-e" );
                argsList.add( (String) i.next() );
            }
        }

        if ( importSchema != null )
        {
            argsList.add( "-C" );
            argsList.add( importSchema );
        }

        argsList.add( "--classpath" );
        argsList.add( classesDirectory.getAbsolutePath() );

        if ( classOfPortType != null )
        {
            if ( portTypeName == null )
            {
                argsList.add( classOfPortType );
            }
            else
            {
                throw new AxisPluginException(
                    "invalid parameters, can not use portTypeName and classOfPortType together" );
            }
        }

        getLog().debug( "argslist: " + argsList.toString() );

        return (String[]) argsList.toArray( new String[argsList.size()] );
    }

    public void setClassesDirectory( File classesDirectory )
    {
        this.classesDirectory = classesDirectory;
    }

    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    public void setFilename( String filename )
    {
        this.filename = filename;
    }

    public void setClassOfPortType( String classOfPortType )
    {
        this.classOfPortType = classOfPortType;
    }

    public void setInput( String input )
    {
        this.input = input;
    }

    public void setLocation( String location )
    {
        this.location = location;
    }

    public void setPortTypeName( String portTypeName )
    {
        this.portTypeName = portTypeName;
    }

    public void setBindingName( String bindingName )
    {
        this.bindingName = bindingName;
    }

    public void setServiceElementName( String serviceElementName )
    {
        this.serviceElementName = serviceElementName;
    }

    public void setServicePortName( String servicePortName )
    {
        this.servicePortName = servicePortName;
    }

    public void setNamespace( String namespace )
    {
        this.namespace = namespace;
    }

    public void setPackageToNamespace( String packageToNamespace )
    {
        this.packageToNamespace = packageToNamespace;
    }

    public void setMethods( ArrayList methods )
    {
        this.methods = methods;
    }

    public void setAll( boolean all )
    {
        this.all = all;
    }

    public void setOutputWSDLMode( String outputWSDLMode )
    {
        this.outputWSDLMode = outputWSDLMode;
    }

    public void setLocationImport( String locationImport )
    {
        this.locationImport = locationImport;
    }

    public void setNamespaceImpl( String namespaceImpl )
    {
        this.namespaceImpl = namespaceImpl;
    }

    public void setOutputImpl( String outputImpl )
    {
        this.outputImpl = outputImpl;
    }

    public void setImplClass( String implClass )
    {
        this.implClass = implClass;
    }

    public void setExcludes( ArrayList excludes )
    {
        this.excludes = excludes;
    }

    public void setStopClasses( ArrayList stopClasses )
    {
        this.stopClasses = stopClasses;
    }

    public void setTypeMappingVersion( String typeMappingVersion )
    {
        this.typeMappingVersion = typeMappingVersion;
    }

    public void setSoapAction( String soapAction )
    {
        this.soapAction = soapAction;
    }

    public void setStyle( String style )
    {
        this.style = style;
    }

    public void setUse( String use )
    {
        this.use = use;
    }

    public void setExtraClasses( ArrayList extraClasses )
    {
        this.extraClasses = extraClasses;
    }

    public void setImportSchema( String importSchema )
    {
        this.importSchema = importSchema;
    }

    public void setProject( MavenProject project )
    {
        this.project = project;
    }

    public void setProjectHelper( MavenProjectHelper projectHelper )
    {
        this.projectHelper = projectHelper;
    }

}
