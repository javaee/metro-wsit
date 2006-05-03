/*
 * $Id: ExportSamlAssertionFilter.java,v 1.1 2006-05-03 22:57:48 arungupta Exp $
 */

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

package com.sun.xml.wss.impl.filter;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.xml.soap.SOAPElement;

import java.util.HashMap;
import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;

import com.sun.xml.wss.saml.Assertion;
//import com.sun.xml.wss.saml.AuthorityBinding;

import com.sun.xml.wss.core.SamlAssertionHeaderBlock;
import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.core.SignatureHeaderBlock;
import com.sun.xml.wss.core.SecurityTokenReference;

import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;

import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.keyinfo.KeyIdentifierStrategy;

import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;

import com.sun.xml.wss.impl.HarnessUtil;
import org.w3c.dom.Element;

/*
 * 
 */
public class ExportSamlAssertionFilter {
    
    /* (non-Javadoc)
     */
    public static void process(FilterProcessingContext context) throws XWSSecurityException {
        
        //make a DynamicPolicyCallback to obtain the SAML assertion
        try {
                SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
                SecurityHeader securityHeader =
                    secureMessage.findOrCreateSecurityHeader();
 
                AuthenticationTokenPolicy policy = 
                    (AuthenticationTokenPolicy)context.getSecurityPolicy();
                AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy =
                    (AuthenticationTokenPolicy.SAMLAssertionBinding)policy.getFeatureBinding();
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

                try {
                    if ( System.getProperty("com.sun.xml.wss.saml.binding.jaxb") == null) {
                        _assertion = (Assertion)com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion.fromElement(resolvedPolicy.getAssertion());
                    } else {
                        _assertion = (Assertion)com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion.fromElement(resolvedPolicy.getAssertion());
                    }
                } catch (SAMLException ex) {
                    //ignore
                }
                Element _authorityBinding = resolvedPolicy.getAuthorityBinding();

                if ((_assertion == null) && (_authorityBinding == null)) {
                    throw new XWSSecurityException(
                        "None of SAML Assertion, SAML AuthorityBinding information was set into " +
                        " the Policy by the CallbackHandler"); 
                }

                //TODO: check that the Confirmation Method of the assertion is indeed SV
                if (_assertion != null) {
                    if (_authorityBinding == null)  {
                        if ( System.getProperty("com.sun.xml.wss.saml.binding.jaxb") == null) {
                            ((com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion)_assertion).toElement(securityHeader);
                        } else {
                            ((com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion)_assertion).toElement(securityHeader);
                        }
                        HashMap tokenCache = context.getTokenCache();
                        //assuming unique IDs
                        tokenCache.put(((com.sun.xml.wss.saml.Assertion)_assertion).getAssertionID(), _assertion);
                    } else {
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
                    SecurityTokenReference tokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    tokenRef.setWsuId(resolvedPolicy.getSTRID());

                    if (_authorityBinding != null) {
                        tokenRef.setSamlAuthorityBinding(_authorityBinding, secureMessage.getSOAPPart());
                    }

                    KeyIdentifierStrategy strat = new KeyIdentifierStrategy(assertionId); 
                    strat.insertKey(tokenRef, context.getSecurableSoapMessage());
                    securityHeader.insertHeaderBlock(tokenRef);
                }
                
            } catch (Exception e) {
                // log
                throw new XWSSecurityException(e);
            }
        }
}
