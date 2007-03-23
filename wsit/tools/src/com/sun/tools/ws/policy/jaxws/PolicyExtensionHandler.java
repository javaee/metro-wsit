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

package com.sun.tools.ws.policy.jaxws;

import com.sun.tools.ws.api.wsdl.TWSDLExtensible;
import com.sun.tools.ws.api.wsdl.TWSDLExtensionHandler;
import com.sun.tools.ws.api.wsdl.TWSDLParserContext;
import com.sun.tools.ws.util.xml.XmlUtil;
import com.sun.xml.ws.policy.PolicyConstants;
import org.w3c.dom.Element;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */

public class PolicyExtensionHandler extends TWSDLExtensionHandler {
    
    
    public String getNamespaceURI() {
        return PolicyConstants.POLICY_NAMESPACE_URI;
    }
    
    /*  we need default constructor, so that our service provider could be looked up and instantiated  */
    public PolicyExtensionHandler() {
    }

    /* only skip the element if it is either <wsp:Policy/> or <wsp:PolicyReference/> element */
    private boolean handleExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return XmlUtil.matchesTagNS(e, PolicyConstants.POLICY) 
                    || XmlUtil.matchesTagNS(e, PolicyConstants.POLICY_REFERENCE);
    }
    
    @Override
    public boolean handlePortTypeExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }
    
    @Override
    public boolean handleDefinitionsExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }

    @Override
    public boolean handleBindingExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }
    
    @Override
    public boolean handleOperationExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    

    @Override
    public boolean handleInputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    

    @Override
    public boolean handleOutputExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    

    @Override
    public boolean handleFaultExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    

    @Override
    public boolean handleServiceExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    
    
    @Override
    public boolean handlePortExtension(TWSDLParserContext context, TWSDLExtensible parent, Element e) {
        return handleExtension(context, parent, e);
    }    
}
