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

package com.sun.xml.ws.rm.protocol;


import com.sun.xml.ws.addressing.EndpointReferenceImpl;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * <p>Java class for Acks To that refers to W3c  version
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AcksTo", namespace="http://schemas.xmlsoap.org/ws/2005/02/rm" )

public class W3CAcksToImpl extends EndpointReferenceImpl {

     public W3CAcksToImpl(){

     }

    public W3CAcksToImpl(URI uri) {
         super(uri);
    }

}
