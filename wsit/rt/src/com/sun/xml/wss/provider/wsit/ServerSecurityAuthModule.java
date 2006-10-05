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
 * $Id: ServerSecurityAuthModule.java,v 1.2 2006-10-05 11:42:43 mayankmishra Exp $
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
import com.sun.enterprise.security.jauth.ServerAuthModule;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.ProcessingContextImpl;

import com.sun.xml.wss.impl.MessageConstants;

public class ServerSecurityAuthModule extends WssProviderAuthModule 
                                      implements ServerAuthModule {

       public ServerSecurityAuthModule() {
       }

       public void initialize (AuthPolicy requestPolicy,
                               AuthPolicy responsePolicy,
                               CallbackHandler handler,
                               Map options) {
            super.initialize(requestPolicy, responsePolicy, handler, options, false);
            //todo add tango init code
       }

       public void validateRequest (AuthParam param,
                                    Subject subject,
                                    Map sharedState)
                   throws AuthException {
            //todo add tango code
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

       public void secureResponse (AuthParam param,
                                   Subject subject,
                                   Map sharedState)
                   throws AuthException {
            //todo add tango code
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

       public void disposeSubject (Subject subject,
                                   Map sharedState)
                   throws AuthException {
             if (subject == null) {
                // log
                throw new AuthException("Subject is null in disposeSubject");
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

       private void populateContextFromSharedState(ProcessingContextImpl context, Map sharedState) {
           context.setExtraneousProperty(
               MessageConstants.AUTH_SUBJECT, sharedState.get(REQUESTER_SUBJECT));
           context.setExtraneousProperty(
               MessageConstants.REQUESTER_KEYID, sharedState.get(REQUESTER_KEYID));
           context.setExtraneousProperty(
               MessageConstants.REQUESTER_ISSUERNAME, sharedState.get(REQUESTER_ISSUERNAME));
           context.setExtraneousProperty(
               MessageConstants.REQUESTER_SERIAL, sharedState.get(REQUESTER_SERIAL));
       }

   private void populateSharedStateFromContext(Map<String, Object> sharedState, ProcessingContextImpl context) {
           sharedState.put(
               REQUESTER_SUBJECT, context.getExtraneousProperty(MessageConstants.AUTH_SUBJECT));
           sharedState.put(
               REQUESTER_KEYID, context.getExtraneousProperty(MessageConstants.REQUESTER_KEYID));
           sharedState.put(
               REQUESTER_ISSUERNAME, 
                   context.getExtraneousProperty(MessageConstants.REQUESTER_ISSUERNAME));
           sharedState.put(
               REQUESTER_SERIAL, context.getExtraneousProperty(MessageConstants.REQUESTER_SERIAL));

       }
}
