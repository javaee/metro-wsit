/*
 * Advice.java
 *
 * Created on August 18, 2005, 12:00 PM
 *
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
package com.sun.xml.wss.saml;

/**
 *
 * @author abhijit.das@Sun.COM
 */

/**
 *The <code>Advice</code> element contains additional information that the issuer wishes to
 *provide. This information MAY be ignored by applications without affecting
 *either the semantics or validity. Advice elements MAY be specified in
 *an extension schema.
 *
 * <p>The following schema fragment specifies the expected content contained within SAML Advice element.
 *
 * <pre>
 * &lt;complexType name="AdviceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}AssertionIDReference"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}Assertion"/>
 *         &lt;any/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 */
public interface Advice {
    
}
