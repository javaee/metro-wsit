/*
 * $Id: RequestedSecurityTokenImpl.java,v 1.13 2010-04-27 14:20:29 m_potociar Exp $
 */

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

package com.sun.xml.ws.security.trust.impl.elements;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedSecurityTokenType;
import com.sun.xml.ws.security.secconv.WSSCConstants;
import com.sun.xml.ws.security.secconv.impl.elements.SecurityContextTokenImpl;
import com.sun.xml.ws.security.secconv.impl.bindings.SecurityContextTokenType;
import com.sun.xml.ws.security.trust.impl.WSTrustElementFactoryImpl;

import com.sun.istack.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

/**
 * Implementation for the RequestedSecurityToken.
 * 
 * @author Manveen Kaur
 */
public class RequestedSecurityTokenImpl extends RequestedSecurityTokenType implements RequestedSecurityToken {

    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);

    Token containedToken = null;
    
    private final static QName SCT_QNAME = 
            new QName("http://schemas.xmlsoap.org/ws/2005/02/sc", "SecurityContextToken");
    
    //private final static QName SAML11_Assertion_QNAME = 
      //      new QName("urn:oasis:names:tc:SAML:1.0:assertion", "Assertion");
    
    //private final static QName EncryptedData_QNAME = new QName("http://www.w3.org/2001/04/xmlenc#", "EncryptedData");
    
    /**
      * Empty default constructor.
      */
    public RequestedSecurityTokenImpl() {
        //Empty default constructor.
    }
    
    public RequestedSecurityTokenImpl(@NotNull final RequestedSecurityTokenType rdstType){
        final Object rdst = rdstType.getAny();
        if (rdst instanceof JAXBElement){
            final JAXBElement rdstEle = (JAXBElement)rdst; 
            final QName name = rdstEle.getName();
            if(SCT_QNAME.equals(name)){
                final SecurityContextTokenType sctType = (SecurityContextTokenType)rdstEle.getValue();
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
                Element token = (new WSTrustElementFactoryImpl()).toElement(rdstEle);
                containedToken = new GenericToken(token);
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
    public static RequestedSecurityTokenType fromElement(@NotNull final org.w3c.dom.Element element)
        throws WSTrustException {
        try {
            final JAXBContext context =
                WSTrustElementFactory.getContext();
            final javax.xml.bind.Unmarshaller unmarshaller = context.createUnmarshaller();
            
            return unmarshaller.unmarshal(element, RequestedSecurityTokenType.class).getValue();
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0021_ERROR_UNMARSHAL_DOM_ELEMENT(), ex);                        
            throw new WSTrustException(LogStringsMessages.WST_0021_ERROR_UNMARSHAL_DOM_ELEMENT(), ex);
        }
    }

    /*
     * Return the security token contained in the RequestedSecurityToken.
     */
    public Token getToken() {
        return containedToken;
    }
    
    public final void setToken(final Token token) {
        if (token != null)  {
            final String tokenType = token.getType();
            if (WSSCConstants.SECURITY_CONTEXT_TOKEN.equals(tokenType)){
                final JAXBElement<SecurityContextTokenType> sctElement =
                (new com.sun.xml.ws.security.secconv.impl.bindings.ObjectFactory()).createSecurityContextToken((SecurityContextTokenType)token);
                setAny(sctElement);
            }else {
                final Element element = (Element)token.getTokenValue();
                setAny(element);
            }
        }
        containedToken = token;
    }
}
