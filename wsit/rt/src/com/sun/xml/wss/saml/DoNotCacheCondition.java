/*
 * DoNotCacheCondition.java
 *
 * Created on August 18, 2005, 12:32 PM
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
 *This is an implementation of the abstract <code>Condition</code> class, which
 * specifes that the assertion this <code>DoNotCacheCondition</code> is part of,
 * is the new element in SAML 1.1, that allows an assertion party to express that
 * an assertion should not be cached by the relying party for future use. In another
 * word, such an assertion is meant only for "one-time" use by the relying party.
 * 
 * <p>The following schema fragment specifies the expected content contained within 
 * SAML DoNotCacheCondition element.
 * <pre>
 * &lt;complexType name="DoNotCacheConditionType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:1.0:assertion}ConditionAbstractType">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public interface DoNotCacheCondition {
    
}
