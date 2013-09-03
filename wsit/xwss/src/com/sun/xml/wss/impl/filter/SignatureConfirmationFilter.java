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

/*
 * SignatureConfirmationFilter.java
 *
 * Created on January 24, 2006, 6:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.filter;

import com.sun.xml.ws.security.opt.impl.tokens.SignatureConfirmation;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
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
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;

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
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.FILTER_DOMAIN,
            LogDomainConstants.FILTER_DOMAIN_BUNDLE);
    
    public static void process(FilterProcessingContext context) throws XWSSecurityException{
        
        if(!context.isInboundMessage()){
            //The message is outgoing message
            //Check for the property receivedSignValues in context.Extraneous properties
            //If it is not null, add a SignatureConfirmation Header for each of the values in the property
            
            List scList = (ArrayList)context.getExtraneousProperty("receivedSignValues");
            //SignatureConfirmationPolicy policy = (SignatureConfirmationPolicy)context.getSecurityPolicy();
            
            setSignConfirmValues(context, scList);
            
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

    private static void setSignConfirmValues(com.sun.xml.wss.impl.FilterProcessingContext context, List scList) 
            throws XWSSecurityException{
        if(scList != null){
            Iterator it = scList.iterator();
            if(context instanceof JAXBFilterProcessingContext){
                JAXBFilterProcessingContext optContext = (JAXBFilterProcessingContext)context;
                com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader secHeader = 
                        optContext.getSecurityHeader();
                ((NamespaceContextEx)optContext.getNamespaceContext()).addWSS11NS();
                if(!it.hasNext()){
                    // Insert a SignatureConfirmation element with no Value attribute
                    String id = optContext.generateID();
                    SignatureConfirmation scHeader = new SignatureConfirmation(id, optContext.getSOAPVersion());
                    secHeader.add(scHeader);
                    optContext.getSignatureConfirmationIds().add(id);
                }
                
                while(it.hasNext()){
                    byte[] signValue = (byte[])it.next();
                    String id = optContext.generateID();
                    SignatureConfirmation scHeader = new SignatureConfirmation(id, optContext.getSOAPVersion());
                    scHeader.setValue(signValue);
                    secHeader.add(scHeader);
                    optContext.getSignatureConfirmationIds().add(id);
                }
            } else{
                SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
                SecurityHeader secHeader = secureMessage.findOrCreateSecurityHeader();

                if(!it.hasNext()){
                    // Insert a SignatureConfirmation element with no Value attribute
                    String id = secureMessage.generateId();
                    SignatureConfirmationHeaderBlock signConfirm = new SignatureConfirmationHeaderBlock(id);
                    secHeader.insertHeaderBlock(signConfirm);
                    context.getSignatureConfirmationIds().add(id);
                }

                while(it.hasNext()){

                    String signValue = (String)it.next();     
                    String id = secureMessage.generateId();
                    SignatureConfirmationHeaderBlock signConfirm = new SignatureConfirmationHeaderBlock(id);
                    signConfirm.setSignatureValue(signValue);
                    secHeader.insertHeaderBlock(signConfirm);
                    context.getSignatureConfirmationIds().add(id);
                }         
            }
        }
    }
    
}
