
To Install WSIT and run the sample:

1. Follow the steps in 
https://wsit.dev.java.net/source/browse/*checkout*/wsit/wsit/docs/howto/WSIT_Download_Build_Install.html

to download, build and install WSIT in Glassfish or Tomcat.

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

7. You will be prompted to enter the username/password. Two pairs alice/alice, bob/bob
are preconfigured to use with this sample.

------------------------------------------------------------------------------

To set up and run the sample with AccessManager:


