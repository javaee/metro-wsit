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
import java.util.List;
import java.util.Map;

/**
 * @author Alexey Stashok
 */
public class ContentType {
    private MimeType mimeType;
    private Map<String, String> parameters = new HashMap<String, String>();
    
    public MimeType getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public static ContentType createContentType(String contentType) {
        String[] entities = contentType.split(";");
        String mimeTypeS = entities[0].trim().toLowerCase();
        List<MimeType> mimeTypeList = MimeType.mimeName2mime.get(mimeTypeS);
        assert mimeTypeList != null;
        
        ContentType dct = new ContentType();
        for(MimeType mime : mimeTypeList) {
            int ctEmbedParamsAmount = mime.getEmbeddedParams().size();
            
            for(int i=1; i<entities.length; i++) {
                String[] keyVal = entities[i].split("=");
                assert keyVal.length == 2;
                
                String key = keyVal[0].trim();
                String value = keyVal[1].trim();
                String valToCompare = mime.getEmbeddedParams().get(key);
                if (valToCompare != null) {
                    if (valToCompare.equals(value)) {
                        ctEmbedParamsAmount--;
                    }
                } else {
                    dct.parameters.put(key, value);
                }
            }
            
            if (ctEmbedParamsAmount == 0) {
                dct.mimeType = mime;
                return dct;
            }
            
            dct.parameters.clear();
        }
        
        throw new AssertionError("Unknown content-type");
    }
    
    public static class EncodedContentType {
        public int mimeId;
        public Map<Integer, String> params;
        
        public EncodedContentType(int mimeId, Map<Integer, String> params) {
            this.mimeId = mimeId;
            this.params = params;
        }
    }
}
