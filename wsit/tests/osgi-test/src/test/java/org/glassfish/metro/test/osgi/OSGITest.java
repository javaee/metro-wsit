/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.metro.test.osgi;

import com.sun.tools.ws.resources.WscompileMessages;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Set of simple tests which should guarantee that Metro imported/exported
 * packages can be properly loaded by GlassFish (or basically any OSGi framework).
 *
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGITest {

    @Inject
    private static BundleContext ctx;
    private static final String JAXB_SPEC_VERSION = System.getProperty("jaxb.spec.version");
    private static final String JAXB_IMPL_VERSION = System.getProperty("jaxb.impl.version");
    private static final String METRO_VERSION = System.getProperty("metro.version");
    private static final String HK2_VERSION = System.getProperty("hk2.version", "2.2.0-b20");
    private static final String GF_VERSION = System.getProperty("glassfish.version");
    private static final String PFL_VERSION = System.getProperty("pfl.version", "4.0.0-b003");


    @Configuration
    public static Option[] config() {
        return options(
                localRepository(System.getProperty("mvn.repo")),
                repositories("http://repo1.maven.org/maven2",
                             "https://maven.java.net/content/repositories/promoted/",
                             "http://maven.java.net/content/repositories/snapshots/"),
                mavenBundle().groupId("org.osgi").artifactId("org.osgi.compendium").version("4.3.0"),

                systemPackage("com.sun.nio"),
                systemPackage("sun.rmi.rmic"),
                systemPackage("sun.misc"),
                systemPackage("com.ibm.security.util"),
                systemPackage("com.ibm.security.x509"),
                systemPackage("com.sun.net.httpserver"),
                systemPackage("com.sun.tools.javac"),
                systemPackage("sun.tools.javac"),
                systemPackage("sun.corba"), // pfl

                // APIs
                mavenBundle().groupId("javax.annotation").artifactId("javax.annotation-api").version("1.2"),
                mavenBundle().groupId("javax.enterprise.deploy").artifactId("javax.enterprise.deploy-api").version("1.6"),
                mavenBundle().groupId("javax.interceptor").artifactId("javax.interceptor-api").version("1.2"),
                mavenBundle().groupId("javax.mail").artifactId("javax.mail-api").version("1.5.0"),
                mavenBundle().groupId("javax.resource").artifactId("javax.resource-api").version("1.7"),
                mavenBundle().groupId("javax.servlet").artifactId("javax.servlet-api").version("3.1.0"),
                mavenBundle().groupId("javax.transaction").artifactId("javax.transaction-api").version("1.2"),
                mavenBundle().groupId("javax.xml.rpc").artifactId("javax.xml.rpc-api").version("1.1.1"),
                mavenBundle().groupId("javax.xml.registry").artifactId("javax.xml.registry-api").version("1.0.5"),

                // GlassFish
                mavenBundle().groupId("org.glassfish.hk2").artifactId("class-model").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("config-types").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("core").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-api").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-config").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-locator").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-runlevel").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("hk2-utils").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2").artifactId("osgi-resource-locator").version("1.0.1"),
                mavenBundle().groupId("org.glassfish.hk2.external").artifactId("asm-all-repackaged").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2.external").artifactId("bean-validator").version(HK2_VERSION),
                mavenBundle().groupId("org.glassfish.hk2.external").artifactId("cglib").version(HK2_VERSION),

                mavenBundle().groupId("org.glassfish.external").artifactId("management-api").version("3.2.0-b001"),
                mavenBundle().groupId("org.glassfish.gmbal").artifactId("gmbal").version("4.0.0-b001"),
                mavenBundle().groupId("org.glassfish.ha").artifactId("ha-api").version("3.1.9"),

                mavenBundle().groupId("org.glassfish.main.admin").artifactId("config-api").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.common").artifactId("annotation-framework").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.common").artifactId("common-util").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.common").artifactId("glassfish-api").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.common").artifactId("internal-api").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.common").artifactId("simple-glassfish-api").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.deployment").artifactId("deployment-common").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.deployment").artifactId("dol").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.external").artifactId("ant").version(GF_VERSION),
                mavenBundle().groupId("org.glassfish.main.grizzly").artifactId("nucleus-grizzly-all").version(GF_VERSION),

                mavenBundle().groupId("org.glassfish.pfl").artifactId("pfl-asm").version(PFL_VERSION),
                mavenBundle().groupId("org.glassfish.pfl").artifactId("pfl-basic").version(PFL_VERSION),
                mavenBundle().groupId("org.glassfish.pfl").artifactId("pfl-dynamic").version(PFL_VERSION),
                mavenBundle().groupId("org.glassfish.pfl").artifactId("pfl-tf").version(PFL_VERSION),

                mavenBundle().groupId("org.eclipse.persistence").artifactId("javax.persistence").version("2.1.0"),
                mavenBundle().groupId("org.jboss.weld").artifactId("weld-osgi-bundle").version("2.0.3.Final"),

                mavenBundle().groupId("com.sun.mail").artifactId("javax.mail").version("1.5.0"),

                //Metro APIs
                mavenBundle("javax.xml.bind", "jaxb-api", JAXB_SPEC_VERSION).startLevel(1),
                mavenBundle("org.glassfish.metro", "webservices-api-osgi", METRO_VERSION).startLevel(1),

                //Metro OSGi bundles
                mavenBundle("com.sun.xml.bind", "jaxb-osgi", JAXB_IMPL_VERSION),
                mavenBundle("com.sun.xml.bind", "jaxb-extra-osgi", JAXB_IMPL_VERSION),
                mavenBundle("org.glassfish.metro", "webservices-osgi", METRO_VERSION),
                mavenBundle("org.glassfish.metro", "webservices-extra-jdk-packages", METRO_VERSION),
                junitBundles(),
                felix());
    }

    @Test
    public void testJaxbAPI() {
        Class<?> c = loadClass("javax.xml.bind.JAXBContext");
        assertClassLoadedByBundle(c, "jaxb-api");
    }

    @Test
    public void testWsApiOSGi() {
        Class<?> c = loadClass("javax.xml.ws.Service");
        assertClassLoadedByBundle(c, "org.glassfish.metro.webservices-api-osgi");
    }

    @Test
    public void testWsOSGi() {
        Class<?> c = loadClass("com.sun.xml.ws.api.WSService");
        assertClassLoadedByBundle(c, "org.glassfish.metro.webservices-osgi");
    }

    @Test
    public void testLocalization() {
        Assert.assertEquals("Missing WSDL_URI", WscompileMessages.WSIMPORT_MISSING_FILE());
    }

    @Test
    public void testXmlResolver() {
        Class<?> c = loadClass("com.sun.org.apache.xml.internal.resolver.CatalogManager");
        //class is there but loaded from the system bundle
        Assert.assertNull("xmlresolver not loaded from JDK", FrameworkUtil.getBundle(c));
    }

    @Test
    public void testContextFactory101() {
        Class<?> c = loadClass("com.sun.xml.bind.ContextFactory_1_0_1");
        assertClassLoadedByBundle(c, "com.sun.xml.bind.jaxb-osgi");
    }

    @Test
    public void testLogger() {
        Class<?> c = loadClass("com.sun.istack.logging.Logger");
        assertClassLoadedByBundle(c, "com.sun.xml.bind.jaxb-osgi");
    }

    @Test
    public void testMsvDatatypes() {
        Class<?> c = loadClass("com.sun.msv.datatype.xsd.IntegerType");
        assertClassLoadedByBundle(c, "com.sun.xml.bind.jaxb-extra-osgi");
    }

    private Class<?> loadClass(String className) {
        try {
            return ctx.getBundle().loadClass(className);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(OSGITest.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            Assert.fail("Cannot find and load class: " + className);
        }
        return null;
    }

    private void assertClassLoadedByBundle(Class<?> c, String bundle) {
        Bundle b = FrameworkUtil.getBundle(c);
        Assert.assertEquals("Class '" + c.getName() + "' was loaded by '"
                + b.getSymbolicName() + "', expected was '" + bundle + "'",
                bundle, b.getSymbolicName());
        Assert.assertEquals("Bundle '" + bundle + "' is not running", Bundle.ACTIVE, b.getState());
    }
}
