/*
 * $Id: Assertion.java,v 1.2 2006-05-10 22:49:50 jdg6688 Exp $
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;

//import com.sun.xml.wss.impl.dsig.DSigResolver;
import com.sun.xml.wss.impl.dsig.WSSPolicyConsumerImpl;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.bind.util.ListImpl;
import com.sun.xml.wss.saml.util.SAMLUtil;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AssertionImpl;
import java.lang.reflect.Constructor;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AssertionTypeImpl;
import java.util.Calendar;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.TimeZone;
import java.util.Date;
import java.util.Set;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dom.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.*;

import java.security.cert.X509Certificate;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import javax.xml.soap.*;

/**
 * This object stands for <code>Assertion</code> element. An Assertion is a package
 * of information that supplies one or more <code>Statement</code> made by an
 * issuer. There are three kinds of assertions Au     [java] <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
 * [java] <Conditions NotBefore="2005-08-16T13:21:50.503+05:30" NotOnOrAfter="2005-08-16T15:21:50.504+05:30" xmlns="urn:oasis:names:tc:SAML:1.0:assertion"/>
 * [java] <Subject xmlns="urn:oasis:names:tc:SAML:1.0:assertion">
 * [java]     <NameIdentifier Format="urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName">CN=SAML User,OU=SU,O=SAML
 * User,L=Los Angeles,ST=CA,C=US</NameIdentifier>
 * [java]     <SubjectConfirmation>
 * [java]         <ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:sender-vouches</ConfirmationMethod>
 * [java]     </SubjectConfirmation>
 * [java] </Subject>
 * [java] <Attribute AttributeName="attribute1" AttributeNamespace="urn:com:sun:xml:wss:attribute" xmlns="urn:oasis:names:tc:SAML:1.0:assertion">
 * [java]     <AttributeValue>ATTRIBUTE1</AttributeValue>
 * [java] </Attribute>
 * thentication, Authorization
 * Decision and Attribute assertion.
 */
public class Assertion  extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AssertionImpl implements com.sun.xml.wss.saml.Assertion {
    
    private Element signedAssertion = null;
    
    public Assertion (AssertionImpl assertion) {
        this.setAdvice (assertion.getAdvice ());
        this.setAssertionID (assertion.getAssertionID ());
        this.setConditions (assertion.getConditions ());
        this.setIssueInstant (assertion.getIssueInstant ());
        this.setIssuer (assertion.getIssuer ());
        this.setMajorVersion (assertion.getMajorVersion ());
        this.setMinorVersion (assertion.getMinorVersion ());
        this.setSignature (assertion.getSignature ());
        this.setStatement (assertion.getStatementOrSubjectStatementOrAuthenticationStatement ());
    }
    
    protected static Logger log = Logger.getLogger (
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    
    /**
     * sign the saml assertion (Enveloped Signature)
     * @param pubKey PublicKey to be used for Signature verification
     * @param privKey PrivateKey to be used for Signature calculation
     */
    
    public Element sign (PublicKey pubKey, PrivateKey privKey) throws SAMLException {
        
        
        
        //Check if the signature is already calculated
        if ( signedAssertion != null) {
            return signedAssertion;
        }
        
        //Calculate the enveloped signature
        try {
            
            XMLSignatureFactory fac = WSSPolicyConsumerImpl.getInstance ().getSignatureFactory ();
            return sign (fac.newDigestMethod (DigestMethod.SHA1,null),SignatureMethod.RSA_SHA1, pubKey,privKey);
            
        } catch (Exception ex) {
            // log here
            throw new SAMLException (ex);
        }
    }
    
    public Element sign(X509Certificate cert, PrivateKey privKey) throws SAMLException {
        //Check if the signature is already calculated
        if ( signedAssertion != null) {
            return signedAssertion;
        }
        
        //Calculate the enveloped signature
        try {
            
            XMLSignatureFactory fac = WSSPolicyConsumerImpl.getInstance ().getSignatureFactory ();
            return sign (fac.newDigestMethod (DigestMethod.SHA1,null),SignatureMethod.RSA_SHA1, cert,privKey);
            
        } catch (Exception ex) {
            // log here
            throw new SAMLException (ex);
        }
    }
    
    
    
    /**
     * sign the saml assertion (Enveloped Signature)
     * @param digestMethod DigestMethod to be used
     * @param signatureMethod SignatureMethod to be used. 
     * @param pubKey PublicKey to be used for Signature verification
     * @param privKey PrivateKey to be used for Signature calculation
     */
    
    public Element sign (DigestMethod digestMethod, String signatureMethod,PublicKey pubKey, PrivateKey privKey) throws SAMLException {
        
        
        
        //Check if the signature is already calculated
        if ( signedAssertion != null) {
            return signedAssertion;
            //return;
        }
        
        //Calculate the enveloped signature
        try {
            XMLSignatureFactory fac = WSSPolicyConsumerImpl.getInstance ().getSignatureFactory ();
            ArrayList transformList = new ArrayList ();
            
            Transform tr1 = fac.newTransform (Transform.ENVELOPED, (TransformParameterSpec) null);
            Transform tr2 = fac.newTransform (CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null);
            transformList.add (tr1);
            transformList.add (tr2);
            
            String uri = "#" + this.getAssertionID ();
            Reference ref = fac.newReference (uri,digestMethod,transformList, null, null);
            
            // Create the SignedInfo
            SignedInfo si = fac.newSignedInfo
                    (fac.newCanonicalizationMethod
                    (CanonicalizationMethod.EXCLUSIVE,
                    (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod (signatureMethod, null),
                    Collections.singletonList (ref));
            
            // Create a KeyValue containing the DSA PublicKey that was generated
            KeyInfoFactory kif = fac.getKeyInfoFactory ();
            KeyValue kv = kif.newKeyValue (pubKey);
            
            // Create a KeyInfo and add the KeyValue to it
            KeyInfo ki = kif.newKeyInfo (Collections.singletonList (kv));
            
            // Instantiate the document to be signed
            Document doc =  XMLUtil.newDocument ();
            
            
            //Document document;
            
            //Element assertionElement = this.toElement(doc);
            Element assertionElement = this.toElement (doc);
            //try {
            //    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //    DocumentBuilder builder = factory.newDocumentBuilder();
            //    document = builder.newDocument();
            //} catch (Exception ex) {
            //    throw new XWSSecurityException("Unable to create Document : " + ex.getMessage());
            //}
            //document.appendChild(assertionElement);
            //doc.appendChild(assertionElement);
            
            
            
            // Create a DOMSignContext and specify the DSA PrivateKey and
            // location of the resulting XMLSignature's parent element
            DOMSignContext dsc = new DOMSignContext (privKey, assertionElement);
            HashMap map = new HashMap ();
            map.put (this.getAssertionID (),assertionElement);
            
            dsc.setURIDereferencer (new DSigResolver (map,assertionElement));
            XMLSignature signature = fac.newXMLSignature (si, ki);
            dsc.putNamespacePrefix ("http://www.w3.org/2000/09/xmldsig#", "ds");
            
            // Marshal, generate (and sign) the enveloped signature
            signature.sign (dsc);
            
            signedAssertion = assertionElement;
            return assertionElement;
        } catch (Exception ex) {
            throw new SAMLException (ex);
        }
        //return signedAssertion;
    }
    
    public Element sign(DigestMethod digestMethod, String signatureMethod, X509Certificate cert, PrivateKey privKey) throws SAMLException {
         //Check if the signature is already calculated
        if ( signedAssertion != null) {
            return signedAssertion;
            //return;
        }
        
        //Calculate the enveloped signature
        try {
            XMLSignatureFactory fac = WSSPolicyConsumerImpl.getInstance ().getSignatureFactory ();
            ArrayList transformList = new ArrayList ();
            
            Transform tr1 = fac.newTransform (Transform.ENVELOPED, (TransformParameterSpec) null);
            Transform tr2 = fac.newTransform (CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null);
            transformList.add (tr1);
            transformList.add (tr2);
            
            String uri = "#" + this.getAssertionID ();
            Reference ref = fac.newReference (uri,digestMethod,transformList, null, null);
            
            // Create the SignedInfo
            SignedInfo si = fac.newSignedInfo
                    (fac.newCanonicalizationMethod
                    (CanonicalizationMethod.EXCLUSIVE,
                    (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod (signatureMethod, null),
                    Collections.singletonList (ref));
            
            // Instantiate the document to be signed
            Document doc = MessageFactory.newInstance().createMessage().getSOAPPart();
            X509SubjectKeyIdentifier keyIdentifier = new X509SubjectKeyIdentifier(doc);
            keyIdentifier.setCertificate(cert);
            keyIdentifier.setReferenceValue(Base64.encode(X509SubjectKeyIdentifier.getSubjectKeyIdentifier(cert)));
            SecurityTokenReference str = new SecurityTokenReference();
            str.setReference(keyIdentifier);
           /* KeyIdentifier kid = new KeyIdentifierImpl(MessageConstants.X509SubjectKeyIdentifier_NS, MessageConstants.MessageConstants.BASE64_ENCODING_NS);
            kid.setValue(Base64.encode(X509SubjectKeyIdentifier.getSubjectKeyIdentifier(cert) ));
            SecurityTokenReference str = new SecurityTokenReferenceImpl(kid);*/
            DOMStructure domKeyInfo = new DOMStructure(str.getAsSoapElement());
            KeyInfoFactory kif = fac.getKeyInfoFactory ();
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(domKeyInfo));
                
            Element assertionElement = this.toElement (doc);
            //try {
            //    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //    DocumentBuilder builder = factory.newDocumentBuilder();
            //    document = builder.newDocument();
            //} catch (Exception ex) {
            //    throw new XWSSecurityException("Unable to create Document : " + ex.getMessage());
            //}
            //document.appendChild(assertionElement);
            //doc.appendChild(assertionElement);
            
            
            
            // Create a DOMSignContext and specify the DSA PrivateKey and
            // location of the resulting XMLSignature's parent element
            DOMSignContext dsc = new DOMSignContext (privKey, assertionElement);
            HashMap map = new HashMap ();
            map.put (this.getAssertionID (),assertionElement);
            
            dsc.setURIDereferencer (new DSigResolver (map,assertionElement));
            XMLSignature signature = fac.newXMLSignature (si, ki);
            dsc.putNamespacePrefix ("http://www.w3.org/2000/09/xmldsig#", "ds");
            
            // Marshal, generate (and sign) the enveloped signature
            signature.sign (dsc);
            
            signedAssertion = assertionElement;
            return assertionElement;
        } catch (Exception ex) {
            throw new SAMLException (ex);
        }
    }
    
    public Element toElement (Node doc) throws XWSSecurityException {
        if ( signedAssertion == null) {
            
            signedAssertion = SAMLUtil.toElement (doc, this);
        }
        
        
        return signedAssertion;
    }
    
    private boolean signatureVerified = false;
    
    /* (non-Javadoc)
     *
     */
    public void signatureWasVerified (boolean flag) {
        signatureVerified = flag;
    }
    
    /* (non-Javadoc)
     *
     */
    public boolean signatureWasVerified () {
        return signatureVerified;
    }
    
    public boolean isSigned () {
        return _Signature != null?true:false;
    }
    
    /**
     * This constructor is used to build <code>Assertion</code> object from a
     * block of existing XML that has already been built into a DOM.
     *
     * @param assertionElement A <code>org.w3c.dom.Element</code> representing
     *        DOM tree for <code>Assertion</code> object
     * @exception SAMLException if it could not process the Element properly,
     *            implying that there is an error in the sender or in the
     *            element definition.
     */
    public static Assertion fromElement (org.w3c.dom.Element element)
    throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance ("com.sun.xml.wss.saml.internal.saml11.jaxb10");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller ();
            return new Assertion ((AssertionImpl)u.unmarshal (element));
        } catch ( Exception ex) {
            throw new SAMLException (ex.getMessage ());
        }
    }
    
    private void setStatement (List statement) {
        this._StatementOrSubjectStatementOrAuthenticationStatement = new ListImpl (statement);
    }

    public String getType() {
        return MessageConstants.SAML_v1_1_NS;
    }

    public Object getTokenValue() {
        //TODO: Implement this method
        return null;
    }
    
    /**
     * This constructor is used to populate the data members: the
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
    public Assertion (
            String assertionID, java.lang.String issuer, Calendar issueInstant,
            Conditions conditions, Advice advice, List statements)
            throws SAMLException {
        if ( assertionID != null)
            setAssertionID (assertionID);
        
        if ( issuer != null)
            setIssuer (issuer);
        
        if ( issueInstant != null)
            setIssueInstant (issueInstant);
        
        if ( conditions != null)
            setConditions (conditions);
        
        if ( advice != null)
            setAdvice (advice);
        
        if ( statements != null)
            setStatement (statements);
        
    }
    
    
    private class DSigResolver implements URIDereferencer{
        //TODO : Convert DSigResolver to singleton class.
        Element elem = null;
        Map map = null;
        Class _nodeSetClass = null;
        String optNSClassName = "org.jcp.xml.dsig.internal.dom.DOMSubTreeData";
        Constructor _constructor = null;
        Boolean  _false = new Boolean (false);
        DSigResolver (Map map,Element elem){
            this.elem = elem;
            this.map = map;
            init();
        }
        
        void init (){
            try{
                _nodeSetClass = Class.forName (optNSClassName);
                _constructor = _nodeSetClass.getConstructor (new Class [] {org.w3c.dom.Node.class,boolean.class});
            }catch(LinkageError le){
                // logger.log (Level.FINE,"Not able load JSR 105 RI specific NodeSetData class ",le);
            }catch(ClassNotFoundException cne){
                // logger.log (Level.FINE,"Not able load JSR 105 RI specific NodeSetData class ",cne);
            }catch(NoSuchMethodException ne){
                
            }
        }
        public Data dereference (URIReference uriRef, XMLCryptoContext context) throws URIReferenceException {
            try{
                String uri = null;
                uri = uriRef.getURI ();
                return dereferenceURI (uri,context);
            }catch(Exception ex){
                throw new URIReferenceException (ex);
            }
        }
        Data dereferenceURI (String uri, XMLCryptoContext context) throws URIReferenceException{
            if(uri.charAt (0) == '#'){
                uri =  uri.substring (1,uri.length ());
                Element el = elem.getOwnerDocument ().getElementById (uri);
                if(el == null){
                    el = (Element)map.get (uri);
                }
                
                if(_constructor != null){
                    try{
                        return (Data)_constructor.newInstance (new Object[] {el,_false});
                    }catch(Exception ex){
                        //TODO: ignore this ?
                        ex.printStackTrace ();
                    }
                }else{
                    final HashSet nodeSet = new HashSet ();
                    toNodeSet (el,nodeSet);
                    return new NodeSetData (){
                        public Iterator iterator (){
                            return nodeSet.iterator ();
                        }
                    };
                }
                
            }
            
            return null;
            //throw new URIReferenceException("Resource "+uri+" was not found");
        }
        
        void toNodeSet (final Node rootNode,final Set result){
            switch (rootNode.getNodeType ()) {
                case Node.ELEMENT_NODE:
                    result.add (rootNode);
                    Element el=(Element)rootNode;
                    if (el.hasAttributes ()) {
                        NamedNodeMap nl = ((Element)rootNode).getAttributes ();
                        for (int i=0;i<nl.getLength ();i++) {
                            result.add (nl.item (i));
                        }
                    }
                    //no return keep working
                case Node.DOCUMENT_NODE:
                    for (Node r=rootNode.getFirstChild ();r!=null;r=r.getNextSibling ()){
                        if (r.getNodeType ()==Node.TEXT_NODE) {
                            result.add (r);
                            while ((r!=null) && (r.getNodeType ()==Node.TEXT_NODE)) {
                                r=r.getNextSibling ();
                            }
                            if (r==null)
                                return;
                        }
                        toNodeSet (r,result);
                    }
                    return;
                case Node.COMMENT_NODE:
                    return;
                case Node.DOCUMENT_TYPE_NODE:
                    return;
                default:
                    result.add (rootNode);
            }
            return;
        }
        
    }
}
