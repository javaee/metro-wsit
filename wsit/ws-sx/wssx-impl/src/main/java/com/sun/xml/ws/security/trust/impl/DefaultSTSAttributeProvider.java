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

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.wss.saml.*;
import com.sun.xml.wss.saml.util.SAMLUtil;


import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Element;

/**
 *
 * @author Jiandong Guo
 */
public class DefaultSTSAttributeProvider implements STSAttributeProvider{
   
    public Map<QName, List<String>> getClaimedAttributes(final Subject subject, final String appliesTo, final String tokenType, final Claims claims){
        final Set<Principal> principals = subject.getPrincipals();
        final Map<QName, List<String>> attrs = new HashMap<QName, List<String>>();
        if (principals != null && !principals.isEmpty()){
            final Iterator iterator = principals.iterator();
            while (iterator.hasNext()){
                final String name = principals.iterator().next().getName();
                if (name != null){
                    List<String> nameIds = new ArrayList<String>();
                    nameIds.add(name);
                    attrs.put(new QName("http://sun.com", NAME_IDENTIFIER), nameIds);
                    break;
                }
            }       
        }else {
            //handle the case that the authentication token is SAML assertion
            Set<Object> set = subject.getPublicCredentials();
            Element samlAssertion = null;
            try {
                for (Object obj : set) {
                    if (obj instanceof XMLStreamReader) {
                        XMLStreamReader reader = (XMLStreamReader) obj;
                        //To create a DOM Element representing the Assertion :
                        samlAssertion = SAMLUtil.createSAMLAssertion(reader);
                    } else if (obj instanceof Element){
                        samlAssertion = (Element) obj;
                    }
                    break;
                }
                if (samlAssertion != null){
                    this.addAttributes(samlAssertion, attrs, false);
                }
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }

        // Check if it is the ActAs case
        if ("true".equals(claims.getOtherAttributes().get(new QName("ActAs")))){
            //ToDo: have a general mechanism to handle ActAs tokens

            // Get the ActAs token
            Element token = null;
            for (Object obj : claims.getSupportingProperties()){
                if (obj instanceof Subject){
                    token = (Element)((Subject)obj).getPublicCredentials().iterator().next();
                        break;
                }
            }

            try {
                if (token != null){
                    addAttributes(token, attrs, true);
                }
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }

        if (attrs.size() < 2){
            // Set up a dumy attribute value
            final QName key = new QName("http://sun.com", "token-requestor");
            List<String> tokenRequestor = new ArrayList<String>();
            tokenRequestor.add("authenticated");
            attrs.put(key, tokenRequestor);
        }
       
        return attrs;
    }

    private void addAttributes(Element token, Map<QName, List<String>> attrs, boolean isActAs) throws SAMLException{
        // only handle the case of UsernameToken and SAML assertion here
        String name = null;
        String nameNS = null;
        String tokenName = token.getLocalName();
        if ("UsernameToken".equals(tokenName)){
            // an UsernameToken: get the user name
            name = token.getElementsByTagNameNS("*", "Username").item(0).getFirstChild().getNodeValue();
        } else if ("Assertion".equals(tokenName)){
            // an SAML assertion
            Assertion assertion = AssertionUtil.fromElement(token);

            com.sun.xml.wss.saml.Subject subject = null;
            NameID nameID = null;

            // SAML 2.0
            try {
                subject = assertion.getSubject();
            }catch (Exception ex){
                subject = null;
            }

            if (subject != null){
                nameID = subject.getNameId();
            }

            List<Object> statements = assertion.getStatements();
            for (Object s : statements){
                if (s instanceof AttributeStatement){
                    List<Attribute> samlAttrs = ((AttributeStatement)s).getAttributes();
                    for (Attribute samlAttr : samlAttrs){
                        String attrName = samlAttr.getName();
                        String attrNS = samlAttr.getNameFormat();
                        List<Object> samlAttrValues = samlAttr.getAttributes();
                        List<String> attrValues = new ArrayList<String>();
                        for (Object samlAttrValue : samlAttrValues){
                            attrValues.add(((Element)samlAttrValue).getFirstChild().getNodeValue());
                        }
                        attrs.put(new QName(attrNS, attrName), attrValues);
                    }

                    // for SAML 1.0, 1.1
                    if (subject == null){
                        subject = ((AttributeStatement)s).getSubject();
                    }
                } else if (s instanceof AuthenticationStatement){
                    subject = ((AuthenticationStatement)s).getSubject();
                }
            }

            // Get the user identifier in the Subject:
            if (nameID != null){
                //SAML 2.0 case
                name = nameID.getValue();
                nameNS = nameID.getNameQualifier();
            }else{
                // SAML 1.0, 1.1. case
                NameIdentifier nameIdentifier = subject.getNameIdentifier();
                if (nameIdentifier != null){
                    name = nameIdentifier.getValue();
                    nameNS = nameIdentifier.getNameQualifier();
                }
            }
        }

        String idName = isActAs ? "ActAs" : NAME_IDENTIFIER;
        List<String> nameIds = new ArrayList<String>();
        if (name != null){
            nameIds.add(name);
        }
        attrs.put(new QName(nameNS, idName), nameIds);
    }
}
