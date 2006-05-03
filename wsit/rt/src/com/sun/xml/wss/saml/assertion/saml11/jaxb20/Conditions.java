/*
 * $Id: Conditions.java,v 1.1 2006-05-03 22:58:14 arungupta Exp $
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
import com.sun.xml.wss.saml.internal.saml11.jaxb20.ConditionsType;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * The validity of an <code>Assertion</code> MAY be subject to a set of
 * <code>Conditions</code>. Each <code>Condition</code> evaluates to a value that
 * is Valid, Invalid or Indeterminate.
 */
public class Conditions extends ConditionsType
    implements com.sun.xml.wss.saml.Conditions {
    
	protected static Logger log =
		Logger.getLogger(
			LogDomainConstants.WSS_API_DOMAIN,
			LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

	/**
	Constructor taking in nothing (SAML spec allows it)
	*/
	public Conditions() {
            super();
	}

	
        private void setaudienceRestrictionConditionOrDoNotCacheConditionOrCondition(List condition ) {
            this.audienceRestrictionConditionOrDoNotCacheConditionOrCondition = condition;
        }
        
	/**
	 * Constructs an instance of <code>Conditions</code>.
	 *
	 * @param notBefore specifies the earliest time instant at which the
	 *        assertion is valid.
	 * @param notOnOrAfter specifies the time instant at which the assertion
	 *        has expired.
	 * @param condition
	 * @param arc the <code>AudienceRestrictionCondition</code> to be
	 *        added. Can be null, if no audience restriction.
	 * @param doNotCacheCnd
	 * @exception SAMLException if there is a problem in input data and it
	 *            cannot be processed correctly.
	 */
	public Conditions(
		GregorianCalendar notBefore,
		GregorianCalendar notOnOrAfter,
		List condition,
		List arc,
		List doNotCacheCnd)
		{
            
            DatatypeFactory factory = null;
            try {
                factory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                factory = null;
            }
            
            if ( factory != null) {
            setNotBefore(factory.newXMLGregorianCalendar(notBefore));
            setNotOnOrAfter(factory.newXMLGregorianCalendar(notOnOrAfter));
            }
            
            if ( condition != null) {
                setaudienceRestrictionConditionOrDoNotCacheConditionOrCondition(condition);
            } else if ( arc != null) {
                setaudienceRestrictionConditionOrDoNotCacheConditionOrCondition(arc);
            } else if ( doNotCacheCnd != null) {
                setaudienceRestrictionConditionOrDoNotCacheConditionOrCondition(doNotCacheCnd);
            }
	}

	/**
	 * Constructs a <code>Conditions</code> element from an existing XML block.
	 *
	 * @param conditionsElement A <code>org.w3c.dom.Element</code> representing
	 *        DOM tree for <code>Conditions</code> object
	 * @exception SAMLException if it could not process the Element properly,
	 *            implying that there is an error in the sender or in the
	 *            element definition.
	 */
	public static ConditionsType fromElement(org.w3c.dom.Element element)
		throws SAMLException {
            try {
                JAXBContext jc =
                    JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
                javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (ConditionsType)u.unmarshal(element);
            } catch ( Exception ex) {
                throw new SAMLException(ex.getMessage());
            }
	}
}
