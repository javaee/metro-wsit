/*
 * $Id: AuthnStatement.java,v 1.1 2010-10-05 12:03:58 m_potociar Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.wss.saml.assertion.saml20.jaxb20;

import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.LogStringsMessages;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.AuthnStatementType;
import com.sun.xml.wss.saml.util.SAML20JAXBUtil;
import com.sun.xml.wss.util.DateUtils;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Level;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.w3c.dom.Element;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

/**
 * The <code>AuthnStatement</code> element supplies a
 * statement by the issuer that its subject was authenticated by a
 * particular means at a particular time. The
 * <code>AuthnStatement</code> element is of type
 * <code>AuthnStatementType</code>, which extends the
 * <code>SubjectStatementAbstractType</code> with the additional element and
 * attributes.
 */
public class AuthnStatement extends AuthnStatementType
        implements com.sun.xml.wss.saml.AuthnStatement {
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);    
    private Date authnInstantDate = null;
    private Date sessionDate = null;
    
    /**
     *Default constructor
     */
    protected AuthnStatement() {
        super();
    }
    
    /**
     * This constructor builds an authentication statement element from an
     * existing XML block.
     *
     * @param element representing a DOM tree element.
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public static AuthnStatementType fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc = SAML20JAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AuthnStatementType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
    
    
//    private void setAuthnContext(AuthnContext authnContext) {
//        this.authnContext = authnContext;
//    }
    
    /**
     * Constructor for authentication statement
     *
     * @param authMethod (optional) A String specifies the type of authentication
     *        that took place.
     * @param authInstant (optional) A GregorianCalendar specifies the time at which the
     *        authentication that took place.
     * @param subject (required) A Subject object
     * @param subjectLocality (optional) A <code>SubjectLocality</code> object.
     * @param authorityBinding (optional) A List of <code>AuthorityBinding</code>
     *        objects.
     * @exception SAMLException if there is an error in the sender.
     */
    public AuthnStatement(
            GregorianCalendar authInstant, SubjectLocality subjectLocality, 
            AuthnContext authnContext, String sessionIndex, GregorianCalendar sessionNotOnOrAfter) {
                
        if ( authInstant != null) {
            try {
                DatatypeFactory factory = DatatypeFactory.newInstance();
                setAuthnInstant(factory.newXMLGregorianCalendar(authInstant));
            }catch ( DatatypeConfigurationException ex ) {
                //ignore
            }
        }
        
        if ( subjectLocality != null)
            setSubjectLocality(subjectLocality);
        
        if ( authnContext != null)
            setAuthnContext(authnContext);
        
        if(sessionIndex != null)
            setSessionIndex(sessionIndex);
        
        if ( sessionNotOnOrAfter != null) {
            try {
                DatatypeFactory factory = DatatypeFactory.newInstance();
                setSessionNotOnOrAfter(factory.newXMLGregorianCalendar(sessionNotOnOrAfter));
            }catch ( DatatypeConfigurationException ex ) {
                //ignore
            }
        }
    }
    
    public AuthnStatement(AuthnStatementType authStmtType) {
        setAuthnInstant(authStmtType.getAuthnInstant());
        setAuthnContext(authStmtType.getAuthnContext());
        setSubjectLocality(authStmtType.getSubjectLocality());        
        setSessionIndex(authStmtType.getSessionIndex());
        setSessionNotOnOrAfter(authStmtType.getSessionNotOnOrAfter());
    }

    public Date getAuthnInstantDate() {
        if(authnInstantDate != null){
            return authnInstantDate;
        } 
        try {
            if(super.getAuthnInstant() != null){
                authnInstantDate = DateUtils.stringToDate(super.getAuthnInstant().toString());
            }
        } catch (ParseException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0429_SAML_AUTH_INSTANT_OR_SESSION_PARSE_FAILED(), ex);
        }
        return authnInstantDate;
    }

    public Date getSessionNotOnOrAfterDate() {
        if(sessionDate != null){
            return sessionDate;
        }
        try {
            if(super.getSessionNotOnOrAfter() != null){
                sessionDate = DateUtils.stringToDate(super.getSessionNotOnOrAfter().toString());
            }
        } catch (ParseException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0429_SAML_AUTH_INSTANT_OR_SESSION_PARSE_FAILED(), ex);
        }
        return sessionDate;
    }

    public String getSubjectLocalityAddress() {
        if(super.getSubjectLocality() != null){
            return super.getSubjectLocality().getAddress();
        }
        return null;
    }

    public String getSubjectLocalityDNSName() {
        if(super.getSubjectLocality() != null){
            return super.getSubjectLocality().getDNSName();
        }
        return null;
    }

    public String getAuthnContextClassRef() {
        Iterator it = super.getAuthnContext().getContent().iterator();        
        while(it.hasNext()){
            Object obj = it.next();
            if(obj instanceof JAXBElement){
                JAXBElement element = (JAXBElement)obj;
                if(element.getName().getLocalPart().equalsIgnoreCase("AuthnContextClassRef")){                    
                    return element.getValue().toString();
                }
            }
        }
        return null;
    }

    public String getAuthenticatingAuthority() {
        Iterator it = super.getAuthnContext().getContent().iterator();        
        while(it.hasNext()){
            Object obj = it.next();
            if(obj instanceof JAXBElement){
                JAXBElement element = (JAXBElement)obj;
                if(element.getName().getLocalPart().equalsIgnoreCase("AuthenticatingAuthority")){
                    return element.getValue().toString();
                }
            }
        }
        return null;
    }
    
    @Override
    public String getSessionIndex(){
        if(super.getSessionIndex() != null){
            return super.getSessionIndex();
        }
        return null;
    }
}