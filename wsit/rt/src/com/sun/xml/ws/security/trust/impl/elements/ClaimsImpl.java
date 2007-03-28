/*
 * $Id: ClaimsImpl.java,v 1.10 2007-03-28 19:46:55 jdg6688 Exp $
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

package com.sun.xml.ws.security.trust.impl.elements;



import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.security.trust.impl.bindings.ClaimsType;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

/**
 * Implementation class for Claims.
 *
 * @author Manveen Kaur
 */
public class ClaimsImpl extends ClaimsType implements Claims {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
     
    
    public ClaimsImpl() {
        // default constructor
    }
    
    public ClaimsImpl(String dialect) {
        setDialect(dialect);
    }
    
    public ClaimsImpl(ClaimsType clType){
        setDialect(clType.getDialect());
        getAny().addAll(clType.getAny());
        getOtherAttributes().putAll(clType.getOtherAttributes());
    }
    
    public static ClaimsType fromElement(final org.w3c.dom.Element element)
    throws WSTrustException {
        try {
            final javax.xml.bind.Unmarshaller unmarshaller = WSTrustElementFactory.getContext().createUnmarshaller();
            return (ClaimsType)((JAXBElement)unmarshaller.unmarshal(element)).getValue();
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0021_ERROR_UNMARSHAL_DOM_ELEMENT(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0021_ERROR_UNMARSHAL_DOM_ELEMENT(), ex);
        }
    }
    
}
