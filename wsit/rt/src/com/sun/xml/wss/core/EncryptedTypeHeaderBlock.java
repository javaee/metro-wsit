/*
 * $Id: EncryptedTypeHeaderBlock.java,v 1.3.2.2 2010-07-14 14:05:30 m_potociar Exp $
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

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

/**
 * Schema definition for an EncryptedType is as follows:
 * <xmp>
 * <complexType name='EncryptedType' abstract='true'>
 *     <sequence>
 *         <element name='EncryptionMethod' type='xenc:EncryptionMethodType'
 *             minOccurs='0'/>
 *         <element ref='ds:KeyInfo' minOccurs='0'/>
 *         <element ref='xenc:CipherData'/>
 *         <element ref='xenc:EncryptionProperties' minOccurs='0'/>
 *     </sequence>
 *     <attribute name='Id' type='ID' use='optional'/>
 *     <attribute name='Type' type='anyURI' use='optional'/>
 *     <attribute name='MimeType' type='string' use='optional'/>
 *     <attribute name='Encoding' type='anyURI' use='optional'/>
 * </complexType>
 * </xmp>
 *
 * @author Vishal Mahajan
 */
public abstract class EncryptedTypeHeaderBlock extends SecurityHeaderBlockImpl {

    SOAPElement encryptionMethod;

    KeyInfoHeaderBlock keyInfo;

    SOAPElement cipherData;

    SOAPElement encryptionProperties;

    boolean updateRequired = false;

    protected static final Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /**
     * Returns null if id attr is not present
     */
    public String getId() {
        String id = getAttribute("Id");
        if (id.equals(""))
            return null;
        return id;
    }

    public void setId(String id) {
        setAttribute("Id", id);
        setIdAttribute("Id", true);
    }

    /**
     * Returns null if Type attr is not present
     */
    public String getType() {
        String type = getAttribute("Type");
        if (type.equals(""))
            return null;
        return type;
    }

    public void setType(String type) {
        setAttribute("Type", type);
    }

    /**
     * Returns null if MimeType attr is not present
     */
    public String getMimeType() {
        String mimeType = getAttribute("MimeType");
        if (mimeType.equals(""))
            return null;
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        setAttribute("MimeType", mimeType);
    }

    /**
     * Returns null if Encoding attr is not present
     */
    public String getEncoding() {
        String encoding = getAttribute("Encoding");
        if (encoding.equals(""))
            return null;
        return encoding;
    }

    public void setEncoding(String encoding) {
        setAttribute("Encoding", encoding);
    }

    public SOAPElement getEncryptionMethod() {
        return encryptionMethod;
    }

    /**
     * returns the algorithm URI
     */
    public String getEncryptionMethodURI() {
        if (encryptionMethod == null)
            return null;
        return encryptionMethod.getAttribute("Algorithm");
    }

    public void setEncryptionMethod(SOAPElement encryptionMethod) {
        this.encryptionMethod = encryptionMethod;
        updateRequired = true;
    }

    public void setEncryptionMethod(String algorithmURI)
        throws XWSSecurityException {

        try {
            encryptionMethod =
                getSoapFactory().createElement(
                    "EncryptionMethod",
                    MessageConstants.XENC_PREFIX,
                    MessageConstants.XENC_NS);
        } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0351.error.setting.encryption.method", e.getMessage());
            throw new XWSSecurityException(e);
        }
        encryptionMethod.setAttribute("Algorithm", algorithmURI);
        updateRequired = true;
    }

    public KeyInfoHeaderBlock getKeyInfo() {
        return keyInfo;
    }

    public void setKeyInfo(KeyInfoHeaderBlock keyInfo) {
        this.keyInfo = keyInfo;
        updateRequired = true;
    }

    /**
     * @throws XWSSecurityException
     *     If CipherData element is not present OR
     *     If CipherValue element is not present inside CipherData.
     */
    public String getCipherValue() throws XWSSecurityException {

        if (cipherData == null) {
            log.log(Level.SEVERE, "WSS0347.missing.cipher.data");
            throw new XWSSecurityException("Cipher data has not been set");
        }

        Iterator cipherValues = null;
        try {
            cipherValues =
                cipherData.getChildElements(
                    getSoapFactory().createName(
                        "CipherValue",
                        MessageConstants.XENC_PREFIX,
                        MessageConstants.XENC_NS));
        } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0352.error.getting.cipherValue", e.getMessage());
            throw new XWSSecurityException(e);
        }

        if (!cipherValues.hasNext()) {
            log.log(Level.SEVERE, "WSS0353.missing.cipherValue");
            throw new XWSSecurityException("Cipher Value not present");
        }

        return getFullTextChildrenFromElement((SOAPElement) cipherValues.next()); 
    }

    public SOAPElement getCipherData(boolean create) throws XWSSecurityException {
        if (cipherData == null && create) {
            try {
                cipherData =
                    getSoapFactory().createElement(
                       "CipherData",
                       MessageConstants.XENC_PREFIX,
                       MessageConstants.XENC_NS);
            } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0395.creating.cipherData");
            throw new XWSSecurityException(e);
            }  
        }

        return cipherData;
    }

    public SOAPElement getCipherReference(boolean create, String uri) throws XWSSecurityException {
        SOAPElement cipherReference = null;
        if (create) { 
            try {
                cipherReference =
                    getSoapFactory().createElement(
                        "CipherReference",
                        MessageConstants.XENC_PREFIX,
                        MessageConstants.XENC_NS);

                cipherReference.setAttribute("URI", uri);

                getCipherData(create).addChildElement(cipherReference);
            } catch (SOAPException e) {
                // log
                throw new XWSSecurityException(e);
            }
        } else {
            if (cipherData == null) {
                // log
                throw new XWSSecurityException("CipherData is not present");
            } 

            // need to check
            //Iterator i = cipherData.getChildElements();  
            //cipherReference = (SOAPElement)i.next();
            NodeList nl =  cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherReference");
            if(nl.getLength() > 0)return (SOAPElement)nl.item(0);
        }

        return cipherReference; 
    }

    public void addTransform(String algorithmURI) throws XWSSecurityException {
        SOAPElement cipherReference = getCipherReference(false, null);
    
        try {
           SOAPElement dsTransform = 
                getSoapFactory().createElement(
                    "Transform",
                    MessageConstants.DSIG_PREFIX,
                    MessageConstants.DSIG_NS);

           dsTransform.setAttribute("Algorithm", algorithmURI); 

           // need to check
           SOAPElement xencTransforms = null;
           Iterator i = cipherReference.getChildElements();
           if (i==null || !i.hasNext()) {
               xencTransforms =
                   getSoapFactory().createElement(
                       "Transforms",
                       MessageConstants.XENC_PREFIX,
                       MessageConstants.XENC_NS);

               xencTransforms = cipherReference.addChildElement(xencTransforms);
           } else
               xencTransforms = (SOAPElement)i.next();

           xencTransforms.addChildElement(dsTransform);
           
        } catch (SOAPException e) {
           // log
           throw new XWSSecurityException(e);
        }
    }

    public Iterator getTransforms() throws XWSSecurityException {
        SOAPElement cr =  getCipherReference(false, null);
        if(cr != null) {            
           Iterator it = cr.getChildElements();
           if(it.hasNext()){
               SOAPElement transforms = (SOAPElement)it.next();
               return transforms.getChildElements();
           }
        }
        return null;        
    }

    public SOAPElement getEncryptionProperties() {
        return encryptionProperties;
    }

    public void setEncryptionProperties(SOAPElement encryptionProperties) {
        this.encryptionProperties = encryptionProperties;
        updateRequired = true;
    }

    /**
     * This method should be called when changes are made inside an object
     * through its reference obtained from any of the get methods of this
     * class. For example, if getKeyInfo() call is made and then changes are made
     * inside the keyInfo, this method should be called to reflect changes
     * in the EncryptedType.
     */
    public void saveChanges() {
        updateRequired = true;
    }

    public void initializeEncryptedType(SOAPElement element)
           throws XWSSecurityException {
        try {
            Iterator cnodes = element.getChildElements();
            while (cnodes.hasNext()) {
                Node se = (Node)cnodes.next();

                while (cnodes.hasNext() && !(se.getNodeType() == Node.ELEMENT_NODE)) se = (Node)cnodes.next();

                if ((se == null) || !(se.getNodeType() == Node.ELEMENT_NODE)) break;
                       
                if (((SOAPElement)se).getLocalName().equals("EncryptionMethod"))
                    encryptionMethod = (SOAPElement)se;
                else
                if (((SOAPElement)se).getLocalName().equals("CipherData"))
                    cipherData = (SOAPElement)se;
                else
                if (((SOAPElement)se).getLocalName().equals("KeyInfo")) 
                    keyInfo = new KeyInfoHeaderBlock(
                                   new com.sun.org.apache.xml.internal.security.keys.KeyInfo((Element)se, null));
            }  

        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0354.error.initializing.encryptedType", e.getMessage());
            throw new XWSSecurityException(e);
        } 
    }

    /**
     * @throws XWSSecurityException
     *     If there is problem in initializing the EncryptedType
     *
    public void initializeEncryptedType(SOAPElement element)
        throws XWSSecurityException {

        try {   
        
            Iterator encryptionMethods =
                getChildElements(
                    getSoapFactory().createName(
                        "EncryptionMethod",
                        MessageConstants.XENC_PREFIX,
                        MessageConstants.XENC_NS));
            if (encryptionMethods.hasNext())
                this.encryptionMethod = (SOAPElement) encryptionMethods.next();

            Iterator keyInfos =
                getChildElements(
                    getSoapFactory().createName(
                        "KeyInfo",
                        MessageConstants.DSIG_PREFIX,
                        MessageConstants.DSIG_NS));
            if (keyInfos.hasNext())
                this.keyInfo =
                    new KeyInfoHeaderBlock((SOAPElement) keyInfos.next());

            Iterator cipherDatas = 
                getChildElements(
                    getSoapFactory().createName(
                        "CipherData",
                        MessageConstants.XENC_PREFIX,
                        MessageConstants.XENC_NS));
            if (cipherDatas.hasNext())
                this.cipherData = (SOAPElement) cipherDatas.next();
            else {
                log.log(Level.SEVERE, "WSS0347.missing.cipher.data");
                throw new XWSSecurityException(
                    "CipherData is not present inside EncryptedType");
            }

            Iterator allEncryptionProperties =
                getChildElements(
                    getSoapFactory().createName(
                        "EncryptionProperties",
                        MessageConstants.XENC_PREFIX,
                        MessageConstants.XENC_NS));
            if (allEncryptionProperties.hasNext())
                this.encryptionProperties =
                    (SOAPElement) allEncryptionProperties.next(); 

        } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0354.error.initializing.encryptedType", e.getMessage());
            throw new XWSSecurityException(e);
        }
    } */

    /**
         * From XMLUtil.getFullTextChildrenFromElement() apache xml-secuirty
         * 16may03
         *
         * @param element
         */
    private String getFullTextChildrenFromElement(Element element) {

        StringBuffer sb = new StringBuffer();
        NodeList children = element.getChildNodes();
        int iMax = children.getLength();

        for (int i = 0; i < iMax; i++) {
            Node curr = children.item(i);

            if (curr.getNodeType() == Node.TEXT_NODE) {
                sb.append(((Text) curr).getData());
            }
        }

        return sb.toString();
    }
}
