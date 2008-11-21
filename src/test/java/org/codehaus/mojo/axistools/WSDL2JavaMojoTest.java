package org.codehaus.mojo.axistools;

/*
 * Copyright 2008 The Codehaus.
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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class WSDL2JavaMojoTest
    extends AbstractMojoTestCase
{

    public void testSpacesInWsdlDir()
        throws Exception
    {
        File testPom = new File( getBasedir(), "target/test-classes/folder with spaces/plugin-config.xml" );

        WSDL2JavaMojo mojo = (WSDL2JavaMojo) lookupMojo( "wsdl2java", testPom );

        assertNotNull( mojo );

        mojo.execute();

        String[] javaSources =
            new String[] { "com/ecerami/www/wsdl/HelloService_wsdl/Hello_BindingStub.java",
                "com/ecerami/www/wsdl/HelloService_wsdl/Hello_PortType.java",
                "com/ecerami/www/wsdl/HelloService_wsdl/Hello_Service.java",
                "com/ecerami/www/wsdl/HelloService_wsdl/Hello_ServiceLocator.java" };
        File dir = new File( "target/test-harness/output" );

        for ( int i = 0; i < javaSources.length; i++ )
        {
            assertTrue( "Java source was not generated " + javaSources[i], new File( dir, javaSources[i] ).exists() );
        }
    }
}
