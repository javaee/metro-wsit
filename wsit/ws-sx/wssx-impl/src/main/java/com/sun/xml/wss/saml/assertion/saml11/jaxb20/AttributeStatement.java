/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
 * $Id: AttributeStatement.java,v 1.2 2010-10-21 15:38:00 snajper Exp $
 */

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

import com.sun.xml.wss.saml.Attribute;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AttributeStatementType;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AttributeType;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;
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

    @SuppressWarnings("unchecked")
    private void setAttributes(List attr) {
        this.attribute = attr;
    }
    
    /**
     *Dafault constructor
     */
    public AttributeStatement(Subject subj, List attr) {
        setSubject(subj);
        setAttributes(attr);
    }

    public AttributeStatement(AttributeStatementType attStmtType) {
        if(attStmtType.getSubject() != null){
            Subject sub = new Subject(attStmtType.getSubject());
            setSubject(sub);
        }
        setAttributes(attStmtType.getAttribute());
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
            JAXBContext jc = SAMLJAXBUtil.getJAXBContext();
                    
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
        Iterator it = super.getAttribute().iterator();
        while(it.hasNext()){
            com.sun.xml.wss.saml.assertion.saml11.jaxb20.Attribute obj = 
                    new com.sun.xml.wss.saml.assertion.saml11.jaxb20.Attribute((AttributeType)it.next());
            attValueList.add(obj);
        }
        return attValueList;                 
    }

    @Override
    public Subject getSubject() {
        //Subject subj = new Subject(super.getSubject());
        return (Subject) super.getSubject();
    }   
}
