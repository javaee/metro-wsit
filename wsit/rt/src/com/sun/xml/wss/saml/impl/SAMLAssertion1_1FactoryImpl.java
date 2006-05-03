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


import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import com.sun.xml.wss.saml.*;



/**
 *
 * @author root
 */
public class SAMLAssertion1_1FactoryImpl extends SAMLAssertionFactory {
    
    
    /** Creates a new instance of SAMLAssertion1_1FactoryImpl */
    public SAMLAssertion1_1FactoryImpl() {
    }
    
    public Action createAction(Element actionElement) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Action(actionElement);
    }
    
    public Action createAction(String action, String namespace) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Action(action, namespace);
    }
    
    public Advice createAdvice(List assertionidreference, List assertion, List otherelement) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Advice(assertionidreference, assertion, otherelement);
    }
    
    public AnyType createAnyType() {
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
    
    public AssertionIDReference createAssertionIDReference() {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AssertionIDReference();
    }
    
    public AssertionIDReference createAssertionIDReference(String id) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AssertionIDReference(id);
    }
    
    public Attribute createAttribute(String name, String nameSpace, List values) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Attribute(name, nameSpace, values);
    }
    
    public AttributeDesignator createAttributeDesignator(String name, String nameSpace) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AttributeDesignator( name, nameSpace);
    }
    
    public AttributeStatement createAttributeStatement(Subject subj, List attr) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AttributeStatement(
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Subject)subj, attr);
    }
    
    public AudienceRestrictionCondition createAudienceRestrictionCondition(List audience) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AudienceRestrictionCondition(audience);
    }
    
    public AuthenticationStatement createAuthenticationStatement(
            String authMethod, GregorianCalendar authInstant, Subject subject,
            SubjectLocality subjectLocality, List authorityBinding) {
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AuthenticationStatement(
                authMethod, 
                authInstant,
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Subject)subject, 
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectLocality)subjectLocality, 
                authorityBinding);
    }
    
    public AuthorityBinding createAuthorityBinding(QName authKind, String location, String binding) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AuthorityBinding( 
                authKind, location, binding);
    }
    
    public AuthorizationDecisionStatement createAuthorizationDecisionStatement(
        Subject subject, String resource, String decision, List action, Evidence evidence) {
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.AuthorizationDecisionStatement(
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Subject)subject,
                resource, 
                decision, 
                action, 
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.Evidence)evidence
                );
    }
    
    public Conditions createConditions() {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Conditions();
    }
    
    public Conditions createConditions(
		GregorianCalendar notBefore,
		GregorianCalendar notOnOrAfter,
		List condition,
		List arc,
		List doNotCacheCnd) {
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Conditions(
                notBefore, notOnOrAfter, condition, arc, doNotCacheCnd);
    }
    
    public DoNotCacheCondition createDoNotCacheCondition() {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.DoNotCacheCondition();
    }
    
    public Evidence createEvidence(List assertionIDRef, List assertion) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Evidence( assertionIDRef, assertion);
    }
    
    public NameIdentifier createNameIdentifier(String name, String nameQualifier, String format) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.NameIdentifier( name, nameQualifier, format);
    }
    
    public Subject createSubject(NameIdentifier nameIdentifier, SubjectConfirmation subjectConfirmation) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Subject(
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.NameIdentifier)nameIdentifier, 
                (com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectConfirmation)subjectConfirmation);
    }
    
    public SubjectConfirmation createSubjectConfirmation(String confirmationMethod) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectConfirmation(confirmationMethod);
    }
    
    public SubjectConfirmation createSubjectConfirmation(
        List confirmationMethods, Element subjectConfirmationData,
        Element keyInfo) throws SAMLException {
        
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectConfirmation(confirmationMethods, subjectConfirmationData, keyInfo);
    }
    
    public SubjectLocality createSubjectLocality() {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectLocality();
    }
    
    public SubjectLocality createSubjectLocality(String ipAddress, String dnsAddress) {
        return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.SubjectLocality(ipAddress, dnsAddress);
    }
    
}
