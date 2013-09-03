/*
 * $Id: ServerSecurityAuthModule.java,v 1.7 2009/11/06 12:03:02 kumarjayanti Exp $
 */

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

package com.sun.xml.wss.provider;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import javax.security.auth.Subject;
import javax.security.auth.Destroyable;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.callback.CallbackHandler;

import com.sun.enterprise.security.jauth.AuthParam;
import com.sun.enterprise.security.jauth.AuthPolicy;
//import com.sun.enterprise.security.jauth.SOAPAuthParam;
import com.sun.enterprise.security.jauth.AuthException;
import com.sun.enterprise.security.jauth.ServerAuthModule;

import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.SecurityRecipient;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.ProcessingContextImpl;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;

import com.sun.xml.wss.impl.WssProviderSecurityEnvironment;

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
       }

       public void validateRequest (AuthParam param,
                                    Subject subject,
                                    Map sharedState)
                   throws AuthException {
             try {

                 ProcessingContextImpl context = new ProcessingContextImpl();

                 _sEnvironment.setRequesterSubject(subject, context.getExtraneousProperties());

                 MessagePolicy receiverCnfg = 
                 ((DeclarativeSecurityConfiguration)_policy).receiverSettings();
 
                 context.setSecurityPolicy(receiverCnfg);
                 context.setSOAPMessage(AuthParamHelper.getRequest(param));
                 context.setSecurityEnvironment(_sEnvironment);

                 SecurityRecipient.validateMessage(context); 

                 populateSharedStateFromContext(sharedState, context);
 
                 context.getSecurableSoapMessage().deleteSecurityHeader();

             } catch (XWSSecurityException xwsse) {
                xwsse.printStackTrace();
                throw new AuthException(xwsse.getMessage());
             }
       }

       public void secureResponse (AuthParam param,
                                   Subject subject,
                                   Map sharedState)
                   throws AuthException {
             try {

               ProcessingContextImpl context = new ProcessingContextImpl();
               _sEnvironment.setSubject(subject, context.getExtraneousProperties());

               populateContextFromSharedState(context, sharedState);

 
               MessagePolicy senderCnfg = 
                 ((DeclarativeSecurityConfiguration)_policy).senderSettings();
              
               SOAPMessage msg = AuthParamHelper.getResponse(param);
               context.setSecurityPolicy(senderCnfg);
               context.setSOAPMessage(msg);
               context.setSecurityEnvironment(_sEnvironment);

                if (optimize  != MessageConstants.NOT_OPTIMIZED  && isOptimized(msg)) {
                      context.setConfigType(optimize);
                } else {
		    try{
                      msg.getSOAPBody();
                      msg.getSOAPHeader();
                      context.setConfigType(MessageConstants.NOT_OPTIMIZED);
		    }catch(SOAPException ex){
			throw new AuthException(ex.getMessage());
		    }
                }

               SecurityAnnotator.secureMessage(context);
               

             } catch (XWSSecurityException xwsse) {
               xwsse.printStackTrace();
               throw new AuthException(xwsse.getMessage());
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
                 //subject = new Subject();
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

       private void populateSharedStateFromContext(Map sharedState, ProcessingContextImpl context) {
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
