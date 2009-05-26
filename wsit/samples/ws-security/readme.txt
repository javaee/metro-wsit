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

