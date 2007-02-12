/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package common;

/*
 * SampleUsernamePasswordCallbackHandler.java
 *
 * Created on June 17, 2006, 11:50 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.*;

/**
 *
 * @author Jiandong Guo
 */
public class SampleUsernamePasswordCallbackHandler implements CallbackHandler {
    
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        
        for (int i=0; i < callbacks.length; i++) {
            Callback callback = callbacks[i];
            if (callback instanceof NameCallback) {
                handleUsernameCallback((NameCallback)callback);
            } else if (callback instanceof PasswordCallback) {
                handlePasswordCallback((PasswordCallback)callback);
            }else{
                throw new UnsupportedCallbackException(callback, "Unknow callback for username or password");
            }
        }
    }
    
    private void handleUsernameCallback(NameCallback cb)throws IOException{
        //BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.err.println("***Please Enter Your User Name: ");
        System.err.flush();
        cb.setName((new BufferedReader(new InputStreamReader(System.in))).readLine());
    }
    
    private void handlePasswordCallback(PasswordCallback cb)throws IOException{
        //BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.err.println("***Please Enter Your Password: ");
        System.err.flush();
        cb.setPassword((new BufferedReader(new InputStreamReader(System.in))).readLine().toCharArray());
    }
}
