/*
 * $Id: EmbeddedReference.java,v 1.1 2006-05-03 22:57:35 arungupta Exp $
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

package com.sun.xml.wss.core.reference;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.core.ReferenceElement;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;

/**
 * @author Vishal Mahajan
 */
public class EmbeddedReference extends ReferenceElement {

    protected static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    private SOAPElement embeddedElement;

    /**
     * Creates an "empty" EmbeddedReference element.
     */
    public EmbeddedReference() throws XWSSecurityException {
        try {
            setSOAPElement(
                soapFactory.createElement(
                    "Embedded",
                    "wsse",
                    MessageConstants.WSSE_NS));
        } catch (SOAPException e) {
            log.log(Level.SEVERE,
                    "WSS0750.soap.exception",
                    new Object[] {"wsse:Embedded", e.getMessage()});
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Takes a SOAPElement and checks if it has the right name and structure.
     */
    public EmbeddedReference(SOAPElement element) throws XWSSecurityException {

        setSOAPElement(element);

        if (!(element.getLocalName().equals("Embedded") &&
              XMLUtil.inWsseNS(element))) {
            log.log(Level.SEVERE,
                    "WSS0752.invalid.embedded.reference");
            throw new XWSSecurityException("Invalid EmbeddedReference passed");
        }

        Iterator eachChild = getChildElements();
        Node node = null;
        while (!(node instanceof SOAPElement) && eachChild.hasNext()) {
            node = (Node) eachChild.next();
        }
        if ((node != null) && (node.getNodeType() == Node.ELEMENT_NODE)) {
            embeddedElement  = (SOAPElement) node;
        } else {
            log.log(Level.SEVERE,
                    "WSS0753.missing.embedded.token");
            throw new XWSSecurityException(
                "Passed EmbeddedReference does not contain an embedded element");
        }
    }

    /**
     * Assumes that there is a single embedded element.
     *
     * @return If no child element is present, returns null
     */
    public SOAPElement getEmbeddedSoapElement() {
        return embeddedElement;
    }

    public void setEmbeddedSoapElement(SOAPElement element)
        throws XWSSecurityException {

        if (embeddedElement != null) {
            log.log(Level.SEVERE,
                    "WSS0754.token.already.set");
            throw new XWSSecurityException(
                "Embedded element is already present");
        }
 
        embeddedElement = element;
        
        try {
            addChildElement(embeddedElement);
        } catch (SOAPException e) {
            log.log(Level.SEVERE,
                    "WSS0755.soap.exception",
                    e.getMessage()); 
            throw new XWSSecurityException(e);
        }
    }

    /**
     * If this attr is not present, returns null.
     */
    public String getWsuId() {
        String wsuId = getAttribute("wsu:Id");
        if (wsuId.equals(""))
            return null;
        return wsuId;
    }

    public void setWsuId(String wsuId) {
        setAttributeNS(
            MessageConstants.NAMESPACES_NS,
            "xmlns:" + MessageConstants.WSU_PREFIX,
            MessageConstants.WSU_NS);
        setAttributeNS(
            MessageConstants.WSU_NS,
            MessageConstants.WSU_ID_QNAME,
            wsuId);
    }

} 
