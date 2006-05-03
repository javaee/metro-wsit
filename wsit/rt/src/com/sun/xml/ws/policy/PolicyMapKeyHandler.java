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

package com.sun.xml.ws.policy;


/**
 * This interface defines method that is used to handle actual equality comparison and hash code generation for PolicyMapKey object.
 * <p/>
 * The different implementations of this interface may allow different strategies to be applied for operations mentioned above. This feature 
 * is used within {@link WSPolicyMap} to restrict set of fields to be compared when searching different policy scope maps.
 * 
 * 
 * 
 * @author Marek Potociar
 */
interface PolicyMapKeyHandler {
    boolean areEqual(PolicyMapKey locator1, PolicyMapKey locator2);
    
    int generateHashCode(PolicyMapKey locator);    
}
