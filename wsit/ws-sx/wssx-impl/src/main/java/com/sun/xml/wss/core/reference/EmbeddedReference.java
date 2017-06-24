/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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
 * $Id: EmbeddedReference.java,v 1.2 2010-10-21 15:37:14 snajper Exp $
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

    protected static final Logger log =
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
