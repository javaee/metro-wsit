/*
 * Subject.java
 *
 * Created on August 18, 2005, 12:33 PM
 *
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

    SubjectConfirmation getSubjectConfirmation();
    
    NameIdentifier getNameIdentifier();
    
    NameID getNameId();
        
}
