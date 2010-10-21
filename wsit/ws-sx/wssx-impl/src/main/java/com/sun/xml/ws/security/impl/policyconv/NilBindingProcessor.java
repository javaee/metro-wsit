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

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedSupportingTokens;
import com.sun.xml.ws.security.policy.EndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.MessageLayout;
import com.sun.xml.ws.security.policy.SignedEncryptedSupportingTokens;
import com.sun.xml.ws.security.policy.SignedEndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;

/**
 *
 * @author ashutoshshahi
 */
public class NilBindingProcessor extends BindingProcessor{
    
    public NilBindingProcessor(boolean isServer,boolean isIncoming,XWSSPolicyContainer container){
        this.container = container;
        this.isIncoming = isIncoming;
        this.isServer = isServer;
        this.tokenProcessor  = new TokenProcessor(isServer,isIncoming,pid);
    }
    
    public void process() throws PolicyException{
        container.setPolicyContainerMode(MessageLayout.Strict);
    }
    
    @Override
    protected void protectPrimarySignature()throws PolicyException{
        
    }
    
    @Override
    protected void protectTimestamp(TimestampPolicy tp){
        
    }
    
    @Override
    protected void protectToken(WSSPolicy token){
        
    }
    
    @Override
    protected void protectToken(WSSPolicy token,boolean ignoreSTR){
        
    }
    
    @Override
    protected void addPrimaryTargets()throws PolicyException{
        
    }
    
    @Override
    protected boolean requireSC(){
        return false;
    }

    @Override
    protected EncryptionPolicy getSecondaryEncryptionPolicy() throws PolicyException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void processSupportingTokens(SupportingTokens st) throws PolicyException{
        
        SupportingTokensProcessor stp =  new SupportingTokensProcessor(
                st, tokenProcessor,getBinding(),container,primarySP,primaryEP,pid);
        stp.process();
    }
    
    @Override
    public void processSupportingTokens(SignedSupportingTokens st) throws PolicyException{
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void processSupportingTokens(EndorsingSupportingTokens est) throws PolicyException{
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void processSupportingTokens(SignedEndorsingSupportingTokens est) throws PolicyException{
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void processSupportingTokens(SignedEncryptedSupportingTokens sest) throws PolicyException{
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void processSupportingTokens(EncryptedSupportingTokens est) throws PolicyException{
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected SignaturePolicy getSignaturePolicy(){
        return null;
    }

    @Override
    protected Binding getBinding() {
        return null;
    }

    @Override
    protected void close() {
        
    }

}
