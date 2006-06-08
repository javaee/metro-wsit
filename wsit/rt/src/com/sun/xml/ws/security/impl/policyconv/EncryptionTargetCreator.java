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
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class EncryptionTargetCreator {
    
    public AlgorithmSuite algorithmSuite;
    public boolean enforce = false;
    /** Creates a new instance of EncryptionTargetCreator */
    public EncryptionTargetCreator(AlgorithmSuite algorithmSuite,boolean enforce) {
        this.algorithmSuite = algorithmSuite;
        this.enforce = enforce;
    }
    
    public EncryptionTarget newQNameEncryptionTarget(QName targetValue){
        EncryptionTarget target = new EncryptionTarget();
        target.setEnforce(enforce);
        target.setDataEncryptionAlgorithm(algorithmSuite.getEncryptionAlgorithm());
        target.setType(EncryptionTarget.TARGET_TYPE_VALUE_QNAME);
        target.setQName(targetValue);
        //target.setValue(EncryptionTarget.BODY);
        target.setValue("{"+targetValue.getNamespaceURI()+"}"+targetValue.getLocalPart());
        target.setContentOnly(false);
        return target;
    }
    
    public EncryptionTarget newXpathEncryptionTarget(String xpathTarget){
        EncryptionTarget target = new EncryptionTarget();
        target.setType(EncryptionTarget.TARGET_TYPE_VALUE_XPATH);
        target.setValue(xpathTarget);
        target.setEnforce(enforce);
        target.setDataEncryptionAlgorithm(algorithmSuite.getEncryptionAlgorithm());
        target.setContentOnly(false);
        return target;
    }
    
    public EncryptionTarget newURIEncryptionTarget(String uri){
        EncryptionTarget target = new EncryptionTarget();
        target.setEnforce(enforce);
        target.setDataEncryptionAlgorithm(algorithmSuite.getEncryptionAlgorithm());
        target.setType(EncryptionTarget.TARGET_TYPE_VALUE_URI);
        target.setValue(uri);
        target.setContentOnly(false);
        return target;
    }
}
