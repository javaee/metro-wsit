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

import com.sun.xml.ws.security.policy.RequiredElements;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;
import com.sun.xml.wss.impl.policy.mls.MandatoryTargetPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.mls.Target;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class RequiredElementsProcessor {
    
    private List<RequiredElements> assertionList;
    private MessagePolicy mp;
    /** Creates a new instance of RequiredElementProcessor */
    public RequiredElementsProcessor(List<RequiredElements> al,MessagePolicy mp) {
        this.assertionList = al;
    }
    
    
    public void process() throws PolicyGenerationException{
        Vector<String> targetValues = new Vector<String>();
        MandatoryTargetPolicy mt = new MandatoryTargetPolicy();
        MandatoryTargetPolicy.FeatureBinding mfb = new MandatoryTargetPolicy.FeatureBinding();
        mt.setFeatureBinding(mfb);
        List<Target> targets = mfb.getTargetBindings();
        for(RequiredElements re : assertionList){
            Iterator itr = re.getTargets();
            while(itr.hasNext()){
                String xpathExpr = (String)itr.next();
                if(!targetValues.contains(xpathExpr)){
                    targetValues.add(xpathExpr);
                    Target tr = new Target();
                    tr.setType(Target.TARGET_TYPE_VALUE_XPATH);
                    tr.setValue(xpathExpr);
                    tr.setContentOnly(false);
                    tr.setEnforce(true);
                    targets.add(tr);
                }
            }
        } 
       mp.append(mt);  
    }
}
