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

import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.Header;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
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
    private boolean encryptAttachments = false;    
    private HashSet<Header> encryptedParts = new HashSet<Header>();
    //  private EncryptionTargetCreator etc =null;
    private EncryptionTargetCreator etCreator = null;
    /** Creates a new instance of EncryptionAssertionProcessor */
    public EncryptionAssertionProcessor(AlgorithmSuite algorithmSuite,boolean enforce) {
        this.algorithmSuite = algorithmSuite;
        //this.enforce = enforce;
        this.etCreator = new EncryptionTargetCreator(algorithmSuite,enforce);
    }
    
    public EncryptionTargetCreator getTargetCreator(){
        return etCreator;
    }
    
    public void process(EncryptedParts encryptParts,EncryptionPolicy.FeatureBinding binding){
        if(SecurityPolicyUtil.isEncryptedPartsEmpty(encryptParts)){
            if(!bodyEncrypted){
                EncryptionTarget target = etCreator.newQNameEncryptionTarget(EncryptionTarget.BODY_QNAME);
                target.setContentOnly(true);
                binding.addTargetBinding(target);              
                bodyEncrypted = true;
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
        
        if(encryptParts.hasAttachments() && !encryptAttachments){
            EncryptionTarget target = etCreator.newURIEncryptionTarget(MessageConstants.PROCESS_ALL_ATTACHMENTS);
            target.setContentOnly(true);
            etCreator.addAttachmentTransform(target, MessageConstants.SWA11_ATTACHMENT_CIPHERTEXT_TRANSFORM);
            binding.addTargetBinding(target);
            encryptAttachments = true;
        }
    }
    
    //TODO:merge multiple EncryptedElements
    public void process(EncryptedElements encryptedElements , EncryptionPolicy.FeatureBinding binding){
        Iterator<String> eeItr = encryptedElements.getTargets();
        while(eeItr.hasNext()){
            String xpathTarget = eeItr.next();
            EncryptionTarget target = etCreator.newXpathEncryptionTarget(xpathTarget);
            binding.addTargetBinding(target);          
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
