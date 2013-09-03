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

/**
 *
 * @author SAAJ RI Development Team
 */
package com.sun.xml.messaging.saaj.soap.ver1_2;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.soap.Node;
import com.sun.xml.messaging.saaj.SOAPExceptionImpl;
import com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl;
import com.sun.xml.messaging.saaj.soap.impl.EnvelopeImpl;
import com.sun.xml.messaging.saaj.soap.name.NameImpl;


public class ExpressEnvelope1_2Impl extends Envelope1_2Impl  {
    
    protected static final Logger log =
            Logger.getLogger(ExpressEnvelope1_2Impl.class.getName(),
            "com.sun.xml.messaging.saaj.soap.ver1_2.LocalStrings");
    
    public ExpressEnvelope1_2Impl(SOAPDocumentImpl ownerDoc, String prefix) {
        super(ownerDoc, prefix);
    }
    
    public ExpressEnvelope1_2Impl(
            SOAPDocumentImpl ownerDoc,
            String prefix,
            boolean createHeader,
            boolean createBody)
            throws SOAPException {
        super(
                ownerDoc,
                prefix,
                createHeader,
                createBody);
    }
    
    protected NameImpl getBodyName(String prefix) {
        return NameImpl.createBody1_2Name(prefix);
    }
    
    protected NameImpl getHeaderName(String prefix) {
        return NameImpl.createHeader1_2Name(prefix);
    }
    
    /*
     * Override setEncodingStyle of ElementImpl to restrict adding encodingStyle
     * attribute to SOAP Envelope (SOAP 1.2 spec, part 1, section 5.1.1)
     */
    public void setEncodingStyle(String encodingStyle) throws SOAPException {
        log.severe("SAAJ0404.ver1_2.no.encodingStyle.in.envelope");
        throw new SOAPExceptionImpl("encodingStyle attribute cannot appear on Envelope");
    }
    
    /*
     * Override addAttribute of ElementImpl to restrict adding encodingStyle
     * attribute to SOAP Envelope (SOAP 1.2 spec, part 1, section 5.1.1)
     */
    public SOAPElement addAttribute(Name name, String value)
    throws SOAPException {
        if (name.getLocalName().equals("encodingStyle")
        && name.getURI().equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
            setEncodingStyle(value);
        }
        return super.addAttribute(name, value);
    }
    
    public SOAPElement addAttribute(QName name, String value)
    throws SOAPException {
        if (name.getLocalPart().equals("encodingStyle")
        && name.getNamespaceURI().equals(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
            setEncodingStyle(value);
        }
        return super.addAttribute(name, value);
    }
    
    
    /*
     * Override addChildElement method to ensure that no element
     * is added after body in SOAP 1.2.
     */
    public SOAPElement addChildElement(Name name) throws SOAPException {
        // check if body already exists
        if (getBody() != null) {
            log.severe("SAAJ0405.ver1_2.body.must.last.in.envelope");
            throw new SOAPExceptionImpl(
                    "Body must be the last element in" + " SOAP Envelope");
        }
        return super.addChildElement(name);
    }
    
    public SOAPElement addChildElement(QName name) throws SOAPException {
        // check if body already exists
        if (getBody() != null) {
            log.severe("SAAJ0405.ver1_2.body.must.last.in.envelope");
            throw new SOAPExceptionImpl(
                    "Body must be the last element in" + " SOAP Envelope");
        }
        return super.addChildElement(name);
    }
    
    
    /*
     * Ideally we should be overriding other addChildElement() methods as well
     * but we are not adding them here since internally all those call the
     * method addChildElement(Name name).
     * In future, if this behaviour changes, then we would need to override
     * all the rest of them as well.
     *
     */
    
    public SOAPElement addTextNode(String text) throws SOAPException {
        log.log(
                Level.SEVERE,
                "SAAJ0416.ver1_2.adding.text.not.legal",
                getElementQName());
        throw new SOAPExceptionImpl("Adding text to SOAP 1.2 Envelope is not legal");
    }
    
    public SOAPBody getBodyWC(){
        return body;
        
    }
    
    public SOAPHeader getHeaderWC(){
        return header;
    }
}
