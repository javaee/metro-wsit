/*
 * $Id: AttributeStatement.java,v 1.6 2008/07/03 05:29:14 ofung Exp $
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

import com.sun.xml.wss.saml.Attribute;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.Subject;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.AttributeStatementType;
import com.sun.xml.wss.saml.internal.saml20.jaxb20.AttributeType;
import com.sun.xml.wss.saml.util.SAML20JAXBUtil;
import java.util.ArrayList;
import java.util.Iterator;
import org.w3c.dom.Element;

import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;

/**
 *The <code>AttributeStatement</code> element supplies a statement by the issuer that the
 *specified subject is associated with the specified attributes.
 */
public class AttributeStatement extends AttributeStatementType
    implements com.sun.xml.wss.saml.AttributeStatement {
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    private List<Attribute> attValueList = null;

    private void setAttributes(List attr) {
        this.attributeOrEncryptedAttribute = attr;
    }
    
    /**
     *Dafault constructor
     */
    public AttributeStatement(List attr) {        
        setAttributes(attr);
    }
    
    public AttributeStatement(AttributeStatementType attStmtType) {        
        setAttributes(attStmtType.getAttributeOrEncryptedAttribute());
    }

    /**
     * Constructs an <code>AttributStatement</code> element from an existing
     * XML block
     * @param element representing a DOM tree element
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public static AttributeStatementType fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc = SAML20JAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AttributeStatementType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    public List<Attribute> getAttributes(){
        if(attValueList == null){
            attValueList = new ArrayList<Attribute>();
        }else{
            return attValueList;
        }
        Iterator it = super.getAttributeOrEncryptedAttribute().iterator();
        while(it.hasNext()){
            com.sun.xml.wss.saml.assertion.saml20.jaxb20.Attribute obj = 
                    new com.sun.xml.wss.saml.assertion.saml20.jaxb20.Attribute((AttributeType)it.next());
            attValueList.add(obj);
        }
        return attValueList;                 
    }

    public Subject getSubject() {
        throw new UnsupportedOperationException("getSubject() on statement object is not supported for SAML 2.0 "+
                "Make the direct call of getSubject() method on SAML2.0 assertion");
    }
   
}