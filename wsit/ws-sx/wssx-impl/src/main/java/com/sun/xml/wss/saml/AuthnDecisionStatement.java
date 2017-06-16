/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * AuthorizationDecisionStatement.java
 *
 * Created on August 18, 2005, 12:31 PM
 *
 */

package com.sun.xml.wss.saml;

import java.util.List;

/**
 *
 * @author abhijit.das@Sun.COM
 */

/**
 * The <code>AuthorizationDecisionStatement</code> element supplies a statement
 * by the issuer that the request for access by the specified subject to the
 * specified resource has resulted in the specified decision on the basis of
 * some optionally specified evidence.
 *
 * <p>The following schema fragment specifies the expected content contained within 
 * SAML AuthorizationDecisionStatement element.
 * <pre>
 * &lt;complexType name="AuthorizationDecisionStatementType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{urn:oasis:names:tc:SAML:1.0:assertion}SubjectStatementAbstractType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}Action" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}Evidence" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Decision" use="required" type="{urn:oasis:names:tc:SAML:1.0:assertion}DecisionType" /&gt;
 *       &lt;attribute name="Resource" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 *
 */
public interface AuthnDecisionStatement {
    
    /**
     * Gets the value of the action property.   
     *
     * @return Objects of the following type(s) are in the list {@link Action }
     * 
     * 
     */
    public List<Action> getActionList();
    
    /**
     * Gets the value of the evidence property.
     * 
     * @return object is {@link Evidence }
     *     
     */
    public Evidence getEvidence();
    
    /**
     * Gets the value of the decision property.
     * 
     * @return object is {@link java.lang.String }
     *     
     */
    public String getDecisionValue();
    
    /**
     * Gets the value of the resource property.
     * 
     * @return object is {@link java.lang.String }
     *     
     */
    public String getResource();
}
