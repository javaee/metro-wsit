/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * SAMLAssertionFactory.java
 *
 * Created on August 18, 2005, 11:46 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.saml;

import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.wss.XWSSecurityException;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import com.sun.xml.wss.saml.impl.SAMLAssertion2_1FactoryImpl;
import com.sun.xml.wss.saml.impl.SAMLAssertion2_2FactoryImpl;
import javax.xml.bind.JAXBContext;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author abhijit.das@Sun.com
 */
public abstract class SAMLAssertionFactory {
    
    /**
     * SAML Version 1.1 &amp; SAML Version 2.0
     */
    public static final String SAML1_1 = "Saml1.1";
    public static final String SAML2_0 = "Saml2.0";
    public static String SAML_VER_CHECK = null;
    
    
    protected SAMLAssertionFactory() {
        //do nothing
    }
    
    
    /**
     *
     * Create an instance of SAMLAssertionFactory.
     *
     * @param samlVersion A String representing the saml version. Possible values {SAMLAssertionFactory.SAML1_1} &amp; {SAMLAssertionFactory.SAML2_0}
     */
    public static SAMLAssertionFactory newInstance(String samlVersion) throws XWSSecurityException {
        if ( samlVersion.intern() == SAML1_1) {
            SAML_VER_CHECK = SAML1_1;
            return new SAMLAssertion2_1FactoryImpl();
        } else if (samlVersion.intern() == SAML2_0 && System.getProperty("com.sun.xml.wss.saml.binding.jaxb")== null ){
            SAML_VER_CHECK = SAML2_0;
            return new SAMLAssertion2_2FactoryImpl();
        } else {
            throw new XWSSecurityException("Unsupported SAML Version");
        }
    }
    
    
    /**
     * Creates an <code>Action</code> element.
     * @param namespace The attribute "namespace" of
     *        <code>Action</code> element
     * @param action A String representing an action
     */
    public abstract Action createAction(String action, String namespace) throws SAMLException;
    
    /**
     * Creates an <code>Advice</code> element.
     * @param assertionidreference A List of <code>AssertionIDReference</code>.
     * @param assertion A List of Assertion
     * @param otherelement A List of any element defined as
     */
    public abstract Advice createAdvice(List assertionidreference, List assertion, List otherelement) throws SAMLException;
    
    /**
     * Creates an <code>AnyType</code> element if the System property "com.sun.xml.wss.saml.binding.jaxb"
     * is set. Otherwise returns null.
     */
    public abstract AnyType createAnyType() throws SAMLException;
    
    /**
     * Creates and return an Assertion from the data members: the
     * <code>assertionID</code>, the issuer, time when assertion issued,
     * the conditions when creating a new assertion , <code>Advice</code>
     * applicable to this <code>Assertion</code> and a set of
     * <code>Statement</code>(s) in the assertion.
     *
     * @param assertionID <code>AssertionID</code> object contained within this
     *        <code>Assertion</code> if null its generated internally.
     * @param issuer The issuer of this assertion.
     * @param issueInstant Time instant of the issue. It has type
     *        <code>dateTime</code> which is built in to the W3C XML Schema
     *        Types specification. if null, current time is used.
     * @param conditions <code>Conditions</code> under which the this
     *        <code>Assertion</code> is valid.
     * @param advice <code>Advice</code> applicable for this
     *        <code>Assertion</code>.
     * @param statements List of <code>Statement</code> objects within this
     *         <code>Assertion</code>. It could be of type
     *         <code>AuthenticationStatement</code>,
     *         <code>AuthorizationDecisionStatement</code> and
     *         <code>AttributeStatement</code>. Each Assertion can have
     *         multiple type of statements in it.
     * @exception SAMLException if there is an error in processing input.
     */
    public abstract Assertion createAssertion(
            String assertionID,
            java.lang.String issuer,
            GregorianCalendar issueInstant,
            Conditions conditions,
            Advice advice,
            List statements) throws SAMLException;
    public abstract Assertion createAssertion(
            String assertionID,
            java.lang.String issuer,
            GregorianCalendar issueInstant,
            Conditions conditions,
            Advice advice,
            List statements,JAXBContext jcc) throws SAMLException;
    
    /**
     * Creates and return an Assertion from the data members: the
     * <code>ID</code>, the issuer, time when assertion issued,
     * the conditions when creating a new assertion , <code>Advice</code>
     * applicable to this <code>Assertion</code>, <code>Subject</code>and a set of
     * <code>Statement</code>(s) in the assertion.
     *
     * @param ID <code>ID</code> object contained within this
     *        <code>Assertion</code> if null its generated internally.
     * @param issuer The issuer of this assertion.
     * @param issueInstant Time instant of the issue. It has type
     *        <code>dateTime</code> which is built in to the W3C XML Schema
     *        Types specification. if null, current time is used.
     * @param conditions <code>Conditions</code> under which the this
     *        <code>Assertion</code> is valid.
     * @param advice <code>Advice</code> applicable for this
     *        <code>Assertion</code>.
     * @param subject <code>Subject</code> applicable for this <code>Assertion</code>
     * @param statements List of <code>Statement</code> objects within this
     *         <code>Assertion</code>. It could be of type
     *         <code>AuthnStatement</code>,
     *         <code>AuthzDecisionStatement</code> and
     *         <code>AttributeStatement</code>. Each Assertion can have
     *         multiple type of statements in it.
     * @exception SAMLException if there is an error in processing input.
     */
    public abstract Assertion createAssertion(
            String ID,
            NameID issuer,
            GregorianCalendar issueInstant,
            Conditions conditions,
            Advice advice,
            Subject subject,
            List statements) throws SAMLException;
    /**
     * Creates and return an Assertion from the data members: the
     * <code>ID</code>, the issuer, time when assertion issued,
     * the conditions when creating a new assertion , <code>Advice</code>
     * applicable to this <code>Assertion</code>, <code>Subject</code>, a set of
     * <code>Statement</code>(s) ,and a jaxbcontext for  the assertion.
     *
     * @param ID <code>ID</code> object contained within this
     *        <code>Assertion</code> if null its generated internally.
     * @param issuer The issuer of this assertion.
     * @param issueInstant Time instant of the issue. It has type
     *        <code>dateTime</code> which is built in to the W3C XML Schema
     *        Types specification. if null, current time is used.
     * @param conditions <code>Conditions</code> under which the this
     *        <code>Assertion</code> is valid.
     * @param advice <code>Advice</code> applicable for this
     *        <code>Assertion</code>.
     * @param subject <code>Subject</code> applicable for this <code>Assertion</code>
     * @param statements List of <code>Statement</code> objects within this
     *         <code>Assertion</code>. It could be of type
     *         <code>AuthnStatement</code>,
     *         <code>AuthzDecisionStatement</code> and
     *         <code>AttributeStatement</code>. Each Assertion can have
     *         multiple type of statements in it.
     * @param jcc JAXBContext to be used for marshaling and unmarshalling the assertions.
     * @exception SAMLException if there is an error in processing input.
     */
     
    public abstract Assertion createAssertion(
            String ID,
            NameID issuer,
            GregorianCalendar issueInstant,
            Conditions conditions,
            Advice advice,
            Subject subject,
            List statements,JAXBContext jcc) throws SAMLException;
    
    /**
     * Creates and returns an <code>Assertion</code> object from the given SAML <code>org.w3c.dom.Element</code>.
     *
     * @param element A <code>org.w3c.dom.Element</code> representing
     *        DOM tree for <code>Assertion</code> object
     * @exception SAMLException if it could not process the Element properly,
     *            implying that there is an error in the sender or in the
     *            element definition.
     */
    public abstract Assertion createAssertion(org.w3c.dom.Element element) throws SAMLException;
    
    /**
     * Creates and returns an <code>Assertion</code> object from the given SAML <code>XMLStreamReader</code>.
     *
     * @param reader An <code>XMLStreamReader</code> representing
     *        the tree for an <code>Assertion</code> object
     * @exception SAMLException if it could not process the Element properly,
     *            implying that there is an error in the sender or in the
     *            element definition.
     */
    public abstract Assertion createAssertion(XMLStreamReader reader) throws SAMLException;
    
    /**
     * Creates and returns an <code>AssertionIDReference</code> object. AssertionID
     * will be generated automatically.
     *
     * @return null if the system property "com.sun.xml.wss.saml.binding.jaxb" is not set
     * otherwise returns AssertionIDReference.
     */
    public abstract AssertionIDReference createAssertionIDReference() throws SAMLException;
    
    /**
     * Creates and returns an <code>AssertionIDRef</code> object. AssertionID
     * will be generated automatically.
     *
     * @return null if the system property "com.sun.xml.wss.saml.binding.jaxb" is not set
     * otherwise returns AssertionIDReference.
     */
    public abstract AssertionIDRef createAssertionIDRef() throws SAMLException;
    
    /**
     * Creates and returns an <code>AssertionIDReference</code> object.
     *
     * @param id <code>String</code> of an AssertionID
     *
     * @return null if the system property "com.sun.xml.wss.saml.binding.jaxb" is not set
     * otherwise returns AssertionIDReference.
     */
    public abstract AssertionIDReference createAssertionIDReference(String id) throws SAMLException;
    
    /**
     * Creates and returns an <code>AssertionIDRef</code> object.
     *
     * @param id <code>String</code> of an AssertionID
     *
     * @return null if the system property "com.sun.xml.wss.saml.binding.jaxb" is not set
     * otherwise returns AssertionIDReference.
     */
    public abstract AssertionIDRef createAssertionIDRef(String id) throws SAMLException;
    
    /**
     * Constructs an instance of <code>Attribute</code>.
     *
     * @param name A String representing <code>AttributeName</code> (the name
     *        of the attribute).
     * @param nameSpace A String representing the namespace in which
     *        <code>AttributeName</code> elements are interpreted.
     * @param values A List representing the <code>AttributeValue</code> object.
     */
    public abstract Attribute createAttribute(String name, String nameSpace, List values) throws SAMLException;
    
    /**
     * Constructs an instance of <code>Attribute</code>.
     *
     * @param name A String representing <code>AttributeName</code> (the name
     *        of the attribute).     
     * @param values A List representing the <code>AttributeValue</code> object.
     */
    public abstract Attribute createAttribute(String name, List values) throws SAMLException;
    /**
     * Constructs an instance of <code>AttributeDesignator</code>.
     *
     * @param name the name of the attribute.
     * @param nameSpace the namespace in which <code>AttributeName</code>
     *        elements are interpreted.
     */
    public abstract AttributeDesignator createAttributeDesignator(String name, String nameSpace) throws SAMLException;
    
    
    /**
     *
     * Constructs an instance of <code>AttributeStatement</code>.
     * @param subj SAML Subject
     * @param attr List of attributes
     */
    public abstract AttributeStatement createAttributeStatement(Subject subj, List attr) throws SAMLException;
    
     /**
     *
     * Constructs an instance of <code>AttributeStatement</code>.
     *
     * @param attr List of attributes
     */
    public abstract AttributeStatement createAttributeStatement(List attr) throws SAMLException;
    
    /**
     * Constructs an instance of <code>AudienceRestrictionCondition</code>.
     * It takes in a <code>List</code> of audience for this
     * condition, each of them being a String.
     * @param audience A List of audience to be included within this condition
     */
    public abstract AudienceRestrictionCondition createAudienceRestrictionCondition(List audience) throws SAMLException;
    
     /**
     * Constructs an instance of <code>AudienceRestriction</code>.
     * It takes in a <code>List</code> of audience for this
     * condition, each of them being a String.
     * @param audience A List of audience to be included within this condition
     */
    public abstract AudienceRestriction createAudienceRestriction(List audience) throws SAMLException;
    
    /**
     * Constructs an instance of <code>AuthenticationStatement</code>.
     *
     * @param authMethod (optional) A String specifies the type of authentication
     *        that took place. Pass <b>null</b> if not required.
     * @param authInstant (optional) A GregorianCalendar object specifing the time at which the
     *        authentication that took place. Pass null if not required.
     * @param subject (required) A Subject object
     * @param subjectLocality (optional) A <code>SubjectLocality</code> object. Pass <b>null</b> if not required.
     * @param authorityBinding (optional) A List of <code>AuthorityBinding</code>. Pass <b>null</b> if not required.
     *        objects.
     */
    public abstract AuthenticationStatement createAuthenticationStatement(
            String authMethod, GregorianCalendar authInstant, Subject subject,
            SubjectLocality subjectLocality, List authorityBinding) throws SAMLException;
    
    /**
     * Constructs an instance of <code>AuthenticationStatement</code>.
     *    
     * @param authInstant (optional) A GregorianCalendar object specifing the time at which the
     *        authentication that took place. Pass null if not required.     
     * @param subjectLocality (optional) A <code>SubjectLocality</code> object. Pass <b>null</b> if not required.
     * @param authnContext (optional) A <code>AuthnContext</code> object. Pass <b>null</b> if not required.
     *        objects.
     */
    public abstract AuthnStatement createAuthnStatement(
            GregorianCalendar authInstant, SubjectLocality subjectLocality, AuthnContext authnContext, 
            String sessionIndex, GregorianCalendar sessionNotOnOrAfter) throws SAMLException;
    
    /**
     *Constructs an instance of <code>AuthorityBinding</code>.
     *@param authKind A QName representing the type of SAML protocol queries
     *       to which the authority described by this element will
     *       respond.
     *@param location A String representing a URI reference describing how to locate and communicate with the
     *       authority.
     *@param binding A String representing a URI reference identifying the SAML
     *       protocol binding to use in  communicating with the authority.
     */
    public abstract AuthorityBinding createAuthorityBinding(QName authKind, String location, String binding) throws SAMLException;
    
    public abstract AuthnContext createAuthnContext() throws SAMLException;
    
    public abstract AuthnContext createAuthnContext(String authContextClassref, String authenticatingAuthority) throws SAMLException;
    
    /**
     * Constructs an instance of <code>AuthorizationDecisionStatement</code>.
     *
     * @param subject (required) A Subject object
     * @param resource (required) A String identifying the resource to which
     *        access authorization is sought.
     * @param decision (required) The decision rendered by the issuer with
     *        respect to the specified resource.
     * @param action (required) A List of Action objects specifying the set of
     *        actions authorized to be performed on the specified resource.
     * @param evidence (optional) An Evidence object representing a set of
     *        assertions that the issuer replied on in making decisions.
     */
    public abstract AuthorizationDecisionStatement createAuthorizationDecisionStatement(
            Subject subject, String resource, String decision, List action, Evidence evidence) throws SAMLException;
    
    /**
     * Constructs an instance of <code>AuthnDecisionStatement</code>.
     *     
     * @param resource (required) A String identifying the resource to which
     *        access authorization is sought.
     * @param decision (required) The decision rendered by the issuer with
     *        respect to the specified resource.
     * @param action (required) A List of Action objects specifying the set of
     *        actions authorized to be performed on the specified resource.
     * @param evidence (optional) An <code>Evidence</code> object representing a set of
     *        assertions that the issuer replied on in making decisions.
     */
    public abstract AuthnDecisionStatement createAuthnDecisionStatement(
            String resource, String decision, List action, Evidence evidence) throws SAMLException;
    
    /**
     * Constructs an instance of default <code>Conditions</code> object.
     */
    public abstract Conditions createConditions() throws SAMLException;
    
    /**
     * Constructs an instance of <code>Conditions</code>.
     *
     * @param notBefore specifies the earliest time instant at which the
     *        assertion is valid.
     * @param notOnOrAfter specifies the time instant at which the assertion
     *        has expired.
     * @param condition
     * @param arc the <code>AudienceRestrictionCondition</code> to be
     *        added. Can be null, if no audience restriction.
     * @param doNotCacheCnd
     */
    public abstract Conditions createConditions(
            GregorianCalendar notBefore,
            GregorianCalendar notOnOrAfter,
            List condition,
            List arc,
            List doNotCacheCnd) throws SAMLException;
    
    /**
     * Constructs an instance of <code>Conditions</code>.
     *
     * @param notBefore specifies the earliest time instant at which the
     *        assertion is valid.
     * @param notOnOrAfter specifies the time instant at which the assertion
     *        has expired.
     * @param condition
     * @param ar the <code>AudienceRestriction</code> to be
     *        added. Can be null, if no audience restriction.
     * @param oneTimeUse
     * @param proxyRestriction
     */
    public abstract Conditions createConditions(
            GregorianCalendar notBefore,
            GregorianCalendar notOnOrAfter,
            List condition,
            List ar,
            List oneTimeUse,
            List proxyRestriction) throws SAMLException;
    
    /**
     * Constructs an instance of <code>DoNotCacheCondition</code>
     */
    public abstract DoNotCacheCondition createDoNotCacheCondition() throws SAMLException;
    
    public abstract OneTimeUse createOneTimeUse() throws SAMLException;
    
    /**
     * Constructs an Evidence from a List of <code>Assertion</code> and
     * <code>AssertionIDReference</code> objects.
     *
     * @param assertionIDRef List of <code>AssertionIDReference</code> objects.
     * @param assertion List of <code>Assertion</code> objects.
     */
    public abstract Evidence createEvidence(List assertionIDRef, List assertion) throws SAMLException;
    
    /**
     * Constructs a <code>NameQualifier</code> instance.
     *
     * @param name The string representing the name of the Subject
     * @param nameQualifier The security or administrative domain that qualifies
     *        the name of the <code>Subject</code>. This is optional could be
     *        null.
     * @param format The syntax used to describe the name of the
     *        <code>Subject</code>. This optional, could be null.
     */
    public abstract NameIdentifier createNameIdentifier(String name, String nameQualifier, String format) throws SAMLException;
    
    /**
     * Constructs a <code>NameID</code> instance.
     *
     * @param name The string representing the name of the Subject
     * @param nameQualifier The security or administrative domain that qualifies
     *        the name of the <code>Subject</code>. This is optional could be
     *        null.
     * @param format The syntax used to describe the name of the
     *        <code>Subject</code>. This optional, could be null.
     */
    public abstract NameID createNameID(String name, String nameQualifier, String format) throws SAMLException;
    
    
    /**
     * Constructs a Subject object from a <code>NameIdentifier</code>
     * object and a <code>SubjectConfirmation</code> object.
     *
     * @param nameIdentifier <code>NameIdentifier</code> object.
     * @param subjectConfirmation <code>SubjectConfirmation</code> object.
     */
    public abstract Subject createSubject(NameIdentifier nameIdentifier, SubjectConfirmation subjectConfirmation) throws SAMLException;
    
     /**
     * Constructs a Subject object from a <code>NameID</code>
     * object and a <code>SubjectConfirmation</code> object.
     *
     * @param nameID <code>NameID</code> object.
     * @param subjectConfirmation <code>SubjectConfirmation</code> object.
     */
    public abstract Subject createSubject(NameID nameID, SubjectConfirmation subjectConfirmation) throws SAMLException;
    
    /**
     * Creates and returns a <code>SubjectConfirmation</code> object.
     *
     * @param confirmationMethod A URI (String) that identifies a protocol used
     *        to authenticate a <code>Subject</code>. Please refer to
     *        <code>draft-sstc-core-25</code> Section 7 for a list of URIs
     *        identifying common authentication protocols.
     */
    public abstract SubjectConfirmation createSubjectConfirmation(String confirmationMethod) throws SAMLException;
    
    /**
     * Creates and returns a <code>SubjectConfirmation</code> object.
     *
     * @param nameID <code>NameID</code> object.
     * @param method A URI (String) that identifies a protocol used
     *        to authenticate a <code>Subject</code>. Please refer to
     *        <code>draft-sstc-core-25</code> Section 7 for a list of URIs
     *        identifying common authentication protocols.
     */
    public abstract SubjectConfirmation createSubjectConfirmation(NameID nameID,String method) throws SAMLException;
    
    public abstract SubjectConfirmation createSubjectConfirmation(
            List confirmationMethods,SubjectConfirmationData scd,KeyInfo keyInfo) throws SAMLException ;
    
    
    /**
     * Constructs a <code>SubjectConfirmation</code> instance.
     *
     * @param confirmationMethods A list of <code>confirmationMethods</code>
     *        each of which is a URI (String) that identifies a protocol
     *        used to authenticate a <code>Subject</code>. Please refer to
     *        <code>draft-sstc-core-25</code> Section 7 for
     *        a list of URIs identifying common authentication protocols.
     * @param subjectConfirmationData Additional authentication information to
     *        be used by a specific authentication protocol. Can be passed as
     *        null if there is no <code>subjectConfirmationData</code> for the
     *        <code>SubjectConfirmation</code> object.
     * @param keyInfo An XML signature element that specifies a cryptographic
     *        key held by the <code>Subject</code>.
     */
    public abstract SubjectConfirmation createSubjectConfirmation(
            List confirmationMethods, Element subjectConfirmationData,
            Element keyInfo) throws SAMLException;
    
    /**
     * Constructs a <code>SubjectConfirmation</code> instance.
     *
     * @param nameID <code>NameID</code> object.     
     * @param subjectConfirmationData Additional authentication information to
     *        be used by a specific authentication protocol. Can be passed as
     *        null if there is no <code>subjectConfirmationData</code> for the
     *        <code>SubjectConfirmation</code> object.
     * @param confirmationMethods A list of <code>confirmationMethods</code>
     *        each of which is a URI (String) that identifies a protocol
     *        used to authenticate a <code>Subject</code>. Please refer to
     *        <code>draft-sstc-core-25</code> Section 7 for
     *        a list of URIs identifying common authentication protocols.
     *
     */
    public abstract SubjectConfirmation createSubjectConfirmation(
            NameID nameID, SubjectConfirmationData subjectConfirmationData,
            String confirmationMethods) throws SAMLException;
    
     /**
     * Constructs a <code>SubjectConfirmation</code> instance.
     *
     * @param nameID <code>NameID</code> object.     
     * @param keyInfoConfirmationData Additional authentication information to
     *        be used by a specific authentication protocol. Can be passed as
     *        null if there is no <code>KeyInfoConfirmationData</code> for the
     *        <code>SubjectConfirmation</code> object.
     * @param confirmationMethods A list of <code>confirmationMethods</code>
     *        each of which is a URI (String) that identifies a protocol
     *        used to authenticate a <code>Subject</code>. Please refer to
     *        <code>draft-sstc-core-25</code> Section 7 for
     *        a list of URIs identifying common authentication protocols.
     *
     */
    public abstract SubjectConfirmation createSubjectConfirmation(
            NameID nameID, KeyInfoConfirmationData keyInfoConfirmationData,
            String confirmationMethods) throws SAMLException;
    
    public abstract SubjectConfirmationData createSubjectConfirmationData(
            String address, String inResponseTo, GregorianCalendar notBefore,
            GregorianCalendar notOnOrAfter, String recipient, Element keyInfo) throws SAMLException;
    public abstract SubjectConfirmationData createSubjectConfirmationData(
            String address, String inResponseTo, GregorianCalendar notBefore,
            GregorianCalendar notOnOrAfter, String recipient, KeyInfo keyInfo) throws SAMLException;
    
    public abstract KeyInfoConfirmationData createKeyInfoConfirmationData(Element keyInfo) throws SAMLException;
    
    /**
     * Constructs a <code>SubjectLocality</code> instance.
     */
    public abstract SubjectLocality createSubjectLocality() throws SAMLException;
    
    /**
     * Constructs an instance of <code>SubjectLocality</code>.
     *
     * @param ipAddress String representing the IP Address of the entity
     *        that was authenticated.
     * @param dnsAddress String representing the DNS Address of the entity that
     *        was authenticated. As per SAML specification  they are both
     *        optional, so values can be null.
     */
    public abstract SubjectLocality createSubjectLocality(String ipAddress, String dnsAddress) throws SAMLException;
}
