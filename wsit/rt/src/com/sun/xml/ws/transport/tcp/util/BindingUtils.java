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
import java.util.List;
import javax.xml.ws.soap.MTOMFeature;

/**
 * @author Alexey Stashok
 */
public final class BindingUtils {
    private static List<String> SOAP11_PARAMS;
    private static List<String> SOAP12_PARAMS;
    private static List<String> MTOM11_PARAMS;
    private static List<String> MTOM12_PARAMS;
    
    private static NegotiatedBindingContent SOAP11_BINDING_CONTENT;
    
    private static NegotiatedBindingContent SOAP12_BINDING_CONTENT;
    
    private static NegotiatedBindingContent MTOM11_BINDING_CONTENT;
    
    private static NegotiatedBindingContent MTOM12_BINDING_CONTENT;
    
    static {
        initiate();
    }
    
    private static void initiate() {
        // Fill out negotiation mime parameters
        
        // Add SOAP parameters
        SOAP11_PARAMS = Arrays.asList(new String[] {TCPConstants.CHARSET_PROPERTY, TCPConstants.TRANSPORT_SOAP_ACTION_PROPERTY});
        SOAP12_PARAMS = Arrays.asList(new String[] {TCPConstants.CHARSET_PROPERTY, TCPConstants.SOAP_ACTION_PROPERTY});
        
        // Add MTOM parameters
        MTOM11_PARAMS = new ArrayList<String>(SOAP11_PARAMS);
        MTOM11_PARAMS.add("boundary");
        MTOM11_PARAMS.add("start-info");
        MTOM11_PARAMS.add("type");
        
        MTOM12_PARAMS = new ArrayList<String>(SOAP12_PARAMS);
        MTOM12_PARAMS.add("boundary");
        MTOM12_PARAMS.add("start-info");
        MTOM12_PARAMS.add("type");

        SOAP11_BINDING_CONTENT =
                new NegotiatedBindingContent(new ArrayList<String>(1), SOAP11_PARAMS);
        
        SOAP12_BINDING_CONTENT =
                new NegotiatedBindingContent(new ArrayList<String>(1), SOAP12_PARAMS);
        
        MTOM11_BINDING_CONTENT =
                new NegotiatedBindingContent(new ArrayList<String>(2), MTOM11_PARAMS);
        
        MTOM12_BINDING_CONTENT =
                new NegotiatedBindingContent(new ArrayList<String>(2), MTOM12_PARAMS);
        
        // Fill out negotiation mime types
        
        // Add FI stateful if enabled
        if (TCPSettings.getInstance().getEncodingMode() == EncodingMode.FI_STATEFUL) {
            SOAP11_BINDING_CONTENT.negotiatedMimeTypes.add(MimeTypeConstants.FAST_INFOSET_STATEFUL_SOAP11);
            SOAP12_BINDING_CONTENT.negotiatedMimeTypes.add(MimeTypeConstants.FAST_INFOSET_STATEFUL_SOAP12);
        }
        
        // Add FI stateless if enabled
        if (TCPSettings.getInstance().getEncodingMode() == EncodingMode.FI_STATELESS) {
            SOAP11_BINDING_CONTENT.negotiatedMimeTypes.add(MimeTypeConstants.FAST_INFOSET_SOAP11);
            SOAP12_BINDING_CONTENT.negotiatedMimeTypes.add(MimeTypeConstants.FAST_INFOSET_SOAP12);
        }
        
        // Add SOAP mime types
        SOAP11_BINDING_CONTENT.negotiatedMimeTypes.add(MimeTypeConstants.SOAP11);
        SOAP12_BINDING_CONTENT.negotiatedMimeTypes.add(MimeTypeConstants.SOAP12);
        
        // Add MTOM mime types
        MTOM11_BINDING_CONTENT.negotiatedMimeTypes.addAll(SOAP11_BINDING_CONTENT.negotiatedMimeTypes);
        MTOM11_BINDING_CONTENT.negotiatedMimeTypes.add(MimeTypeConstants.MTOM);
        MTOM12_BINDING_CONTENT.negotiatedMimeTypes.addAll(SOAP12_BINDING_CONTENT.negotiatedMimeTypes);
        MTOM12_BINDING_CONTENT.negotiatedMimeTypes.add(MimeTypeConstants.MTOM);
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
        public final List<String> negotiatedMimeTypes;
        public final List<String> negotiatedParams;
        
        public NegotiatedBindingContent(List<String> negotiatedMimeTypes, List<String> negotiatedParams) {
            this.negotiatedMimeTypes = negotiatedMimeTypes;
            this.negotiatedParams = negotiatedParams;
        }
    }
}
