/*
 * $Id: ReferenceListHeaderBlock.java,v 1.3.2.2 2010-07-14 14:05:32 m_potociar Exp $
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

import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

/**
 * A ReferenceList element is defined as follows:
 * <xmp>
 * <element name='ReferenceList'>
 *     <complexType>
 *         <choice minOccurs='1' maxOccurs='unbounded'>
 *             <element name='DataReference' type='xenc:ReferenceType'/>
 *             <element name='KeyReference' type='xenc:ReferenceType'/>
 *         </choice>
 *     </complexType>
 * </element>
 * </xmp>
 *
 * @author Vishal Mahajan
 */
public class ReferenceListHeaderBlock extends SecurityHeaderBlockImpl {

    private static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /**
     * TOD0 (Probably) : Currently this class supports only data references.
     */

    private Document ownerDoc;

    private int size = 0;

    /**
     * Creates an empty ReferenceList element.
     *
     * @throws XWSSecurityException
     *     If there is problem creating a ReferenceList element.
     */
    public ReferenceListHeaderBlock() throws XWSSecurityException {
        try {
            setSOAPElement(
                getSoapFactory().createElement(
                    "ReferenceList",
                    MessageConstants.XENC_PREFIX,
                    MessageConstants.XENC_NS));
            addNamespaceDeclaration(
                MessageConstants.XENC_PREFIX,
                MessageConstants.XENC_NS);
        } catch (SOAPException e) {
            log.log(Level.SEVERE, "WSS0360.error.creating.rlhb", e.getMessage());
            throw new XWSSecurityException(e);
        }
        ownerDoc = getAsSoapElement().getOwnerDocument();
    }

    /**
     * Create an empty ReferenceList element whose owner document is doc
     *
     * @throws XWSSecurityException
     *     If there is problem creating a ReferenceList element.
     */
    public ReferenceListHeaderBlock(Document doc) throws XWSSecurityException {
        try {
            setSOAPElement(
                (SOAPElement) doc.createElementNS(
                    MessageConstants.XENC_NS,
                    MessageConstants.XENC_REFERENCE_LIST_QNAME));
            addNamespaceDeclaration(
                MessageConstants.XENC_PREFIX,
                MessageConstants.XENC_NS);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0361.error.creating.rlhb", e.getMessage());
            throw new XWSSecurityException(e);
        }
        ownerDoc = doc;
    }

    /**
     * @throws XWSSecurityException
     *     If the given element does not have an appropriate name.
     */
    public ReferenceListHeaderBlock(SOAPElement element)
        throws XWSSecurityException {

        super(element);

        if (!(element.getLocalName().equals("ReferenceList") &&
              XMLUtil.inEncryptionNS(element))) {
            log.log(Level.SEVERE, "WSS0362.error.creating.rlhb", element.getTagName());
            throw new XWSSecurityException("Invalid ReferenceList passed");
        }
        ownerDoc = element.getOwnerDocument();
        size =
            element.getElementsByTagNameNS(MessageConstants.XENC_NS, "DataReference").getLength();
    }

    public int size() {
        return size;
    }

    /**
     * Adds a reference to the reference list.
     *
     * @throws XWSSecurityException
     *     If there is problem adding a reference
     */
    public void addReference(String referenceURI)
        throws XWSSecurityException {
        try {
            Element dataRefElement =
                ownerDoc.createElementNS(
                    MessageConstants.XENC_NS,
                    MessageConstants.XENC_PREFIX + ":DataReference");
            dataRefElement.setAttribute("URI", referenceURI);
            XMLUtil.prependChildElement(
                this,
                dataRefElement,
                false,
                ownerDoc);
            size ++;
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0363.error.adding.datareference", e.getMessage());
            throw new XWSSecurityException(e);
        }
    }

    /**
     * @return Iterator over referenceURI Strings
     */
    @SuppressWarnings("unchecked")
    public Iterator getReferences() {

        Vector references = new Vector();
        Iterator eachChild = getChildElements();

        while (eachChild.hasNext()) {
            Node object = (Node)eachChild.next();

            if (object.getNodeType() == Node.ELEMENT_NODE) {
                SOAPElement element = (SOAPElement) object;

                if (element.getLocalName().equals("DataReference") &&
                    XMLUtil.inEncryptionNS(element)) {
                    references.addElement(element.getAttribute("URI"));
                }
            }
        }
        return references.iterator();
    }

    public NodeList getDataRefElements() {
        return getElementsByTagNameNS(MessageConstants.XENC_NS, "DataReference");
    }


    public static SecurityHeaderBlock fromSoapElement(SOAPElement element)
        throws XWSSecurityException {
        return SecurityHeaderBlockImpl.fromSoapElement(
            element, ReferenceListHeaderBlock.class);
    }

}
