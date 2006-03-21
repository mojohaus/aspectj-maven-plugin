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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.mojo.axistools.axis.AxisPluginException;
import org.codehaus.mojo.axistools.java2wsdl.DefaultJava2WSDLPlugin;

import java.io.File;
import java.util.ArrayList;

/**
 * A Plugin for generating stubs for WSDL files using Axis WSDL2Java.
 *
 * @author jesse <jesse.mcconnell@gmail.com>
 * @version $Id$
 * @goal java2wsdl
 * @phase process-classes
 * @description Java2WSDL plugin
 */
public class Java2WSDLMojo
    extends AbstractMojo
{


    /**
     * the directory the compile objects will be located for java2wsdl to source from
     *
     * @parameter expression="${project.build.directory}/classes
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
        throws MojoExecutionException, MojoFailureException
    {
        DefaultJava2WSDLPlugin plugin = new DefaultJava2WSDLPlugin();


        plugin.setAll( all );
        plugin.setBindingName( bindingName );
        plugin.setClassesDirectory( classesDirectory );
        plugin.setClassOfPortType( classOfPortType );
        plugin.setExcludes( excludes );
        plugin.setExtraClasses( extraClasses );
        plugin.setFilename( filename );
        plugin.setImplClass( implClass );
        plugin.setImportSchema( importSchema );
        plugin.setInput( input );
        plugin.setLocation( location );
        plugin.setLocationImport( locationImport );
        plugin.setMethods( methods );
        plugin.setNamespace( namespace );
        plugin.setNamespaceImpl( namespaceImpl );
        plugin.setOutputDirectory( outputDirectory );
        plugin.setOutputImpl( outputImpl );
        plugin.setOutputWSDLMode( outputWSDLMode );
        plugin.setPackageToNamespace( packageToNamespace );
        plugin.setPortTypeName( portTypeName );
        plugin.setServiceElementName( serviceElementName );
        plugin.setServicePortName( servicePortName );
        plugin.setSoapAction( soapAction );
        plugin.setStopClasses( stopClasses );
        plugin.setStyle( style );
        plugin.setTypeMappingVersion( typeMappingVersion );
        plugin.setUse( use );
        plugin.setLog (getLog ());
        plugin.setProjectHelper (projectHelper);
        plugin.setProject (project);

        try {
            plugin.execute();
        }
        catch ( AxisPluginException e)
        {
            throw new MojoExecutionException("error executing plugin", e);
        }

    }

}
