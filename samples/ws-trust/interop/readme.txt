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

			WS-Trust Interop Samples
These samples demonstrate interoparability between WSIT and Microsoft's WCF for ws-trust. 

To Install WSIT and run the samples:

1. Follow the steps in [1] to download, build and install WSIT in Glassfish or Tomcat.

2. Edit <checkout dir>/wsit/wsit/samples/ws-trust/interop/build.properties to set tomcat.home/glassfish.home.

3. Start tomcat or glassfish.

4. There are 4 scenarios under src directory. In each directory, you can choose the client,sts and server 
   to be either sun or ms. There are 3 ways to choose this.
   a. edit build.properties, set client,sts and server properties to either sun or ms and run ant
   b. run targets s-s-s,s-s-m, s-m-s, s-m-m, m-s-s, m-s-m, m-m-s, m-m-m. For eg, run "ant s-m-s" to choose 
	sun client, MS STS and sun server. The first letter denotes client, the second letter denotes  the 
	STS and the third letter denotes the service.
   c. run "ant -Dclient=sun -Dsts=ms -Dserver=ms" where you define the properties on the commandline for 
	client, sts and server. 

5. By default the property sun.host is set to localhost and ms.host is set to microsoft's public endpoint.
   Edit these if necessary.

6. If you are behind a firewall which requires proxy for accessing the end points, edit your <java home>/jre/lib/net.properties
   and set the proxies. Or use the setProxy task of ant in the build.xml

7. For secureconversation-mutual-certificate-11 scenrio, since runtime MEX (Metadata Exchange) is not used 
   in the microsoft wsdl, set the STS manually by uncommenting the approprite STS in etc/client-config/wsit-client.xml .
   For s-m-m, set the microsoft STS, and for s-s-m, set the sun STS

8. For the transport-binding , set the keystore and trust store of server to the keystores used in the samples. For eg, in domain.xml
   the properties should be as follows in glassfish

<jvm-options>-Djavax.net.ssl.keyStore=${com.sun.aas.installRoot}/xws-security/etc/server-keystore.jks</jvm-options>
<jvm-options>-Djavax.net.ssl.trustStore=${com.sun.aas.installRoot}/xws-security/etc/server-truststore.jks</jvm-options>
	For tomcat, the connection attributes should be set as follows. 
<Connector port="8181" ....... 
keystoreFile="xws-security/etc/server-keystore.jks" truststoreFile="xws-security/etc/server-truststore.jks" keyAlias="bob"

Note that the port number is changed to 8181 as by default the URLs work with glassfish default SSL port number of 8181.


-------------------------------------------------------------------------------------------------------
[1] https://wsit.dev.java.net/source/browse/*checkout*/wsit/wsit/docs/howto/WSIT_Download_Build_Install.html
[2] http://java.sun.com/javaee/downloads/index.jsp

