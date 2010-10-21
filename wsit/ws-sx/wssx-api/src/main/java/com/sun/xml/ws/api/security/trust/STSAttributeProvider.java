/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.security.trust;

import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;

/**
 * <p>
 * This interface is a plugin for attrinute services to a Security Token Service (STS).
 * An attribute service provides the attributes about a requestor. The attributes are 
 * included in the issued toekn for the requestor using with the target servicce for 
 * authentication and authorization purpose. The usual services mechanism is used to find implementing class
 * of <code>STSAttributeProvider</code>.
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
     * @return map of attribut key and values. The key of the map is a <code>QName</code> contains the key name the the name space 
     *         for the key. The value of the map is a <code>List</code> of <code>String</code> contains
     *         a list of the values. One particular value with the requestor 
     *         identity to be in the issued token with key name<code>NAME_IDENTIFIER</code> must be in the map.
     */  
    Map<QName, List<String>> getClaimedAttributes(Subject subject, String appliesTo, String tokenType, Claims claims);
}
