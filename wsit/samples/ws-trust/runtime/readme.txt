
Run the sample:

1. Copy the directory /wsit/wsit/samples/ws-trust/certs/xws-security to 
<GLASSFISH_HOME> or <TOMCAT_HOME>.

2. Edit /wsit/wsit/samples/ws-trust/runtime/src/fs/build.properties to set java.home and
   tomcat.home/glassfish.home.

3. Edit /wsit/wsit/samples/ws-trust/runtime/src/common/KeyStoreCallbackHandler.java,
line 50, to set the keyStoreURL to your actual sts key store location.

Similiarly, Edit /wsit/wsit/samples/ws-trust/runtime/src/common/TrustStoreCallbackHandler.java,
line 38, to set the keyStoreURL to your actual sts trust store location.

4. Edit /wsit/wsit/samples/ws-trust/runtime/common/KeyStoreCallbackHandler.java,
line 50, to set the keyStoreURL to your actual sts key store location.

Similiarly, Edit /wsit/wsit/samples/ws-trust/runtime/src/fs/etc/service/PingService.wsdl,
line 133-134 to replace $WSIT_HOME with your Glassfish/Tomcat location.

5. Start tomcat or glassfish.

6. To run the sample, go to
   /wsit/wsit/samples/ws-trust/validate/src/fs, and run "ant run-sample".

7. You will be prompted to enter the username/password. Two pairs alice/alice, bob/bob
   are pre-configured to use with this sample.
