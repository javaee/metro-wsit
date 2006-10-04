/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
/*
 * $Id: ClientSecurityAuthModule.java,v 1.1 2006-10-04 16:49:05 kumarjayanti Exp $
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.wss.provider.wsit;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import javax.security.auth.Subject;
import javax.security.auth.Destroyable;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.callback.CallbackHandler;

import com.sun.enterprise.security.jauth.AuthParam;
import com.sun.enterprise.security.jauth.AuthPolicy;
import com.sun.enterprise.security.jauth.AuthException;
import com.sun.enterprise.security.jauth.ClientAuthModule;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.wss.impl.AlgorithmSuite;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.SecurityRecipient;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import java.util.Hashtable;
import javax.xml.soap.SOAPException;

public class ClientSecurityAuthModule extends WssProviderAuthModule
        implements ClientAuthModule {
    
    public ClientSecurityAuthModule() {
    }
    
    public void initialize(AuthPolicy requestPolicy,
            AuthPolicy responsePolicy,
            CallbackHandler handler,
            Map options) {
        super.initialize(requestPolicy, responsePolicy, handler, options, true);
        //todo add Tango code
    }
    
    public void secureRequest(AuthParam param,
            Subject subject,
            Map sharedState)
            throws AuthException {
        
        boolean optimized = "true".equals((String)sharedState.get(OPTIMIZED));
        
        Packet packet = (Packet)sharedState.get(PACKET);
        
        try {
            ProcessingContext ctx = initializeProcessingContext(param, sharedState, packet, optimized, false);
            // keep the message
            Message msg = packet.getMessage();
            msg = secureOutboundMessage(msg, ctx, optimized);
            packet.setMessage(msg);
          
        }catch (XWSSecurityException ex){
            throw getSOAPFaultException(ex);
        }
        
    }
    
    public void validateResponse(AuthParam param,
            Subject subject,
            Map sharedState)
            throws AuthException {
        boolean optimized = "true".equals((String)sharedState.get(OPTIMIZED));
        
        Packet packet = (Packet)sharedState.get(PACKET);
        try {
            ProcessingContext ctx = initializeProcessingContext(param, sharedState, packet, optimized, true);
            // keep the message
            Message msg = packet.getMessage();
            msg = verifyInboundMessage(msg, ctx, optimized);
            packet.setMessage(msg);
            
        }catch (XWSSecurityException ex){
            throw getSOAPFaultException(ex);
        }
    }
    
    
    
    
    public void disposeSubject(Subject subject,
            Map sharedState)
            throws AuthException {
        if (subject == null) {
            // log
            throw new AuthException("Error disposing Subject: null value for Subject");
        }
        
        if (!subject.isReadOnly()) {
            // log
            subject = new Subject();
            return;
        }
        
        Set principals = subject.getPrincipals();
        Set privateCredentials = subject.getPrivateCredentials();
        Set publicCredentials = subject.getPublicCredentials();
        
        try {
            principals.clear();
        } catch (UnsupportedOperationException uoe) {
            // log
        }
        
        Iterator pi = privateCredentials.iterator();
        while (pi.hasNext()) {
            try {
                Destroyable dstroyable =
                        (Destroyable)pi.next();
                dstroyable.destroy();
            } catch (DestroyFailedException dfe) {
                // log
            } catch (ClassCastException cce) {
                // log
            }
        }
        
        Iterator qi = publicCredentials.iterator();
        while (qi.hasNext()) {
            try {
                Destroyable dstroyable =
                        (Destroyable)qi.next();
                dstroyable.destroy();
            } catch (DestroyFailedException dfe) {
                // log
            } catch (ClassCastException cce) {
                // log
            }
        }
    }
}
