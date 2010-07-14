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

/*
 * WSITServerAuthModule.java
 *
 * Created on November 5, 2006, 8:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.message.Message;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ServerAuthModule;
import javax.xml.soap.SOAPMessage;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.provider.wsit.logging.LogDomainConstants;
import com.sun.xml.wss.provider.wsit.logging.LogStringsMessages;

/**
 *
 * @author kumar.jayanti
 */
public class WSITServerAuthModule implements ServerAuthModule {

    private static final Logger log =
        Logger.getLogger(
        LogDomainConstants.WSIT_PVD_DOMAIN,
        LogDomainConstants.WSIT_PVD_DOMAIN_BUNDLE);
    
    Class[] supported = new Class[2];
    boolean debug = false;
    protected static final String DEBUG = "debug";
    
    /** Creates a new instance of WSITServerAuthModule */
    public WSITServerAuthModule() {
        supported[0] = SOAPMessage.class;
        supported[1] = Message.class;
    }

    @SuppressWarnings("unchecked")
    public void initialize(MessagePolicy requestPolicy,
	       MessagePolicy responsePolicy,
	       CallbackHandler handler,
	       Map options) throws AuthException {
        String bg = (String)options.get(DEBUG);
        if (bg !=null && bg.equals("true")) debug = true;
    }

    public Class[] getSupportedMessageTypes() {
        return supported;
    }

    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        if (subject == null) {
                // log
                log.log(Level.SEVERE, LogStringsMessages.WSITPVD_0037_NULL_SUBJECT());
                throw new AuthException(LogStringsMessages.WSITPVD_0037_NULL_SUBJECT());
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
    
}
