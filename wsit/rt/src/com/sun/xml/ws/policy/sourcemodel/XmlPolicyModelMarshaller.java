/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import java.util.Collection;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import com.sun.xml.txw2.TXW;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.output.StaxSerializer;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.util.Map;

public final class XmlPolicyModelMarshaller extends PolicyModelMarshaller {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(XmlPolicyModelMarshaller.class);
    
    private final boolean marshallInvisible;
    
    XmlPolicyModelMarshaller(boolean marshallInvisible) {
        this.marshallInvisible = marshallInvisible;
    }
    
    public void marshal(final PolicySourceModel model, final Object storage) throws PolicyException {
        if (storage instanceof TypedXmlWriter) {
            marshal(model, (TypedXmlWriter) storage);
        } else if (storage instanceof XMLStreamWriter) {
            marshal(model, (XMLStreamWriter) storage);
        } else {
            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0022_STORAGE_TYPE_NOT_SUPPORTED(storage.getClass().getName())));
        }
    }
    
    public void marshal(final Collection<PolicySourceModel> models, final Object storage) throws PolicyException {
        for (PolicySourceModel model : models) {
            marshal(model, storage);
        }
    }
    
    /**
     * Marshal a policy onto the given TypedXmlWriter.
     *
     * @param model A policy source model.
     * @param writer A typed XML writer.
     */
    private void marshal(final PolicySourceModel model, final TypedXmlWriter writer) throws PolicyException {
        final TypedXmlWriter policy = writer._element(model.getNamespaceVersion().asQName(XmlToken.Policy), TypedXmlWriter.class);
        marshalPolicyAttributes(model, policy);
        marshal(model.getNamespaceVersion(), model.getRootNode(), policy);
    }
    
    /**
     * Marshal a policy onto the given XMLStreamWriter.
     *
     * @param model A policy source model.
     * @param writer An XML stream writer.
     */
    private void marshal(final PolicySourceModel model, final XMLStreamWriter writer) throws PolicyException {
        final StaxSerializer serializer = new StaxSerializer(writer);
        final TypedXmlWriter policy = TXW.create(model.getNamespaceVersion().asQName(XmlToken.Policy), TypedXmlWriter.class, serializer);
        
        final Map<String, String> nsMap = model.getNamespaceToPrefixMapping();
        
        if (!marshallInvisible && nsMap.containsKey(PolicyConstants.SUN_POLICY_NAMESPACE_URI)) {
            nsMap.remove(PolicyConstants.SUN_POLICY_NAMESPACE_URI);
        }
        
        for (Map.Entry<String, String> nsMappingEntry : nsMap.entrySet()) {
            policy._namespace(nsMappingEntry.getKey(), nsMappingEntry.getValue());
        }
        
        marshalPolicyAttributes(model, policy);
        marshal(model.getNamespaceVersion(), model.getRootNode(), policy);
        policy.commit();
        serializer.flush();
    }
    
    /**
     * Marshal the Policy root element attributes onto the TypedXmlWriter.
     *
     * @param model The policy source model.
     * @param writer The typed XML writer.
     */
    private static void marshalPolicyAttributes(final PolicySourceModel model, final TypedXmlWriter writer) {
        final String policyId = model.getPolicyId();
        if (policyId != null) {
            writer._attribute(PolicyConstants.WSU_ID, policyId);
        }
        
        final String policyName = model.getPolicyName();
        if (policyName != null) {
            writer._attribute(model.getNamespaceVersion().asQName(XmlToken.Name), policyName);
        }
    }
    
    /**
     * Marshal given ModelNode and child elements on given TypedXmlWriter.
     *
     * @param rootNode The ModelNode that is marshalled.
     * @param writer The TypedXmlWriter onto which the content of the rootNode is marshalled.
     */
    private void marshal(final NamespaceVersion nsVersion, final ModelNode rootNode, final TypedXmlWriter writer) {
        for (ModelNode node : rootNode) {
            final AssertionData data = node.getNodeData();
            if (marshallInvisible || data == null || !data.isPrivateAttributeSet()) {
                TypedXmlWriter child = null;
                if (data == null) {
                    child = writer._element(nsVersion.asQName(node.getType().getXmlToken()), TypedXmlWriter.class);
                } else {
                    child = writer._element(data.getName(), TypedXmlWriter.class);
                    final String value = data.getValue();
                    if (value != null) {
                        child._pcdata(value);
                    }
                    if (data.isOptionalAttributeSet()) {
                        child._attribute(nsVersion.asQName(XmlToken.Optional), Boolean.TRUE);                        
                    }
                    if (data.isIgnorableAttributeSet()) {
                        child._attribute(nsVersion.asQName(XmlToken.Ignorable), Boolean.TRUE);                        
                    }
                    for (Entry<QName, String> entry : data.getAttributesSet()) {
                        child._attribute(entry.getKey(), entry.getValue());
                    }
                }
                marshal(nsVersion, node, child);
            }
        }
    }
    
}
