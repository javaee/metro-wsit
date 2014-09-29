/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2014 Oracle and/or its affiliates. All rights reserved.
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
 * $Id: X509IssuerSerial.java,v 1.2 2010-10-21 15:37:14 snajper Exp $
 */

package com.sun.xml.wss.core.reference;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;

import org.apache.xml.security.keys.content.x509.XMLX509IssuerSerial;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.core.ReferenceElement;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;

/**
 * @author Vishal Mahajan
 */
public class X509IssuerSerial extends ReferenceElement {

    private static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    private XMLX509IssuerSerial delegate;
    private X509Certificate cert = null;

    private Document ownerDoc;

    /**
    * Constructor X509IssuerSerial
    *
    * @param element
    */
    public X509IssuerSerial(SOAPElement element)
        throws XWSSecurityException {

        boolean throwAnException = false;
        if (!(element.getLocalName().equals("X509Data") &&
              XMLUtil.inSignatureNS(element))) {
              throwAnException = true;
        }

        SOAPElement issuerSerialElement;
        try {
            issuerSerialElement =
                (SOAPElement) element.getChildElements(
                    soapFactory.createName(
                        "X509IssuerSerial",
                        MessageConstants.DSIG_PREFIX,
                        MessageConstants.DSIG_NS)).next();
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    "WSS0750.soap.exception",
                    new Object[] {"ds:X509IssuerSerial", e.getMessage()}); 
            throw new XWSSecurityException(e);
        }

        Iterator issuerNames;
        try {
            issuerNames =
                issuerSerialElement.getChildElements(
                    soapFactory.createName(
                        "X509IssuerName",
                        MessageConstants.DSIG_PREFIX,
                        MessageConstants.DSIG_NS));
        } catch (SOAPException e) {
            log.log(Level.SEVERE,
                    "WSS0758.soap.exception",
                    new Object[] {"ds:X509IssuerName", e.getMessage()}); 
            throw new XWSSecurityException(e);
        }
        if (!issuerNames.hasNext())
            throwAnException = true;
        SOAPElement issuerNameElement = (SOAPElement) issuerNames.next();
        String issuerName =
            XMLUtil.getFullTextFromChildren(issuerNameElement);

        Iterator serialNumbers;
        try {
            serialNumbers =
                issuerSerialElement.getChildElements(
                    soapFactory.createName(
                        "X509SerialNumber",
                        MessageConstants.DSIG_PREFIX,
                        MessageConstants.DSIG_NS));
        } catch (SOAPException e) {
            log.log(Level.SEVERE,
                    "WSS0758.soap.exception",
                    new Object[] {"ds:X509SerialNumber", e.getMessage()});
            throw new XWSSecurityException(e);
        }
        if (!serialNumbers.hasNext())
            throwAnException = true;
        SOAPElement serialNumberElement = (SOAPElement) serialNumbers.next();
        String serialNumberString =
            XMLUtil.getFullTextFromChildren(serialNumberElement);
        BigInteger serialNumber = new BigInteger(serialNumberString);

        if (throwAnException) {
            log.log(Level.SEVERE,
                    "WSS0759.error.creating.issuerserial");
            throw new XWSSecurityException(
                "Cannot create X509IssuerSerial object out of given element");
        }
        ownerDoc = element.getOwnerDocument();
        delegate =
            new XMLX509IssuerSerial(ownerDoc, issuerName, serialNumber);
    }

    /**
    * Constructor X509IssuerSerial
    *
    * @param doc
    * @param X509IssuerName
    * @param X509SerialNumber
    */
    public X509IssuerSerial(
        Document doc,
        String X509IssuerName,
        BigInteger X509SerialNumber) {
        delegate =
            new XMLX509IssuerSerial(doc, X509IssuerName, X509SerialNumber);
        ownerDoc = doc;
    }

    /**
     * Constructor X509IssuerSerial
     *
     * @param doc
     * @param X509IssuerName
     * @param X509SerialNumber
     */
    public X509IssuerSerial(
        Document doc,
        String X509IssuerName,
        String X509SerialNumber) {
        delegate =
            new XMLX509IssuerSerial(doc, X509IssuerName, X509SerialNumber);
        ownerDoc = doc;
    }

    /**
     * Constructor X509IssuerSerial
     *
     * @param doc
     * @param X509IssuerName
     * @param X509SerialNumber
     */
    public X509IssuerSerial(
        Document doc,
        String X509IssuerName,
        int X509SerialNumber) {
        delegate =
            new XMLX509IssuerSerial(doc, X509IssuerName, X509SerialNumber);
        ownerDoc = doc;
    }

    /**
     * Constructor X509IssuerSerial
     *
     * @param doc
     * @param x509certificate
     */
    public X509IssuerSerial(Document doc, X509Certificate x509certificate) {
        delegate = new XMLX509IssuerSerial(doc, x509certificate);
        ownerDoc = doc;
    }

    /**
     * Method getSerialNumber
     *
     * @throws XWSSecurityException
     */
    public BigInteger getSerialNumber() throws XWSSecurityException {
        try {
            return delegate.getSerialNumber();
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getSerialNumberInteger
     *
     * @throws XWSSecurityException
     */
    public int getSerialNumberInteger() throws XWSSecurityException {
        try {
            return delegate.getSerialNumberInteger();
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Method getIssuerName
     *
     * @throws XWSSecurityException
     */
    public String getIssuerName() throws XWSSecurityException {
        try {
            return delegate.getIssuerName();
        } catch (Exception e) {
            log.log(Level.SEVERE,
                "WSS0763.exception.issuername", 
                new Object[] {e.getMessage()});             
            throw new XWSSecurityException(e);
        }
    }

    public SOAPElement getAsSoapElement() throws XWSSecurityException {
        try {
            SOAPElement issuerSerialElement = (SOAPElement) delegate.getElement();
            SOAPElement x509DataElement =
                (SOAPElement) ownerDoc.createElementNS(
                    MessageConstants.DSIG_NS,
                    MessageConstants.DSIG_PREFIX + ":X509Data");
            x509DataElement.addNamespaceDeclaration(
                MessageConstants.DSIG_PREFIX, MessageConstants.DSIG_NS);
            x509DataElement.addChildElement(issuerSerialElement);
            setSOAPElement(x509DataElement);
            return x509DataElement;
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    "WSS0750.soap.exception",
                    new Object[] {"ds:X509IssuerSerial", e.getMessage()}); 
            throw new XWSSecurityException(e);
        }
    }
    
    public void setCertificate(X509Certificate cert){
        this.cert = cert;
    }
 
    public X509Certificate getCertificate(){
        return cert;
    }
}
