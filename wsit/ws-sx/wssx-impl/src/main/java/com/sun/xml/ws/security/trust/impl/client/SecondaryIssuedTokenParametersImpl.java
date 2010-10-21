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

package com.sun.xml.ws.security.trust.impl.client;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.api.security.trust.client.SecondaryIssuedTokenParameters;

/**
 *
 * @author Jiandong Guo
 */
public class SecondaryIssuedTokenParametersImpl implements SecondaryIssuedTokenParameters{

    private String tokenType = null;
    
    private String keyType = null;
    
    private long keySize = -1;
    
    private String signatureAlg = null;
    
    private String encAlg = null;
    
    private String canAlg = null;
    
    private String keyWrapAlg = null;
    
    private String signWith = null;
    
    private String encryptWith = null;
    
    private Claims claims = null;
    
    public void setTokenType(String tokenType){
        this.tokenType = tokenType;
    }
    
    public void setKeyType(String keyType){
        this.keyType = keyType;
    }
    
    public void setKeySize(long keySize){
        this.keySize = keySize;
    }
    
    public void setSignWith(String signWithAlg){
        this.signWith = signWithAlg;
    }
    
    public void setEncryptWith(String encWithAlg){
        this.encryptWith = encWithAlg;
    }
    
    public void setSignatureAlgorithm(String sigAlg){
        this.signatureAlg = sigAlg;
    }
    
    public void setEncryptionAlgorithm(String encAlg){
        this.encAlg = encAlg;
    }
    
    public void setCanonicalizationAlgorithm(String canAlg){
        this.canAlg = canAlg;
    }
    
    public void setKeyWrapAlgorithm(String keyWrapAlg){
        this.keyWrapAlg = keyWrapAlg;
    }
    
    public void setClaims(Claims claims){
        this.claims = claims;
    }

    public String getTokenType(){
        return this.tokenType;
    }
    
    public String getKeyType(){
        return this.keyType;
    }
    
    public long getKeySize(){
        return this.keySize;
    }
    
    public String getSignatureAlgorithm(){
        return this.signatureAlg;
    }
    
    public String getEncryptionAlgorithm(){
        return this.encAlg;
    }
    
    public String getCanonicalizationAlgorithm(){
        return this.canAlg;
    }
    
    public String getKeyWrapAlgorithm(){
        return this.keyWrapAlg;
    }
    
    public String getSignWith(){
        return signWith;
    }
    
    public String getEncryptWith(){
        return encryptWith;
    }
    
    public Claims getClaims(){
        return this.claims;
    }
}
