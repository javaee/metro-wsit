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

package com.sun.xml.ws.config.management.policy;

import com.sun.xml.ws.config.management.ManagementConstants;
import com.sun.xml.ws.config.management.policy.ManagedServiceAssertion.ImplementationRecord;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.SimpleAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

/**
 *
 * @author Fabian Ritzmann
 */
public class ManagedServiceAssertionTest extends TestCase {
    
    public ManagedServiceAssertionTest(String testName) {
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
     * Test of getID method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetID() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ManagedServiceAssertion.ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        String expResult = "id1";
        String result = instance.getID();
        assertEquals(expResult, result);
    }

    /**
     * Test of getID method, of class ManagedServiceAssertion.
     */
    public void testNoID() {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        try {
            ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
            fail("Expected AssertionCreationException because the ManagedServiceAssertion requires an id attribute.");
        } catch (AssertionCreationException e) {
            // expected
        }
    }

    /**
     * Test of getStart method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetStart() throws AssertionCreationException {
        final HashMap<QName, String> attributes = new HashMap<QName, String>();
        attributes.put(ManagedServiceAssertion.ID_ATTRIBUTE_QNAME, "id1");
        attributes.put(ManagedServiceAssertion.START_ATTRIBUTE_QNAME, "notify");
        final AssertionData data = AssertionData.createAssertionData(ManagedServiceAssertion.MANAGED_SERVICE_QNAME,
                null, attributes, false, false);
        ManagedServiceAssertion instance = new ManagedServiceAssertion(data, null);
        String expResult = "notify";
        String result = instance.getStart();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommunicationServerImplementations method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetCommunicationServerImplementations() throws AssertionCreationException {
        final HashMap<QName, String> implementationAttributes = new HashMap<QName, String>();
        implementationAttributes.put(ManagedServiceAssertion.CLASS_NAME_ATTRIBUTE_QNAME, "CommunicationServerTestClass");
        final AssertionData implementationData = AssertionData.createAssertionData(
                ManagedServiceAssertion.COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME, null, implementationAttributes, false, false);
        final PolicyAssertion implementationParameter = new SimpleAssertion(implementationData, null) { };

        final LinkedList<PolicyAssertion> implementationsParameters = new LinkedList<PolicyAssertion>();
        implementationsParameters.add(implementationParameter);
        final AssertionData implementationsData = AssertionData.createAssertionData(
                ManagedServiceAssertion.COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME, null, null, false, false);
        final PolicyAssertion implementationsParameter = new SimpleAssertion(implementationsData, implementationsParameters) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(implementationsParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ManagedServiceAssertion.ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<String, String> expMap = new HashMap<String, String>();
        final ImplementationRecord expResult = new ImplementationRecord("CommunicationServerTestClass", expMap, null);
        final Collection<ImplementationRecord> records = instance.getCommunicationServerImplementations();
        assertEquals(1, records.size());
        final ImplementationRecord record = records.iterator().next();
        assertEquals(expResult, record);
    }

    /**
     * Test of getConfiguratorImplementation method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException 
     */
    public void testGetConfiguratorImplementation() throws AssertionCreationException {
        final HashMap<QName, String> configuratorAttributes = new HashMap<QName, String>();
        configuratorAttributes.put(ManagedServiceAssertion.CLASS_NAME_ATTRIBUTE_QNAME, "ConfiguratorTestClass");
        final AssertionData configuratorData = AssertionData.createAssertionData(
                ManagedServiceAssertion.CONFIGURATOR_IMPLEMENTATION_PARAMETER_QNAME, null, configuratorAttributes, false, false);
        final PolicyAssertion configuratorParameter = new SimpleAssertion(configuratorData, null) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(configuratorParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ManagedServiceAssertion.ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<String, String> expMap = new HashMap<String, String>();
        ImplementationRecord expResult = new ImplementationRecord("ConfiguratorTestClass", expMap, null);
        ImplementationRecord result = instance.getConfiguratorImplementation();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConfigSaverImplementation method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetConfigSaverImplementation() throws AssertionCreationException {
        final HashMap<QName, String> configSaverAttributes = new HashMap<QName, String>();
        configSaverAttributes.put(ManagedServiceAssertion.CLASS_NAME_ATTRIBUTE_QNAME, "ConfigSaverTestClass");
        final AssertionData configSaverData = AssertionData.createAssertionData(
                ManagedServiceAssertion.CONFIG_SAVER_IMPLEMENTATION_PARAMETER_QNAME, null, configSaverAttributes, false, false);
        final PolicyAssertion configSaverParameter = new SimpleAssertion(configSaverData, null) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(configSaverParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ManagedServiceAssertion.ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<String, String> expMap = new HashMap<String, String>();
        ImplementationRecord expResult = new ImplementationRecord("ConfigSaverTestClass", expMap, null);
        ImplementationRecord result = instance.getConfigSaverImplementation();
        assertEquals(expResult, result);
    }

    /**
     * Test of getConfigReaderImplementation method, of class ManagedServiceAssertion.
     * @throws AssertionCreationException
     */
    public void testGetConfigReaderImplementation() throws AssertionCreationException {
        final HashMap<QName, String> configReaderAttributes = new HashMap<QName, String>();
        configReaderAttributes.put(ManagedServiceAssertion.CLASS_NAME_ATTRIBUTE_QNAME, "ConfigReaderTestClass");
        final AssertionData configReaderData = AssertionData.createAssertionData(
                ManagedServiceAssertion.CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME, null, configReaderAttributes, false, false);
        final PolicyAssertion configReaderParameter = new SimpleAssertion(configReaderData, null) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(configReaderParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ManagedServiceAssertion.ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<String, String> expMap = new HashMap<String, String>();
        ImplementationRecord expResult = new ImplementationRecord("ConfigReaderTestClass", expMap, null);
        ImplementationRecord result = instance.getConfigReaderImplementation();
        assertEquals(expResult, result);
    }

    public void testGetConfigReaderImplementationJdbcDataSourceName() throws AssertionCreationException {
        final HashMap<QName, String> parameterAttributes = new HashMap<QName, String>();
        parameterAttributes.put(ManagedServiceAssertion.NAME_ATTRIBUTE_QNAME, ManagementConstants.JDBC_DATA_SOURCE_PARAMETER_NAME);
        final AssertionData parameterData = AssertionData.createAssertionData(
                ManagedServiceAssertion.PARAMETER_PARAMETER_QNAME, "source1", parameterAttributes, false, false);
        final LinkedList<PolicyAssertion> configReaderParameters = new LinkedList<PolicyAssertion>();
        configReaderParameters.add(new SimpleAssertion(parameterData, null) { });

        final AssertionData configReaderData = AssertionData.createAssertionData(
                ManagedServiceAssertion.CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME, null, null, false, false);
        final PolicyAssertion configReaderParameter = new SimpleAssertion(configReaderData, configReaderParameters) { };

        final LinkedList<PolicyAssertion> managedServiceParameters = new LinkedList<PolicyAssertion>();
        managedServiceParameters.add(configReaderParameter);
        final HashMap<QName, String> managedServiceAttributes = new HashMap<QName, String>();
        managedServiceAttributes.put(ManagedServiceAssertion.ID_ATTRIBUTE_QNAME, "id1");
        final AssertionData managedServiceData = AssertionData.createAssertionData(
                ManagedServiceAssertion.MANAGED_SERVICE_QNAME, null, managedServiceAttributes, false, false);

        final ManagedServiceAssertion instance = new ManagedServiceAssertion(managedServiceData, managedServiceParameters);
        final HashMap<String, String> expMap = new HashMap<String, String>();
        ImplementationRecord implementation = instance.getConfigReaderImplementation();
        Map<String, String> parameters = implementation.getParameters();
        String expResult = "source1";
        String result = parameters.get(ManagementConstants.JDBC_DATA_SOURCE_PARAMETER_NAME);
        assertEquals(expResult, result);
    }

}