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
