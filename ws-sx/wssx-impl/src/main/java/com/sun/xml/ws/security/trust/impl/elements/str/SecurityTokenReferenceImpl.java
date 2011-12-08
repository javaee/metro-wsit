/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * $Id: SecurityTokenReferenceImpl.java,v 1.2 2010-10-21 15:36:57 snajper Exp $
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
        this.getOtherAttributes().putAll(strType.getOtherAttributes());
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

    public void setTokenType(String tokenType){
        getOtherAttributes().put(TOKEN_TYPE, tokenType);
    }

    public String getTokenType(){
       return getOtherAttributes().get(TOKEN_TYPE);
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
