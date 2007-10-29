/*
 * $Id: RequestedSecurityTokenImpl.java,v 1.2 2007-10-29 23:04:29 jdg6688 Exp $
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

package com.sun.xml.ws.security.trust.impl.wssx.elements;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

//import com.sun.xml.security.core.xenc.EncryptedDataType;

import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestedSecurityTokenType;
import com.sun.xml.ws.security.secconv.WSSCConstants;
import com.sun.xml.ws.security.secconv.impl.wssx.elements.SecurityContextTokenImpl;
import com.sun.xml.ws.security.secconv.impl.wssx.bindings.SecurityContextTokenType;
import com.sun.xml.ws.security.trust.WSTrustVersion;

import com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AssertionType;

/**
 * Implementation for the RequestedSecurityToken.
 * 
 * @author Manveen Kaur
 */
public class RequestedSecurityTokenImpl extends RequestedSecurityTokenType implements RequestedSecurityToken {

    Token containedToken = null;
    
    private final static QName SecurityContextToken_QNAME = 
            new QName("http://docs.oasis-open.org/ws-sx/ws-secureconversation/200512", "SecurityContextToken");
    
    private final static QName SAML11_Assertion_QNAME = 
            new QName("urn:oasis:names:tc:SAML:1.0:assertion", "Assertion");
    
    private final static QName EncryptedData_QNAME = new QName("http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
    
    /**
      * Empty default constructor.
      */
    public RequestedSecurityTokenImpl() {
    }
    
    public RequestedSecurityTokenImpl(RequestedSecurityTokenType rdstType){
        Object rdst = rdstType.getAny();
        if (rdst instanceof JAXBElement){
            JAXBElement rdstEle = (JAXBElement)rdst; 
            QName name = rdstEle.getName();
            if(SecurityContextToken_QNAME.equals(name)){
                SecurityContextTokenType sctType = (SecurityContextTokenType)rdstEle.getValue();
                setToken(new SecurityContextTokenImpl(sctType));
            }/*else if(EncryptedData_QNAME.equals(name)){
               EncryptedDataType edType = (EncryptedDataType)rdstEle.getValue();
               setToken(edType);  
            }else if(SAML11_Assertion_QNAME.equals(name)){
                AssertionType assertionType = (AssertionType)rdstEle.getValue();
                setToken(new Assertion(assertionType));
            }*/
            else{
                setAny(rdstEle);
                containedToken = new GenericToken((Element)rdstEle.getValue());
            }
        }
        else{
            setToken(new GenericToken((Element)rdst));
        }
    }

    public RequestedSecurityTokenImpl(Token token) {
        setToken(token);
    }
    
    /**
     * Constructs a <code>RequestedSecurityToken</code> element from
     * an existing XML block.
     *
     * @param requestedSecurityTokenElement A
     *        <code>org.w3c.dom.Element</code> representing DOM tree
     *        for <code>RequestedSecurityToken</code> object.
     * @exception WSTrustException if it could not process the
     *            <code>org.w3c.dom.Element</code> properly, implying that
     *            there is an error in the sender or in the element definition.
     */
    public static RequestedSecurityTokenType fromElement(org.w3c.dom.Element element)
        throws WSTrustException {
        try {
            final JAXBContext context =
                WSTrustElementFactory.getContext(WSTrustVersion.WS_TRUST_13);
            final javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();
            
            return unmarshaller.unmarshal(element, RequestedSecurityTokenType.class).getValue();
        } catch ( Exception ex) {
            throw new WSTrustException(ex.getMessage(), ex);
        }
    }

    /*
     * Return the security token contained in the RequestedSecurityToken.
     */
    public Token getToken() {
        return containedToken;
    }
    
    public void setToken(Token token) {
        if (token != null)  {
            String tokenType = token.getType();
            if (WSSCConstants.SECURITY_CONTEXT_TOKEN.equals(tokenType)){
                JAXBElement<SecurityContextTokenType> sctElement =
                (new com.sun.xml.ws.security.secconv.impl.wssx.bindings.ObjectFactory()).createSecurityContextToken((SecurityContextTokenType)token);
                setAny(sctElement);
            }else {
                Element element = (Element)token.getTokenValue();
                setAny(element);
            }
        }
        containedToken = token;
    }
}
