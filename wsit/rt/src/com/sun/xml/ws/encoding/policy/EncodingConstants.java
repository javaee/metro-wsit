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

package com.sun.xml.ws.encoding.policy;

import javax.xml.namespace.QName;

/**
 * File holding all encoding constants
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class EncodingConstants {    
    /** Prevents creation of new EncodingConstants instance */
    private EncodingConstants() {
    }
    
    public static final String SUN_FI_SERVICE_NS = "http://java.sun.com/xml/ns/wsit/2006/09/policy/fastinfoset/service";
    public static final QName OPTIMIZED_FI_SERIALIZATION_ASSERTION = new QName(SUN_FI_SERVICE_NS, "OptimizedFastInfosetSerialization");
    
    public static final String SUN_ENCODING_CLIENT_NS = "http://java.sun.com/xml/ns/wsit/2006/09/policy/encoding/client";
    public static final QName SELECT_OPTIMAL_ENCODING_ASSERTION = new QName(SUN_ENCODING_CLIENT_NS, "AutomaticallySelectOptimalEncoding");    
    
    public static final String OPTIMIZED_MIME_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization";
    public static final QName OPTIMIZED_MIME_SERIALIZATION_ASSERTION = new QName(OPTIMIZED_MIME_NS, "OptimizedMimeSerialization");
    
    public static final String ENCODING_NS = "http://schemas.xmlsoap.org/ws/2004/09/policy/encoding";
    public static final QName UTF816FFFE_CHARACTER_ENCODING_ASSERTION = new QName(ENCODING_NS, "Utf816FFFECharacterEncoding");
}