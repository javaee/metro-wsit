/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.policy.sourcemodel;

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
    
    private boolean marshallInvisible;
    
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
        final TypedXmlWriter policy = writer._element(PolicyConstants.POLICY, TypedXmlWriter.class);
        marshalPolicyAttributes(model, policy);
        marshal(model.getRootNode(), policy);
    }
    
    /**
     * Marshal a policy onto the given XMLStreamWriter.
     *
     * @param model A policy source model.
     * @param writer An XML stream writer.
     */
    private void marshal(final PolicySourceModel model, final XMLStreamWriter writer) throws PolicyException {
        final StaxSerializer serializer = new StaxSerializer(writer);
        final TypedXmlWriter policy = TXW.create(PolicyConstants.POLICY, TypedXmlWriter.class, serializer);
        
        Map<String, String> nsMap = model.getNamespaceToPrefixMapping();
        
        if (!marshallInvisible) {
            if (nsMap.containsKey(PolicyConstants.SUN_POLICY_NAMESPACE_URI)) {
                nsMap.remove(PolicyConstants.SUN_POLICY_NAMESPACE_URI);
            }
        }
        
        for (Map.Entry<String, String> nsMappingEntry : nsMap.entrySet()) {            
            policy._namespace(nsMappingEntry.getKey(), nsMappingEntry.getValue());
        }
        
        marshalPolicyAttributes(model, policy);
        marshal(model.getRootNode(), policy);
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
            writer._attribute(PolicyConstants.POLICY_NAME, policyName);
        }
    }
    
    /**
     * Marshal given ModelNode and child elements on given TypedXmlWriter.
     *
     * @param rootNode The ModelNode that is marshalled.
     * @param writer The TypedXmlWriter onto which the content of the rootNode is marshalled.
     */
    private void marshal(final ModelNode rootNode, final TypedXmlWriter writer) {
        for (ModelNode node : rootNode) {
            final AssertionData data = node.getNodeData();
            if (marshallInvisible || data == null || !data.isPrivateAttributeSet()) {
                TypedXmlWriter child = null;
                if (data == null) {
                    child = writer._element(node.getType().asQName(), TypedXmlWriter.class);
                } else {
                    child = writer._element(data.getName(), TypedXmlWriter.class);
                    final String value = data.getValue();
                    if (value != null) {
                        child._pcdata(value);
                    }
                    for (Entry<QName, String> entry : data.getAttributesSet()) {
                        child._attribute(entry.getKey(), entry.getValue());
                    }
                }
                marshal(node, child);
            }
        }
    }
    
}
