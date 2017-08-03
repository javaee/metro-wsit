#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
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

2. Copy the directory /wsit/wsit/samples/ws-security/certs/xws-security to 
<GLASSFISH_HOME> or <TOMCAT_HOME>.

3. Set up WSIT_HOME system property:

    For Glassfish: 

    # Open the file <GLASSFISH_HOME>/domains/domain1/config/domain.xml in a text editor.
    # Add the following JVM option.
      <jvm-options>-DWSIT_HOME=${com.sun.aas.installRoot}</jvm-options>

    For Tomcat, set CATALINA_OPTS=-DWSIT_HOME=<TOMCAT_HOME>.

4. There are 4 samples inside  /wsit/wsit/samples/ws-security/src folder :
	i )  mcs : Mutual Certificate Security
	ii)  un_symmetric : Username Token with Symmetric binding
	iii) saml_sv_certificatev : Saml Sender Vouches with Certificate 
	iv) secure_attachments : A sample showing attachments secured through signature/encryption

	Edit build.properties of each sample (which is inside each sample) to set java.home and tomcat.home/glassfish.home.

5. Start tomcat or glassfish.

6. To run the sample, go to 
   /wsit/wsit/samples/ws-security/src/<sample_name>, and run "ant run-sample".

Note : To run un_symmetric sample, you need to create glassfish user with name
("wsit") and password("wsit").

 --------------------------------------------------------------------------------------------

[1] https://wsit.dev.java.net/source/browse/*checkout*/wsit/wsit/docs/howto/WSIT_Download_Build_Install.html
[2] http://java.sun.com/javaee/downloads/index.jsp

