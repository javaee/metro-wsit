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

package com.sun.xml.ws.transport.tcp.util;

import com.sun.istack.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.xml.ws.encoding.fastinfoset.FastInfosetMIMETypes;

/**
 * @author Stashok Alexey
 */

/**
 * Class MimeType represents mime type of WS request/response
 * and also contains embedded parameters of mime type.
 * Embedded parameters are parameters of content type, which
 * values are constant for certain mime type
 *
 * Class is not thread safe
 */
public final class MimeType {
    public static final Map<String, List<MimeType>> mimeName2mime;
    
    public static final MimeType SOAP11 = new MimeType("text/xml", Collections.<String, String>emptyMap());
    public static final MimeType SOAP12 = new MimeType("application/soap+xml", Collections.<String, String>emptyMap());
    
    public static final MimeType FAST_INFOSET_SOAP11 = new MimeType(FastInfosetMIMETypes.SOAP_11, Collections.<String, String>emptyMap());
    public static final MimeType FAST_INFOSET_SOAP12 = new MimeType(FastInfosetMIMETypes.SOAP_12, Collections.<String, String>emptyMap());
    
    public static final MimeType FAST_INFOSET_STATEFUL_SOAP11 = new MimeType(FastInfosetMIMETypes.STATEFUL_SOAP_11, Collections.<String, String>emptyMap());
    public static final MimeType FAST_INFOSET_STATEFUL_SOAP12 = new MimeType(FastInfosetMIMETypes.STATEFUL_SOAP_12, Collections.<String, String>emptyMap());
    
    public static final MimeType MTOM11 = new MimeType("multipart/related", new HashMap<String, String>());
    public static final MimeType MTOM12 = new MimeType("multipart/related", new HashMap<String, String>());
    
    public static final MimeType ERROR = new MimeType("x-tcp/error", Collections.<String, String>emptyMap());
    
    static {
        MTOM11.getEmbeddedParams().put("start-info", "\"text/xml\"");
        MTOM11.getEmbeddedParams().put("type", "\"application/xop+xml\"");
        
        MTOM12.getEmbeddedParams().put("start-info", "\"application/soap+xml\"");
        MTOM12.getEmbeddedParams().put("type", "\"application/xop+xml\"");
        
        mimeName2mime = new HashMap<String, List<MimeType>>();
        mimeName2mime.put(SOAP11.getMimeType(), Collections.singletonList(SOAP11));
        mimeName2mime.put(SOAP12.getMimeType(), Collections.singletonList(SOAP12));
        mimeName2mime.put(FAST_INFOSET_SOAP11.getMimeType(), Collections.singletonList(FAST_INFOSET_SOAP11));
        mimeName2mime.put(FAST_INFOSET_SOAP12.getMimeType(), Collections.singletonList(FAST_INFOSET_SOAP12));
        mimeName2mime.put(FAST_INFOSET_STATEFUL_SOAP11.getMimeType(), Collections.singletonList(FAST_INFOSET_STATEFUL_SOAP11));
        mimeName2mime.put(FAST_INFOSET_STATEFUL_SOAP12.getMimeType(), Collections.singletonList(FAST_INFOSET_STATEFUL_SOAP12));
        mimeName2mime.put(MTOM11.getMimeType(), Arrays.asList(MTOM11, MTOM12));
        mimeName2mime.put(ERROR.getMimeType(), Collections.singletonList(ERROR));
    }
    
    private String mimeType;
    private Map<String, String> embeddedParams;
    
    private String stringRepresentation = "";
    /** hash code to check whether stringRepresentation
     *  really represents current MimeType values */
    private int srHashCode = 0;
    
    public MimeType() {
    }
    
    public MimeType(final String mimeType, final Map<String, String> embeddedParams) {
        this.mimeType = mimeType;
        this.embeddedParams = embeddedParams;
        makeStringRepresentation();
    }
    
    public @NotNull String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(@NotNull final String mimeType) {
        this.mimeType = mimeType;
        makeStringRepresentation();
    }
    
    public @NotNull Map<String, String> getEmbeddedParams() {
        return embeddedParams;
    }
    
    public void setEmbeddedParams(@NotNull final Map<String, String> embeddedParams) {
        this.embeddedParams = embeddedParams;
        makeStringRepresentation();
    }
    
    public boolean equals(final Object object) {
        if (object instanceof MimeType) {
            return object.hashCode() == hashCode();
        }
        
        return false;
    }
    
    public int hashCode() {
        return mimeType.hashCode() + embeddedParams.hashCode();
    }
    
    public String toString() {
        final int hashCode = hashCode();
        if (srHashCode != hashCode) {
            srHashCode = hashCode;
            makeStringRepresentation();
        }
        
        return stringRepresentation;
    }
    
    private void makeStringRepresentation() {
        final StringBuffer buffer = new StringBuffer();
        if (mimeType != null) {
            buffer.append(mimeType);
            if (embeddedParams != null) {
                for(Map.Entry<String, String> entry : embeddedParams.entrySet()) {
                    buffer.append(';');
                    buffer.append(entry.getKey());
                    buffer.append('=');
                    buffer.append(entry.getValue());
                }
            }
        }
        stringRepresentation = buffer.toString();
    }
}
