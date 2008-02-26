/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
