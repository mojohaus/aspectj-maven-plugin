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


import org.apache.axis.utils.CLArgsParser;
import org.apache.axis.utils.CLOption;
import org.apache.axis.utils.CLOptionDescriptor;
import org.apache.axis.utils.CLUtil;
import org.apache.axis.utils.DefaultAuthenticator;
import org.apache.axis.utils.Messages;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


import org.apache.axis.wsdl.WSDL2Java;
//import org.apache.axis.wsdl.toJava.JavaGeneratorFactory;

public class MojoWSDL2Java extends WSDL2Java {



   public void execute(String args[]) throws Exception {
      // Parse the arguments
      CLArgsParser argsParser = new CLArgsParser(args, options);

      // Print parser errors, if any
      if (null != argsParser.getErrorString()) {
         System.err.println(
               Messages.getMessage("error01", argsParser.getErrorString()));
         printUsage();
      }

      // Get a list of parsed options
      List clOptions = argsParser.getArguments();
      int size = clOptions.size();

      // Parse the options and configure the emitter as appropriate.
      for (int i = 0; i < size; i++) {
         parseOption((CLOption) clOptions.get(i));
      }

      // validate argument combinations

      validateOptions();
      parser.run(wsdlURI);

      // everything is good
   }    // run
}
