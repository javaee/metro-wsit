/*
 * AudienceRestrictionCondition.java
 *
 * Created on August 18, 2005, 12:29 PM
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
 * This is an implementation of the abstract <code>Condition</code> class, which
 * specifes that the assertion this AuthenticationCondition is part of, is
 * addressed to one or more specific audience.
 * 
 * <p>The following schema fragment specifies the expected content contained within SAML
 * AudienceRestrictionCondition element.
 * <pre>
 * &lt;complexType name="AudienceRestrictionConditionType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:1.0:assertion}ConditionAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:1.0:assertion}Audience" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public interface AudienceRestrictionCondition {
    
}
