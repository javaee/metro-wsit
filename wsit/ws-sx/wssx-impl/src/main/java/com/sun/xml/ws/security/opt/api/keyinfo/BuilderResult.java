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

package com.sun.xml.ws.security.opt.api.keyinfo;

import com.sun.xml.ws.security.opt.api.EncryptedKey;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import java.security.Key;

/**
 * 
 * Class to store results from TokenBuilder. Stores the various key information
 * @author K.Venugopal@sun.com
 */
public class BuilderResult {
    private Key dataProtectionKey = null;
    private Key keyProtectionKey = null;
    private KeyInfo keyInfo = null;
    private EncryptedKey encryptedKey = null;
    private String dpKID = "";
    /** Creates a new instance of BuilderResult */
    public BuilderResult() {
    }

    /**
     * 
     * @return the data protection key
     */
    public Key getDataProtectionKey() {
        return dataProtectionKey;
    }

    /**
     * 
     * @param dataProtectionKey set the data protection key
     */
    public void setDataProtectionKey(final Key dataProtectionKey) {
        this.dataProtectionKey = dataProtectionKey;
    }

    /**
     * 
     * @return the key protection key
     */
    public Key getKeyProtectionKey() {
        return keyProtectionKey;
    }

    /**
     * 
     * @param keyProtectionKey store the key protection key
     */
    public void setKeyProtectionKey(final Key keyProtectionKey) {
        this.keyProtectionKey = keyProtectionKey;
    }

    /**
     * 
     * @return the stored keyInfo
     */
    public KeyInfo getKeyInfo() {
        return keyInfo;
    }

    /**
     * 
     * @param keyInfo store the keyInfo from <CODE>TokenBuilder</CODE>
     */
    public void setKeyInfo(final KeyInfo keyInfo) {
        this.keyInfo = keyInfo;
    }

    /**
     * 
     * @return the encryptedKey
     */
    public EncryptedKey getEncryptedKey() {
        return encryptedKey;
    }

    /**
     * 
     * @param encryptedKey store the encryptedKey for Signature or Encryption
     */
    public void setEncryptedKey(final EncryptedKey encryptedKey) {
        this.encryptedKey = encryptedKey;
    }
    
    public void setDPTokenId(final String id){
        this.dpKID = id;
    }
    
    public String getDPTokenId(){
        return dpKID;
    }
}
