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
