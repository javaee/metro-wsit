/*
 * WSITClientAuthModule.java
 *
 * Created on November 5, 2006, 8:27 PM
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
import javax.security.auth.message.module.ClientAuthModule;
import javax.xml.soap.SOAPMessage;

/**
 *
 * @author kumar.jayanti
 */
public class WSITClientAuthModule implements ClientAuthModule {
    
    private Class[] supported = new Class[2];
    protected static final String DEBUG = "debug";
    private boolean debug= false;
    
    /** Creates a new instance of WSITClientAuthModule */
    public WSITClientAuthModule() {
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

    public AuthStatus secureRequest(MessageInfo messageInfo, Subject clientSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

    public AuthStatus validateResponse(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
        return AuthStatus.SUCCESS;
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        if (subject == null) {
            // log
            throw new AuthException("Error disposing Subject: null value for Subject");
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
