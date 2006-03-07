package org.codehaus.mojo.axistools.axis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

/**
 * bean for axis mapping in the plugin configuration for emitter use
 *
 * @author <a href="mailto:Olivier.LAMY@accor.com">olamy</a>
 *         created 29 nov. 2005
 */
public class Mapping
{

    Log log = LogFactory.getLog( Mapping.class );

    public Mapping()
    {
        // nothing
        log.debug( "new Mapping" );
    }

    /**
     * the namespace to map
     */
    private String namespace;

    /**
     * the target package for namespace
     */
    private String targetPackage;

    /**
     * @return Mapping Returns the namespace.
     */
    public String getNamespace()
    {
        return this.namespace;
    }

    /**
     * @param namespace The namespace to set.
     */
    public void setNamespace( String namespace )
    {
        this.namespace = namespace;
    }

    /**
     * @return Mapping Returns the targetPackage.
     */
    public String getTargetPackage()
    {
        return this.targetPackage;
    }

    /**
     * @param targetPackage The targetPackage to set.
     */
    public void setTargetPackage( String targetPackage )
    {
        this.targetPackage = targetPackage;
    }


    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return new StringBuffer( "namespace : " ).append( this.namespace )
            .append( ", targetPackage " ).append( this.targetPackage ).toString();
    }


}
