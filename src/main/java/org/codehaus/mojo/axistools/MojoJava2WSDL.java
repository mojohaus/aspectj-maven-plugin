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


import org.apache.axis.wsdl.Java2WSDL;
import org.apache.maven.plugin.MojoExecutionException;


public class MojoJava2WSDL extends Java2WSDL {

   public void execute(String args[]) throws Exception {
       MojoJava2WSDL j = new MojoJava2WSDL();
       
       int status = j.run(args);
       
       if (status == 1) {
           throw new MojoExecutionException("Java2WSDL had a problem, it returned a failure status");
       }
   }    
}
