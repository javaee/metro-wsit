/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
 * $Id: Conditions.java,v 1.2 2010-10-21 15:37:59 snajper Exp $
 */

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;

import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.bind.util.ListImpl;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.LogStringsMessages;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.ConditionsType;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.ConditionsTypeImpl;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;
import com.sun.xml.wss.util.DateUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;

/**
 * The validity of an <code>Assertion</code> MAY be subject to a set of
 * <code>Conditions</code>. Each <code>Condition</code> evaluates to a value that
 * is Valid, Invalid or Indeterminate.
 */
public class Conditions extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.ConditionsImpl
        implements com.sun.xml.wss.saml.Conditions {

    private Date notBeforeField = null;
    private Date notOnOrAfterField = null;
    
    protected static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /**
    Constructor taking in nothing (SAML spec allows it)
     */
    public Conditions() {
        super();
    }
    @SuppressWarnings("unchecked")
    private void setaudienceRestrictionConditionOrDoNotCacheConditionOrCondition(List condition) {
        this._AudienceRestrictionConditionOrDoNotCacheConditionOrCondition = new ListImpl(condition);
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
            Calendar notBefore,
            Calendar notOnOrAfter,
            List condition,
            List arc,
            List doNotCacheCnd) {

        setNotBefore(notBefore);
        setNotOnOrAfter(notOnOrAfter);

        if (condition != null) {
            setaudienceRestrictionConditionOrDoNotCacheConditionOrCondition(condition);
        } else if (arc != null) {
            setaudienceRestrictionConditionOrDoNotCacheConditionOrCondition(arc);
        } else if (doNotCacheCnd != null) {
            setaudienceRestrictionConditionOrDoNotCacheConditionOrCondition(doNotCacheCnd);
        }
    }    

    public Conditions(ConditionsType cType){                                    
            setNotBefore(cType.getNotBefore());
            setNotOnOrAfter(cType.getNotOnOrAfter());                        
            setaudienceRestrictionConditionOrDoNotCacheConditionOrCondition(
                    cType.getAudienceRestrictionConditionOrDoNotCacheConditionOrCondition());            
	}
    
    public Date getNotBeforeDate(){
        try {
            if(notBeforeField != null){
                return notBeforeField;
            }
            if(super.getNotBefore() != null){
                notBeforeField = DateUtils.stringToDate(super.getNotBefore().toString());
            }
        } catch (ParseException ex) {
            log.log(Level.SEVERE, null, ex);
        }
            return notBeforeField;
        }
        
        public Date getNotOnOrAfterDate(){
        try {
            if(notOnOrAfterField != null){
                return notOnOrAfterField;
            }
            if(super.getNotOnOrAfter() != null){
                notOnOrAfterField = DateUtils.stringToDate(super.getNotOnOrAfter().toString());
            }
        } catch (ParseException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0430_SAML_GET_NOT_BEFORE_DATE_OR_GET_NOT_ON_OR_AFTER_DATE_PARSE_FAILED(), ex);
        }
            return notOnOrAfterField;
        }
        @SuppressWarnings("unchecked")
        public List<Object> getConditions(){
            return (List<Object>) super.getAudienceRestrictionConditionOrDoNotCacheConditionOrCondition();
        }

    /**
     * Constructs a <code>Conditions</code> element from an existing XML block.
     *
     * @param element A <code>org.w3c.dom.Element</code> representing
     *        DOM tree for <code>Conditions</code> object
     * @exception SAMLException if it could not process the Element properly,
     *            implying that there is an error in the sender or in the
     *            element definition.
     */
    public static ConditionsTypeImpl fromElement(org.w3c.dom.Element element)
            throws SAMLException {
        try {
            JAXBContext jc =
                    SAMLJAXBUtil.getJAXBContext();
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (ConditionsTypeImpl) u.unmarshal(element);
        } catch (Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
}
