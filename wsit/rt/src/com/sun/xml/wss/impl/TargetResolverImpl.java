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

package com.sun.xml.wss.impl;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.verifier.TargetResolver;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.LogStringsMessages;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class TargetResolverImpl implements TargetResolver{
    private ProcessingContext ctx = null;
    private FilterProcessingContext fpContext = null;
    private static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /** Creates a new instance of TargetResolverImpl */
    public TargetResolverImpl(ProcessingContext ctx) {
        this.ctx = ctx;
    }
    
    public void resolveAndVerifyTargets(
            List<Target> actualTargets, List<Target> inferredTargets, WSSPolicy actualPolicy) throws XWSSecurityException {
        
        String policyType = PolicyTypeUtil.signaturePolicy(actualPolicy) ? "Signature" : "Encryption";
        boolean isEndorsing = false;
        
        if ( PolicyTypeUtil.signaturePolicy(actualPolicy)) {
            SignaturePolicy.FeatureBinding fp = (SignaturePolicy.FeatureBinding)actualPolicy.getFeatureBinding();
            if (fp.isEndorsingSignature()) {
                isEndorsing = true;
            }
        }
        
        fpContext = new FilterProcessingContext(ctx);
        SecurityHeader header = fpContext.getSecurableSoapMessage().findSecurityHeader();
        Document doc = header.getOwnerDocument();
        
        for(Target actualTarget : actualTargets){
            boolean found = false;
            String targetInPolicy = getTargetValue(doc,actualTarget);
            for(Target inferredTarget : inferredTargets){
                String targetInMessage = getTargetValue(doc,inferredTarget);
                if(targetInPolicy!=null && targetInPolicy.equals(targetInMessage)){
                    found = true;
                    break;
                }
            }
            if(!found && targetInPolicy!=null ){
                //check if message has the target
                //check if the message has the element
                NodeList nl = doc.getElementsByTagName(targetInPolicy);
                if(nl!=null && nl.getLength()>0){
                    log.log(Level.SEVERE,LogStringsMessages.WSS_0206_POLICY_VIOLATION_EXCEPTION());
                    log.log(Level.SEVERE,LogStringsMessages.WSS_0814_POLICY_VERIFICATION_ERROR_MISSING_TARGET(targetInPolicy, policyType));
                    if (isEndorsing) {
                        throw new XWSSecurityException("Policy verification error:" +
                                "Missing target " + targetInPolicy + " for Endorsing " + policyType);
                    } else {
                        throw new XWSSecurityException("Policy verification error:" +
                                "Missing target " + targetInPolicy + " for " + policyType);
                    }
                    
                }
            }
        }
    }
    
    private String getTargetValue(final Document doc, final Target actualTarget) {
        String targetInPolicy = null;
        if(actualTarget.getType() == Target.TARGET_TYPE_VALUE_QNAME){
            targetInPolicy = actualTarget.getQName().getLocalPart();
        }else if(actualTarget.getType() == Target.TARGET_TYPE_VALUE_URI){
            String val = actualTarget.getValue();
            String id = null;
            if(val.charAt(0) == '#')
                id = val.substring(1,val.length());
            else
                id = val;
            Element signedElement = doc.getElementById(id);
            if(signedElement != null){
                targetInPolicy = signedElement.getLocalName();
            }
        }
        return targetInPolicy;
    }
    
    public boolean isTargetPresent(List<Target> actualTargets) throws XWSSecurityException {
        FilterProcessingContext fpContext = new FilterProcessingContext(ctx);
        SecurityHeader header = fpContext.getSecurableSoapMessage().findSecurityHeader();
        Document doc = header.getOwnerDocument();
        for(Target actualTarget : actualTargets){
            if(actualTarget.getType() == Target.TARGET_TYPE_VALUE_XPATH){
                String val = actualTarget.getValue();
                try{
                    XPathFactory xpathFactory = XPathFactory.newInstance();
                    XPath xpath = xpathFactory.newXPath();
                    xpath.setNamespaceContext(fpContext.getSecurableSoapMessage().getNamespaceContext());
                    XPathExpression xpathExpr = xpath.compile(val);
                    NodeList nodes = (NodeList)xpathExpr.evaluate((Object)fpContext.getSecurableSoapMessage().getSOAPPart(),XPathConstants.NODESET);
                    if(nodes != null && nodes.getLength() >0){
                        return true;
                    }
                }catch(XPathExpressionException xpe){
                    throw new XWSSecurityException(xpe);
                }
            }else{
                String targetInPolicy = getTargetValue(doc,actualTarget);
                NodeList nl = doc.getElementsByTagName(targetInPolicy);
                if(nl!=null && nl.getLength()>0){
                    return true;
                }
            }
        }
        return false;
    }
    
}
