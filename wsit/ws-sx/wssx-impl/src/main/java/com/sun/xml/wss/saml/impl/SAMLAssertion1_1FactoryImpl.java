/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * SAMLAssertion1_1FactoryImpl.java
 *
 * Created on August 18, 2005, 12:34 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.saml.impl;


import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Element;
import com.sun.xml.wss.saml.*;
import javax.xml.bind.JAXBContext;



/**
 *
 * @author root
 */
public class SAMLAssertion1_1FactoryImpl extends SAMLAssertionFactory {
    
    
    /** Creates a new instance of SAMLAssertion1_1FactoryImpl */
    public SAMLAssertion1_1FactoryImpl() {
    }
    
    public Action createAction(Element actionElement) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Action(actionElement);
    }
    
    public Action createAction(String action, String namespace) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Action(action, namespace);
    }
    
    public Advice createAdvice(List assertionidreference, List assertion, List otherelement) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Advice(assertionidreference, assertion, otherelement);
    }
    
    public AnyType createAnyType() throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AnyType();
    }
    
    public Assertion createAssertion(org.w3c.dom.Element element) throws SAMLException {
        return com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion.fromElement(element);
    }
    
    public Assertion createAssertion(
            String assertionID,
            java.lang.String issuer,
            GregorianCalendar issueInstant,
            Conditions conditions,
            Advice advice,
            List statements) throws SAMLException {
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion(
                assertionID, issuer, issueInstant,
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Conditions)conditions,
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Advice)advice,
                statements);
    }
    public Assertion createAssertion(
            String assertionID,
            java.lang.String issuer,
            GregorianCalendar issueInstant,
            Conditions conditions,
            Advice advice,
            List statements,JAXBContext jcc) throws SAMLException {
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion(
                assertionID, issuer, issueInstant,
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Conditions)conditions,
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Advice)advice,
                statements,jcc);
    }
    
    public Assertion createAssertion(
            String ID,
            NameID issuer,
            GregorianCalendar issueInstant,
            Conditions conditions,
            Advice advice,
            Subject subject,
            List statements) throws SAMLException {
        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    public Assertion createAssertion(
            String ID,
            NameID issuer,
            GregorianCalendar issueInstant,
            Conditions conditions,
            Advice advice,
            Subject subject,
            List statements,JAXBContext jcc) throws SAMLException {
        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public AssertionIDReference createAssertionIDReference() throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AssertionIDReference();
    }
    
    public AssertionIDRef createAssertionIDRef() throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public AssertionIDReference createAssertionIDReference(String id) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AssertionIDReference(id);
    }
    
    public AssertionIDRef createAssertionIDRef(String id) throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public Attribute createAttribute(String name, String nameSpace, List values) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Attribute(name, nameSpace, values);
    }
    
    public Attribute createAttribute(String name, List values) throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public AttributeDesignator createAttributeDesignator(String name, String nameSpace) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AttributeDesignator( name, nameSpace);
    }
    
    public AttributeStatement createAttributeStatement(Subject subj, List attr) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AttributeStatement(
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Subject)subj, attr);
    }
    
    public AttributeStatement createAttributeStatement(List attr) throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public AudienceRestrictionCondition createAudienceRestrictionCondition(List audience) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AudienceRestrictionCondition(audience);
    }
    
    public AudienceRestriction createAudienceRestriction(List audience) throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public AuthenticationStatement createAuthenticationStatement(
            String authMethod, GregorianCalendar authInstant, Subject subject,
            SubjectLocality subjectLocality, List authorityBinding) throws SAMLException{
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AuthenticationStatement(
                authMethod,
                authInstant,
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Subject)subject,
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectLocality)subjectLocality,
                authorityBinding);
    }
    
    public AuthnStatement createAuthnStatement(
            GregorianCalendar authInstant, SubjectLocality subjectLocality, AuthnContext authnContext, 
            String sessionIndex, GregorianCalendar sessionNotOnOrAfter) throws SAMLException{
        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public AuthorityBinding createAuthorityBinding(QName authKind, String location, String binding) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AuthorityBinding(
                authKind, location, binding);
    }
    
    public AuthnContext createAuthnContext() throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public AuthnContext createAuthnContext(String authContextClassref, String authenticatingAuthority) throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
     
    public AuthorizationDecisionStatement createAuthorizationDecisionStatement(
            Subject subject, String resource, String decision, List action, Evidence evidence) throws SAMLException{
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AuthorizationDecisionStatement(
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Subject)subject,
                resource,
                decision,
                action,
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Evidence)evidence
                );
    }
    
    public AuthnDecisionStatement createAuthnDecisionStatement(
            String resource, String decision, List action, Evidence evidence) throws SAMLException{
        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public Conditions createConditions() throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Conditions();
    }
    
    public Conditions createConditions(
            GregorianCalendar notBefore,
            GregorianCalendar notOnOrAfter,
            List condition,
            List arc,
            List doNotCacheCnd) throws SAMLException{
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Conditions(
                notBefore, notOnOrAfter, condition, arc, doNotCacheCnd);
    }
    
    public Conditions createConditions(
            GregorianCalendar notBefore,
            GregorianCalendar notOnOrAfter,
            List condition,
            List ar,
            List oneTimeUse,
            List proxyRestriction) throws SAMLException{
        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public DoNotCacheCondition createDoNotCacheCondition() throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.DoNotCacheCondition();
    }
    
    public OneTimeUse createOneTimeUse() throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public Evidence createEvidence(List assertionIDRef, List assertion) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Evidence( assertionIDRef, assertion);
    }
    
    public NameIdentifier createNameIdentifier(String name, String nameQualifier, String format) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.NameIdentifier( name, nameQualifier, format);
    }
    
    public NameID createNameID(String name, String nameQualifier, String format) throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public Subject createSubject(NameIdentifier nameIdentifier, SubjectConfirmation subjectConfirmation) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Subject(
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.NameIdentifier)nameIdentifier,
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectConfirmation)subjectConfirmation);
    }
    
    public Subject createSubject(NameID nameID, SubjectConfirmation subjectConfirmation) throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public SubjectConfirmation createSubjectConfirmation(String confirmationMethod) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectConfirmation(confirmationMethod);
    }
    
    public SubjectConfirmation createSubjectConfirmation(NameID nameID, String method) throws SAMLException{
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public SubjectConfirmation createSubjectConfirmation(
            List confirmationMethods, Element subjectConfirmationData,
            Element keyInfo) throws SAMLException {
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectConfirmation(confirmationMethods, subjectConfirmationData, keyInfo);
    }
               
     public SubjectConfirmation createSubjectConfirmation(
            List confirmationMethods,SubjectConfirmationData scd,KeyInfo keyInfo) throws SAMLException {
       com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectConfirmation sc = new com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectConfirmation();
        
//        try {
//            if ( keyInfo != null) {
//                sc.setKeyInfo(keyInfo);
//            }
//            if ( scd != null) {
//                sc.setSubjectConfirmationData(( com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectConfirmationDataImpl)scd);
//            }
//        } catch (Exception ex) {
//            // log here
//            throw new SAMLException(ex);
//        }
//        sc.setConfirmationMethod(confirmationMethods);
        return sc;
    }
    public SubjectConfirmation createSubjectConfirmation(
            NameID nameID, SubjectConfirmationData subjectConfirmationData,
            String confirmationMethod) throws SAMLException {
        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
     public SubjectConfirmation createSubjectConfirmation(
            NameID nameID, KeyInfoConfirmationData subjectConfirmationData,
            String confirmationMethod) throws SAMLException {
        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public SubjectConfirmationData createSubjectConfirmationData(
            String address, String inResponseTo, GregorianCalendar notBefore,
            GregorianCalendar notOnOrAfter, String recipient, Element keyInfo) throws SAMLException{
        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public SubjectConfirmationData createSubjectConfirmationData(
            String address, String inResponseTo, GregorianCalendar notBefore,
            GregorianCalendar notOnOrAfter, String recipient, KeyInfo keyInfo) throws SAMLException{        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public KeyInfoConfirmationData createKeyInfoConfirmationData(Element keyInfo) throws SAMLException{
        
        throw new UnsupportedOperationException("Not Supported for SAML1.0");
    }
    
    public SubjectLocality createSubjectLocality() throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectLocality();
    }
    
    public SubjectLocality createSubjectLocality(String ipAddress, String dnsAddress) throws SAMLException{
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectLocality(ipAddress, dnsAddress);
    }

    public Assertion createAssertion(XMLStreamReader reader) throws SAMLException {
        throw new UnsupportedOperationException("Not Yet Supported");
    }    
}
