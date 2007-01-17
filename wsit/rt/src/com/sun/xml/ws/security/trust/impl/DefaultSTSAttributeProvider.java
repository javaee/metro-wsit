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

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.api.security.trust.STSAttributeProvider;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;

/**
 *
 * @author Jiandong Guo
 */
public class DefaultSTSAttributeProvider implements STSAttributeProvider{
    
    public Map<QName, List<String>> getClaimedAttributes(final Subject subject, final String appliesTo, final String tokenType, final Claims claims){
        final Set<Principal> principals = subject.getPrincipals();
        final Map<QName, List<String>> attrs = new HashMap<QName, List<String>>();
        if (principals != null){
            final Iterator iterator = principals.iterator();
            while (iterator.hasNext()){
                final String name = principals.iterator().next().getName();
                if (name != null){
                    List<String> nameIds = new ArrayList<String>();
                    nameIds.add(name);
                    attrs.put(new QName("http://sun.com", NAME_IDENTIFIER), nameIds);
                    break;
                }
            }       
        }
       
        // Set up a dumy attribute value
        final QName key = new QName("http://sun.com", "token-requestor");
        List<String> tokenRequestor = new ArrayList<String>();
        tokenRequestor.add("authenticated");
        attrs.put(key, tokenRequestor);
       
        return attrs;
    }   
}
