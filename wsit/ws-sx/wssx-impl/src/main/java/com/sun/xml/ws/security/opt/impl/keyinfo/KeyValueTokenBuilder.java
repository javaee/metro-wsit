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

package com.sun.xml.ws.security.opt.impl.keyinfo;

import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import java.util.logging.Level;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;
import java.security.Key;
import java.security.KeyPair;

/**
 *
 * @author shyam.rao@sun.com
 */
public class KeyValueTokenBuilder extends TokenBuilder{
    
    AuthenticationTokenPolicy.KeyValueTokenBinding binding = null;
    /** Creates a new instance of X509TokenBuilder */
    public KeyValueTokenBuilder(JAXBFilterProcessingContext context, AuthenticationTokenPolicy.KeyValueTokenBinding binding) {
        super(context);
        this.binding = binding;
    }
    
    /**
     * 
     * @return BuilderResult
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public BuilderResult process() throws XWSSecurityException{
                
        String referenceType = binding.getReferenceType();
        if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST, LogStringsMessages.WSS_1851_REFERENCETYPE_X_509_TOKEN(referenceType));
        }
        Key dataProtectionKey = null;
        BuilderResult result = new BuilderResult();
        KeyPair keyPair = (KeyPair)context.getExtraneousProperties().get("UseKey-RSAKeyPair");
        /*if(keyPair == null){
            KeyPairGenerator kpg;            
            try{
                kpg = KeyPairGenerator.getInstance("RSA");
                //RSAKeyGenParameterSpec rsaSpec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F0);
                //kpg.initialize(rsaSpec);                
            }catch (NoSuchAlgorithmException ex){
                throw new XWSSecurityException("Unable to create key pairs in Security Layer for KeyValueToken/RsaToken policy", ex);
            }
            //catch (InvalidAlgorithmParameterException ex){
            //    throw new XWSSecurityException("Unable to create key pairs in Security Layer for KeyValueToken/RsaToken policy", ex);
            //}
            kpg.initialize(512);
            keyPair = kpg.generateKeyPair();
            if(keyPair == null){
                throw new XWSSecurityException("RSA keypair is not generated/set for supporting token (KeyValueToken or RsaToken).");
            }
        }*/
        if (keyPair != null){
            dataProtectionKey = keyPair.getPrivate();
            if (dataProtectionKey == null) {
                //log here
                throw new XWSSecurityException("PrivateKey null inside PrivateKeyBinding set for KeyValueToken/RsaToken Policy ");
            }
            buildKeyInfo(keyPair.getPublic());
            result.setDataProtectionKey(dataProtectionKey);
            result.setKeyInfo(keyInfo);
        }
        return result;
    }    
}
