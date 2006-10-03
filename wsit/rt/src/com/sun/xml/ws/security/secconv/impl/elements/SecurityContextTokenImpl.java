/*
 * $Id: SecurityContextTokenImpl.java,v 1.4 2006-10-03 22:46:31 jdg6688 Exp $
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

package com.sun.xml.ws.security.secconv.impl.elements;

import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.secconv.WSSCConstants;
import com.sun.xml.ws.security.secconv.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.secconv.impl.bindings.SecurityContextTokenType;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;

/**
 * SecurityContextToken Implementation
 *
 * @author Manveen Kaur manveen.kaur@sun.com
 */
public class SecurityContextTokenImpl extends SecurityContextTokenType implements SecurityContextToken {
    
    private String instance = null;
    private URI identifier = null;
    private List<Object> extElements = null;
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.WSSC_IMPL_DOMAIN,
            LogDomainConstants.WSSC_IMPL_DOMAIN_BUNDLE);
    
    public SecurityContextTokenImpl() {
        // empty c'tor
    }
    
    public SecurityContextTokenImpl(URI identifier, String instance, String wsuId) {
        if (identifier != null) {
            setIdentifier(identifier);
        }
        if (instance != null) {
            setInstance(instance);
        }
        
        if (wsuId != null){
            setWsuId(wsuId);
        }                
    }
    
    // useful for converting from JAXB to our owm impl class
    public SecurityContextTokenImpl(SecurityContextTokenType sTokenType){
        List<Object> list = sTokenType.getAny();
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            if(object instanceof JAXBElement){
                JAXBElement obj = (JAXBElement)object;
                
                String local = obj.getName().getLocalPart();
                if (local.equalsIgnoreCase("Instance")) {
                    setInstance((String)obj.getValue());
                } else if (local.equalsIgnoreCase("Identifier")){
                    try {
                        setIdentifier(new URI((String)obj.getValue()));
                    }catch (URISyntaxException ex){
                        throw new RuntimeException(ex);
                    }
                }
            }else{
                getAny().add(object);
                if(extElements == null){
                    extElements = new ArrayList<Object>();
                    extElements.add(object);
                }
            }
        }
        
        setWsuId(sTokenType.getId());
    }
    
    public URI getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(URI identifier) {
        this.identifier = identifier;
        JAXBElement<String> iElement =
                (new ObjectFactory()).createIdentifier(identifier.toString());
        getAny().add(iElement);
        log.log(Level.FINE,
                "WSSC1004.secctx.token.id.value",
                new Object[] {identifier.toString()});

    }
    
    public String getInstance() {
        return instance;
    }
    
    public void setInstance(String instance) {
        this.instance = instance;
        JAXBElement<String> iElement =
                (new ObjectFactory()).createInstance(instance);
        getAny().add(iElement);
    }
    
    public void setWsuId(String wsuId){
        log.log(Level.FINE,
                "WSSC1005.secctx.token.wsuid.value",
                new Object[] {wsuId});
        setId(wsuId);        
    }
    
    public String getWsuId(){
        return getId();
    }
    
    public String getType() {
        return WSSCConstants.SECURITY_CONTEXT_TOKEN;
    }
    
    public Object getTokenValue() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            
            javax.xml.bind.Marshaller marshaller = WSTrustElementFactory.getContext().createMarshaller();
            JAXBElement<SecurityContextTokenType> tElement =  (new ObjectFactory()).createSecurityContextToken((SecurityContextTokenType)this);
            marshaller.marshal(tElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    public List getExtElements() {
        return extElements;
    }
}
