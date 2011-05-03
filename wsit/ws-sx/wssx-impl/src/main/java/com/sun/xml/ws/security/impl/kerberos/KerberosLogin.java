/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.security.impl.kerberos;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Key;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import sun.security.krb5.EncryptionKey;

/**
 *
 * @author ashutosh.shahi@sun.com
 */
public class KerberosLogin {

    private static final String javaVersion = System.getProperty("java.version");
    private static final boolean isJava6Or5 = javaVersion.startsWith("1.6") || javaVersion.startsWith("1.5");  //double check
     private static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    /** Creates a new instance of KerberosLogin */
    public KerberosLogin() {
    }
    @SuppressWarnings("unchecked")
    public KerberosContext login(String loginModule, String servicePrincipal, boolean credDeleg) throws XWSSecurityException{
        KerberosContext krbContext = new KerberosContext();
        LoginContext lc = null;
        try {
            lc = new LoginContext(loginModule, new com.sun.security.auth.callback.TextCallbackHandler());
        } catch (LoginException le) {
            throw new XWSSecurityException("Cannot create LoginContext. ", le);
        } catch (SecurityException se) {
            throw new XWSSecurityException("Cannot create LoginContext. ", se);
        }
        
        try{
            // attempt authentication
            lc.login();
            // if we return with no exception, authentication succeeded
        } catch (AccountExpiredException aee) {
            throw new XWSSecurityException("Your Kerberos account has expired.", aee);
        } catch (CredentialExpiredException cee) {
            throw new XWSSecurityException("Your credentials have expired.", cee);
        }  catch (FailedLoginException fle) {
            throw new XWSSecurityException("Authentication Failed", fle);
        } catch (Exception e) {
            throw new XWSSecurityException("Unexpected Exception in Kerberos login - unable to continue", e);
        }
        
        try{
            Subject loginSubject = lc.getSubject();
            Subject.doAsPrivileged(loginSubject,
                    new  KerberosClientSetupAction(servicePrincipal, credDeleg),
                    null);
            
            Set<Object> setPubCred =  loginSubject.getPublicCredentials();
            Iterator<Object> iter1 = setPubCred.iterator();
            GSSContext gssContext=null;
            while(iter1.hasNext()){
                Object pubObject = iter1.next();
                if(pubObject instanceof byte[]){
                    krbContext.setKerberosToken((byte[])pubObject);
                } else if(pubObject instanceof GSSContext){
                    gssContext = (GSSContext)pubObject;
                    krbContext.setGSSContext(gssContext);
                }
            }
            Set<Object> setPrivCred =  loginSubject.getPrivateCredentials();
            Iterator<Object> iter2 = setPrivCred.iterator();
            while(iter2.hasNext()){
                Object privObject = iter2.next();
                if(privObject instanceof KerberosTicket){
                    KerberosTicket kerbTicket = (KerberosTicket)privObject;
                    try {
                        if(kerbTicket.getServer().getName().equals(gssContext.getTargName().toString())){
                            SecretKey sKey = kerbTicket.getSessionKey();
                            byte[] secret = sKey.getEncoded();
                            krbContext.setSecretKey(secret);
                            break;
                        }
                    } catch (GSSException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (java.security.PrivilegedActionException pae) {
            throw new XWSSecurityException(pae);
        }
        krbContext.setOnce(true);
        return krbContext;
    }
    @SuppressWarnings("unchecked")
    public KerberosContext login(String loginModule, byte[] token) throws XWSSecurityException{
        KerberosContext krbContext = new KerberosContext();
        LoginContext lc = null;
        try {
            lc = new LoginContext(loginModule, new com.sun.security.auth.callback.TextCallbackHandler());
        } catch (LoginException le) {
            throw new XWSSecurityException("Cannot create LoginContext. ", le);
        } catch (SecurityException se) {
            throw new XWSSecurityException("Cannot create LoginContext. ", se);
        }
        
        try{
            // attempt authentication
            lc.login();
            // if we return with no exception, authentication succeeded
        } catch (AccountExpiredException aee) {
            throw new XWSSecurityException("Your Kerberos account has expired.", aee);
        } catch (CredentialExpiredException cee) {
            throw new XWSSecurityException("Your credentials have expired.", cee);
        }  catch (FailedLoginException fle) {
            throw new XWSSecurityException("Authentication Failed", fle);
        } catch (Exception e) {
            throw new XWSSecurityException("Unexpected Exception in Kerberos login - unable to continue", e);
        }
        
        try{
            Subject loginSubject = lc.getSubject();
            Subject.doAsPrivileged(loginSubject,
                    new  KerberosServerSetupAction(token),
                    null);
            
            Set<Object> setPubCred =  loginSubject.getPublicCredentials();
            Iterator<Object> iter1 = setPubCred.iterator();
            GSSContext gssContext=null;
            while(iter1.hasNext()){
                Object pubObject = iter1.next();
                if(pubObject instanceof byte[]){
                    krbContext.setKerberosToken((byte[])pubObject);
                } else if(pubObject instanceof GSSContext){
                    gssContext = (GSSContext)pubObject;
                    krbContext.setGSSContext(gssContext);
                }
            }
            if (!isJava6Or5) {
                if (gssContext != null && gssContext.isEstablished()) {
                    /**
                     *ExtendedGSSContext ex = (ExtendedGSSContext)x;
                     *Key k = (Key)ex.inquireSecContext(
                     *InquireType.KRB5_GET_SESSION_KEY);
                     **/
                    Class inquireType;
                    try {
                        inquireType = Class.forName("com.sun.security.jgss.InquireType");
                        Class extendedGSSContext = Class.forName("com.sun.security.jgss.ExtendedGSSContext");
                        Method inquireSecContext = extendedGSSContext.getMethod("inquireSecContext", inquireType);
                        Key key = (Key)inquireSecContext.invoke(gssContext, Enum.valueOf(inquireType, "KRB5_GET_SESSION_KEY"));
                        krbContext.setSecretKey(key.getEncoded());
                    } catch (IllegalAccessException ex) {
                        log.log(Level.SEVERE, null, ex);
                        throw new XWSSecurityException(ex);
                    } catch (IllegalArgumentException ex) {
                        log.log(Level.SEVERE, null, ex);
                        throw new XWSSecurityException(ex);
                    } catch (InvocationTargetException ex) {
                        log.log(Level.SEVERE, null, ex);
                        throw new XWSSecurityException(ex);
                    } catch (NoSuchMethodException ex) {
                        log.log(Level.SEVERE, null, ex);
                        throw new XWSSecurityException(ex);
                    } catch (SecurityException ex) {
                        log.log(Level.SEVERE, null, ex);
                        throw new XWSSecurityException(ex);
                    } catch (ClassNotFoundException ex) {
                        log.log(Level.SEVERE, null, ex);
                        throw new XWSSecurityException(ex);
                    }

                } else {
                    throw new XWSSecurityException("GSSContext was null in the Login Subject");
                }
            } else {
                Set<Object> setPrivCred = loginSubject.getPrivateCredentials();
                Iterator<Object> iter2 = setPrivCred.iterator();
                while (iter2.hasNext()) {
                    Object privObject = iter2.next();
                    if (privObject instanceof EncryptionKey) {
                        EncryptionKey encKey = (EncryptionKey) privObject;
                        byte[] keyBytes = encKey.getBytes();
                        krbContext.setSecretKey(keyBytes);
                        break;
                    }
                }
            }
        } catch (java.security.PrivilegedActionException pae) {
            throw new XWSSecurityException(pae);
        }
        krbContext.setOnce(false);
        return krbContext;
    }
    
    class KerberosClientSetupAction implements java.security.PrivilegedExceptionAction {
        String server;
        boolean credentialDelegation = false;
        
        public KerberosClientSetupAction(String server, boolean credDeleg){
            this.server = server;
            credentialDelegation = credDeleg;
        }
        
        public Object run() throws Exception {
            
            try {
                Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
                GSSManager manager = GSSManager.getInstance();
                GSSName serverName = manager.createName(server, null);
                
                GSSContext context = manager.createContext(serverName,
                        krb5Oid,
                        null,
                        GSSContext.DEFAULT_LIFETIME);
                context.requestMutualAuth(false);  // Mutual authentication
                context.requestConf(false);  // Will use confidentiality later
                context.requestInteg(true); // Will use integrity later
                
                context.requestCredDeleg(credentialDelegation);
                
                byte[] token = new byte[0];
                token = context.initSecContext(token, 0, token.length);
                
                AccessControlContext acc = AccessController.getContext();
                Subject loginSubject = Subject.getSubject(acc);
                loginSubject.getPublicCredentials().add(context);
                loginSubject.getPublicCredentials().add(token);
                
            } catch (Exception e) {
                throw new java.security.PrivilegedActionException(e);
            }
            return null;
        }
        
    }
    
    class KerberosServerSetupAction implements java.security.PrivilegedExceptionAction {
        
        byte[] token;
        
        public KerberosServerSetupAction(byte[] token){
            this.token = token;
        }
        @SuppressWarnings("unchecked")
        public Object run() throws Exception {
            
            try {
                
                final GSSManager manager = GSSManager.getInstance();
                final Oid krb5Oid = new Oid("1.2.840.113554.1.2.2");
                if (isJava6Or5) {
                    AccessController.doPrivileged(new java.security.PrivilegedAction() {

                        public Object run() {
                            try {
                                manager.addProviderAtFront(new com.sun.xml.ws.security.jgss.XWSSProvider(), krb5Oid);
                            } catch (GSSException gsse) {
                                gsse.printStackTrace();
                            }
                            return null;
                        }
                    });
                }
                
                GSSContext context = manager.createContext((GSSCredential)null);
                byte[] outToken = context.acceptSecContext(token, 0, token.length);
                if(outToken != null && outToken.length != 0){
                    // error condition - should be zero for kerberos w/o mutual authentication
                }
                AccessControlContext acc = AccessController.getContext();
                Subject loginSubject = Subject.getSubject(acc);
                loginSubject.getPublicCredentials().add(context);
                loginSubject.getPublicCredentials().add(token);
            } catch (Exception e) {
                throw new java.security.PrivilegedActionException(e);
            }
            return null;
        }
        
    }
    
}
