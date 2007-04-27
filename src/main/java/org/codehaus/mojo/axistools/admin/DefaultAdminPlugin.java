package org.codehaus.mojo.axistools.admin;

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
