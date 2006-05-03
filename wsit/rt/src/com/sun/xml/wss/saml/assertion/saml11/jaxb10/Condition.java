/*
 * $Id: Condition.java,v 1.1 2006-05-03 22:58:11 arungupta Exp $
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

package com.sun.xml.wss.saml.assertion.saml11.jaxb10;


/**
 * This is an abstract class which servers as an extension point for new
 * conditions.  This is one of the element within the <code>Conditions</code>
 * object.  Extension elements based on this class MUST use xsi:type attribute
 * to indicate the derived type.
 */
public abstract class Condition extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.ConditionAbstractTypeImpl 
    implements com.sun.xml.wss.saml.Condition {
    
}
