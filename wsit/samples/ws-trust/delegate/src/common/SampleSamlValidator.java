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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.SAMLAssertionValidator;
import com.sun.xml.wss.saml.util.SAMLUtil;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.AssertionUtil;
import com.sun.xml.wss.saml.Attribute;
import com.sun.xml.wss.saml.AttributeStatement;
import com.sun.xml.wss.saml.AuthenticationStatement;
import com.sun.xml.wss.saml.NameID;
import com.sun.xml.wss.saml.NameIdentifier;
import java.util.*;
import org.w3c.dom.Element;

import com.sun.xml.wss.saml.Subject;

/**
 *
 * @author jdg
 */
public class SampleSamlValidator implements SAMLAssertionValidator {

    public void validate(Element assertionEle) throws SAMLValidationException {
        String name = null;
        String actAs = null;
        String role = null;
        
        try{
            Assertion assertion = AssertionUtil.fromElement(assertionEle);

            Subject subject = null;
            NameID nameID = null;

            // SAML 2.0
            try {
                subject = assertion.getSubject();
            } catch (Exception ex){
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
                        String attrValue = ((Element)samlAttr.getAttributes().iterator().next()).getFirstChild().getNodeValue();

                        if (attrName.equals("ActAs")){
                            actAs = attrValue;
                        } else if (attrName.equals("Role")){
                            role = attrValue;
                        }
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
            }else{
                // SAML 1.0, 1.1. case
                NameIdentifier nameIdentifier = subject.getNameIdentifier();
                if (nameIdentifier != null){
                    name = nameIdentifier.getValue();
                }
            }
        }catch (Exception ex){
            throw new SAMLValidationException(ex);
        }

        System.out.println("User: " + name + " accesses the Ping service, ");
        if (actAs != null){
            System.out.println("acting as: " + actAs);
        }
        System.out.println("with the role: " + role);
    }

    public void validate(XMLStreamReader assertion) throws SAMLValidationException {
        if (assertion != null){
            try {
                Element element = SAMLUtil.createSAMLAssertion(assertion);
                this.validate(element);
            } catch (XWSSecurityException ex) {
               throw new SAMLValidationException(ex);
            } catch (XMLStreamException ex) {
                 throw new SAMLValidationException(ex);
            }
        }
    }
}
