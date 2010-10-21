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
 * $Id: AudienceRestrictionCondition.java,v 1.2 2010-10-21 15:37:59 snajper Exp $
 */

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;

import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.bind.util.ListImpl;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AudienceRestrictionConditionTypeImpl;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;

/**
 * This is an implementation of the abstract <code>Condition</code> class, which
 * specifes that the assertion this AuthenticationCondition is part of, is
 *addressed to one or more specific audience.
 */
public class AudienceRestrictionCondition extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AudienceRestrictionConditionImpl 
    implements com.sun.xml.wss.saml.AudienceRestrictionCondition {
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    @SuppressWarnings("unchecked")
    private void setAudience(List audience) {
        _Audience = new ListImpl(audience);
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
    public static AudienceRestrictionConditionTypeImpl fromElement(org.w3c.dom.Element element)
        throws SAMLException {
        try {
            JAXBContext jc =
                    SAMLJAXBUtil.getJAXBContext();
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AudienceRestrictionConditionTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
     public List<String> getAudience() {
         List base = super.getAudience();
         List<String> ret = new ArrayList<String>();
         for (Object obj : base) {
             ret.add((String)obj);
         }
         return ret;
     }
}
