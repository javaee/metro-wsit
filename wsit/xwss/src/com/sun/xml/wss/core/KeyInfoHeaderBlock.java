/*
 * $Id: KeyInfoHeaderBlock.java,v 1.6 2008/07/03 05:28:49 ofung Exp $
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
package com.sun.xml.wss.core;

import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.keys.content.KeyName;
import com.sun.org.apache.xml.internal.security.keys.content.KeyValue;
import com.sun.org.apache.xml.internal.security.keys.content.MgmtData;
import com.sun.org.apache.xml.internal.security.keys.content.PGPData;
import com.sun.org.apache.xml.internal.security.keys.content.RetrievalMethod;
import com.sun.org.apache.xml.internal.security.keys.content.SPKIData;
import com.sun.org.apache.xml.internal.security.keys.content.X509Data;
import com.sun.org.apache.xml.internal.security.keys.content.keyvalues.DSAKeyValue;
import com.sun.org.apache.xml.internal.security.keys.content.keyvalues.RSAKeyValue;
import com.sun.org.apache.xml.internal.security.transforms.Transforms;
import com.sun.org.apache.xml.internal.security.utils.ElementProxy;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.api.security.trust.WSTrustException;


/**
 * Corresponds to the schema representation for a KeyInfo. 
 <element name="KeyInfo" type="ds:KeyInfoType"/>
   <complexType name="KeyInfoType" mixed="true">
     <choice maxOccurs="unbounded">
       <element ref="ds:KeyName"/>
       <element ref="ds:KeyValue"/>
       <element ref="ds:RetrievalMethod"/>
       <element ref="ds:X509Data"/>
       <element ref="ds:PGPData"/>
       <element ref="ds:SPKIData"/>
       <element ref="ds:MgmtData"/>
       <element ref="wsse:SecurityTokenReference"/>
       <any processContents="lax" namespace="##other"/>
       <!-- (1,1) elements from (0,unbounded) namespaces -->
     </choice>
     <attribute name="Id" type="ID" use="optional"/>
   </complexType>
*/
public class KeyInfoHeaderBlock  extends SecurityHeaderBlockImpl {

    public static final String SignatureSpecNS = MessageConstants.DSIG_NS;

    public static final String SignatureSpecNSprefix =
        MessageConstants.DSIG_PREFIX;

    public static final String TAG_KEYINFO = "KeyInfo";

    // delegate ds:KeyInfo member from XML DSIG
    KeyInfo delegateKeyInfo = null;

    boolean dirty = false;

    private static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /**
     *  baseURI URI to be used as context for all relative URIs.
     *  Accepted by all Apache XMLSIG elements
     */
    String baseURI = null;

    /**
     * The Owner Document of this KeyInfo
     */
    private Document document;

    /** 
     * constructor that creates an empty KeyInfo
     * @param ownerDoc the OwnerDocument of the KeyInfo
     */
    public KeyInfoHeaderBlock(Document ownerDoc) throws XWSSecurityException {
        try {
            this.document = ownerDoc;
            delegateKeyInfo = new KeyInfo(ownerDoc);
            dirty = true;
            setSOAPElement(getAsSoapElement());
        } catch (Exception e) {
            log.log(
                Level.SEVERE,
                "WSS0318.exception.while.creating.keyinfoblock",
                e);
            throw new XWSSecurityException(e);
        }       
    }

    /** 
     * constructor that takes Apache KeyInfo
     * @param keyinfo the KeyInfo from XML DSIG
     */
    public KeyInfoHeaderBlock(KeyInfo keyinfo) throws XWSSecurityException {
        this.document = keyinfo.getDocument();
        delegateKeyInfo = keyinfo;
        dirty = true;
        setSOAPElement(getAsSoapElement());
    }

    /**
     * Method addKeyName.
     *
     * @param keynameString
     */
    public void addKeyName(String keynameString) {
        delegateKeyInfo.addKeyName(keynameString);
        dirty = true;
    }
    
    /**
     * Method addBinarySecret
     *
     * @param secret byte[] representing secret
     */
    public void addBinarySecret(SOAPElement binarySecret) {
        delegateKeyInfo.addUnknownElement(binarySecret);
        dirty = true;
    }

    /**
     * Method add.
     *
     * @param keyname
     */
    public void addKeyName(SOAPElement keyname) throws XWSSecurityException {
        try {
            KeyName keynm = new KeyName(keyname,null); 
            delegateKeyInfo.add(keynm);
            dirty = true;
        } catch (XMLSecurityException e) {
            log.log(Level.SEVERE, "WSS0319.exception.adding.keyname", e);
            throw new XWSSecurityException(e);  
        }
    }

    /**
     * Method addKeyValue
     *
     * @param pk
     */
    public void addKeyValue(PublicKey pk) {
        delegateKeyInfo.addKeyValue(pk);
        dirty = true;
    }

    /**
     * Method addKeyValue
     *
     * @param unknownKeyValueElement
     */
    public void addUnknownKeyValue(SOAPElement unknownKeyValueElement) {
        delegateKeyInfo.addKeyValue(unknownKeyValueElement);
        dirty = true;
    }

    /**
     * Method add
     *
     * @param dsakeyvalue
     */
    public void addDSAKeyValue(SOAPElement dsakeyvalue) 
        throws XWSSecurityException{
        try {
            DSAKeyValue dsaKval = new DSAKeyValue(dsakeyvalue, null);
            delegateKeyInfo.add(dsaKval);
            dirty = true;
        } catch(XMLSecurityException e) {
            log.log(Level.SEVERE, 
                    "WSS0355.error.creating.keyvalue", 
                    new Object[] {"DSA", e.getMessage()}); 
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method add
     *
     * @param rsakeyvalue
     */
    public void addRSAKeyValue(SOAPElement rsakeyvalue) 
        throws XWSSecurityException{
        try {
             RSAKeyValue rsaKval = new RSAKeyValue(rsakeyvalue, null);
             delegateKeyInfo.add(rsaKval);
             dirty = true;
        } catch(XMLSecurityException e) {
            log.log(Level.SEVERE, 
                    "WSS0355.error.creating.keyvalue", 
                    new Object[] {"RSA", e.getMessage()});
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method addKeyValue
     *
     * @param keyvalue
     */
    public void addKeyValue(SOAPElement keyvalue) 
        throws XWSSecurityException {
        try {
            KeyValue kval = new KeyValue(keyvalue,null);
            delegateKeyInfo.add(kval);
            dirty = true;
        } catch(XMLSecurityException e) {
            log.log(Level.SEVERE, 
                    "WSS0355.error.creating.keyvalue", 
                    new Object[] {"", e.getMessage()});
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method addMgmtData
     *
     * @param mgmtdata
     */
    public void addMgmtData(String mgmtdata) {
        delegateKeyInfo.addMgmtData(mgmtdata);
        dirty = true;
    }

    /**
     * Method add
     *
     * @param mgmtdata
     */
    public void addMgmtData(SOAPElement mgmtdata)
        throws XWSSecurityException {
        try {
            MgmtData mgmtData = new MgmtData(mgmtdata, null);
            delegateKeyInfo.add(mgmtData);
            dirty = true;
        } catch(XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method addPGPData
     *
     * @param pgpdata
     */
    public void addPGPData(SOAPElement pgpdata)
        throws XWSSecurityException {
        try {
            PGPData pgpData = new PGPData(pgpdata, null);
            delegateKeyInfo.add(pgpData);
            dirty = true;
        } catch(XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method addRetrievalMethod
     *
     * @param URI
     * @param transforms
     * @param type
     */
    public void addRetrievalMethod(String URI, Transforms transforms,
        String type) {
        delegateKeyInfo.addRetrievalMethod(URI,transforms, type);
        dirty = true;
    }

    /**
     * Method addRetrievalMethod
     *
     * @param retrievalmethod
     */
    public void addRetrievalMethod(SOAPElement retrievalmethod)
        throws XWSSecurityException {
        try {
            RetrievalMethod rm = new RetrievalMethod(retrievalmethod, null);
            delegateKeyInfo.add(rm);
            dirty = true;
        } catch(XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method add
     *
     * @param spkidata
     */
    public void addSPKIData(SOAPElement spkidata) throws XWSSecurityException {
        try {
            SPKIData spki = new SPKIData(spkidata,null);
            delegateKeyInfo.add(spki);
            dirty = true;
        } catch (XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method addX509Data
     *
     * @param x509data
     * @throws XWSSecurityException
     */
    public void addX509Data(SOAPElement x509data) throws XWSSecurityException {
        try {
            X509Data x509Data = new X509Data(x509data,null);
            delegateKeyInfo.add(x509Data);
            dirty = true;
        } catch (XMLSecurityException e) {
            log.log(Level.SEVERE, "WSS0356.error.creating.x509data", e.getMessage());
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method addUnknownElement
     *
     * @param element
     */
    public void addUnknownElement(SOAPElement element) {
        delegateKeyInfo.addUnknownElement(element);
        dirty = true;
    }

    /**
     * Method keyNameCount
     *
     *
     */
    public int keyNameCount() {
        return delegateKeyInfo.lengthKeyName();
    }

    /**
     * Method keyValueCount
     *
     *
     */
    public int keyValueCount() {
        return delegateKeyInfo.lengthKeyValue();
    }

    /**
     * Method mgmtDataCount
     *
     *
     */
    public int mgmtDataCount() {
        return delegateKeyInfo.lengthMgmtData();
    }

    /**
     * Method pgpDataCount
     *
     *
     */
    public int pgpDataCount() {
        return delegateKeyInfo.lengthPGPData();
    }

    /**
     * Method retrievalMethodCount
     *
     *
     */
    public int retrievalMethodCount() {
        return delegateKeyInfo.lengthRetrievalMethod();
    }

    /**
     * Method spkiDataCount
     *
     *
     */
    public int spkiDataCount() {
        return delegateKeyInfo.lengthSPKIData();
    }

    /**
     * Method x509DataCount
     *
     *
     */
    public int x509DataCount() {
        return delegateKeyInfo.lengthX509Data();
    }

    /**
     * Method unknownElementCount
     *
     *
     */
    public int unknownElementCount() {
        return delegateKeyInfo.lengthUnknownElement();
    }

    /**
     * Method getKeyName
     *
     * @param index
     * 0 is the lowest index
     *
     * @throws XWSSecurityException
     */
    public SOAPElement getKeyName(int index) throws XWSSecurityException {
        try {
            return convertToSoapElement(delegateKeyInfo.itemKeyName(index));
        } catch (XMLSecurityException e) {
            log.log(Level.SEVERE, "WSS0320.exception.getting.keyname", e);
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getKeyNameString
     *
     * @param index
     * 0 is the lowest index 
     *
     * @throws XWSSecurityException
     */
    public String getKeyNameString(int index) throws XWSSecurityException {
        try {
            return (delegateKeyInfo.itemKeyName(index)).getKeyName();
        } catch (XMLSecurityException e) {
            log.log(Level.SEVERE, "WSS0320.exception.getting.keyname", e);
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getKeyValueElement
     *
     * @param index
     * 0 is the lowest index
     *
     * @throws XWSSecurityException
     */
    public SOAPElement getKeyValueElement(int index) throws XWSSecurityException {
        try {
            return convertToSoapElement(delegateKeyInfo.itemKeyValue(index));
        } catch (XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getKeyValue
     *
     * @param index
     * 0 is the lowest index
     *
     * @throws XWSSecurityException
     */
    public KeyValue getKeyValue(int index) throws XWSSecurityException {
        try {
            return delegateKeyInfo.itemKeyValue(index);
        } catch (XMLSecurityException e) {
            log.log(Level.SEVERE, 
                    "WSS0357.error.getting.keyvalue", 
                    new Object[] {Integer.valueOf(index), e.getMessage()});
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getMgmtData
     *
     * @param index
     * 0 is the lowest index 
     *
     * @throws XWSSecurityException
     */
    public SOAPElement getMgmtData(int index) throws XWSSecurityException {
        try {
            return convertToSoapElement(delegateKeyInfo.itemMgmtData(index));
        } catch (XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getPGPData
     *
     * @param index
     * 0 is the lowest index
     *
     * @throws XWSSecurityException
     */
    public SOAPElement getPGPData(int index) throws XWSSecurityException {
        try {
            return convertToSoapElement(delegateKeyInfo.itemPGPData(index));
        } catch (XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getRetrievalMethod
     *
     * @param index
     * 0 is the lowest index 
     *
     * @throws XWSSecurityException
     */
    public SOAPElement getRetrievalMethod(int index)
           throws XWSSecurityException {
        try {
            return convertToSoapElement(
                delegateKeyInfo.itemRetrievalMethod(index));
        } catch (XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getSPKIData
     *
     * @param index
     * 0 is the lowest index 
     *
     * @throws XWSSecurityException
     */
    public SOAPElement getSPKIData(int index) throws XWSSecurityException {
        try {
            return convertToSoapElement(delegateKeyInfo.itemSPKIData(index));
        } catch (XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getX509DataElement
     *
     * @param index
     * 0 is the lowest index
     *
     * @throws XWSSecurityException
     */
    public SOAPElement getX509DataElement(int index) throws XWSSecurityException {
        try {
            return convertToSoapElement(delegateKeyInfo.itemX509Data(index));
        } catch (XMLSecurityException e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getX509Data
     *
     * @param index
     * 0 is the lowest index
     *
     * @throws XWSSecurityException
     */
    public X509Data getX509Data(int index) throws XWSSecurityException {
        try {
            return delegateKeyInfo.itemX509Data(index);
        } catch (XMLSecurityException e) {
            log.log(Level.SEVERE, 
                    "WSS0358.error.getting.x509data", 
                    new Object[] {Integer.valueOf(index), e.getMessage()});
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getUnknownElement
     *
     * @param index
     * 0 is the lowest index
     *
     */
    public SOAPElement getUnknownElement(int index)
        throws XWSSecurityException {
        // There is bug in Apache KeyInfo.itemUnknownElement()
        // the lowest index for it is 1 (not 0).
        try {
            Element unknownElem = delegateKeyInfo.itemUnknownElement(index + 1);
            if (unknownElem instanceof SOAPElement)
                return (SOAPElement) unknownElem;
            else
                return (SOAPElement) document.importNode(unknownElem, true);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method containsKeyName
     *
     *
     */
    public boolean containsKeyName() {
        return delegateKeyInfo.containsKeyName();
    }

    /**
     * Method containsKeyValue
     *
     *
     */
    public boolean containsKeyValue() {
        return delegateKeyInfo.containsKeyValue();
    }

    /**
     * Method containsMgmtData
     *
     *
     */
    public boolean containsMgmtData() {
        return delegateKeyInfo.containsMgmtData();
    }

    /**
     * Method containsPGPData
     *
     *
     */
    public boolean containsPGPData() {
        return delegateKeyInfo.containsPGPData();
    }

    /**
     * Method containsRetrievalMethod
     *
     *
     */
    public boolean containsRetrievalMethod() {
        return delegateKeyInfo.containsRetrievalMethod();
    }

    /**
     * Method containsSPKIData
     *
     *
     */
    public boolean containsSPKIData() {
        return delegateKeyInfo.containsSPKIData();
    }

    /**
     * Method containsUnknownElement
     *
     *
     */
    public boolean containsUnknownElement() {
        return delegateKeyInfo.containsUnknownElement();
    }

    /**
     * Method containsX509Data
     *
     *
     */
    public boolean containsX509Data() {
        return delegateKeyInfo.containsX509Data();
    }


    // WSS spec allows a SecurityTokenReference element to be a direct
    // child of ds:KeyInfo

    /**
     * Method addSecurityTokenReference
     *
     * @param reference
     */
    public void addSecurityTokenReference(SecurityTokenReference reference) 
        throws XWSSecurityException {
        delegateKeyInfo.addUnknownElement(reference.getAsSoapElement());
        dirty = true;
    }

    /**
     * Method getSecurityTokenReference
     *
     * @param index
     *
     * @throws XWSSecurityException
     *
     * @return the index^th token reference element from the KeyInfo
     *         0 is the lowest index.
     */
    public SecurityTokenReference getSecurityTokenReference(int index)
        throws XWSSecurityException {
        org.w3c.dom.Element delegateElement = delegateKeyInfo.getElement();
        int res = 0;
        NodeList nl = delegateElement.getChildNodes();

        for (int j = 0; j < nl.getLength(); j++) {
            Node current = nl.item(j);
            if (current.getNodeType() == Node.ELEMENT_NODE) {

               String lName = current.getLocalName();
               String nspac = current.getNamespaceURI();

               if (lName.equals(
                   MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME) &&
                   nspac.equals(MessageConstants.WSSE_NS)) { 
                if (res == index) {
                    return new SecurityTokenReference((SOAPElement) current);
                }
                res++;
              }   
            }
          }
        return null;
    }

    /**
     * Method securityTokenReferenceCount
     *
     * @return the count of security token references
     */
    public int securityTokenReferenceCount() {
        org.w3c.dom.Element delegateElement = delegateKeyInfo.getElement();
        int res = 0;
        NodeList nl = delegateElement.getChildNodes();

        for (int j = 0; j < nl.getLength(); j++) {
            Node current = nl.item(j);
            if ((current.getNodeType() == Node.ELEMENT_NODE) &&
                 MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME.equals(
                     current.getLocalName()) &&
                 MessageConstants.WSSE_NS.equals(current.getNamespaceURI())) {

                res++;
            }
        }
        return res;
    }

    /**
     * Method containsSecurityTokenReference
     *
     * @return true if this KeyInfo contains wsse:SecurityTokenReference's
     */
    public boolean containsSecurityTokenReference() {
        return (securityTokenReferenceCount() > 0);
    }
    
    /**
     * Method addEncryptedKey
     *
     * @param reference
     */
    public void addEncryptedKey(EncryptedKeyToken reference) 
        throws XWSSecurityException {
        delegateKeyInfo.addUnknownElement(reference.getAsSoapElement());
        dirty = true;
    }
    
    /**
     * Method getEncryptedKey
     *
     * @param index
     *
     * @throws XWSSecurityException
     *
     * @return the index^th token reference element from the KeyInfo
     *         0 is the lowest index.
     */
    public EncryptedKeyToken getEncryptedKey(int index)
        throws XWSSecurityException {
        org.w3c.dom.Element delegateElement = delegateKeyInfo.getElement();
        int res = 0;
        NodeList nl = delegateElement.getChildNodes();

        for (int j = 0; j < nl.getLength(); j++) {
            Node current = nl.item(j);
            if (current.getNodeType() == Node.ELEMENT_NODE) {

               String lName = current.getLocalName();
               String nspac = current.getNamespaceURI();

               if (lName.equals(
                   MessageConstants.XENC_ENCRYPTED_KEY_LNAME) &&
                   nspac.equals(MessageConstants.XENC_NS)) { 
                if (res == index) {
                    return new EncryptedKeyToken((SOAPElement) current);
                }
                res++;
              }   
            }
          }
        return null;
    }
    
    /**
     * Method encryptedKeyTokenCount
     *
     * @return the count of encrypted key token references
     */
    public int encryptedKeyTokenCount() {
        org.w3c.dom.Element delegateElement = delegateKeyInfo.getElement();
        int res = 0;
        NodeList nl = delegateElement.getChildNodes();

        for (int j = 0; j < nl.getLength(); j++) {
            Node current = nl.item(j);
            if ((current.getNodeType() == Node.ELEMENT_NODE) &&
                 MessageConstants.XENC_ENCRYPTED_KEY_LNAME.equals(
                     current.getLocalName()) &&
                 MessageConstants.XENC_NS.equals(current.getNamespaceURI())) {

                res++;
            }
        }
        return res;
    }

    /**
     * Method containsEncryptedKeyToken
     *
     * @return true if this KeyInfo contains wsse:SecurityTokenReference's
     */
    public boolean containsEncryptedKeyToken() {
        return (encryptedKeyTokenCount() > 0);
    }

   
    public BinarySecret getBinarySecret(int index)
        throws XWSSecurityException {
        org.w3c.dom.Element delegateElement = delegateKeyInfo.getElement();
        int res = 0;
        NodeList nl = delegateElement.getChildNodes();
                                                                                                                        
        for (int j = 0; j < nl.getLength(); j++) {
            Node current = nl.item(j);
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                                                                                                                        
               String lName = current.getLocalName();
               String nspac = current.getNamespaceURI();
                                                                                                                        
               if (lName.equals(
                   MessageConstants.BINARY_SECRET_LNAME) &&
                   nspac.equals(WSTrustConstants.WST_NAMESPACE)) {
                if (res == index) {
                    try {
                        return WSTrustElementFactory.newInstance().createBinarySecret((SOAPElement) current);
                    }catch (WSTrustException ex) {
                        throw new XWSSecurityException(ex);
                    }
                }
                res++;
              }
            }
          }
        return null;
    }
                                                                                                                        
    /**
     * Method binarySecretCount
     *
     * @return the count of binarySecret tokens
     */
    public int binarySecretCount() {
        org.w3c.dom.Element delegateElement = delegateKeyInfo.getElement();
        int res = 0;
        NodeList nl = delegateElement.getChildNodes();
                                                                                                                        
        for (int j = 0; j < nl.getLength(); j++) {
            Node current = nl.item(j);
            if ((current.getNodeType() == Node.ELEMENT_NODE) &&
                 MessageConstants.BINARY_SECRET_LNAME.equals(
                     current.getLocalName()) &&
                 WSTrustConstants.WST_NAMESPACE.equals(current.getNamespaceURI())) {
                                                                                                                        
                res++;
            }
        }
        return res;
    }

    /**
     * Method containsBinarySecret
     *
     * @return true if this KeyInfo contains BinarySecret
     */
    public boolean containsBinarySecret() {
        return (binarySecretCount() > 0);
    }


    /**
     * Method setId
     */
    public void setId(String id) {
        delegateKeyInfo.setId(id);
    }

    /**
     * Method getId
     *
     * @return the id
     */
    public String getId() {
       return delegateKeyInfo.getId();
    }

    /**
     * Method getKeyInfo
     *
     * @return the XML DSIG KeyInfo which is wrapped by this class
     */
    public final KeyInfo getKeyInfo() {
        return delegateKeyInfo;
    }

    /**
     * Method setBaseURI :  BaseURI accepted by Apache KeyInfo Ctor
     * @param uri  Base URI to be used as context for all relative URIs.
     */
    public void setBaseURI(String uri) {
        baseURI = uri;
    }

    /**
     * Method to return the KeyInfo as a SOAPElement.
     *
     * @return SOAPElement
     * @throws XWSSecurityException
     *     If owner soap document is not set.
     * @see #setDocument(Document)
     */
    public SOAPElement getAsSoapElement() throws XWSSecurityException {
        if (document == null) {
            throw new XWSSecurityException("Document not set");
        }
        if (dirty) {
            setSOAPElement(convertToSoapElement(delegateKeyInfo));
            dirty = false;
        }
        return delegateElement;
    }

    /**
     * setDocument
     * @param doc The owner Document of this KeyInfo
     */
    public void setDocument(Document doc) {
        this.document = doc;
    }

    // this constructor should be protected.
    /**
     * parse and create the KeyInfo element
     * @param element the KeyInfo element
     * NOTE : this constructor assumes a fully initialized XML KeyInfo
     * No additions are allowed on the keyinfo, only we can get existing
     * values. For example addkeyName() will have no impact on the KeyInfo
     * will not append a KeyName child to the KeyInfo.
     */
    public KeyInfoHeaderBlock(SOAPElement element) throws XWSSecurityException {
        super(element);
        try {
            // The BaseURI in Apache KeyInfo is seems optional
            // However the purpose of it is not clear
            this.document = element.getOwnerDocument();
            delegateKeyInfo = 
                new KeyInfo(element,baseURI);
        } catch (XMLSecurityException e) {
            log.log(
                Level.SEVERE,
                "WSS0318.exception.while.creating.keyinfoblock",
                e);
          throw new XWSSecurityException(e); 
        }
    }

   /**
    * This method should be called when changes are made inside an object
    * through its reference obtained from any of the get methods of this
    * class. As an example, if getKeyInfo() call is made and then changes are made
    * inside the keyInfo, this method should be called to reflect changes
    * when getAsSoapElement() is called finally.
    *
    */ 
    public void saveChanges() {
        dirty = true;
    }

    public static SecurityHeaderBlock fromSoapElement(SOAPElement element)
        throws XWSSecurityException {
        return SecurityHeaderBlockImpl.fromSoapElement(element, 
            KeyInfoHeaderBlock.class);
    }

    private SOAPElement convertToSoapElement(ElementProxy proxy)
        throws XWSSecurityException {
        try {
            Element elem = proxy.getElement();
            if (elem instanceof SOAPElement)
                return (SOAPElement) elem;
            else
                return (SOAPElement) document.importNode(elem, true);
        } catch (Exception e) {
            log.log(
                Level.SEVERE,
                "WSS0321.exception.converting.keyinfo.tosoapelem",
                e);
            throw new XWSSecurityException(e);
        }
    }

    public void addX509Data(X509Data x509Data) throws XWSSecurityException {
        try {
            delegateKeyInfo.add(x509Data);
            dirty = true;
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0359.error.adding.x509data", e.getMessage());
            throw new XWSSecurityException(e);
        }
    }
}
