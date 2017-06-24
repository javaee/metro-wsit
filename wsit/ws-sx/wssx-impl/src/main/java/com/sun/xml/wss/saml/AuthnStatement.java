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
 * AuthenticationStatement.java
 *
 * Created on August 18, 2005, 12:30 PM
 *
 */

package com.sun.xml.wss.saml;

import java.util.Date;
import java.util.List;

/**
 *
 * @author abhijit.das@Sun.COM
 */
/**
 * The <code>AuthenticationStatement</code> element supplies a
 * statement by the issuer that its subject was authenticated by a
 * particular means at a particular time. The
 * <code>AuthenticationStatement</code> element is of type
 * <code>AuthenticationStatementType</code>, which extends the
 * <code>SubjectStatementAbstractType</code> with the additional element and
 * attributes.
 *
 * <p>The following schema fragment specifies the expected content contained within SAML 
 * AuthenticationStatement element.
 *
 * <pre>
 * &lt;complexType name="AuthenticationStatementType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{urn:oasis:names:tc:SAML:1.0:assertion}SubjectStatementAbstractType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}SubjectLocality" minOccurs="0"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}AuthorityBinding" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="AuthenticationInstant" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="AuthenticationMethod" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 */
public interface AuthnStatement {            
    
    /**
     * Gets the value of the authnInstant property.
     * 
     * @return object is {@link java.util.Date }
     *     
     */
    public Date getAuthnInstantDate();
    
    /**
     * Gets the value of the sessionIndex property.
     * 
     * @return object is {@link java.lang.String }
     *     
     */
    public String getSessionIndex();
    
    /**
     * Gets the value of the sessionNotOnOrAfter property.
     * 
     * @return object is {@link java.util.Date }
     *     
     */
    public Date getSessionNotOnOrAfterDate();
       
    /**
     * Gets the value of the SubjectLocality address property.
     * 
     * @return object is {@link java.lang.String }
     *     
     */
    public String getSubjectLocalityAddress();
    
    /**
     * Gets the value of the SubjectLocality dnsName property.
     * 
     * @return object is {@link java.lang.String }
     *     
     */
    public String getSubjectLocalityDNSName();
    
    /**
     * Gets the value of the AuthnContext's AuthnContextClassRef property.
     * 
     * @return object is {@link java.lang.String }
     *     
     */
    public String getAuthnContextClassRef();
    
    /**
     * Gets the value of the AuthnContext's AuthenticatingAuthority property.
     * 
     * @return object is {@link java.lang.String }
     *     
     */
    public String getAuthenticatingAuthority();
}
