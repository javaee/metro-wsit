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
import com.sun.xml.ws.transport.tcp.util.TCPSettings.EncodingMode;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.ws.soap.MTOMFeature;

/**
 * @author Alexey Stashok
 */
public final class BindingUtils {
    private static List<String> MTOM_SOAP_PARAMS;
    
    private static NegotiatedBindingContent SOAP11_BINDING_CONTENT;
    
    private static NegotiatedBindingContent SOAP12_BINDING_CONTENT;
    
    private static NegotiatedBindingContent MTOM11_BINDING_CONTENT;
    
    private static NegotiatedBindingContent MTOM12_BINDING_CONTENT;
    
    static {
        initiate();
    }
    
    private static void initiate() {
        MTOM_SOAP_PARAMS = Arrays.asList(new String[] {"boundary"});
        
        SOAP11_BINDING_CONTENT =
                new NegotiatedBindingContent(new ArrayList<MimeType>(1), Collections.<String>emptyList());
        
        SOAP12_BINDING_CONTENT =
                new NegotiatedBindingContent(new ArrayList<MimeType>(1), Collections.<String>emptyList());
        
        MTOM11_BINDING_CONTENT =
                new NegotiatedBindingContent(new ArrayList<MimeType>(2), MTOM_SOAP_PARAMS);
        
        MTOM12_BINDING_CONTENT =
                new NegotiatedBindingContent(new ArrayList<MimeType>(2), MTOM_SOAP_PARAMS);
        
        MTOM11_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.MTOM11);
        MTOM12_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.MTOM12);
        
        if (TCPSettings.getInstance().getEncodingMode() == EncodingMode.FI_STATEFUL) {
            SOAP11_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.FAST_INFOSET_STATEFUL_SOAP11);
            SOAP12_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.FAST_INFOSET_STATEFUL_SOAP12);
            MTOM11_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.FAST_INFOSET_STATEFUL_SOAP11);
            MTOM12_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.FAST_INFOSET_STATEFUL_SOAP12);
        } else if (TCPSettings.getInstance().getEncodingMode() == EncodingMode.FI_STATELESS) {
            SOAP11_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.FAST_INFOSET_SOAP11);
            SOAP12_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.FAST_INFOSET_SOAP12);
            MTOM11_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.FAST_INFOSET_SOAP11);
            MTOM12_BINDING_CONTENT.negotiatedMimeTypes.add(MimeType.FAST_INFOSET_SOAP12);
        }
    }
    
    public static NegotiatedBindingContent getNegotiatedContentTypesAndParams(final WSBinding binding) {
        if (binding.getSOAPVersion().equals(SOAPVersion.SOAP_11)) {
            if (isMTOMEnabled(binding)) {
                return MTOM11_BINDING_CONTENT;
            } else {
                return SOAP11_BINDING_CONTENT;
            }
        } else if (binding.getSOAPVersion().equals(SOAPVersion.SOAP_12)) {
            if (isMTOMEnabled(binding)) {
                return MTOM12_BINDING_CONTENT;
            } else {
                return SOAP12_BINDING_CONTENT;
            }
        }
        
        throw new AssertionError(MessagesMessages.WSTCP_0009_UNKNOWN_BINDING(binding));
    }
    
    private static boolean isMTOMEnabled(final WSBinding binding) {
        return binding.isFeatureEnabled(MTOMFeature.class);
    }
    
    public static final class NegotiatedBindingContent {
        public final List<MimeType> negotiatedMimeTypes;
        public final List<String> negotiatedParams;
        
        public NegotiatedBindingContent(List<MimeType> negotiatedMimeTypes, List<String> negotiatedParams) {
            this.negotiatedMimeTypes = negotiatedMimeTypes;
            this.negotiatedParams = negotiatedParams;
        }
    }
}
