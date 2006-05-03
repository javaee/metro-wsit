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

/*
 * SignatureConfirmationFilter.java
 *
 * Created on January 24, 2006, 6:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.filter;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.wss.XWSSecurityException;

import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.core.SignatureConfirmationHeaderBlock;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.policy.mls.SignatureConfirmationPolicy;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Process SignatureConfirmation: Add SignatureConfirmation or verify
 * received SignatureConfirmation
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class SignatureConfirmationFilter {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.FILTER_DOMAIN,
            LogDomainConstants.FILTER_DOMAIN_BUNDLE);
    
    public static void process(FilterProcessingContext context) throws XWSSecurityException{
        
        if(!context.isInboundMessage()){
            //The message is outgoing message
            //Check for the property receivedSignValues in context.Extraneous properties
            //If it is not null, add a SignatureConfirmation Header for each of the values in the property
            
            List scList = (ArrayList)context.getExtraneousProperty("receivedSignValues");
            SignatureConfirmationPolicy policy = (SignatureConfirmationPolicy)context.getSecurityPolicy();
            
            SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
            SecurityHeader secHeader = secureMessage.findOrCreateSecurityHeader();
            
            if(scList != null){
                Iterator it = scList.iterator();
                
                if(!it.hasNext()){
                    // Insert a SignatureConfirmation element with no Value attribute
                     SignatureConfirmationHeaderBlock signConfirm = new SignatureConfirmationHeaderBlock(
                           secureMessage.generateId());
                     secHeader.insertHeaderBlock(signConfirm);
                }
                
                while(it.hasNext()){
                    
                    String signValue = (String)it.next();             
                    SignatureConfirmationHeaderBlock signConfirm = new SignatureConfirmationHeaderBlock(
                            secureMessage.generateId());
                    signConfirm.setSignatureValue(signValue);
                    secHeader.insertHeaderBlock(signConfirm);
                }
            
            }
            
            
        } else {
            // The message is incoming message
            // Take out all the SignatureConfirmation security headers, and check if each of the values is present
            // in the SignatureConfirmation property of context.Extraneous properties
            // Also make sure that all the values in SignatureConfirmation are exhausted

            //SignatureConfirmationPolicy policy = (SignatureConfirmationPolicy)context.getSecurityPolicy();
            
            SecurityHeader secHeader = context.getSecurableSoapMessage().findSecurityHeader();
            if(secHeader == null){
                //log
                throw new XWSSecurityException(
                        "Message does not confirm to SignatureConfirmation Policy:" + 
                        "wsse11:SignatureConfirmation element not found in Header");
            }
            
            Object temp = context.getExtraneousProperty("SignatureConfirmation");
            List scList = null;
            if(temp != null && temp instanceof ArrayList)
                scList = (ArrayList)temp;
            if(scList != null){
            
                SignatureConfirmationHeaderBlock signConfirm = null;
                SOAPElement sc = null;
                try{
                    SOAPFactory factory = SOAPFactory.newInstance();
                    Name name = factory.createName(
                            MessageConstants.SIGNATURE_CONFIRMATION_LNAME,
                            MessageConstants.WSSE11_PREFIX,
                            MessageConstants.WSSE11_NS);
                    Iterator i = secHeader.getChildElements(name);
                    if(!i.hasNext()){
                        throw new XWSSecurityException("Message does not confirm to Security Policy:" + 
                                "wss11:SignatureConfirmation Element not found");
                    }
                    while(i.hasNext()){
                        sc = (SOAPElement)i.next();
                        try{
                            signConfirm = new SignatureConfirmationHeaderBlock(sc);
                        } catch( XWSSecurityException xwsse){
                            throw SecurableSoapMessage.newSOAPFaultException(
                                MessageConstants.WSSE_INVALID_SECURITY,
                                "Failure in SignatureConfirmation validation\n" + 
                                "Message is: " + xwsse.getMessage(),
                                xwsse );
                        }
                        String signValue = signConfirm.getSignatureValue();

                        //Case when there was no Signature in sent message, the received message should have one
                        //SignatureConfirmation with no Value attribute
                        if(signValue == null){
                            if(i.hasNext() || !scList.isEmpty()){                            
                                log.log(Level.SEVERE, "Failure in SignatureConfirmation Validation");
                                throw new XWSSecurityException("Failure in SignatureConfirmation Validation");
                            }
                        } else if(scList.contains(signValue)){ // match the Value in received message
                            //with the stored value
                            scList.remove(signValue);
                        }else{
                            log.log(Level.SEVERE, "Failure in SignatureConfirmation Validation");
                            throw new XWSSecurityException("Mismatch in SignatureConfirmation Element");
                        }
                    }
                
                } catch(SOAPException se){
                    throw new XWSSecurityException(se);
                }
                if(!scList.isEmpty()){
                    log.log(Level.SEVERE, "Failure in SignatureConfirmation Validation");
                    throw new XWSSecurityException("Failure in SignatureConfirmation");
                }
                context.setExtraneousProperty("SignatureConfirmation", MessageConstants._EMPTY);
                /*if (context.getMode() == FilterProcessingContext.WSDL_POLICY) {
                    SignatureConfirmationPolicy policy = new SignatureConfirmationPolicy();
                    context.getInferredSecurityPolicy().append(policy);
                }*/
            }        
        }
    }
    
}
