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

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.Header;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.Target;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class EncryptionAssertionProcessor {
    private AlgorithmSuite algorithmSuite = null;
    private boolean bodyEncrypted = false;
    private boolean enforce = false;
    private HashSet<Header> encryptedParts = new HashSet<Header>();
    //  private EncryptionTargetCreator etc =null;
    private EncryptionTargetCreator etCreator = null;
    /** Creates a new instance of EncryptionAssertionProcessor */
    public EncryptionAssertionProcessor(AlgorithmSuite algorithmSuite,boolean enforce) {
        this.algorithmSuite = algorithmSuite;
        this.enforce = enforce;
        this.etCreator = new EncryptionTargetCreator(algorithmSuite,enforce);
    }
    
    public EncryptionTargetCreator getTargetCreator(){
        return etCreator;
    }
    
    public void process(EncryptedParts encryptParts,EncryptionPolicy.FeatureBinding binding){
        if(SecurityPolicyUtil.isEncryptedPartsEmpty(encryptParts)){
            if(!bodyEncrypted){
                
            }
        }
        Iterator tv = encryptParts.getTargets();
        while(tv.hasNext()){
            Header ht = (Header)tv.next();
            if(!seenEncryptedParts(ht)){
                EncryptionTarget target = etCreator.newQNameEncryptionTarget(new QName(ht.getURI(),ht.getLocalName()));
                target.isSOAPHeadersOnly(true);
                binding.addTargetBinding(target);
            }
        }
        
        if(encryptParts.hasBody() && !bodyEncrypted){
            EncryptionTarget target = etCreator.newQNameEncryptionTarget(EncryptionTarget.BODY_QNAME);
            target.setContentOnly(true);
            binding.addTargetBinding(target);
            bodyEncrypted = true;
        }
    }
    
    //TODO:merge multiple EncryptedElements
    public void process(EncryptedElements encryptedElements , EncryptionPolicy.FeatureBinding binding){
        Iterator<String> eeItr = encryptedElements.getTargets();
        while(eeItr.hasNext()){
            String xpathTarget = eeItr.next();
            EncryptionTarget target = etCreator.newXpathEncryptionTarget(xpathTarget);
        }
    }
    
    private boolean seenEncryptedParts(Header header){
        if(encryptedParts.contains(header)){
            return true;
        }
        encryptedParts.add(header);
        return false;
    }
    
    public void process(QName targetName,EncryptionPolicy.FeatureBinding binding){
        EncryptionTarget target = etCreator.newQNameEncryptionTarget(targetName);
        binding.addTargetBinding(target);
    }
}
