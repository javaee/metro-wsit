/*
 * $Id: ImportSamlAssertionFilter.java,v 1.1 2006-05-03 22:57:48 arungupta Exp $
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

import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.AssertionUtil;
import java.security.PublicKey;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.logging.Level;

import javax.xml.soap.SOAPElement;

import org.w3c.dom.Element;

import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolver;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;

import com.sun.xml.wss.impl.resolver.URIResolver;
import com.sun.xml.wss.impl.misc.KeyResolver;

import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityHeader;

import com.sun.xml.wss.saml.util.SAMLUtil;
import com.sun.xml.wss.saml.Subject;
import com.sun.xml.wss.saml.AttributeStatement;
import com.sun.xml.wss.saml.AuthenticationStatement;

import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;

import java.util.logging.Logger;

import org.w3c.dom.NodeList;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;

/**
 * @author Kumar Jayanti
 */
public class ImportSamlAssertionFilter{

    protected static Logger log =  Logger.getLogger( LogDomainConstants.FILTER_DOMAIN,LogDomainConstants.FILTER_DOMAIN_BUNDLE);

    public static void process(FilterProcessingContext context)	throws XWSSecurityException {
  
        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        SecurityHeader wsseSecurity = secureMessage.findSecurityHeader();
        Assertion samlAssertion = null;
        SOAPElement samlElement = null;

        if( context.getMode() == FilterProcessingContext.ADHOC || 
            context.getMode() == FilterProcessingContext.DEFAULT || 
            context.getMode() == FilterProcessingContext.WSDL_POLICY) {
 
            NodeList nl = wsseSecurity.getElementsByTagNameNS( 
                               MessageConstants.SAML_v1_0_NS, MessageConstants.SAML_ASSERTION_LNAME);
            int nodeListLength = nl.getLength();

            //for now we dont allow multiple saml assertions
            if (nodeListLength == 0) {
                // Log Message
                throw new XWSSecurityException(
                "No SAML Assertion found, Reciever requirement not met");
            } else if (nodeListLength > 1) {
                throw new XWSSecurityException(
                    "More than one SAML Assertion found, Reciever requirement not met");
            }else{
                samlElement = (SOAPElement)nl.item(0);
                try {
                    samlAssertion = AssertionUtil.fromElement(samlElement);
                } catch(Exception e) {
                    log.log(Level.SEVERE,"WSS0418.saml.import.exception");
                    throw SecurableSoapMessage.newSOAPFaultException(
                            MessageConstants.WSSE_INVALID_SECURITY,
                            "Exception while importing SAML Token",
                            e);
                }
            }

            if (context.getMode() == FilterProcessingContext.ADHOC) {

                //try to validate against the policy
                AuthenticationTokenPolicy policy = (AuthenticationTokenPolicy)context.getSecurityPolicy();
                AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy =
                    (AuthenticationTokenPolicy.SAMLAssertionBinding)policy.getFeatureBinding();

                //ensure the authorityId if specified matches
                if (!"".equals(samlPolicy.getAuthorityIdentifier())) {
                    if (!samlPolicy.getAuthorityIdentifier().equals(samlAssertion.getIssuer())) {
                        //log here
                        XWSSecurityException xwse = new XWSSecurityException("Invalid Assertion Issuer, expected "  + 
                            samlPolicy.getAuthorityIdentifier() + ", found " + samlAssertion.getIssuer());
                        throw SecurableSoapMessage.newSOAPFaultException(
                            MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                            "Received SAML Assertion has invalid Issuer",
                                xwse);
                    
                    }
                }
            }

        }else {
             if (context.getMode() == FilterProcessingContext.POSTHOC) {
                 throw new XWSSecurityException(
                     "Internal Error: Called ImportSAMLAssertionFilter in POSTHOC Mode");
             }

             if (context.getMode() == FilterProcessingContext.WSDL_POLICY) {
                 AuthenticationTokenPolicy.SAMLAssertionBinding bind =
                     new AuthenticationTokenPolicy.SAMLAssertionBinding();
                 ((MessagePolicy)context.getInferredSecurityPolicy()).append(bind);
             }
                                                                                                  
            try{
                samlAssertion = AssertionUtil.fromElement(wsseSecurity.getCurrentHeaderElement());
            } catch(Exception ex) {
                throw SecurableSoapMessage.newSOAPFaultException(
                MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                "Exception while importing SAML Assertion",
                ex);
            }
        }

        HashMap tokenCache = context.getTokenCache();
        //assuming unique IDs
        tokenCache.put(samlAssertion.getAssertionID(), samlAssertion);

        //if (!samlAssertion.isTimeValid()) {
        //    log.log(Level.SEVERE, "WSS0417.saml.timestamp.invalid");
        //    throw SecurableSoapMessage.newSOAPFaultException(
        //        MessageConstants.WSSE_FAILED_AUTHENTICATION,
        //        "SAML Condition (notBefore, notOnOrAfter) Validation failed",
        //            new Exception(
        //                "SAML Condition (notBefore, notOnOrAfter) Validation failed"));
        //}

        //ensure it is an SV assertion
        /*String confirmationMethod = AssertionUtil.getConfirmationMethod(samlElement);
        if (!MessageConstants.SAML_SENDER_VOUCHES.equals(confirmationMethod)) {
            XWSSecurityException xwse = new XWSSecurityException("Invalid ConfirmationMethod "  + confirmationMethod);
            throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_INVALID_SECURITY,
                        "Invalid ConfirmationMethod",
                        xwse);
        }*/
        
        context.getSecurityEnvironment().validateSAMLAssertion(context.getExtraneousProperties(), samlElement);
        
        context.getSecurityEnvironment().updateOtherPartySubject(
                DefaultSecurityEnvironmentImpl.getSubject(context), samlAssertion);
        
    }

}
