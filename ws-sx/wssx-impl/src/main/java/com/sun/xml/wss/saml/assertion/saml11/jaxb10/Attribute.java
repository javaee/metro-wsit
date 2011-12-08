/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;

import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.bind.util.ListImpl;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.AttributeType;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AttributeTypeImpl;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;
import java.util.LinkedList;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;

/**
 * The <code>Attribute</code> element specifies an attribute of the assertion subject.
 * The <code>Attribute</code> element is an extension of the <code>AttributeDesignator</code> element
 * that allows the attribute value to be specified.
 */
public class Attribute extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AttributeImpl
        implements com.sun.xml.wss.saml.Attribute {
    
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    
    /**
     * Constructs an attribute element from an existing XML block.
     *
     * @param element representing a DOM tree element.
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public static AttributeTypeImpl fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc =
                    SAMLJAXBUtil.getJAXBContext();
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AttributeTypeImpl)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    private void setAttributeValue( List values) {
        Iterator it = values.iterator();
        List typeList = new LinkedList();
        while ( it.hasNext()) {
            List tmpList = new LinkedList();
            tmpList.add(it.next());
            AnyType type = new AnyType();
            type.setContent(tmpList);
            typeList.add(type);
        }
        this._AttributeValue = new ListImpl(typeList);
    }
    
    /**
     * Constructs an instance of <code>Attribute</code>.
     *
     * @param name A String representing <code>AttributeName</code> (the name
     *        of the attribute).
     * @param nameSpace A String representing the namespace in which
     *        <code>AttributeName</code> elements are interpreted.
     * @param values A List of DOM element representing the
     *        <code>AttributeValue</code> object.
     * @exception SAMLException if there is an error in the sender or in the
     *            element definition.
     */
    public Attribute(String name, String nameSpace, List values) {
        setAttributeName(name);
        setAttributeNamespace(nameSpace);
        setAttributeValue(values);
        /*List attValues = new LinkedList();
        List typeList = new LinkedList();
         
        Iterator it = values.iterator();
        while ( it.hasNext()) {
            AnyType type = new AnyType();
            typeList.clear();
            typeList.add(it.next());
            type.setContent(typeList);
            attValues.add(type);
        }
        setAttributeValue(attValues);*/
        
    }

    public Attribute(AttributeType attType) {
        setAttributeName(attType.getAttributeName());
        setAttributeNamespace(attType.getAttributeNamespace());
        setAttributeValue(attType.getAttributeValue());
    } 
    @SuppressWarnings("unchecked")
    public List<Object> getAttributes() {
        return (List<Object>)super.getAttributeValue();
    }

    public String getFriendlyName() {
        return null;
    }

    public String getName() {
        return super.getAttributeName();
    }

    public String getNameFormat() {
        return super.getAttributeNamespace();
    }
}
