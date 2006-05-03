/*
 * $Id: AudienceRestrictionCondition.java,v 1.1 2006-05-03 22:58:13 arungupta Exp $
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

import com.sun.xml.wss.saml.SAMLException;


import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AudienceRestrictionConditionType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * This is an implementation of the abstract <code>Condition</code> class, which
 * specifes that the assertion this AuthenticationCondition is part of, is
 *addressed to one or more specific audience.
 */
public class AudienceRestrictionCondition extends AudienceRestrictionConditionType
    implements com.sun.xml.wss.saml.AudienceRestrictionCondition {
    
    protected static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    
    private void setAudience(List audience) {
        this.audience = audience;
    }
    
    /**
    This constructor takes in a <code>List</code> of audience for this
    condition, each of them being a String.
    @param audience A List of audience to be included within this condition
    @exception SAMLException if the <code>List</code> is empty or if there is
    some error in processing the contents of the <code>List</code>
    */
    public AudienceRestrictionCondition(List audience) {
        setAudience(audience);
    }

    /**
     * Constructs an <code>AudienceRestrictionCondition</code> element from an
     * existing XML block.
     *
     * @param audienceRestrictionConditionElement A
     *        <code>org.w3c.dom.Element</code> representing DOM tree for
     *        <code>AudienceRestrictionCondition</code> object.
     * @exception SAMLException if it could not process the
     *            <code>org.w3c.dom.Element</code> properly, implying that there
     *            is an error in the sender or in the element definition.
     */
    public static AudienceRestrictionConditionType fromElement(org.w3c.dom.Element element)
        throws SAMLException {
        try {
            JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AudienceRestrictionConditionType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
}
