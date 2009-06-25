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

package com.sun.xml.ws.management.server;

import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.output.StaxSerializer;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelGenerator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.sourcemodel.attach.ExternalAttachmentsUnmarshaller;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken;
import com.sun.xml.ws.resources.ManagementMessages;
import com.sun.xml.ws.util.xml.XMLStreamReaderToXMLStreamWriter;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import java.net.URI;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Fabian Ritzmann
 */
public class ManagementWSDLPatcher extends XMLStreamReaderToXMLStreamWriter {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(ManagementWSDLPatcher.class);
    private static final PolicyModelMarshaller POLICY_MARSHALLER = PolicyModelMarshaller.getXmlMarshaller(true);
    private static final PolicyModelGenerator POLICY_GENERATOR = PolicyModelGenerator.getGenerator();
    private final Map<URI, Policy> urnToPolicy;
    private XmlToken skipToken;
    private boolean inBinding = false;

    public ManagementWSDLPatcher(Map<URI, Policy> urnToPolicy) {
        this.urnToPolicy = urnToPolicy;
    }

    /**
     * If we find a policy element, skip it. If we find a binding element,
     * marshal any policies onto it.
     *
     * @param i The i-th attribute of the current element
     * @throws XMLStreamException If a parsing error occured
     */
    @Override
    protected void handleStartElement() throws XMLStreamException {
        if (this.skipToken != null) {
            return;
        }
        final QName elementName = this.in.getName();
        final XmlToken policyToken = NamespaceVersion.resolveAsToken(elementName);
        if (policyToken != XmlToken.UNKNOWN) {
            this.skipToken = policyToken;
            return;
        }
        else if (elementName.equals(WSDLConstants.QNAME_BINDING)) {
            this.inBinding = true;
            super.handleStartElement();
            final Policy bindingPolicy = urnToPolicy.get(ExternalAttachmentsUnmarshaller.BINDING_ID);
            if (bindingPolicy != null) {
                try {
                    final PolicySourceModel policyModel = POLICY_GENERATOR.translate(bindingPolicy);
                    final StaxSerializer serializer = new FragmentSerializer(this.out);
                    final TypedXmlWriter policy = TXW.create(NamespaceVersion.v1_5.asQName(XmlToken.Policy), TypedXmlWriter.class, serializer);
                    POLICY_MARSHALLER.marshal(policyModel, policy);
                    policy.commit();
                } catch (PolicyException ex) {
                    throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_0004_CANNOT_MARSHAL(this.out)), ex);
                }
            }
        }
        else {
            super.handleStartElement();
        }
    }

    /**
     * Skip all policy expressions.
     *
     * @throws XMLStreamException If a parsing error occured
     */
    @Override
    protected void handleEndElement() throws XMLStreamException {
        final QName elementName = this.in.getName();
        this.inBinding = !elementName.equals(WSDLConstants.QNAME_BINDING);
        if (this.skipToken == null) {
            super.handleEndElement();
            return;
        }
        else {
            final XmlToken policyToken = NamespaceVersion.resolveAsToken(elementName);
            if (this.skipToken.equals(policyToken)) {
                this.skipToken = null;
                return;
            }
        }
    }

    /**
     * Skip all policy attributes
     *
     * @param i The i-th attribute of the current element
     * @throws XMLStreamException If a parsing error occured
     */
    @Override
    protected void handleAttribute(int i) throws XMLStreamException {
        final QName attributeName = this.in.getAttributeName(i);
        final XmlToken policyToken = NamespaceVersion.resolveAsToken(attributeName);
        switch (policyToken) {
            case PolicyUris:
                return;
        }
        super.handleAttribute(i);
    }

    @Override
    protected void handleCharacters() throws XMLStreamException {
        super.handleCharacters();
    }
}