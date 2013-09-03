/*
 * $Id: DirectReference.java,v 1.5 2008/07/03 05:28:50 ofung Exp $
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

package com.sun.xml.wss.core.reference;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;


import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.core.ReferenceElement;

/**
 * @author Vishal Mahajan
 */
public class DirectReference extends ReferenceElement {

    protected static final Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /**
     * Creates a DirectReference element.
     */
    public DirectReference() throws XWSSecurityException {
        try {
            setSOAPElement(
                soapFactory.createElement(
                    "Reference",
                    "wsse",
                    MessageConstants.WSSE_NS));
        } catch (SOAPException e) {
            log.log(Level.SEVERE,
                    "WSS0750.soap.exception",
                    new Object[] {"wsse:Reference", e.getMessage()});
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Takes a SOAPElement and checks if it has the right name.
     */
    public DirectReference(SOAPElement element, boolean isBSP) throws XWSSecurityException {
        setSOAPElement(element);
        if (!(element.getLocalName().equals("Reference") &&
              XMLUtil.inWsseNS(element))) {
            log.log(Level.SEVERE,
                    "WSS0751.invalid.direct.reference",
                    "{"+element.getNamespaceURI()+"}"+element.getLocalName());
            throw new XWSSecurityException("Invalid DirectReference passed");
        }
 
        if (isBSP && (getURI()==null)) {
                throw new XWSSecurityException("Violation of BSP R3062" + 
                        ": A wsse:Reference element in a SECURITY_TOKEN_REFERENCE MUST specify a URI attribute");
        }
    }

    public DirectReference(SOAPElement element) throws XWSSecurityException {
        this(element, false);
    }

    /**
     * If this attr is not present, returns null.
     */
    public String getValueType() {
        String valueType = getAttribute("ValueType");
        if (valueType.equals(""))
            return null;
        return valueType;
    }

    public void setValueType(String valueType) {
        setAttribute("ValueType", valueType);
    }

    /**
     * @return URI attr value.
     *         If this attr is not present, returns null.
     */
    public String getURI() {
        String uri = getAttribute("URI");
        if (uri.equals(""))
            return null;
        return uri;
    }

    /**
     * @param uri Value to be assigned to URI attr.
     */
    public void setURI(String uri) {
        setAttribute("URI", uri);
    }

    /**
     * @param uri Value to be assigned to URI attr.
     */
    public void setSCTURI(String uri, String instance) {
        setAttribute("URI", uri);
        //setAttributeNS(MessageConstants.WSSC_NS, MessageConstants.WSSC_PREFIX + ":Instance" , instance);
        setAttribute("ValueType", MessageConstants.SCT_VALUETYPE);
    }
} 
