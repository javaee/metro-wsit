/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.wss;

import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.misc.DefaultRealmAuthenticationAdapter;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import java.net.URL;
import java.util.Map;
import javax.security.auth.Subject;

/**
 * This abstract class defines an SPI that Metro Application developers can implement, to handle custom
 * username/password and username/Password-Digest authentication. 
 * 
 * The SPI implementation class needs to 
 * specified as a META-INF/services entry with name "com.sun.xml.xwss.RealmAuthenticator". 
 * A default implementation of this SPI is returned if no entry is configured.
 *
 * 
 */
public abstract class RealmAuthenticationAdapter {

    public static final String UsernameAuthenticator = "com.sun.xml.xwss.RealmAuthenticator";
    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
    // Prefixing with META-INF/ instead of /META-INF/. /META-INF/ is working fine
    // when loading from a JAR file but not when loading from a plain directory.
    private static final String JAR_PREFIX = "META-INF/";

    /** Creates a new instance of RealmAuthenticator */
    protected RealmAuthenticationAdapter() {
    }

    /**
     * 
     * @param callerSubject the callerSubject should contain the appropriate principal's of the caller after a successful authentication
     * @param username the username
     * @param password the password
     * @return true if authentication succeeds
     * @throws com.sun.xml.wss.XWSSecurityException, if there is an authentication failure
     */
    public abstract boolean authenticate(Subject callerSubject, String username, String password) throws XWSSecurityException;

    /**
     * 
     * @param callerSubject the callerSubject should contain the appropriate principal's of the caller after a successful authentication
     * @param username the username
     * @param password the password
     * @param runtimeProps Map of runtime properties that can be used in authentication decision
     * @return true if authentication succeeds
     * @throws com.sun.xml.wss.XWSSecurityException, if there is an authentication failure
     */
    public  boolean authenticate(Subject callerSubject, String username, String password, Map runtimeProps) throws XWSSecurityException {
        //make a default implementation for backward compatibility
        return this.authenticate(callerSubject, username, password);
    }
    
    /**
     * 
     * @param callerSubject  the callerSubject should contain the appropriate principal's of the caller after a successful authentication
     * @param username the username
     * @param passwordDigest the password-digest
     * @param nonce a nonce sent by the caller in the UsernameToken
     * @param created the creation time of the UsernameToken
     * @return true if authentication succeeds
     * @throws com.sun.xml.wss.XWSSecurityException if there is an authentication failure
     */
    public boolean authenticate(Subject callerSubject, String username, String passwordDigest, String nonce, String created) throws XWSSecurityException {
        throw new XWSSecurityException("Default Implementation : Override this authenticate method in your RealmAuthenticationAdapter");
    }
    
    /**
     * 
     * @param callerSubject  the callerSubject should contain the appropriate principal's of the caller after a successful authentication
     * @param username the username
     * @param passwordDigest the password-digest
     * @param nonce a nonce sent by the caller in the UsernameToken
     * @param created the creation time of the UsernameToken
     * @param runtimeProps Map of runtime properties that can be used in authentication decision
     * @return true if authentication succeeds
     * @throws com.sun.xml.wss.XWSSecurityException if there is an authentication failure
     */
    public boolean authenticate(Subject callerSubject, String username, String passwordDigest, String nonce, String created, Map runtimeProps) throws XWSSecurityException {
        //make a default implementation for backward compatibility
        return this.authenticate(callerSubject, username, passwordDigest, nonce, created);
    }

    /**
     * 
     * @param context optional context that can be used to locate the META-INF/services entry "com.sun.xml.xwss.RealmAuthenticator"
     * @return a new instance of the RealmAuthenticationAdapter
     */
    public static RealmAuthenticationAdapter newInstance(Object context) {
        RealmAuthenticationAdapter adapter = null;
        URL url = null;

        if (context == null) {
            url = SecurityUtil.loadFromClasspath("META-INF/services/" + UsernameAuthenticator);
        } else {
            url = SecurityUtil.loadFromContext("/META-INF/services/" + UsernameAuthenticator, context);
        }
        
        if (url != null) {
            Object obj = SecurityUtil.loadSPIClass(url, UsernameAuthenticator);
            if ((obj != null) && !(obj instanceof RealmAuthenticationAdapter)) {
                throw new XWSSecurityRuntimeException("Class :" + obj.getClass().getName() + " is not a valid RealmAuthenticationProvider");
            }
            adapter = (RealmAuthenticationAdapter) obj;
        }

        if (adapter != null) {
            return adapter;
        }
        return new DefaultRealmAuthenticationAdapter();
    }
}
