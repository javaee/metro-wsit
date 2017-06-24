/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import com.sun.xml.wss.impl.callback.PrivateKeyCallback;

import java.io.FileInputStream;
import java.io.IOException;
import javax.security.auth.callback.Callback;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Set;
import javax.security.auth.callback.UnsupportedCallbackException;

public class KeyStoreCallbackHandler implements javax.security.auth.callback.CallbackHandler {

    private String keyStoreURL;
    private String keyStorePassword;
    private String keyStoreType;

    private KeyStore keyStore;

    public void handle(Callback[] callbacks)throws IOException, UnsupportedCallbackException{
        for (Callback callback : callbacks){
            if (callback instanceof KeyStoreCallback){
                Map map = ((KeyStoreCallback)callback).getRuntimeProperties();
                Set keys = map.keySet();
                System.out.println("KeyStoreCallbackHandler Print out keys");
                for (Object key : keys){
                    System.out.println("key="+key);
                    System.out.println("value="+map.get(key));
                }
                ((KeyStoreCallback)callback).setKeystore(keyStore);
            }else if (callback instanceof PrivateKeyCallback){
                ((PrivateKeyCallback)callback).setKeystore(keyStore);
                ((PrivateKeyCallback)callback).setAlias("wssip");
                try{
                    ((PrivateKeyCallback)callback).setKey((PrivateKey)keyStore.getKey("wssip", "changeit".toCharArray()));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }

    }

    public KeyStoreCallbackHandler() {
        try {
            this.keyStoreURL = "C:/metro/apache-tomcat-5.5.16/xws-security/etc/sts-keystore.jks";
            this.keyStoreType = "JKS";
            this.keyStorePassword = "changeit";

            initKeyStore();
        }catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

     private void initKeyStore() throws IOException {
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(new FileInputStream(keyStoreURL), keyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}
