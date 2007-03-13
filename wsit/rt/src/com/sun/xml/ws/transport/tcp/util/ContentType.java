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

import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexey Stashok
 */
public final class ContentType {
    private MimeType mimeType;
    private final Map<String, String> parameters = new HashMap<String, String>(4);
    
    public ContentType() {
    }
    
    public MimeType getMimeType() {
        return mimeType;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void parse(final String contentType) throws WSTCPException {
        parameters.clear();
        
        final int mimeDelim = contentType.indexOf(';');
        final String mimeTypeS;
        final List<MimeType> mimeTypeList;
        
        if (mimeDelim == -1) { // If the contentType doesn't have params
            mimeTypeS = contentType.trim().toLowerCase();
            mimeTypeList = MimeType.mimeName2mime.get(mimeTypeS);
            final MimeType mimeType;
            // If there is only one corresponding MimeType in List without embedded params - it is the one
            if (mimeTypeList != null && mimeTypeList.size() == 1 &&
                    ((mimeType = mimeTypeList.get(0)).getEmbeddedParams().size() == 0)) {
                // this situation is expected when working with FI codec. Process it fast
                this.mimeType = mimeType;
                return;
            }
        } else {
            mimeTypeS = contentType.substring(0, mimeDelim).trim().toLowerCase();
            mimeTypeList = MimeType.mimeName2mime.get(mimeTypeS);
        }
        
        if (mimeTypeList != null) {
            // If several ContentTypes have the same mimeType - check each according to embedded params
            for(MimeType mime : mimeTypeList) {
                int ctEmbedParamsAmount = mime.getEmbeddedParams().size();
                int delim = mimeDelim + 1;
                // Scan ContentType string's params, decode them
                while(delim < contentType.length()) {
                    int nextDelim = contentType.indexOf(';', delim);
                    if (nextDelim == -1) nextDelim = contentType.length();
                    
                    int eqDelim = contentType.indexOf('=', delim);
                    if (eqDelim == -1) eqDelim = nextDelim;
                    
                    final String key = contentType.substring(delim, eqDelim).trim();
                    final String value = contentType.substring(eqDelim + 1, nextDelim).trim();
                    final String valToCompare = mime.getEmbeddedParams().get(key);
                    if (valToCompare != null && valToCompare.equals(value)) {
                        ctEmbedParamsAmount--;
                    } else {
                        parameters.put(key, value);
                    }
                    
                    delim = nextDelim + 1;
                }
                if (ctEmbedParamsAmount == 0) {
                    this.mimeType = mime;
                    return;
                }
                
                parameters.clear();
            }
        }
        
        throw new WSTCPException(WSTCPError.createNonCriticalError(TCPConstants.UNKNOWN_CONTENT_ID,
                MessagesMessages.WSTCP_0011_UNKNOWN_CONTENT_TYPE(contentType)));
    }
    
    public boolean equals(Object o) {
        if (o != null && o instanceof ContentType) {
            ContentType ctToCompare = (ContentType) o;
            return ctToCompare.mimeType == mimeType && ctToCompare.parameters.equals(parameters);
        }
        
        return false;
    }
}
