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
 * SignatureConfirmationHeaderblock.java
 *
 * Created on January 20, 2006, 5:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.core;

import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.impl.MessageConstants;

import org.w3c.dom.Node;
import org.w3c.dom.Attr;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPFactory;

import com.sun.xml.wss.XWSSecurityException;

import java.util.Iterator;

/**
 * wsse11:SignatureConfirmation
 *
 * @author ashutosh.shahi@sun.com
 */
public class SignatureConfirmationHeaderBlock extends SecurityHeaderBlockImpl{
    
    private String signatureValue = null;
    private String wsuId = null;
    
    /** Creates a new instance of SignatureConfirmationHeaderblock */
    public SignatureConfirmationHeaderBlock(String wsuId, String signatureValue) {
        this.wsuId = wsuId;
        this.signatureValue = signatureValue;
    }
    
    public SignatureConfirmationHeaderBlock(SOAPElement element) throws XWSSecurityException {
        
        if(!(MessageConstants.SIGNATURE_CONFIRMATION_LNAME.equals(element.getLocalName()) && 
                XMLUtil.inWsse11NS(element))){
            throw new XWSSecurityException("Invalid SignatureConfirmation Header Block passed");
        }
        
        setSOAPElement(element);
        
        String wsuId = getAttributeNS(MessageConstants.WSU_NS, "Id");
        if (!"".equals(wsuId))
            setId(wsuId);
        
        String signatureValue = getAttribute("Value");
        try{
            if (!"".equals(signatureValue)){
                setSignatureValue(signatureValue);
            }
        } catch(Exception ex){
            throw new XWSSecurityException(ex);
        }
        
        Iterator children = getChildElements();
        Node object = null;
        
        while (children.hasNext()) {
            
            object = (Node)children.next();
            if (object.getNodeType() == Node.ELEMENT_NODE) {
                throw new XWSSecurityException("Child Element Nodes not allowed inside SignatureConfirmation");
            }else if(object.getNodeType() == Node.ATTRIBUTE_NODE){
                Attr attr = (Attr)object; 
                if(!(("Id".equals(attr.getLocalName()) && MessageConstants.WSU_NS.equals(attr.getNamespaceURI())) || 
                        "Value".equals(attr.getLocalName()))){
                    throw new XWSSecurityException("The attribute " + attr.getLocalName() + "not allowed in SignatureConfirmation");
                }
            }
        }
    }
    
    public SignatureConfirmationHeaderBlock(String wsuId){
       
        this.wsuId = wsuId;
    }
    
    public static SecurityHeaderBlock fromSoapElement(SOAPElement element) throws XWSSecurityException{
        return SecurityHeaderBlockImpl.fromSoapElement(element,
                SignatureConfirmationHeaderBlock.class);
    }
    
     public SOAPElement getAsSoapElement() throws XWSSecurityException {
        
        SOAPElement signConfirm;
        
        try {
            SOAPFactory sFactory = getSoapFactory();
            signConfirm = 
                    sFactory.createElement(
                    MessageConstants.SIGNATURE_CONFIRMATION_LNAME, 
                    MessageConstants.WSSE11_PREFIX, 
                    MessageConstants.WSSE11_NS);

            signConfirm.addNamespaceDeclaration(
                    MessageConstants.WSSE11_PREFIX,
                    MessageConstants.WSSE11_NS);
            
            try{
            if(signatureValue != null){
                Name name = sFactory.createName("Value");
                signConfirm.addAttribute(name, signatureValue);
            }
            } catch(Exception ex){
                throw new XWSSecurityException(ex);
            }
            if(wsuId != null){
                Name name = sFactory.createName("Id", MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS);
                signConfirm.addAttribute(name, wsuId);
            }
        } catch (SOAPException se) {
            throw new XWSSecurityException(
                    "There was an error creating Signature Confirmation " +
                    se.getMessage());
        }
        
        setSOAPElement(signConfirm);
        
        return signConfirm;
     } 
 
    public String getId() {
        return this.wsuId;
    }
    
    public void setId(String wsuId) {
        this.wsuId = wsuId;
    }
    
    public String getSignatureValue(){
        return this.signatureValue;
    }
    
    public void setSignatureValue(String signatureValue){
        this.signatureValue = signatureValue;
    }
    
}
