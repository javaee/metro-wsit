/*
 * $Id: LifetimeImpl.java,v 1.6 2007-01-12 14:44:11 raharsha Exp $
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


import javax.xml.bind.JAXBException;

import com.sun.xml.ws.security.wsu10.AttributedDateTime;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.impl.bindings.LifetimeType;

import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.istack.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import com.sun.xml.ws.security.trust.logging.LogStringsMessages;


/**
 *
 * @author Manveen Kaur
 */
public class LifetimeImpl extends LifetimeType implements Lifetime {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    public LifetimeImpl() {
        // default empty constructor
    }
    
    public LifetimeImpl(AttributedDateTime created,  AttributedDateTime expires) {
        if (created != null) {
            setCreated(created);
        }
        if (expires !=null) {
            setExpires(expires);
        }
    }
    
    public LifetimeImpl(@NotNull final LifetimeType ltType){
        this(ltType.getCreated(), ltType.getExpires());
    }
    
    /**
     * Constructs a <code>Lifetime</code> element from
     * an existing XML block.
     *
     * @param lifetimeElement A
     *        <code>org.w3c.dom.Element</code> representing DOM tree
     *        for <code>Lifetime</code> object.
     * @exception WSTrustException if it could not process the
     *            <code>org.w3c.dom.Element</code> properly, implying that
     *            there is an error in the sender or in the element definition.
     */
    public static LifetimeType fromElement(@NotNull final org.w3c.dom.Element element)
    throws WSTrustException {
        try {
            final javax.xml.bind.Unmarshaller u = WSTrustElementFactory.getContext().createUnmarshaller();
            return (LifetimeType)u.unmarshal(element);
        } catch ( JAXBException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0021_ERROR_UNMARSHAL_DOM_ELEMENT(ex));
            throw new WSTrustException("Error in unmarshalling DOM element", ex);
        }
    }
    
}
