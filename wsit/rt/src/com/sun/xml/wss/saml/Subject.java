/*
 * Subject.java
 *
 * Created on August 18, 2005, 12:33 PM
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
 * The <code>Subject</code> element specifies one or more subjects. It contains either or
both of the following elements:<code>NameIdentifier</code>;
An identification of a subject by its name and security domain.
<code>SubjectConfirmation</code>;
Information that allows the subject to be authenticated.

If a <code>Subject</code> element contains more than one subject specification,
the issuer is asserting that the surrounding statement is true for
all of the subjects specified. For example, if both a
<code>NameIdentifier</code> and a <code>SubjectConfirmation</code> element are
present, the issuer is asserting that the statement is true of both subjects
being identified. A <Subject> element SHOULD NOT identify more than one
principal.
 *
 * <p>The following schema fragment specifies the expected content contained within 
 * SAML Subject element.
 *
 * <pre>
 * &lt;complexType name="SubjectType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}NameIdentifier"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}SubjectConfirmation" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}SubjectConfirmation"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
*/
public interface Subject {
    
}
