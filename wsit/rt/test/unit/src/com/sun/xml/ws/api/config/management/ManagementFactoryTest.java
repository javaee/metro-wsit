/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.xml.ws.api.config.management;

//import com.sun.xml.ws.config.management.jmx.JMXAgent;
import com.sun.xml.ws.config.management.policy.ManagedServiceAssertion;
import com.sun.xml.ws.config.management.server.DefaultConfigurator;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;

//import java.util.Collection;
import java.util.HashMap;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

/**
 *
 * @author Fabian Ritzmann
 */
public class ManagementFactoryTest extends TestCase {
    
    public ManagementFactoryTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of createCommunicationImpls method, of class ManagementFactory.
     * @throws AssertionCreationException
     */
    public void testCreateCommunicationImpls() throws AssertionCreationException {
//        final HashMap<QName, String> attributes = new HashMap<QName, String>();
//        attributes.put(ManagedServiceAssertion.ID_ATTRIBUTE_QNAME, "id1");
//        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
//                null, attributes, false, false);
//        final ManagedServiceAssertion assertion = new ManagedServiceAssertion(data, null);
//
//        final NamedParameters initParameters = new NamedParameters();
//        initParameters.put(ManagedEndpoint.ENDPOINT_ID_PARAMETER_NAME, new ManagedEndpoint<String>(null, null, null));
//
//        ManagementFactory instance = new ManagementFactory(assertion);
//        Collection result = instance.createCommunicationImpls(new NamedParameters());
//        assertTrue(JMXAgent.class.isInstance(result));
    }

    /**
     * Test of createConfiguratorImpl method, of class ManagementFactory.
     * @throws AssertionCreationException
     */
    public void testCreateConfiguratorImpl() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ManagedServiceAssertion.ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        final ManagedServiceAssertion assertion = new ManagedServiceAssertion(data, null);

        ManagementFactory instance = new ManagementFactory(assertion);
        Configurator result = instance.createConfiguratorImpl();
        assertTrue(DefaultConfigurator.class.isInstance(result));
    }

    /**
     * Test of createConfigSaverImpl method, of class ManagementFactory.
     */
    public void testCreateConfigSaverImpl() {
//        System.out.println("createConfigSaverImpl");
//        ManagementFactory instance = null;
//        ConfigSaver expResult = null;
//        ConfigSaver result = instance.createConfigSaverImpl();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of createConfigReaderImpl method, of class ManagementFactory.
     */
    public void testCreateConfigReaderImpl() {
//        System.out.println("createConfigReaderImpl");
//        NamedParameters parameters = null;
//        ManagementFactory instance = null;
//        ConfigReader expResult = null;
//        ConfigReader result = instance.createConfigReaderImpl(parameters);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

}
