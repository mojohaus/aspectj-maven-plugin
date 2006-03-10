package org.codehaus.mojo.axistools.axis;

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

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Convience baseclass for shared parameters, getters and setters, and methods
 * for the Axis Plugins
 *
 * @author: jesse
 * @version: $Id:$
 */
public abstract class AbstractAxisPlugin
{

    protected File sourceDirectory;

    protected File outputDirectory;

    protected File timestampDirectory;

    protected int staleMillis;

    protected MavenProject project;

    protected ArtifactRepository localRepository;

    protected ArtifactFactory artifactFactory;

    protected List pluginArtifacts;

    protected Log log;

    /**
     * Replaces all characters in the given name except for the '.'. and
     * alphanumeric characters to make it a safe valid file name.
     * <p/>
     * <p/>
     * Possible drawback: This uses JDK 1.4 regular expressions and will not
     * compile with older J2SE versions.
     *
     * @param aName name to make safe
     * @return the safe file name
     */
    protected String createSafeFileName( String aName )
    {
        return aName.replaceAll( "[^\\p{Alnum}\\.]", "-" );
    }

    /**
     * Converts a list to a comma delimited string
     *
     * @param list
     * @return
     */
    protected String listToCommaDelimitedString( List list )
    {
        StringBuffer strbuf = new StringBuffer();

        if ( list != null )
        {
            for ( Iterator i = list.iterator(); i.hasNext(); )
            {
                strbuf.append( (String) i.next() );
                if ( i.hasNext() )
                {
                    strbuf.append( "," );
                }
            }
        }
        return strbuf.toString();
    }

    protected Log getLog()
    {
        return log;
    }

    public void setLog(Log log)
    {
         this.log = log;
    }

    public void setSourceDirectory( File sourceDirectory )
    {
        this.sourceDirectory = sourceDirectory;
    }

    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    public void setTimestampDirectory( File timestampDirectory )
    {
        this.timestampDirectory = timestampDirectory;
    }

    public void setStaleMillis( int staleMillis )
    {
        this.staleMillis = staleMillis;
    }

    public void setProject( MavenProject project )
    {
        this.project = project;
    }

    public void setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;
    }

    public void setArtifactFactory( ArtifactFactory artifactFactory )
    {
        this.artifactFactory = artifactFactory;
    }

    public void setPluginArtifacts( List pluginArtifacts )
    {
        this.pluginArtifacts = pluginArtifacts;
    }

}
