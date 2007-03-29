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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Stashok
 */
public final class ContentType {
    private String mimeType;
    private final Map<String, String> parameters = new HashMap<String, String>(4);
    
    public ContentType() {
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void parse(final String contentType) throws WSTCPException {
        parameters.clear();
        
        final int mimeDelim = contentType.indexOf(';');
        if (mimeDelim == -1) { // If the contentType doesn't have params
            mimeType = contentType.trim().toLowerCase();
            return;
        } else {
            mimeType = contentType.substring(0, mimeDelim).trim().toLowerCase();
        }
        
        int delim = mimeDelim + 1;
        // Scan ContentType string's params, decode them
        while(delim < contentType.length()) {
            int nextDelim = contentType.indexOf(';', delim);
            if (nextDelim == -1) nextDelim = contentType.length();
            
            int eqDelim = contentType.indexOf('=', delim);
            if (eqDelim == -1) eqDelim = nextDelim;
            
            final String key = contentType.substring(delim, eqDelim).trim();
            final String value = contentType.substring(eqDelim + 1, nextDelim).trim();
            parameters.put(key, value);
            
            delim = nextDelim + 1;
        }
    }
    
    public boolean equals(Object o) {
        if (o != null && o instanceof ContentType) {
            ContentType ctToCompare = (ContentType) o;
            return ctToCompare.mimeType == mimeType && ctToCompare.parameters.equals(parameters);
        }
        
        return false;
    }
}
