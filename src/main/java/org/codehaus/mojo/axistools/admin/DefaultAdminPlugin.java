package org.codehaus.mojo.axistools.admin;

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

import org.codehaus.mojo.axistools.axis.AbstractAxisPlugin;
import org.codehaus.mojo.axistools.axis.AxisPluginException;

import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: mlake <mlake@netvue.com>
 * Date: Jan 23, 2007
 * Time: 11:03:05 AM
 * MusicTodayLLC
 */
public class DefaultAdminPlugin extends AbstractAxisPlugin implements AdminPlugin {

    private File configOutputDirectory;

    private boolean isServerConfig;

    private ArrayList inputFiles;

    public void setServerConfig(boolean serverConfig) {
        isServerConfig = serverConfig;
    }

    public void setConfigOutputDirectory(File configOutputDirectory) {
        this.configOutputDirectory = configOutputDirectory;
    }

    public void setInputFiles(ArrayList inputFiles){
        this.inputFiles = inputFiles;
    }





    public void execute()
        throws AxisPluginException {
        ArrayList argsList = new ArrayList();

        if ( !configOutputDirectory.exists() )
        {
            configOutputDirectory.mkdirs();
        }
        String mode = "client";
        if (isServerConfig){
             mode ="server";
        }


        // set the mode to server or config
        argsList.add(mode);

        // set the output file
        argsList.add(configOutputDirectory.getAbsolutePath() + File.separator + mode + "-config.wsdd");


        if ( inputFiles != null && inputFiles.size() > 0 )
           {


               for ( Iterator i = inputFiles.iterator(); i.hasNext(); )
               {
                   argsList.add( (String) i.next() );
               }
           } else {
            throw new AxisPluginException("You must specify at least one inputfile in the pom");
        }

        try
        {

            AdminWrapper wrapper = new AdminWrapper();
            wrapper.execute( (String[]) argsList.toArray(new String[]{}));
        }
        catch ( Throwable t )
        {
            throw new AxisPluginException( "Admin execution failed", t );
        }
    }
}
