/*
 * $Id: NameID.java,v 1.1 2010-03-20 12:33:30 kumarjayanti Exp $
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

package com.sun.xml.wss.saml.assertion.saml20.jaxb20;

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.NameIDType;
import com.sun.xml.wss.saml.util.SAML20JAXBUtil;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;

/**
 *The NameID element specifies a <code>Subject</code> by a combination
 * of a name and a security domain governing the name of the <code>Subject</code>.
 */
public class NameID extends NameIDType implements com.sun.xml.wss.saml.NameID {

    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);


    /**
     * Constructs a <code>NameIdentifer</code> element from an existing XML
     * block.
     *
     * @param NameIDElement A <code>org.w3c.dom.Element</code>
     *        representing DOM tree for <code>NameID</code> object
     * @exception SAMLException if it could not process the
     *            <code>org.w3c.dom.Element</code> properly, implying that there
     *            is an error in the sender or in the element definition.
     */
    public static NameIDType fromElement(org.w3c.dom.Element element)
        throws SAMLException {
        try {
            JAXBContext jc = SAML20JAXBUtil.getJAXBContext();
                
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (NameIDType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    
    /**
     * Constructs a <code>NameQualifier</code> instance.
     *
     * @param name The string representing the name of the Subject
     * @param nameQualifier The security or administrative domain that qualifies
     *        the name of the <code>Subject</code>. This is optional could be
     *        null or "".
     * @param format The syntax used to describe the name of the
     *        <code>Subject</code>. This optional, could be null or "".
     * @exception SAMLException if the input has an error.
     */
    public NameID(String name, String nameQualifier, String format)
        {
        if ( name != null)
            setValue(name);
        
        if ( nameQualifier != null)
            setNameQualifier(nameQualifier);
        
        if ( format != null)
            setFormat(format);
    }
    
    public NameID(NameIDType nameIdType){        
        setValue(nameIdType.getValue());
        setNameQualifier(nameIdType.getNameQualifier());
        setFormat(nameIdType.getFormat());
    }
}
