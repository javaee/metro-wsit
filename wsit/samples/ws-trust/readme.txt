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

2. Copy the directory /wsit/wsit/samples/ws-trust/certs/xws-security to 
<GLASSFISH_HOME> or <TOMCAT_HOME>.

3. Set up WSIT_HOME system property:

    For Glassfish: 

    # Open the file <GLASSFISH_HOME>/domains/domain1/config/domain.xml in a text editor.
    # Add the following JVM option.
      <jvm-options>-DWSIT_HOME=${com.sun.aas.installRoot}</jvm-options>

    For Tomcat, set CATALINA_OPTS=-DWSIT_HOME=<TOMCAT_HOME>.

4. Edit /wsit/wsit/samples/ws-trust/src/fs/build.properties to set java.home and 
   tomcat.home/glassfish.home.

5. Start tomcat or glassfish.

6. To run the sample, go to 
   /wsit/wsit/samples/ws-trust/src/fs, and run "ant run-sample".

7. To run the ws-sx version of the sample, go to 
   /wsit/wsit/samples/ws-trust/src/fs-wssx, and run "ant run-sample".

8. You will be prompted to enter the username/password. Two pairs alice/alice, bob/bob
   are preconfigured to use with this sample.
--------------------------------------------------------------------------------------
To run the sample with the sample customer STSAttributeProvider:


1. In etc/targets.xml, replace the existing create-sts-war target element with:

    <target name="create-sts-war">
        <war warfile="${build.war.home}/jaxws-sts.war"
                webxml="../../etc/sts/web.xml">
                <webinf dir="../../etc/sts" includes="sun-jaxws.xml"/>
                <zipfileset
                           dir="etc/sts"
                           includes="sts.wsdl, *.xsd"
                           prefix="WEB-INF/wsdl"/>
                <zipfileset
                           dir="../../etc/sts"
                           includes="wsit-server.xml"
                           prefix="WEB-INF/classes"/>
                <zipfileset
                            dir="../../etc/sts/services"
                            includes="com.sun.*"
                            prefix="WEB-INF/classes/META-INF/services"/>
                <classes dir="${build.classes.home}" includes="**/sts/**,
                        **/com/**, **/common/**"/>
        </war>
   </target>

    This new target element places the additional jars and configuration files into
    the jaxws-sts war. This customer STSAttributeProvider allows to plug-in user 
    identities/attributes mappings to the STS so that the issued SAMl assertions contain
    the required user information for the service.
-------------------------------------------------------------------------------------
To run the sample with AccessManager:

This works with Sun Java System Access Manager 7.1 Beta which is available as part of 
java_app_platform_sdk-5_01 release. This sample use AM for authentication and control 
of issuing tokens with a customer STSAuthorizationProvider. One may also develop a 
STSAttributeProvider on top of AM for user identity mappings.

To set up and run the sample with AccessManager:

1. Download and install Java Application Platform SDK at [2] . 
   Get amclientsdk.jar and amserver.war from SDK\addons\accessmanager.

2. Deploy amserver.war into your choice of container. 

3. Open http://localhost:8080/amserver/configurator.jsp in a browser.

4. Post request parameters required. Click Configure button.

5. Login in with the User Name = amAdmin and the password set in the previous step.

6. Create a test user:
    6.1 Click the entry in the realm list
    6.2 Click 'Subjects'
    6.3 Click 'New'
    6.4 ID: Alice
    6.5 First Name: Alice
    6.6 Last Name: Test
    6.7 Full Name: Alice Test
    6.8 Password: alice
    6.9 Click 'OK'

7. Repeat step 6 to create another test user with ID = Bob.

8. Create a policy:
   8.1 Click 'Policies'
   8.2 Click 'New Policy'
   8.3 Name: Access Simple Service
   8.4 Under 'Rules', click 'New'
   8.5 Leave the default URL Policy Agent selected, click 'Next'
   8.6 Name: Simple Service
       Resource Name: http://localhost:8080/jaxws-fs/simple (this MUST match
       exactly what the client asks for. You can use wildcards if you like, though, so
       it could be http://*:*/jaxws-fs/simple)
   8.7 Select 'POST', click 'Finish'
   8.8 Under 'Subjects', click 'New'
   8.9 Select 'Access Manager Identity Subject' and click Next.
   8.10 Filter: User, click 'Search'
   8.11 Select alice, click 'Add', put Name: alice, click 'Finish'
   8.12 Click 'OK'.

9. Stop the container and Install WSIT to it according to the above steps 1 to 4
   for "To Install WSIT and Run the Sample".

10. Copy the amclientsdk.jar into the /wsit/wsit/samples/ws-trust/am directory.

11. Edit /wsit/wsit/samples/ws-trust/am/AMConfig.properties. Set the following properties to suit your
    deployment:

    com.iplanet.am.naming.url
    com.iplanet.am.server.host
    com.iplanet.am.server.port
    com.iplanet.services.debug.directory
    com.iplanet.services.configpath
    common.org

    where 
    com.iplanet.services.configpath points to the Configuration Directory set up in step 4
    common.org points to the Realm Name (sample).
    

From this point on, all paths arerelative to the /wsit/wsit/samples/ws-trust directory.

12. Edit etc/targets.xml. Add the following lines after the initial list of
    properties:

    <property name="build.lib.home" value="${build.home}/lib"/>
    <target name="copy-am-files">   
        <echo message="Copying AM Files"/>
        <copy file="${basedir}/../../am/AMConfig.properties"
                tofile="${build.classes.home}/AMConfig.properties" />
        <mkdir dir="${build.lib.home}"/>
        <copy file="${basedir}/../../am/amclientsdk.jar"
            tofile="${build.lib.home}/amclientsdk.jar" />
        <copy file="${basedir}/../../am/jaas.jar"
            tofile="${build.lib.home}/jaas.jar" />
        <copy file="${basedir}/../../am/jdk_logging.jar"
            tofile="${build.lib.home}/jdk_logging.jar" />
    </target>

    and replace the existing create-sts-war target element with:

    <target name="create-sts-war">
        <antcall target="copy-am-files" />
        <war warfile="${build.war.home}/jaxws-sts.war"
                webxml="../../etc/sts/web.xml">
                <webinf dir="../../etc/sts" includes="sun-jaxws.xml"/>
                <zipfileset
                           dir="etc/sts"
                           includes="sts.wsdl, *.xsd"
                           prefix="WEB-INF/wsdl"/>
                <zipfileset
                           dir="../../etc/sts"
                           includes="wsit-server.xml"
                           prefix="WEB-INF/classes"/>
                <zipfileset
                            dir="../../etc/sts/services"
                            includes="com.sun.xml.ws.api.security.trust.STSAuthorizationProvider"
                            prefix="WEB-INF/classes/META-INF/services"/>
                <classes dir="${build.classes.home}" includes="**/sts/**,
                        **/com/**, **/common/**, AMConfig.properties"/>
                <lib dir="${build.lib.home}" />
        </war>
   </target>

    This new target element places the additional jars and configuration files into
    the jaxws-sts war.

13. Replace the following in etc/targets.xml:

    <target name="compile-callbacks" depends="setup-tc, setup-glassfish" >
        <javac
            fork="true"
            debug="${debug}"
            srcdir="${basedir}/.."
            destdir="${build.classes.home}" 
            includes="common/SampleUsernamePasswordCallbackHandler.java, common/SampleUsernamePasswordValidator.java">
            <classpath refid="jaxws.classpath"/>
        </javac>
    </target>

    by

    <target name="compile-callbacks" depends="setup-tc, setup-glassfish" >
        <javac
            fork="true"
            debug="${debug}"
            srcdir="${basedir}/.."
            destdir="${build.classes.home}" 
            includes="common/SampleUsernamePasswordCallbackHandler.java, common/SampleAMSTSAuthorizationProvider.java, common/SampleAMUsernamePasswordValidator.java">
            <classpath refid="jaxws.classpath"/>
        </javac>
    </target>

14.  Edit /wsit/wsit/samples/ws-trust/src/fs/etc/sts/sts.wsdl:
     replace 
       <sc:Validator name="usernameValidator"  classname="common.SampleUsernamePasswordValidator"/>  
     with
       <sc:Validator name="usernameValidator"  classname="common.SampleAMUsernamePasswordValidator"/>

15. Start the container.

16. Now build and run the sample with "ant run-sample" in src/fs directory. You will be prompted to enter 
    the username/password. 
    Enter Alice/Alice, you should get the balance back from the service.
    Enter Bob/Bob, you should be authenticated in the STS and denied to be issued a SAML token. 
    For the other username/password pairs, you should not be authenticated.

Note: The AM username/password validator and contract put their debug files in the specified 
      directory. 
 --------------------------------------------------------------------------------------------

[1] https://wsit.dev.java.net/source/browse/*checkout*/wsit/wsit/docs/howto/WSIT_Download_Build_Install.html
[2] http://java.sun.com/javaee/downloads/index.jsp

