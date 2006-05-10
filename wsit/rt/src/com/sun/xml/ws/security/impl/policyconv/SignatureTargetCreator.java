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
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.Parameter;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SignatureTargetCreator {
    private boolean enforce = false;
    private AlgorithmSuite algorithmSuite = null;
    private boolean contentOnly = false;
    /**
     * Creates a new instance of SignatureTargetCreator
     */
    public SignatureTargetCreator(boolean enforce,AlgorithmSuite algorithmSuite,boolean contentOnly) {
        this.enforce = enforce;
        this.algorithmSuite = algorithmSuite;
    }
    
    public SignatureTarget newURISignatureTarget(String uid){
        if ( uid != null ) {
            SignatureTarget target = new SignatureTarget();
            target.setType(SignatureTarget.TARGET_TYPE_VALUE_URI);
            target.setDigestAlgorithm(algorithmSuite.getDigestAlgorithm());
            target.setValue("#"+uid);
            addEXC14n(target);
            target.setEnforce(enforce);
            return target;
        }
        return null;
    }
    
    public SignatureTarget newXpathSignatureTarget(String xpathTarget){
        SignatureTarget target = new SignatureTarget();
        target.setType(SignatureTarget.TARGET_TYPE_VALUE_XPATH);
        target.setDigestAlgorithm(algorithmSuite.getDigestAlgorithm());
        addEXC14n(target);
        target.setValue(xpathTarget);
        target.setContentOnly(contentOnly);
        target.setEnforce(enforce);
        return target;
    }
    
    public SignatureTarget newQNameSignatureTarget(QName name){
        SignatureTarget target = new SignatureTarget();
        target.setType(SignatureTarget.TARGET_TYPE_VALUE_QNAME);
        target.setDigestAlgorithm(algorithmSuite.getDigestAlgorithm());
        target.setContentOnly(contentOnly);
        target.setEnforce(enforce);
        target.setQName(name);
        addEXC14n(target);
        return target;
    }
    
    public static void addEXC14n(SignatureTarget target){
        SignatureTarget.Transform tr = target.newSignatureTransform();
        tr.setTransform(CanonicalizationMethod.EXCLUSIVE);
        target.addTransform(tr);
    }
    
    public static void addSTRTransform(SignatureTarget target){
        SignatureTarget.Transform tr = target.newSignatureTransform();
        tr.setTransform(MessageConstants.STR_TRANSFORM_URI);
        target.addTransform(tr);
        tr.setAlgorithmParameters(new Parameter("CanonicalizationMethod",CanonicalizationMethod.EXCLUSIVE));
    }
}
