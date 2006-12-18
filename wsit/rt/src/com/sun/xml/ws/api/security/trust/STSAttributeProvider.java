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

package com.sun.xml.ws.api.security.trust;

import java.util.Map;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;

/**
 * <p>
 * This interface is a plugin for attrinute services to a Security Token Service (STS).
 * An attribute service provides the attributes about a requestor. The attributes are 
 * included in the issued toekn for the requestor using with the target servicce for 
 * authentication and authorization purpose.
 * </p>
 @author Jiandong Guo
 */

public interface STSAttributeProvider {
    public static final String NAME_IDENTIFIER = "NameID";
    
    /**
     * Returns the map of claimed attributes of the requestor apply to the targeted service.
     * @param subject The <code>Subject</code> contgaining authentication information and context of the 
     *                authenticated requestor.
     * @param appliesTo Identifying target service(s) 
     * @param tokenType Type of token to be issued which will contain these attributes.
     * @param cliams Identifying the attributes of the requestor claimed by the target service.
     * @return map of attribute value pairs. The value of the map is a <code>QName</code> contains
     *         the value and the namespace of the value. One particular value with the requestor 
     *         identity to be in the issued token with key <code>NAME_IDENTIFIER</code> must be in the map.
     */  
    Map<String, QName> getClaimedAttributes(Subject subject, String appliesTo, String tokenType, Claims claims);
}
