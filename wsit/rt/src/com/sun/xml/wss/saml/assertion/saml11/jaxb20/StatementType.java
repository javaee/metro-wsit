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

/*
 * StatementType.java
 *
 * Created on July 25, 2005, 2:13 PM
 *
 */

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

/**
 *
 * @author abhijit.das
 */
public interface StatementType {

    /**
     * The Statement is not supported.
     */
    public final static int NOT_SUPPORTED                   = -1;
                                                                                                                             
    /**
     * The Statement is an Authentication Statement.
     */
    public final static int AUTHENTICATION_STATEMENT        = 1;
                                                                                                                             
    /**
     * The Statement is an Authorization Decision Statement.
     */
    public final static int AUTHORIZATION_DECISION_STATEMENT= 2;
                                                                                                                             
    /**
     * The Statement is an Attribute Statement.
     */
    public final static int ATTRIBUTE_STATEMENT             = 3;
    
}
