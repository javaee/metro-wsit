
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


