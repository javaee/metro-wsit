
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

package com.sun.xml.wss.impl.filter;

import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import com.sun.xml.ws.security.opt.impl.message.GSHeaderElement;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.util.WSSElementFactory;

import javax.xml.crypto.Data;
import java.util.HashMap;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import com.sun.xml.wss.impl.keyinfo.KeyIdentifierStrategy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.util.SAMLUtil;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Element;

/*
 *
 */
public class ExportSamlAssertionFilter {
    
    /* (non-Javadoc)
     */
    @SuppressWarnings("unchecked")
    public static void process(FilterProcessingContext context) throws XWSSecurityException {
        
        //make a DynamicPolicyCallback to obtain the SAML assertion
        
        boolean isOptimized = false;
        SecurableSoapMessage secureMessage = null;
        SecurityHeader securityHeader = null;
        com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader optSecHeader = null;
        SecurityHeaderElement she = null;
        if(context instanceof JAXBFilterProcessingContext){
            isOptimized = true;
            optSecHeader = ((JAXBFilterProcessingContext)context).getSecurityHeader();
        } else{
            secureMessage = context.getSecurableSoapMessage();
            securityHeader = secureMessage.findOrCreateSecurityHeader();
        }
        
        AuthenticationTokenPolicy policy =
                (AuthenticationTokenPolicy)context.getSecurityPolicy();
        AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy =
                (AuthenticationTokenPolicy.SAMLAssertionBinding)policy.getFeatureBinding();
        
        if (samlPolicy.getIncludeToken() == samlPolicy.INCLUDE_ONCE) {
            throw new XWSSecurityException("Include Token ONCE not supported for SAMLToken Assertions");
        }
        
        if (samlPolicy.getAssertionType() !=
                AuthenticationTokenPolicy.SAMLAssertionBinding.SV_ASSERTION) {
            // should never be called this way
            throw new XWSSecurityException(
                    "Internal Error: ExportSamlAssertionFilter called for HOK assertion");
        }
        
        //AuthenticationTokenPolicy policyClone = (AuthenticationTokenPolicy)policy.clone();
        samlPolicy =
                (AuthenticationTokenPolicy.SAMLAssertionBinding)policy.getFeatureBinding();
        samlPolicy.isReadOnly(true);
        
        DynamicApplicationContext dynamicContext =
                new DynamicApplicationContext(context.getPolicyContext());
        dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
        dynamicContext.inBoundMessage(false);
        
        AuthenticationTokenPolicy.SAMLAssertionBinding resolvedPolicy =
                context.getSecurityEnvironment().populateSAMLPolicy(context.getExtraneousProperties(), samlPolicy, dynamicContext);
        
        Assertion _assertion = null;
        Element assertionElement = resolvedPolicy.getAssertion();
        Element _authorityBinding = resolvedPolicy.getAuthorityBinding();
        JAXBElement element = null;
        
        if (assertionElement == null) {
            XMLStreamReader reader = resolvedPolicy.getAssertionReader();
            if (reader != null) {
                _assertion = SAMLUtil.createSAMLAssertion(reader, resolvedPolicy.getSAMLVersion());
                element = SAMLUtil.element;
            }
        } else {
            try {
                if (System.getProperty("com.sun.xml.wss.saml.binding.jaxb") == null) {
                    if (assertionElement.getAttributeNode("ID") != null) {
                        _assertion = (Assertion) com.sun.xml.wss.saml.assertion.saml20.jaxb20.Assertion.fromElement(assertionElement);
                    } else {
                        _assertion = (Assertion) com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion.fromElement(assertionElement);
                    }
                } else {
                    _assertion = (Assertion) com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion.fromElement(assertionElement);
                }
            } catch (SAMLException ex) {
                //ignore
            }
        }

        if (samlPolicy.getIncludeToken() == samlPolicy.INCLUDE_NEVER ||
               samlPolicy.getIncludeToken() == samlPolicy.INCLUDE_NEVER_VER2 ) {
            if (_authorityBinding != null) {
                //nullify the assertion set by Callback since IncludeToken is never
                // do this because we have to maintain BackwardCompat with XWSS2.0
                assertionElement = null;
            }
        }
        
        if ((_assertion == null) && (_authorityBinding == null)) {
            throw new XWSSecurityException(
                    "None of SAML Assertion, SAML AuthorityBinding information was set into " +
                    " the Policy by the CallbackHandler");
        }
        
        //TODO: check that the Confirmation Method of the assertion is indeed SV
        if (_assertion != null){
            if(_assertion.getVersion() == null && _authorityBinding == null){
                if(!isOptimized){
                    if ( System.getProperty("com.sun.xml.wss.saml.binding.jaxb") == null) {
                        ((com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion)_assertion).toElement(securityHeader);
                    } else {
                        ((com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion)_assertion).toElement(securityHeader);
                    }
                } else{
                    if(assertionElement != null){
                        she = new GSHeaderElement(assertionElement, ((JAXBFilterProcessingContext)context).getSOAPVersion());
                    }else {
                        she = new GSHeaderElement(element, ((JAXBFilterProcessingContext)context).getSOAPVersion());
                        //with the above constructor setId() is not happening , so set explicitely
                        she.setId(_assertion.getAssertionID());
                    }
                    
                    if(optSecHeader.getChildElement(she.getId()) == null){
                        optSecHeader.add(she);
                    } else{
                        return;
                    }
                    
                }
                HashMap tokenCache = context.getTokenCache();
                //assuming unique IDs
                tokenCache.put(((com.sun.xml.wss.saml.Assertion)_assertion).getAssertionID(), _assertion);
            } else if (_assertion.getVersion() != null){
                if(!isOptimized){
                    ((com.sun.xml.wss.saml.assertion.saml20.jaxb20.Assertion)_assertion).toElement(securityHeader);
                } else{
                    if(assertionElement != null){
                        she = new GSHeaderElement(assertionElement, ((JAXBFilterProcessingContext)context).getSOAPVersion());
                    }else {
                        she = new GSHeaderElement(element, ((JAXBFilterProcessingContext)context).getSOAPVersion());
                        //with the above constructor setId() is not happening , so set explicitely
                        she.setId(_assertion.getAssertionID());
                    }
                    if(optSecHeader.getChildElement(she.getId()) == null){
                        optSecHeader.add(she);
                    } else{
                        return;
                    }
                }
                HashMap tokenCache = context.getTokenCache();
                //assuming unique IDs
                tokenCache.put(((com.sun.xml.wss.saml.Assertion)_assertion).getID(), _assertion);
            }  else {
                //Authoritybinding is set. So the Assertion should not be exported
                if (null == resolvedPolicy.getSTRID()) {
                    throw new XWSSecurityException(
                            "Unsupported configuration: required wsu:Id value " +
                            " for SecurityTokenReference to Remote SAML Assertion not found " +
                            " in Policy");
                }
            }
        }
        
        if (null != resolvedPolicy.getSTRID()) {
            //generate and export an STR into the Header with the given ID
            if ((_assertion == null) && (null == resolvedPolicy.getAssertionId())) {
                throw new XWSSecurityException(
                        "None of SAML Assertion, SAML Assertion Id information was set into " +
                        " the Policy by the CallbackHandler");
            }
            
            String assertionId = resolvedPolicy.getAssertionId();
            if (_assertion != null) {
                assertionId = ((com.sun.xml.wss.saml.Assertion)_assertion).getAssertionID();
            }
            if(!isOptimized){
                SecurityTokenReference tokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                tokenRef.setWsuId(resolvedPolicy.getSTRID());
                // set wsse11:TokenType to SAML1.1 or SAML2.0
                if(_assertion.getVersion() != null){
                    tokenRef.setTokenType(MessageConstants.WSSE_SAML_v2_0_TOKEN_TYPE);
                }else{
                    tokenRef.setTokenType(MessageConstants.WSSE_SAML_v1_1_TOKEN_TYPE);
                }
                
                if (_authorityBinding != null) {
                    tokenRef.setSamlAuthorityBinding(_authorityBinding, secureMessage.getSOAPPart());
                }
                
                KeyIdentifierStrategy strat = new KeyIdentifierStrategy(assertionId);
                strat.insertKey(tokenRef, context.getSecurableSoapMessage());
                securityHeader.insertHeaderBlock(tokenRef);
            } else{
                JAXBFilterProcessingContext optContext = (JAXBFilterProcessingContext)context;
                WSSElementFactory elementFactory = new WSSElementFactory(optContext.getSOAPVersion());
                com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier ref = elementFactory.createKeyIdentifier();
                ref.setValue(assertionId);
                if(_assertion.getVersion() != null){
                    ref.setValueType(MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE);
                } else{
                    ref.setValueType(MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE);
                }
                com.sun.xml.ws.security.opt.impl.keyinfo.SecurityTokenReference secTokRef = elementFactory.createSecurityTokenReference(ref);
                String strId = resolvedPolicy.getSTRID();
                secTokRef.setId(strId);
                if("true".equals(optContext.getExtraneousProperty("EnableWSS11PolicySender"))){
                    // set wsse11:TokenType to SAML1.1 or SAML2.0
                    if(_assertion.getVersion() != null){
                        secTokRef.setTokenType(MessageConstants.WSSE_SAML_v2_0_TOKEN_TYPE);
                    }else{
                        secTokRef.setTokenType(MessageConstants.WSSE_SAML_v1_1_TOKEN_TYPE);
                    }
                    ((NamespaceContextEx)optContext.getNamespaceContext()).addWSS11NS();
                }
                Data data = new SSEData((SecurityElement)she,false,optContext.getNamespaceContext());
                optContext.getElementCache().put(strId,data);
                optSecHeader.add(secTokRef);
            }
        }
        
    }    
}
