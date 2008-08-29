Deploy Maven distribution to java.net repository
------------------------------------------------

Make sure that the versions of webservices-api/rt/tools.jar that you want to upload
to the repository are located in <WSIT_HOME>/dist/image/metro/lib. Then execute:

> cd <WSIT_HOME>
> mvn -f etc/maven/pom.xml deploy


Deploy GlassFish V3 distribution to java.net repository
-------------------------------------------------------

Make sure that the versions of webservices.jar and jaxb.jar that you want to upload
to the repository are located in <WSIT_HOME>/dist/image/metro/for_v3. Then execute:

> cd <WSIT_HOME>
> mvn -f etc/maven/pom-v3-jaxb.xml deploy
> mvn -f etc/maven/pom-v3.xml deploy
