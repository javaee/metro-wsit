/*
 * $Id: SecurityTokenReferenceImpl.java,v 1.4 2007-01-15 10:29:53 raharsha Exp $
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

import com.sun.xml.ws.security.secext10.KeyIdentifierType;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import com.sun.xml.ws.security.secext10.ObjectFactory;
import com.sun.xml.ws.security.secext10.ReferenceType;
import com.sun.xml.ws.security.trust.elements.str.Reference;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;


import java.util.List;
import javax.xml.bind.JAXBElement;

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
    {
        final Reference ref = getReference(strType);
        setReference(ref);
    }
    
    public final void setReference(final Reference ref){
        
        JAXBElement rElement = null;
        final String type = ref.getType();
        final ObjectFactory objFac = new ObjectFactory();
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
    
    private Reference getReference(final SecurityTokenReferenceType strType){
        final List<Object> list = strType.getAny();
        final JAXBElement obj = (JAXBElement)list.get(0);
        final String local = obj.getName().getLocalPart();
        //final Reference ref = null;
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
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document doc = builder.newDocument();
            
            final javax.xml.bind.Marshaller marshaller = WSTrustElementFactory.getContext().createMarshaller();
            final JAXBElement<SecurityTokenReferenceType> rstElement =  (new ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)this);
            marshaller.marshal(rstElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
}
