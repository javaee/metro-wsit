/*
 * $Id: Advice.java,v 1.4.2.2 2010-07-14 14:08:31 m_potociar Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AdviceTypeImpl;

import com.sun.xml.bind.util.ListImpl;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.AdviceType;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;

import javax.xml.bind.JAXBContext;
import org.w3c.dom.Element;

import java.util.List;
import java.util.logging.Logger;


/**
 *The <code>Advice</code> element contains additional information that the issuer wishes to
 *provide. This information MAY be ignored by applications without affecting
 *either the semantics or validity. Advice elements MAY be specified in
 *an extension schema.
 */
public class Advice  extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AdviceImpl 
        implements com.sun.xml.wss.saml.Advice {    
   
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    public static AdviceTypeImpl fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc =
                    SAMLJAXBUtil.getJAXBContext();
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AdviceTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void setAssertionIDReferenceOrAssertionOrAny(
            List assertionIDReferenceOrAssertionOrAny) {
        this._AssertionIDReferenceOrAssertionOrAny = new ListImpl(assertionIDReferenceOrAssertionOrAny);
    }
    
    /**
     * Constructor
     *
     * @param assertionidreference A List of <code>AssertionIDReference</code>.
     * @param assertion A List of Assertion
     * @param otherelement A List of any element defined as
     *        <code>&lt;any namespace="##other" processContents="lax"&gt;</code>;
     */
    public Advice(List assertionidreference, List assertion, List otherelement) {
        if ( null != assertionidreference ) {
            setAssertionIDReferenceOrAssertionOrAny(assertionidreference);
        } else if ( null != assertion) {
            setAssertionIDReferenceOrAssertionOrAny(assertion);
        } else if ( null != otherelement) {
            setAssertionIDReferenceOrAssertionOrAny(otherelement);
        }
    }
    
    public Advice(AdviceType adviceType) {
        if(adviceType != null){
            setAssertionIDReferenceOrAssertionOrAny(adviceType.getAssertionIDReferenceOrAssertionOrAny());
        }
    }
    @SuppressWarnings("unchecked")
    public List<Object> getAdvice(){
        return super.getAssertionIDReferenceOrAssertionOrAny();
    }
}
