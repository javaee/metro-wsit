Deploy Maven distribution to java.net repository
------------------------------------------------

Make sure that the versions of webservices-api/rt/tools.jar that you want to upload
to the repository are located in <WSIT_HOME>/dist/image/metro/lib. Then execute:

> cd <WSIT_HOME>
> mvn -f dist/maven/pom.xml deploy


Deploy GlassFish V3 distribution to java.net repository
-------------------------------------------------------

Make sure that the versions of webservices-osgi.jar and jaxb-osgi.jar that you want
to upload to the repository are located in <WSIT_HOME>/dist/image/metro/for_v3.
Then execute:

> cd <WSIT_HOME>
> mvn -f dist/maven/pom-v3.xml deploy

Customizing Metro version suffix
--------------------------------

To customize the Metro version suffix that will be pushed to Maven, e.g. for promoted builds,
build Metro with "release.impl.version.suffix" property set, such as:

> ant -Drelease.impl.version.suffix="-b01"

Alternatively, if the Metro build is already finished and you just want to update the POM files,
run only "create-pom-files" ANT target:

> ant -Drelease.impl.version.suffix="-b01" create-pom-files

Customizing JAXB dependency version
-----------------------------------

By default, there is a dependecy to a SNAPSHOT JAXB version. To override this with a different version
(such as a promoted JAXB version), set the "jaxb.osgi.dependency.version" property when building Metro
or creating Metro POM files:

> ant -Djaxb.osgi.dependency.version="2.1.10-b02"
or
> ant -Djaxb.osgi.dependency.version="2.1.10-b02" create-pom-files



Of course, you can customize both Metro and JAXB version at the same time:

> ant -Drelease.impl.version.suffix="-b01" -Djaxb.osgi.dependency.version="2.1.10-b02"
or
> ant -Drelease.impl.version.suffix="-b01" -Djaxb.osgi.dependency.version="2.1.10-b02" create-pom-files
