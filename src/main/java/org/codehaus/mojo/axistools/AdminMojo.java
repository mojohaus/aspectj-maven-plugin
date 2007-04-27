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
import org.codehaus.mojo.axistools.admin.DefaultAdminPlugin;
import org.codehaus.mojo.axistools.axis.AxisPluginException;

import java.io.File;
import java.util.ArrayList;

/**
 * Utility for turning xml into Axis deployment operations
 * (wraps org.apache.axis.utils.Admin)
 *
 *
 * @author mlake <mlake@netvue.com>
 * @version $Id: AdminMojo.java 2483 2006-10-07 18:18:29Z mlake $
 * @goal admin
 * @phase process-classes
 * @description Axis Admin plugin
 */
public class AdminMojo extends AbstractMojo {

    /**
     * Where the server-config.wsdd or client-config.wsdd should go
     *
     * @parameter expression="${basedir}/src/main/webapp/WEB-INF"
     */
    private File configOutputDirectory;

    /**
     * Generate a server or client deployment file
     * @parameter expression="true"
     * @required
     */

    private boolean isServerConfig;

    /**
     * Files used to create deployment file 
     * @parameter expression="${inputFile}"
     * @required
     */

    private ArrayList inputFiles;

    /**
     * @parameter expression="${project}"
     * @required
     */

    private MavenProject project;


    public void execute()
            throws MojoExecutionException, MojoFailureException {
        DefaultAdminPlugin plugin = new DefaultAdminPlugin();


        plugin.setConfigOutputDirectory(configOutputDirectory);
        plugin.setLog(getLog());
        plugin.setProject(project);
        plugin.setServerConfig(isServerConfig);
        plugin.setInputFiles(inputFiles);
        try {
            plugin.execute();
        }
        catch (AxisPluginException e) {
            throw new MojoExecutionException("error executing plugin", e);
        }

    }


}
