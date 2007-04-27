package org.codehaus.mojo.axistools.admin;

import org.codehaus.mojo.axistools.axis.AxisPluginException;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: mlake
 * Date: Jan 23, 2007
 * Time: 11:06:59 AM
 * MusicTodayLLC
 */
public interface AdminPlugin {
    String ROLE = AdminPlugin.class.getName();

    void setServerConfig(boolean isServerConfig);

    void setConfigOutputDirectory( File configOutputDirectory );

    void setInputFiles(ArrayList inputFiles);

    void execute() throws AxisPluginException;
}
