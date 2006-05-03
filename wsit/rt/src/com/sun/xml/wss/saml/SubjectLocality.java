/*
 * SubjectLocality.java
 *
 * Created on August 18, 2005, 12:34 PM
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
 * The <code>SubjectLocality</code> element specifies the DNS domain name
 * and IP address for the system entity that performed the authentication.
 * It exists as part of <code>AuthenticationStatement</code> element.
 *
 * <p>The following schema fragment specifies the expected content contained within 
 * SAML SubjectLocality element.
 *
 * <pre>
 * &lt;complexType name="SubjectLocalityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="DNSAddress" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="IPAddress" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
*/

public interface SubjectLocality {
    
}
