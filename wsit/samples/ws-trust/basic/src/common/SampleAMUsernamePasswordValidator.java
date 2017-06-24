/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package common;

import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;

import java.util.*;
import javax.security.auth.Subject;

import java.security.Principal;

import javax.security.auth.login.LoginException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.iplanet.am.util.Debug;
import com.iplanet.am.util.SystemProperties;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.authentication.AuthContext;



public class SampleAMUsernamePasswordValidator implements PasswordValidationCallback.PasswordValidator {
                              
    private final static String ORG_NAME = "common.org";
    
    private static SSOToken selfToken = null;
    private static String orgName = SystemProperties.get(ORG_NAME);

    private static Debug debug = Debug.getInstance("SampleAMUsernamePasswordValidator");

    public boolean validate(PasswordValidationCallback.Request request)
            throws PasswordValidationCallback.PasswordValidationException {
        PasswordValidationCallback.PlainTextPasswordRequest plainTextRequest =
                (PasswordValidationCallback.PlainTextPasswordRequest) request;
        String username = plainTextRequest.getUsername();
        String password = plainTextRequest.getPassword();
        
        SSOToken token = authenticateUser(username, password);

	debug.message("Authenticated username/pasword SSOToken is "+token);

        updateUserSubject(token);

	debug.message("Leaving  SampleAMUsernamePasswordValidator.validate");

        return true;
    }
    
    private void updateUserSubject(SSOToken token){
        Subject subj = SubjectAccessor.getRequesterSubject();
        if (subj == null){
            subj = new Subject();
            SubjectAccessor.setRequesterSubject(subj);
        }

        debug.message("Add to subject - SSOToken is "+token);
        Set set = subj.getPublicCredentials();
        set.clear();
        set.add(token);
    }
    
      private SSOToken authenticateUser(String username, String password) throws PasswordValidationCallback.PasswordValidationException
    {
	debug.message("Entering SampleAMUsernamePasswordValidator.authenticateUser");

        AuthContext ac = null;
        SSOToken token = null;

        debug.message("Username is "+username);
        debug.message("OrgName is "+orgName);

        try {
            debug.message("Trying to make an AuthContext");
            ac = new AuthContext(orgName);
            debug.message("Made an AuthContext");
            ac.login();
            debug.message("Logged in AuthContext");
        } catch (LoginException le) {
            debug.error( "Failed to create AuthContext", le );
            throw new PasswordValidationCallback.PasswordValidationException("Failed to create AuthContext", le);
        }
       
        try { 
            Callback[] callbacks = null;
            // Get the information requested by the plug-ins
            while (ac.hasMoreRequirements()) {
                callbacks = ac.getRequirements();
                
                if (callbacks != null) {
                    addLoginCallbackMessage(callbacks, orgName, username, password);
                    ac.submitRequirements(callbacks);
                }
            }
                    
            if (ac.getStatus() == AuthContext.Status.SUCCESS) {
                debug.message("Authentication successful");
            } else if (ac.getStatus() == AuthContext.Status.FAILED) {
                debug.message("Authentication failed");
                throw new PasswordValidationCallback.PasswordValidationException("Authentication failed");
            } else {
                debug.message("Unknown authentication status: " + ac.getStatus());
                throw new PasswordValidationCallback.PasswordValidationException("Unknown authentication status: " + ac.getStatus());
            }
        } catch (Exception e) {
            debug.error( "Authentication failed", e );
            throw new PasswordValidationCallback.PasswordValidationException("Authentication failed", e);
        }

        try {
            debug.message("Trying to get SSO token");
            token = ac.getSSOToken();
            debug.message("Got SSO token");
        } catch (Exception e) {
            debug.error( "getSSOToken failed", e );
            throw new PasswordValidationCallback.PasswordValidationException("getSSOToken failed", e);
        }

	debug.message("Leaving  SampleAMUsernamePasswordValidator.authenticateUser");

        return token;
    }

    static void addLoginCallbackMessage(Callback[] callbacks, String orgName, String userName, String password) 
    {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                // prompt the user for a username
                debug.message("Name callback");
                NameCallback nc = (NameCallback) callbacks[i];
                nc.setName(userName);
            } else if (callbacks[i] instanceof PasswordCallback) {
                // prompt the user for sensitive information
                debug.message("Password callback");
                PasswordCallback pc = (PasswordCallback) callbacks[i];
                pc.setPassword(password.toCharArray());
            }
        }
    }
}
