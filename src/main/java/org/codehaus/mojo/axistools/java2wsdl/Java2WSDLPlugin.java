package org.codehaus.mojo.axistools.java2wsdl;

import org.codehaus.mojo.axistools.axis.AxisPluginException;

import java.io.File;
import java.util.ArrayList;

/**
 * Java2WSDLPlugin:
 *
 * @author: jesse
 * @date: Feb 21, 2006
 * @version: $Id:$
 */
public interface Java2WSDLPlugin
{
    String ROLE = Java2WSDLPlugin.class.getName();

    void setClassesDirectory( File classesDirectory );

    void setOutputDirectory( File outputDirectory );

    void setFilename( String filename );

    void setClassOfPortType( String classOfPortType );

    void setInput( String input );

    void setLocation( String location );

    void setPortTypeName( String portTypeName );

    void setBindingName( String bindingName );

    void setServiceElementName( String serviceElementName );

    void setServicePortName( String servicePortName );

    void setNamespace( String namespace );

    void setPackageToNamespace( String packageToNamespace );

    void setMethods( ArrayList methods );

    void setAll( boolean all );

    void setOutputWSDLMode( String outputWSDLMode );

    void setLocationImport( String locationImport );

    void setNamespaceImpl( String namespaceImpl );

    void setOutputImpl( String outputImpl );

    void setImplClass( String implClass );

    void setExcludes( ArrayList excludes );

    void setStopClasses( ArrayList stopClasses );

    void setTypeMappingVersion( String typeMappingVersion );

    void setSoapAction( String soapAction );

    void setStyle( String style );

    void setUse( String use );

    void setExtraClasses( ArrayList extraClasses );

    void setImportSchema( String importSchema );

    void execute() throws AxisPluginException;
}
