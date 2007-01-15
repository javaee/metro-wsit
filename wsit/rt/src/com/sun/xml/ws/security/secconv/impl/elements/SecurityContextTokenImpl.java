/*
 * $Id: SecurityContextTokenImpl.java,v 1.7 2007-01-15 10:29:50 raharsha Exp $
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;
import com.sun.xml.ws.security.secconv.logging.LogStringsMessages;

/**
 * SecurityContextToken Implementation
 *
 * @author Manveen Kaur manveen.kaur@sun.com
 */
public class SecurityContextTokenImpl extends SecurityContextTokenType implements SecurityContextToken {
    
    private String instance = null;
    private URI identifier = null;
    private List<Object> extElements = null;
    
    private static final Logger log =
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
        final List<Object> list = sTokenType.getAny();
        for (int i = 0; i < list.size(); i++) {
            final Object object = list.get(i);
            if(object instanceof JAXBElement){
                final JAXBElement obj = (JAXBElement)object;
                
                final String local = obj.getName().getLocalPart();
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
    
    public final void setIdentifier(final URI identifier) {
        this.identifier = identifier;
        final JAXBElement<String> iElement =
                (new ObjectFactory()).createIdentifier(identifier.toString());
        getAny().add(iElement);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_1004_SECCTX_TOKEN_ID_VALUE(identifier.toString()));
        }
    }
    
    public String getInstance() {
        return instance;
    }
    
    public final void setInstance(final String instance) {
        this.instance = instance;
        final JAXBElement<String> iElement =
                (new ObjectFactory()).createInstance(instance);
        getAny().add(iElement);
    }
    
    public final void setWsuId(final String wsuId){
        setId(wsuId);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_1005_SECCTX_TOKEN_WSUID_VALUE(wsuId));
        }
    }
    
    public String getWsuId(){
        return getId();
    }
    
    public String getType() {
        return WSSCConstants.SECURITY_CONTEXT_TOKEN;
    }
    
    public Object getTokenValue() {
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document doc = builder.newDocument();
            
            final javax.xml.bind.Marshaller marshaller = WSTrustElementFactory.getContext().createMarshaller();
            final JAXBElement<SecurityContextTokenType> tElement =  (new ObjectFactory()).createSecurityContextToken((SecurityContextTokenType)this);
            marshaller.marshal(tElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            log.log(Level.SEVERE, 
                    LogStringsMessages.WSSC_0019_ERR_TOKEN_VALUE(), ex);
            throw new RuntimeException(LogStringsMessages.WSSC_0019_ERR_TOKEN_VALUE(), ex);
        }
    }
    
    public List getExtElements() {
        return extElements;
    }
}
