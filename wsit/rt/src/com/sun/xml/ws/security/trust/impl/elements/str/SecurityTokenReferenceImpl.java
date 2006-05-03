/*
 * $Id: SecurityTokenReferenceImpl.java,v 1.1 2006-05-03 22:57:28 arungupta Exp $
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

package com.sun.xml.ws.security.trust.impl.elements.str;

import com.sun.xml.ws.security.impl.bindings.KeyIdentifierType;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.impl.bindings.SecurityTokenReferenceType;
import com.sun.xml.ws.security.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.impl.bindings.ReferenceType;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.trust.elements.str.Reference;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;


import java.util.List;
import javax.xml.bind.JAXBElement;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * SecurityTokenReference implementation
 */
public class SecurityTokenReferenceImpl extends SecurityTokenReferenceType implements SecurityTokenReference {
    
    
    public SecurityTokenReferenceImpl(Reference ref){
        setReference(ref);
    }
    
    public SecurityTokenReferenceImpl(SecurityTokenReferenceType strType)
    throws Exception {
        Reference ref = getReference(strType);
        setReference(ref);
    }
    
    public void setReference(Reference ref){
        
        JAXBElement rElement = null;
        String type = ref.getType();
        ObjectFactory objFac = new ObjectFactory();
        if (KEYIDENTIFIER.equals(type)){
            rElement = objFac.createKeyIdentifier((KeyIdentifierType)ref);
        }
        else if (REFERENCE.equals(type)){
            rElement = objFac.createReference((ReferenceType)ref);
        }else{
            //ToDo
        }
        
        if (rElement != null){
            getAny().clear();
            getAny().add(rElement);
        }
    }
    
    public Reference getReference (){
        return getReference((SecurityTokenReferenceType)this);
    }
    
    private Reference getReference(SecurityTokenReferenceType strType){
        List<Object> list = strType.getAny();
        JAXBElement obj = (JAXBElement)list.get(0);
        String local = obj.getName().getLocalPart();
        Reference ref = null;
        if (REFERENCE.equals(local)) {
            return new DirectReferenceImpl((ReferenceType)obj.getValue());
        }
            
        if (KEYIDENTIFIER.equalsIgnoreCase(local)) {
            return new KeyIdentifierImpl((KeyIdentifierType)obj.getValue());
        }
            
        //ToDo
        return null;       
    }
    
    public String getType() {
        return WSTrustConstants.STR_TYPE;
    }
    
    public Object getTokenValue() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            
            javax.xml.bind.Marshaller marshaller = WSTrustElementFactory.getContext().createMarshaller();
            JAXBElement<SecurityTokenReferenceType> rstElement =  (new ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)this);
            marshaller.marshal(rstElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
}
