/*
 * $Id: SubjectLocality.java,v 1.1 2006-05-03 22:58:12 arungupta Exp $
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

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


/**
The <code>SubjectLocality</code> element specifies the DNS domain name
and IP address for the system entity that performed the authentication.
It exists as part of <code>AuthenticationStatement</code> element.
*/
public class SubjectLocality extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityImpl 
    implements com.sun.xml.wss.saml.SubjectLocality {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /**
    Constructor
    Constructor taking in nothing (assertion schema 25 allows it )
    */
    public SubjectLocality() {
        super();
    }

    /**
     * Constructs an instance of <code>SubjectLocality</code> from an existing
     * XML block.
     *
     * @param localityElement A <code>org.w3c.dom.Element</code> representing
     *        DOM tree for <code>SubjectLocality</code> object.
     * @exception SAMLException if it could not process the Element properly,
     *            implying that there is an error in the sender or in the
     *            element definition.
     */
    public static SubjectLocalityTypeImpl fromElement(org.w3c.dom.Element element)
        throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb10");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (SubjectLocalityTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    /**
     * Constructs an instance of <code>SubjectLocality</code>.
     *
     * @param ipAddress String representing the IP Address of the entity
     *        that was authenticated.
     * @param dnsAddress String representing the DNS Address of the entity that
     *        was authenticated. As per SAML specification  they are both
     *        optional, so values can be null.
     */
    public SubjectLocality(String ipAddress, String dnsAddress) {
        setIPAddress(ipAddress);
        setDNSAddress(dnsAddress);
    }
}
