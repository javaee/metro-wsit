/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.assembler;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.client.WSServiceDelegate;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.binding.BindingImpl;

import com.sun.xml.ws.developer.WSBindingProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature;
import junit.framework.TestCase;
import org.glassfish.gmbal.ManagedObjectManager;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class TubelineAssemblerFactoryImplTest extends TestCase {

    private static final String NAMESPACE = "http://service1.test.ws.xml.sun.com/";
    private static final URI ADDRESS_URL;
    

    static {
        try {
            ADDRESS_URL = new URI("http://localhost:8080/dispatch/Service1Service");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to initialize address URI", e);
        }
    }

    public TubelineAssemblerFactoryImplTest(String testName) {
        super(testName);
    }

    public void testCreateClientNull() {
        try {
            getAssembler(BindingID.SOAP11_HTTP).createClient(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testCreateServerNull() {
        try {
            getAssembler(BindingID.SOAP11_HTTP).createServer(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Test client creation with parameters that correspond to a dispatch client
     * with no wsit-client.xml and with no WSDL.
     */
    public void testCreateDispatchClientNoConfig() throws Exception {
        final BindingID bindingId = BindingID.SOAP11_HTTP;
        final WSBinding binding = bindingId.createBinding();
        final EndpointAddress address = new EndpointAddress(ADDRESS_URL);
        final WSDLPort port = null;
        final QName serviceName = new QName(NAMESPACE, "Service1Service");
        WSService service =  WSService.create(serviceName);
        final QName portName = new QName(NAMESPACE, "Service1Port");
        // Corresponds to Service.addPort(portName, bindingId, address)
        service.addPort(portName, bindingId.toString(), ADDRESS_URL.toString());
        final WSPortInfo portInfo = ((WSServiceDelegate) service).safeGetPort(portName);
        final Container container = MockupMetroConfigLoader.createMockupContainer("metro-default.xml");

        WSBindingProvider wsbp = new WSBindingProvider() {

            public void setOutboundHeaders(List<Header> headers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setOutboundHeaders(Header... headers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setOutboundHeaders(Object... headers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<Header> getInboundHeaders() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setAddress(String address) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public WSEndpointReference getWSEndpointReference() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public WSPortInfo getPortInfo() {
                return portInfo;
            }

            public Map<String, Object> getRequestContext() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Map<String, Object> getResponseContext() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Binding getBinding() {
                return binding;
            }

            public EndpointReference getEndpointReference() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public <T extends EndpointReference> T getEndpointReference(Class<T> clazz) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void close() throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public ManagedObjectManager getManagedObjectManager() {
                return null;
            }
        };

        final ClientTubeAssemblerContext context = new ClientTubeAssemblerContext(
                address,
                port,
                wsbp,
                binding,
                container,
                ((BindingImpl)binding).createCodec(),
                null);
        final Tube tubeline = getAssembler(bindingId).createClient(context);
        assertNotNull(tubeline);
    }

    /**
     * Test client creation with parameters that correspond to a dispatch client
     * with wsit-client.xml.
     */
    public void testCreateDispatchClientNoPoliciesConfig() throws PolicyException {
        Tube tubeline = testDispatch("nopolicies.xml");
        assertNotNull(tubeline);
    }

    /**
     * Test client creation with parameters that correspond to a dispatch client
     * with wsit-client.xml.
     */
    public void testCreateDispatchClientAllFeaturesConfig() throws PolicyException {
        Tube tubeline = testDispatch("allfeatures.xml");
        assertNotNull(tubeline);
    }

    /**
     * Test client creation with parameters that correspond to a dispatch client
     * with wsit-client.xml.
     */
    public void testCreateDispatchClientNoServiceMatchConfig() throws PolicyException {
        Tube tubeline = testDispatch("noservicematch.xml");
        assertNotNull(tubeline);
    }

    /**
     * Execute a sequence that corresponds to:
     * <pre>
     *   Service.createService(null, serviceName);
     *   Service.addPort(portName, bindingId, address);
     * </pre>
     */
    private Tube testDispatch(String configFileName) throws PolicyException {
        final URL wsdlLocation = null;
        final QName serviceName = new QName(NAMESPACE, "Service1Service");
        // Corresponds to Service.createService(wsdlLocation, serviceName)
        final WSServiceDelegate serviceDelegate = new WSServiceDelegate(wsdlLocation, serviceName, Service.class);

        final QName portName = new QName(NAMESPACE, "Service1Port");
        final BindingID bindingId = BindingID.SOAP11_HTTP;
        // Corresponds to Service.addPort(portName, bindingId, address)
        serviceDelegate.addPort(portName, bindingId.toString(), ADDRESS_URL.toString());

        final EndpointAddress address = new EndpointAddress(ADDRESS_URL);
        final WSDLPort port = null;
        final WSPortInfo portInfo = serviceDelegate.safeGetPort(portName);
        final WSBinding binding = bindingId.createBinding(new AddressingFeature(true));
        final Container container = MockupMetroConfigLoader.createMockupContainer("metro-default.xml");

        WSBindingProvider wsbp = new WSBindingProvider() {

            public void setOutboundHeaders(List<Header> headers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setOutboundHeaders(Header... headers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setOutboundHeaders(Object... headers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<Header> getInboundHeaders() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setAddress(String address) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public WSEndpointReference getWSEndpointReference() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public WSPortInfo getPortInfo() {
                return portInfo;
            }

            public Map<String, Object> getRequestContext() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Map<String, Object> getResponseContext() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Binding getBinding() {
                return binding;
            }

            public EndpointReference getEndpointReference() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public <T extends EndpointReference> T getEndpointReference(Class<T> clazz) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void close() throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public ManagedObjectManager getManagedObjectManager() {
                return null;
            }
        };

        final ClientTubeAssemblerContext context = new ClientTubeAssemblerContext(
                address, 
                port,
                wsbp,
                binding,
                container,
                ((BindingImpl)binding).createCodec(),
                null);

        return getAssembler(bindingId).createClient(context);
    }

    private TubelineAssembler getAssembler(BindingID bindingId) {
        TubelineAssemblerFactoryImpl taFactory = new TubelineAssemblerFactoryImpl();
        return taFactory.doCreate(bindingId);
    }
}
