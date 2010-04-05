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

/*
 * WSTrustUtil.java
 *
 * Created on February 7, 2006, 3:37 PM
 *
 */

package com.sun.xml.ws.security.trust.util;

import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.secconv.WSSCElementFactory;
import com.sun.xml.ws.security.secconv.WSSCElementFactory13;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustSOAPFaultException;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.trust.impl.elements.str.KeyIdentifierImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.SecurityTokenReferenceImpl;
import com.sun.xml.ws.security.trust.impl.bindings.AttributedURI;
import com.sun.xml.ws.security.trust.impl.bindings.EndpointReference;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;

import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.SAMLAssertionFactory;

import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.keys.content.X509Data;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 *
 * @author ws-trust-implementation-team
 */
public class WSTrustUtil {
    
    private WSTrustUtil(){
        //private constructor
    }
    
    /**
     *create and return a SOAP 1.1 Fault corresponding to this exception
     */
    public static SOAPFault createSOAP11Fault(final WSTrustSOAPFaultException sfex){
        
        throw new UnsupportedOperationException("To Do");
    }
    
    /**
     *create and return a SOAP 1.2 Fault corresponding to this exception
     */
    public static SOAPFault createSOAP12Fault(final WSTrustSOAPFaultException sfex){
        
        throw new UnsupportedOperationException("To Do");
    }
    
    public static byte[] generateRandomSecret(final int keySize) {        
        // Create binary secret
        final SecureRandom random = new SecureRandom();
        final byte[] secret = new byte[(int)keySize];
        random.nextBytes(secret);
        return secret;
    }
    
   public static SecurityContextToken createSecurityContextToken(final WSTrustElementFactory wsscEleFac) throws WSSecureConversationException{
       final String identifier = "urn:uuid:" + UUID.randomUUID().toString();
       URI idURI;
       try{
           idURI = new URI(identifier);
       }catch (URISyntaxException ex){
           throw new WSSecureConversationException(ex.getMessage(), ex);
       }
       final String wsuId = "uuid-" + UUID.randomUUID().toString();
       if(wsscEleFac instanceof com.sun.xml.ws.security.secconv.WSSCElementFactory){
           return ((WSSCElementFactory)wsscEleFac).createSecurityContextToken(idURI, null, wsuId);
       }else if(wsscEleFac instanceof com.sun.xml.ws.security.secconv.WSSCElementFactory13){
           return ((WSSCElementFactory13)wsscEleFac).createSecurityContextToken(idURI, null, wsuId);
       }
       return null;
   }
   
   public static SecurityContextToken createSecurityContextToken(final WSSCElementFactory eleFac) throws WSSecureConversationException{
       final String identifier = "urn:uuid:" + UUID.randomUUID().toString();
       URI idURI;
       try{
           idURI = new URI(identifier);
       }catch (URISyntaxException ex){
           throw new WSSecureConversationException(ex.getMessage(), ex);
       }
       final String wsuId = "uuid-" + UUID.randomUUID().toString();
       
       return eleFac.createSecurityContextToken(idURI, null, wsuId);
   }
    
   public static SecurityContextToken createSecurityContextToken(final WSTrustElementFactory wsscEleFac, final String identifier) throws WSSecureConversationException{       
       URI idURI;
       try{
           idURI = new URI(identifier);
       }catch (URISyntaxException ex){
           throw new WSSecureConversationException(ex.getMessage(), ex);
       }
       final String wsuId = "uuid-" + UUID.randomUUID().toString();
       final String wsuInstance = "uuid-" + UUID.randomUUID().toString();

       if(wsscEleFac instanceof com.sun.xml.ws.security.secconv.WSSCElementFactory){
           return ((WSSCElementFactory)wsscEleFac).createSecurityContextToken(idURI, wsuInstance, wsuId);
       }else if(wsscEleFac instanceof com.sun.xml.ws.security.secconv.WSSCElementFactory13){
           return ((WSSCElementFactory13)wsscEleFac).createSecurityContextToken(idURI, wsuInstance, wsuId);
       }
       return null;       
   }
   
   public static SecurityContextToken createSecurityContextToken(final WSSCElementFactory eleFac, final String identifier) throws WSSecureConversationException{       
       URI idURI;
       try{
           idURI = new URI(identifier);
       }catch (URISyntaxException ex){
           throw new WSSecureConversationException(ex.getMessage(), ex);
       }
       final String wsuId = "uuid-" + UUID.randomUUID().toString();
       final String wsuInstance = "uuid-" + UUID.randomUUID().toString();
       
       return eleFac.createSecurityContextToken(idURI, wsuInstance, wsuId);
   }
   
   public static SecurityTokenReference createSecurityTokenReference(final String id, final String valueType){
       WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance(); 
       final KeyIdentifier ref = eleFac.createKeyIdentifier(valueType, null);
        ref.setValue(id);
        return eleFac.createSecurityTokenReference(ref);
    }
   
   public static AppliesTo createAppliesTo(final String appliesTo){
       final AttributedURI uri = new AttributedURI();
       uri.setValue(appliesTo);
       final EndpointReference epr = new EndpointReference();
       epr.setAddress(uri);
       final AppliesTo applTo = (new com.sun.xml.ws.policy.impl.bindings.ObjectFactory()).createAppliesTo();
       applTo.getAny().add((new com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory()).createEndpointReference(epr));
       
       return applTo;
   }
   
   public static List<Object> parseAppliesTo(final AppliesTo appliesTo){
       final List<Object> list = appliesTo.getAny();
       EndpointReference epr = null;
       List<Object> result = new ArrayList<Object>();
       if (!list.isEmpty()){
            for (Object obj : list) {
                if (obj instanceof EndpointReference){
                    epr = (EndpointReference)obj;
                } else if (obj instanceof JAXBElement){
                    final JAXBElement ele = (JAXBElement)obj;    
                    final String local = ele.getName().getLocalPart();
                    if (local.equalsIgnoreCase("EndpointReference")) {
                        epr = (EndpointReference)ele.getValue();
                    }
                }
                
                if (epr != null){
                    final AttributedURI uri = epr.getAddress();
                    if (uri != null){
                        result.add(uri.getValue());
                    }
                    for (Object obj2 : epr.getAny()) {
                        try {
                            Element ele = WSTrustElementFactory.newInstance().toElement(obj2);
                            if (ele != null){
                                NodeList nodeList = ele.getElementsByTagNameNS("*", "Identity");
                                if (nodeList.getLength() > 0){
                                    Element identity = (Element)nodeList.item(0);
                                    result.add(identity);
                                    NodeList clist = identity.getChildNodes();
                                    for (int i = 0; i < clist.getLength(); i++){
                                        if (clist.item(i).getNodeType() == Node.TEXT_NODE){
                                            String data = ((Text)clist.item(i)).getData();
                                            X509Certificate cert = (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.decode(data)));
                                            result.add(cert);
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        return result;
   }
   
   public static String getAppliesToURI(final AppliesTo appliesTo){
       final List list = appliesTo.getAny();
       EndpointReference epr = null;
       if (!list.isEmpty()){
            for (int i = 0; i < list.size(); i++) {
                final Object obj = list.get(i);
                if (obj instanceof EndpointReference){
                    epr = (EndpointReference)obj;
                } else if (obj instanceof JAXBElement){
                    final JAXBElement ele = (JAXBElement)obj;    
                    final String local = ele.getName().getLocalPart();
                    if (local.equalsIgnoreCase("EndpointReference")) {
                        epr = (EndpointReference)ele.getValue();
                    }
                }
                
                if (epr != null){
                    final AttributedURI uri = epr.getAddress();
                    if (uri != null){
                        return uri.getValue();
                    }
                }
            }
        }
        return null;
    }
       
    public static String createFriendlyPPID(String displayValue){
        //ToDo
        /*try{
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] hashId = md.digest(com.sun.xml.wss.impl.misc.Base64.decode(displayValue));
            StringBuffer sb = new StringBuffer();
            
        }catch(Exception ex){
            return displayValue;
        }*/
        return displayValue;
    }
    
    public static String elemToString(final BaseSTSResponse rstr, final WSTrustVersion wstVer){
        StringWriter writer = new StringWriter();
        try{
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(WSTrustElementFactory.newInstance(wstVer).toSource(rstr), new StreamResult(writer));
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        return writer.toString();
    }

    public static String elemToString(final BaseSTSRequest rst, final WSTrustVersion wstVer){
        StringWriter writer = new StringWriter();
        try{
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(WSTrustElementFactory.newInstance(wstVer).toSource(rst), new StreamResult(writer));
        }catch(Exception ex){
            ex.printStackTrace();
        }
        
        return writer.toString();
    }      
    
    public static long getCurrentTimeWithOffset(){
        final Calendar cal = new GregorianCalendar();
        int offset = cal.get(Calendar.ZONE_OFFSET);
        if (cal.getTimeZone().inDaylightTime(cal.getTime())) {
            offset += cal.getTimeZone().getDSTSavings();
        }
        
         // always send UTC/GMT time
         final long beforeTime = cal.getTimeInMillis();
         
         return beforeTime - offset;
    }
    
    public static Lifetime createLifetime(long currentTime, long lifespan, WSTrustVersion wstVer) {
        final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'", Locale.getDefault());
        final Calendar cal = new GregorianCalendar();
        synchronized (calendarFormatter) {
            calendarFormatter.setTimeZone(cal.getTimeZone());
            cal.setTimeInMillis(currentTime);
            
            final AttributedDateTime created = new AttributedDateTime();
            created.setValue(calendarFormatter.format(cal.getTime()));
            
            final AttributedDateTime expires = new AttributedDateTime();
            cal.setTimeInMillis(currentTime + lifespan);
            expires.setValue(calendarFormatter.format(cal.getTime()));
            
            final Lifetime lifetime = WSTrustElementFactory.newInstance(wstVer).createLifetime(created, expires);
            
            return lifetime;
        }
    }

    public static long getLifeSpan(Lifetime lifetime){
        final AttributedDateTime created = lifetime.getCreated();
        final AttributedDateTime expires = lifetime.getExpires();
    
        return parseAttributedDateTime(expires).getTime() - parseAttributedDateTime(created).getTime();
    }

    public static Date parseAttributedDateTime(AttributedDateTime time){
        final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'", Locale.getDefault());

        Date date = null;
        synchronized (calendarFormatter){
            try {
                date = calendarFormatter.parse(time.getValue());
            }catch(Exception ex){
                // try a different format
                try{
                    SimpleDateFormat calendarFormatter1
                            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.getDefault());

                    date = calendarFormatter1.parse(time.getValue());
                }catch(ParseException pex){
                    throw new RuntimeException(pex);
                }
            }
        }

        return date;
    }
    
    public static EncryptedKey encryptKey(final Document doc, final byte[] encryptedKey, final X509Certificate cert, final String keyWrapAlgorithm) throws Exception{
        final PublicKey pubKey = cert.getPublicKey();
        final XMLCipher cipher;
        if(keyWrapAlgorithm != null){
            cipher = XMLCipher.getInstance(keyWrapAlgorithm);
        }else{
            cipher = XMLCipher.getInstance(XMLCipher.RSA_OAEP);
        }
        cipher.init(XMLCipher.WRAP_MODE, pubKey);

        EncryptedKey encKey = cipher.encryptKey(doc, new SecretKeySpec(encryptedKey, "AES"));
        final KeyInfo keyinfo = new KeyInfo(doc);

        byte[] skid = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(cert);
        if (skid != null && skid.length > 0){
            final KeyIdentifier keyIdentifier = new KeyIdentifierImpl(MessageConstants.X509SubjectKeyIdentifier_NS,null);
            keyIdentifier.setValue(Base64.encode(skid));
            final SecurityTokenReference str = new SecurityTokenReferenceImpl(keyIdentifier);
            keyinfo.addUnknownElement((Element)doc.importNode(WSTrustElementFactory.newInstance().toElement(str,null), true));
        }else{
            final X509Data x509data = new X509Data(doc);
            x509data.addCertificate(cert);
            keyinfo.add(x509data);
        }
        encKey.setKeyInfo(keyinfo);
        
        return encKey;
    }
    
    public static Assertion addSamlAttributes(Assertion assertion, Map<QName, List<String>> claimedAttrs)throws WSTrustException {
        try {
            String version = assertion.getVersion();
            SAMLAssertionFactory samlFac = null;

            if ("2.0".equals(version)){
                samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);
            }else{
                samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
            }
            Element assertionEle = assertion.toElement(null);
            String samlNS = assertionEle.getNamespaceURI();
            String samlPrefix = assertionEle.getPrefix();
            NodeList asList = assertionEle.getElementsByTagNameNS(samlNS, "AttributeStatement");
            Node as = null;
            if (asList.getLength() > 0){
                as = asList.item(0);
            }
            createAttributeStatement(as, claimedAttrs, samlNS, samlPrefix);
               
            return  samlFac.createAssertion(assertionEle);
        }catch (Exception ex){
            throw new WSTrustException(ex.getMessage());
        }
    }
    
    private static Node createAttributeStatement(Node as, Map<QName, List<String>> claimedAttrs, String samlNS, String samlPrefix)throws WSTrustException{
        try{
            Document doc = null;
            if (as != null){
                doc = as.getOwnerDocument();
            }else{
                doc = newDocument();
                as = doc.createElementNS(samlNS, samlPrefix+":AttributeStatement");
                doc.appendChild(as);
            }
            final Set<Map.Entry<QName, List<String>>> entries = claimedAttrs.entrySet();
            for(Map.Entry<QName, List<String>> entry : entries){
                final QName attrKey = entry.getKey();
                final List<String> values = entry.getValue();
                if (values.size() > 0){
                    Element attrEle = null;
                    if (STSAttributeProvider.NAME_IDENTIFIER.equals(attrKey.getLocalPart())){
                        // create an "actor" attribute
                        attrEle = createActorAttribute(doc, samlNS, samlPrefix, values.get(0));

                    }else {
                        attrEle = createAttribute(doc, samlNS, samlPrefix, attrKey);
                        Iterator valueIt = values.iterator();
                        while (valueIt.hasNext()){
                            Element attrValueEle = doc.createElementNS(samlNS, samlPrefix+":AttributeValue");
                            Text text = doc.createTextNode((String)valueIt.next());
                            attrValueEle.appendChild(text);
                            attrEle.appendChild(attrValueEle);
                        }
                    }
                    as.appendChild(attrEle);
                }
            }
            
            return as;
        }catch (Exception ex){
            throw new WSTrustException(ex.getMessage());
        }
    }

    private static Element createAttribute(Document doc, String samlNS, String samlPrefix, QName attrKey)throws Exception {
        Element attrEle = doc.createElementNS(samlNS, samlPrefix+":Attribute");
        attrEle.setAttribute("AttributeName", attrKey.getLocalPart());
        attrEle.setAttribute("AttributeNamespace", attrKey.getNamespaceURI());
        if (WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE.equals(samlNS)){
            attrEle.setAttribute("Name", attrKey.getLocalPart());
            attrEle.setAttribute("NameFormat", attrKey.getNamespaceURI());
        }
        return attrEle;
    }

    private static Element createActorAttribute(Document doc, String samlNS, String samlPrefix, String name)throws Exception {
        // Create Attribute of the form:
        // <saml:Attribute AttributeName="actor" 
        //          AttributeNamespace="http://schemas.xmlsoap.com/ws/2009/09/identity/claims">
        //      ...
        // </saml:Attribute>
        Element actorEle = createAttribute(doc, samlNS, samlPrefix, new QName("actor", "http://schemas.xmlsoap.com/ws/2009/09/identity/claims"));
        Element attrValueEle = doc.createElementNS(samlNS, samlPrefix+":AttributeValue");
        actorEle.appendChild(attrValueEle);

        // Create inner Attribute of the form:
        // <saml:Attribute AttributeName="name"
        //          AttributeNamespace="http://schemas.xmlsoap.org/ws/2005/05/identity/claims"       			                  AttributeNamespace="http://schemas.xmlsoap.org/ws/2005/05/identity/claims"    	                  xmlns:saml="urn:oasis:names:tc:SAML:1.0:assertion">
        //    <saml:AttributeValue>name</saml:AttributeValue>
        // </saml:Attribute>
        Element nameEle = createAttribute(doc, samlNS, samlPrefix, new QName("name", "http://schemas.xmlsoap.com/ws/2005/05/identity/claims"));
        attrValueEle.appendChild(nameEle);
        Element nameAttrValueEle = doc.createElementNS(samlNS, samlPrefix+":AttributeValue");
        nameEle.appendChild(nameAttrValueEle);
        Text text = doc.createTextNode(name);
        nameAttrValueEle.appendChild(text);

        return actorEle;
    }

    public static Document newDocument(){
        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.newDocument();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        return doc;
    }
}
