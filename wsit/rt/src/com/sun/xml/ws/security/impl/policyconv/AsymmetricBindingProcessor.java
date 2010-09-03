/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.AsymmetricBinding;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.Vector;
import java.util.logging.Level;
import static com.sun.xml.ws.security.impl.policy.Constants.logger;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class AsymmetricBindingProcessor extends BindingProcessor {
    private final AsymmetricBinding binding;
  
    
    /** Creates a new instance of AsymmetricBindingProcessor */
    public AsymmetricBindingProcessor(AsymmetricBinding asBinding,XWSSPolicyContainer container,
            boolean isServer,boolean isIncoming,Vector<SignedParts> signedParts,Vector<EncryptedParts> encryptedParts,
            Vector<SignedElements> signedElements,Vector<EncryptedElements> encryptedElements) {
        this.binding = asBinding;
        this.container = container;
        this.isServer = isServer;
        this.isIncoming = isIncoming;
        protectionOrder = binding.getProtectionOrder();
        tokenProcessor = new TokenProcessor(isServer,isIncoming,pid);
        iAP = new IntegrityAssertionProcessor(binding.getAlgorithmSuite(),binding.isSignContent());
        eAP = new EncryptionAssertionProcessor(binding.getAlgorithmSuite(),false);
        this.signedParts = signedParts;
        this.signedElements = signedElements;
        this.encryptedElements = encryptedElements;
        this.encryptedParts = encryptedParts;
        
    }
    
    
    public void process()throws PolicyException{
        Token st = getSignatureToken();
        Token et = getEncryptionToken();
        if(st != null){
            primarySP = new SignaturePolicy();
            primarySP.setUUID(pid.generateID());
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST,"ID of Primary signature policy is "+primarySP.getUUID());
            }            
            tokenProcessor.addKeyBinding(binding,primarySP,st,true);
            SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)primarySP.getFeatureBinding();
            //spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
            SecurityPolicyUtil.setCanonicalizationMethod(spFB, binding.getAlgorithmSuite());
            spFB.isPrimarySignature(true);
        }
        if(et != null){
            primaryEP = new EncryptionPolicy();
            primaryEP.setUUID(pid.generateID());            
            tokenProcessor.addKeyBinding(binding,primaryEP,et,false);
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST,"ID of Encryption policy is "+primaryEP.getUUID());
            }
        }
        if(protectionOrder == Binding.SIGN_ENCRYPT){
            container.insert(primarySP);
        }else{
            container.insert(primaryEP);
            container.insert(primarySP);
            
        }
        addPrimaryTargets();
        if(foundEncryptTargets && binding.getSignatureProtection()){
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST,"PrimarySignature will be Encrypted");
            }
            protectPrimarySignature();
        }
        if(binding.isIncludeTimeStamp()){
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST,"Timestamp header will be added to the message and will be Integrity protected ");
            }
            TimestampPolicy tp = new TimestampPolicy();
            tp.setUUID(pid.generateID());
            container.insert(tp);
            if(!binding.isDisableTimestampSigning()){
                protectTimestamp(tp);
            }
        }
        if(binding.getTokenProtection()){
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST,"Token reference by primary signature with ID "+primarySP.getUUID()+" will be Integrity protected");
            }
            protectToken((WSSPolicy) primarySP.getKeyBinding());
        }
        
    }
    
    protected Token getEncryptionToken(){
        if(isServer^isIncoming){
              Token token = binding.getInitiatorToken();
             if (token == null){
                token = binding.getRecipientEncryptionToken();
            }
            return token;
        }else{
            Token token= binding.getRecipientToken();
            if (token == null){
                token = binding.getInitiatorEncryptionToken();
            }

            return token;
        }
    }
    
    protected Token getSignatureToken(){
        if(isServer^isIncoming){
            Token token = binding.getRecipientToken();
            if (token == null){
                 token = binding.getRecipientSignatureToken();
            }

            return token;
        }else{
            Token token= binding.getInitiatorToken();
            if (token == null){
                token = binding.getInitiatorSignatureToken();
            }

            return token;
        }
    }
    
    protected Binding getBinding(){
        return binding;
    }
    
    protected EncryptionPolicy getSecondaryEncryptionPolicy() throws PolicyException{
        if(sEncPolicy == null){
            sEncPolicy  = new EncryptionPolicy();
            sEncPolicy.setUUID(pid.generateID());
            Token token = getEncryptionToken();
            tokenProcessor.addKeyBinding(binding,sEncPolicy,token,false);
            container.insert(sEncPolicy);
        }
        return sEncPolicy;
    }
    
    protected void close(){
        
        if(protectionOrder == Binding.SIGN_ENCRYPT){
            container.insert(primaryEP);
        }
    }
}
