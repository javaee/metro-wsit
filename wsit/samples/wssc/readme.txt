#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

To Install WSIT and run the sample:

1. Follow the steps in [1] to download, build and install WSIT in Glassfish or Tomcat.

2. Copy the directory /wsit/wsit/samples/ws-trust/certs/xws-security to 
<GLASSFISH_HOME> or <TOMCAT_HOME>.

3. Set up WSIT_HOME system property:

    For Glassfish: 

    # Open the file <GLASSFISH_HOME>/domains/domain1/config/domain.xml in a text editor.
    # Add the following JVM option.
      <jvm-options>-DWSIT_HOME=${com.sun.aas.installRoot}</jvm-options>

    For Tomcat, set CATALINA_OPTS=-DWSIT_HOME=<TOMCAT_HOME>.

4. Edit /wsit/wsit/samples/ws-trust/src/fs/build.properties to 
   tomcat.home or glassfish.home depending on the container to use.

5. Open <TOMCAT_HOME>/conf/server.xml, uncomment the following element with the modification 
   for keystoreFile and trustStoreFile as indicated:

   <Connector port="8443" maxHttpHeaderSize="8192"
               maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
               enableLookups="false" disableUploadTimeout="true"
               acceptCount="100" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS"  keystoreFile="xws-security/etc/server-keystore.jks" keystorePass="changeit" 
               truststoreFile="xws-security/etc/server-truststore.jks" truststorePass="changeit"/>


6. Start tomcat or glassfish.

7. To run the sample, go to 
   /wsit/wsit/samples/ws-trust/src/fs, and run "ant run-sample".

8. You will be prompted to enter the username/password. Two pairs alice/alice, bob/bob
   are preconfigured to use with this sample.
--------------------------------------------------------------------------------------

[1] https://wsit.dev.java.net/source/browse/*checkout*/wsit/wsit/docs/howto/WSIT_Download_Build_Install.html


