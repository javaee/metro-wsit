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

Customizing Metro version and dependency versions
-------------------------------------------------

There are several properties that control the Metro version as well as other
dependency versions as they are generated in the Maven's pom.xml descriptors
during the Metro build phase. These properties can be customized and passed to
the main Metro build phase to generate custom pom.xml descriptors as required.

Alternatively, once Metro build is already finished and only the pom.xml
descriptors need to be updated "create-pom-files" ANT target can be used
to generate new pom.xml descriptors for the Metro artifacts:

Following is the list of supported properties:

release.impl.version
  - specifies the main Metro version (e.g. "2.1")

release.mvn.version.suffix
  - specifies the main Metro version suffix; for FCS release, this property must
    be set to empty string (""), for non-FCS release the qualifier must start with
    a dash ('-') (e.g. "-SNAPSHOT", "-b03")

jaxb.osgi.dependency.version
  - specifies JAXB OSGi bundle dependency version (e.g. "2.2.1.1-promoted-b2")

woodstox.osgi.dependency.version
  - specifies Woodstox OSGi bundle dependency version (e.g. "3.2.1.1-SNAPSHOT")

woodstox.osgi.release.version
  - specifies Woodstox OSGi bundle release version (e.g. "3.2.1.1-SNAPSHOT").
    WARNING: This property controls the actual version of the Woodstox bundle
    produced by Metro build. It should not be used for customization without
    a very good knowledge of th impact. Also, the property is a temporary hack
    and it will be removed soon completely.


Example: Customizing the properties in the whole Metro build
============================================================

To customize the Metro version suffix that will be pushed to Maven, e.g. for
promoted builds, build Metro with "release.mvn.version.suffix" property set,
such as:

> ant -Drelease.mvn.version.suffix="-b01" clean dist

Example: Generating new pom.xml descriptors for an already existing build
=========================================================================

To customize the Metro version suffix that will be pushed to Maven as well as
Woodstox OSGi dependency version for an already existing build of Metro, use:

> ant -Drelease.mvn.version.suffix="-b01" -Dwoodstox.osgi.dependency.version="4.0.8" create-pom-files

To customize the JAXB OSGi dependency version suffix for an already existing
build of Metro, use:

> ant -Djaxb.osgi.dependency.version="2.1.10-b02" create-pom-files

