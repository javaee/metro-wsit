/*
 * $Id: AuthenticationStatement.java,v 1.4 2010-09-06 08:39:54 sm228678 Exp $
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

import com.sun.xml.wss.saml.AuthorityBinding;
import com.sun.xml.wss.saml.SAMLException;


import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AuthenticationStatementType;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AuthorityBindingType;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;
import com.sun.xml.wss.util.DateUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Level;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.w3c.dom.Element;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;

/**
 * The <code>AuthenticationStatement</code> element supplies a
 * statement by the issuer that its subject was authenticated by a
 * particular means at a particular time. The
 * <code>AuthenticationStatement</code> element is of type
 * <code>AuthenticationStatementType</code>, which extends the
 * <code>SubjectStatementAbstractType</code> with the additional element and
 * attributes.
 */
public class AuthenticationStatement extends AuthenticationStatementType
        implements com.sun.xml.wss.saml.AuthenticationStatement {
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    private List<AuthorityBinding> authorityBindingList = null;
    private Date instantDate = null;
    /**
     *Default constructor
     */
    protected AuthenticationStatement() {
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
    public static AuthenticationStatementType fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc = SAMLJAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AuthenticationStatementType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void setAuthorityBinding(List authorityBinding) {
        this.authorityBinding = authorityBinding;
    }
    
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
    public AuthenticationStatement(
            String authMethod, GregorianCalendar authInstant, Subject subject,
            SubjectLocality subjectLocality, List authorityBinding) {
        
        if ( authMethod != null)
            setAuthenticationMethod(authMethod);
        
        if ( authInstant != null) {
            try {
                DatatypeFactory factory = DatatypeFactory.newInstance();
                setAuthenticationInstant(factory.newXMLGregorianCalendar(authInstant));
            }catch ( DatatypeConfigurationException ex ) {
                //ignore
            }
        }
        
        if ( subject != null)
            setSubject(subject);
        
        if ( subjectLocality != null)
            setSubjectLocality(subjectLocality);
        
        if ( authorityBinding != null)
            setAuthorityBinding(authorityBinding);
    }
    
    public AuthenticationStatement(AuthenticationStatementType authStmtType) {
        setAuthenticationMethod(authStmtType.getAuthenticationMethod());
        setAuthenticationInstant(authStmtType.getAuthenticationInstant());
        if(authStmtType.getSubject() != null){
            Subject subj = new Subject(authStmtType.getSubject());
            setSubject(subj);
        }
        setSubjectLocality(authStmtType.getSubjectLocality());
        setAuthorityBinding(authStmtType.getAuthorityBinding());
    }
    
    public Date getAuthenticationInstantDate(){
        if(instantDate != null){
            return instantDate;
        }        
        try {
            if(super.getAuthenticationInstant() != null){
                instantDate = DateUtils.stringToDate(super.getAuthenticationInstant().toString());
            }
        } catch (ParseException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        return instantDate;
    }

    
    @Override
    public String getAuthenticationMethod(){
        return super.getAuthenticationMethod();
    }
    
    public List<AuthorityBinding> getAuthorityBindingList(){
        if(authorityBindingList != null){
            authorityBindingList = new ArrayList<AuthorityBinding>();
        }else{
            return authorityBindingList;
        }
        Iterator it = super.getAuthorityBinding().iterator();
        while(it.hasNext()){
            com.sun.xml.wss.saml.assertion.saml11.jaxb20.AuthorityBinding obj = 
                    new com.sun.xml.wss.saml.assertion.saml11.jaxb20.AuthorityBinding((AuthorityBindingType)it.next());
            authorityBindingList.add(obj);            
        }
        return authorityBindingList;
    }        

    public String getSubjectLocalityIPAddress() {
        if(super.getSubjectLocality() != null){
            return super.getSubjectLocality().getIPAddress();
        }
        return null;
    }

    public String getSubjectLocalityDNSAddress() {
        if(super.getSubjectLocality() != null){
            return super.getSubjectLocality().getDNSAddress();
        }
        return null;
    }

    public Subject getSubject() {
        return (Subject)super.getSubject();
    }
}
