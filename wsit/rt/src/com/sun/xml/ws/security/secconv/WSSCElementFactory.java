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
 * WSSCElementFactory.java
 *
 * Created on February 16, 2006, 12:11 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.ws.security.secconv;

import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.secconv.impl.elements.SecurityContextTokenImpl;
import com.sun.xml.ws.security.trust.impl.WSTrustElementFactoryImpl;

import java.net.URI;

/**
 *
 * @author Jiandong Guo
 */
public class WSSCElementFactory extends WSTrustElementFactoryImpl{
    
    private static WSSCElementFactory scElemFactory = null;
    
    public static WSSCElementFactory newInstance() {
        if (scElemFactory == null) {
            scElemFactory = new WSSCElementFactory();
        }
        return scElemFactory;
    }
    
    public SecurityContextToken createSecurityContextToken(URI identifier, String instance, String wsuId){
        return new SecurityContextTokenImpl(identifier, instance, wsuId);
    }
}
