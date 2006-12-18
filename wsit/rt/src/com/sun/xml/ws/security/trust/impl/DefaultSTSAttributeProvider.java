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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;

/**
 *
 * @author Jiandong Guo
 */
public class DefaultSTSAttributeProvider implements STSAttributeProvider{
    
    public Map<String, QName> getClaimedAttributes(Subject subject, String appliesTo, String tokenType, Claims claims){
        Set<Principal> principals = subject.getPrincipals();
        Map<String, QName> attrs = new HashMap<String, QName>();
        if (principals != null){
            Iterator iterator = principals.iterator();
            while (iterator.hasNext()){
                String name = principals.iterator().next().getName();
                if (name != null){
                    //attrs.add(name);
                    attrs.put(NAME_IDENTIFIER, new QName("http://sun.com", name));
                    break;
                }
            }       
        }
       
     /*   if (attrs.get(NAME_IDENTIFIER) == null){
            attrs.put(NAME_IDENTIFIER, new QName("http://sun.com", "principal"));
        } */
       
        // Set up a dumy attribute value
        String key = "name";
        QName value = new QName("http://sun.com", "value");
        attrs.put(key, value);
       
        return attrs;
    }   
}
