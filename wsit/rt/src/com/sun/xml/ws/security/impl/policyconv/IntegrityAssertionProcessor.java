/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.Header;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.Target;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class IntegrityAssertionProcessor {
    
    private AlgorithmSuite algorithmSuite = null;
    private boolean contentOnly = false;
    
    
    private boolean seenBody = false;
    private boolean seenAttachments = false;
    private HashSet<Header> signParts  = new HashSet<Header>();
    private boolean allHeaders = false;
    private boolean ENFORCE = false;
    private SignatureTargetCreator targetCreator = null;
    
    /** Creates a new instance of IntegrityAssertionProcessor */
    
    public IntegrityAssertionProcessor(AlgorithmSuite algorithmSuite, boolean contentOnly) {
        this.algorithmSuite = algorithmSuite;
        this.contentOnly = contentOnly;
        targetCreator = new SignatureTargetCreator(false,algorithmSuite,contentOnly);
    }
    
    public SignatureTargetCreator getTargetCreator(){
        return targetCreator;
    }
    
    public void process(SignedParts signedParts,SignaturePolicy.FeatureBinding binding){
        Iterator tv = signedParts.getHeaders();
        if(SecurityPolicyUtil.isSignedPartsEmpty(signedParts)){
            if(!allHeaders){
                SignatureTarget target = targetCreator.newURISignatureTarget("");
                targetCreator.addTransform(target);
                target.setValue(SignatureTarget.ALL_MESSAGE_HEADERS);
                target.isSOAPHeadersOnly(true);
                binding.addTargetBinding(target);
                target.setContentOnly(contentOnly);
                allHeaders = true;
            }
            if(!seenBody){
                SignatureTarget target = targetCreator.newQNameSignatureTarget(Target.BODY_QNAME);
                targetCreator.addTransform(target);
                binding.addTargetBinding(target);
                target.setContentOnly(contentOnly);
                seenBody = true;
            }
        }else{
            while(tv.hasNext()){
                Header ht = (Header)tv.next();
                if(!allHeaders && !seenSignTarget(ht)){
                    SignatureTarget target = targetCreator.newQNameSignatureTarget(new QName(ht.getURI(),ht.getLocalName()));
                    targetCreator.addTransform(target);
                    target.isSOAPHeadersOnly(true);
                    target.setContentOnly(contentOnly);
                    binding.addTargetBinding(target);
                }
            }
            if(signedParts.hasBody()){
                if(!seenBody){
                    SignatureTarget target = targetCreator.newQNameSignatureTarget(Target.BODY_QNAME);
                    targetCreator.addTransform(target);
                    target.setContentOnly(contentOnly);
                    binding.addTargetBinding(target);
                    seenBody = true;
                }
            }
            if(signedParts.hasAttachments()){
                if(!seenAttachments){
                    SignatureTarget target = targetCreator.newURISignatureTarget("");
                    target.setValue(MessageConstants.PROCESS_ALL_ATTACHMENTS);
                    targetCreator.addAttachmentTransform(target, signedParts.attachmentProtectionType());
                    binding.addTargetBinding(target);
                    seenAttachments = true;
                }
            }
        }
        signParts.clear();
    }
    
    public void process(SignedElements signedElements,SignaturePolicy.FeatureBinding binding){
        Iterator<String> itr = signedElements.getTargets();
        while(itr.hasNext()){
            String xpathTarget = itr.next();
            SignatureTarget target = targetCreator.newXpathSignatureTarget(xpathTarget);
            targetCreator.addTransform(target);
            target.setContentOnly(contentOnly);
            //  target.setXPathVersion(signedElements.)
            binding.addTargetBinding(target);
        }
    }
    
    private boolean seenSignTarget(Header name ){
//        Iterator<Header> itr = signParts.iterator();
//        while(itr.hasNext()){
//            Header header = itr.next();
//            if(header.getLocalName().equals(name.getLocalName()) && header.getURI().equals(name.getURI())){
//                return true;
//            }
//        }
        if(signParts.contains(name)){
            return true;
        }
        signParts.add(name);
        return false;
    }
    
    public void process(QName targetName,SignaturePolicy.FeatureBinding binding){
        SignatureTarget target = targetCreator.newQNameSignatureTarget(targetName);
        targetCreator.addTransform(target);
        binding.addTargetBinding(target);
    }
}
