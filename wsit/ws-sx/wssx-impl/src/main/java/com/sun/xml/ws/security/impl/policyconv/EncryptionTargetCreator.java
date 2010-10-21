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

import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.logger;
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
        if(logger.isLoggable(Level.FINE)){
            logger.log(Level.FINE,"QName Encryption Target with value "+target.getValue()+ " has been added");
        }
        return target;
    }
    
    public EncryptionTarget newXpathEncryptionTarget(String xpathTarget){
        EncryptionTarget target = new EncryptionTarget();
        target.setType(EncryptionTarget.TARGET_TYPE_VALUE_XPATH);
        target.setValue(xpathTarget);
        target.setEnforce(enforce);
        target.setDataEncryptionAlgorithm(algorithmSuite.getEncryptionAlgorithm());
        target.setContentOnly(false);
        if(logger.isLoggable(Level.FINE)){
            logger.log(Level.FINE,"XPath Encryption Target with value "+target.getValue()+ " has been added");
        }
        return target;
    }
    
    public EncryptionTarget newURIEncryptionTarget(String uri){
        EncryptionTarget target = new EncryptionTarget();
        target.setEnforce(enforce);
        target.setDataEncryptionAlgorithm(algorithmSuite.getEncryptionAlgorithm());
        target.setType(EncryptionTarget.TARGET_TYPE_VALUE_URI);
        target.setValue(uri);
        target.setContentOnly(false);
        if(logger.isLoggable(Level.FINE)){
            logger.log(Level.FINE,"URI Encryption Target with value "+target.getValue()+ " has been added");
        }
        return target;
    }
    
    public void addAttachmentTransform(EncryptionTarget target, String transformURI){
        EncryptionTarget.Transform tr = target.newEncryptionTransform();
        tr.setTransform(transformURI);
        target.addCipherReferenceTransform(tr);
    }
}
