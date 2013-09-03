
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.AssertionUtil;

import java.util.Iterator;
import java.util.HashMap;
import java.util.logging.Level;
import javax.xml.soap.SOAPElement;
import org.w3c.dom.Element;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;

import java.util.logging.Logger;
import org.w3c.dom.NodeList;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import org.w3c.dom.Text;

/**
 * @author Kumar Jayanti
 */
public class ImportSamlAssertionFilter{

    protected static final Logger log =  Logger.getLogger( LogDomainConstants.FILTER_DOMAIN,LogDomainConstants.FILTER_DOMAIN_BUNDLE);

    public static void process(FilterProcessingContext context)	throws XWSSecurityException {

        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        SecurityHeader wsseSecurity = secureMessage.findSecurityHeader();
        Assertion samlAssertion = null;
        SOAPElement samlElement = null;

        if( context.getMode() == FilterProcessingContext.ADHOC ||
            context.getMode() == FilterProcessingContext.DEFAULT ||
            context.getMode() == FilterProcessingContext.WSDL_POLICY) {

            NodeList nl = null;
            Element elem = null;

            for (Iterator iter = wsseSecurity.getChildElements(); iter.hasNext();) {
                Object obj = iter.next();
                /*if(obj instanceof Text){
                continue;
                }*/
                if(obj instanceof Text){
                continue;
                }
                if (obj instanceof Element) {
                    elem = (Element) obj;
                    if (elem.getAttributeNode("ID") != null) {
                        nl = wsseSecurity.getElementsByTagNameNS(
                                MessageConstants.SAML_v2_0_NS, MessageConstants.SAML_ASSERTION_LNAME);
                        break;
                    } else if (elem.getAttributeNode("AssertionID") != null) {
                        nl = wsseSecurity.getElementsByTagNameNS(
                                MessageConstants.SAML_v1_0_NS, MessageConstants.SAML_ASSERTION_LNAME);
                        break;
                    }
                }
            }
//            if (wsseSecurity.getChildElements()Attributes().equals("AssertionID")){
//                nl = wsseSecurity.getElementsByTagNameNS(
//                        MessageConstants.SAML_v1_0_NS, MessageConstants.SAML_ASSERTION_LNAME);
//            }else{
//                nl = wsseSecurity.getElementsByTagNameNS(
//                        MessageConstants.SAML_v2_0_NS, MessageConstants.SAML_ASSERTION_LNAME);
//            }

            if (nl == null){
                throw new XWSSecurityException("SAMLAssertion is null");
            }
            int nodeListLength = nl.getLength();
            int countSamlInsideAdviceElement = 0;
            for(int i =0; i<nodeListLength; i++){
                if(nl.item(i).getParentNode().getLocalName().equals("Advice")){
                    countSamlInsideAdviceElement++;
                }
            }

            //for now we dont allow multiple saml assertions
            if (nodeListLength == 0) {
                // Log Message
                throw new XWSSecurityException(
                "No SAML Assertion found, Reciever requirement not met");
            //}else if ((nodeListLength - countSamlInsideAdviceElement) > 1) {
            //    throw new XWSSecurityException(
            //        "More than one SAML Assertion found, Reciever requirement not met");
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
                    if (!samlPolicy.getAuthorityIdentifier().equals(samlAssertion.getSamlIssuer())) {
                        //log here
                        XWSSecurityException xwse = new XWSSecurityException("Invalid Assertion Issuer, expected "  +
                            samlPolicy.getAuthorityIdentifier() + ", found " + (samlAssertion.getSamlIssuer()));
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

        AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy = new AuthenticationTokenPolicy.SAMLAssertionBinding();
        samlPolicy.setUUID(samlAssertion.getAssertionID());
        context.getInferredSecurityPolicy().append(samlPolicy);
    }

}
