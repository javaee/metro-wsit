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

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.namespace.QName;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.ws.security.impl.policy.Constants;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityPolicyUtil {
    
    private static final QName signaturePolicy = new QName(MessageConstants.DSIG_NS, MessageConstants.SIGNATURE_LNAME);
    private static final QName usernameTokenPolicy = new QName(MessageConstants.WSSE_NS, MessageConstants.USERNAME_TOKEN_LNAME);
    private static final QName x509TokenPolicy = new QName(MessageConstants.WSSE_NS, "BinarySecurityToken");
    private static final QName timestampPolicy = new QName(MessageConstants.WSU_NS, MessageConstants.TIMESTAMP_LNAME);
    
    /** Creates a new instance of SecurityPolicyUtil */
    public SecurityPolicyUtil() {
    }
    
    public static boolean isSignedPartsEmpty(SignedParts sp){
        if(!(sp.hasBody() || sp.hasAttachments())){
            if(!sp.getHeaders().hasNext()){
                return true;
            }
        }
        return false;
    }
    
    public static boolean isEncryptedPartsEmpty(EncryptedParts ep){
        if(!(ep.hasBody() || ep.hasAttachments())){
            if(!ep.getTargets().hasNext()){
                return true;
            }
        }
        return false;
    }
    
    public static String convertToXWSSConstants(String type){
        if(type.contains(Token.REQUIRE_THUMBPRINT_REFERENCE)){
            return MessageConstants.THUMB_PRINT_TYPE;
        }else if(type.contains(Token.REQUIRE_KEY_IDENTIFIER_REFERENCE)){
            return MessageConstants.KEY_INDETIFIER_TYPE;
        }else if(type.contains(Token.REQUIRE_ISSUER_SERIAL_REFERENCE)){
            return MessageConstants.X509_ISSUER_TYPE ;
        }
        throw new UnsupportedOperationException(type+"  is not supported");
    }
    
    public static void setName(Target target, WSSPolicy policy){
        if(target.getType() == Target.TARGET_TYPE_VALUE_URI){
            target.setPolicyName(getQNameFromPolicy(policy));
        }
    }
    
    private static QName getQNameFromPolicy(WSSPolicy policy){
        if(PolicyTypeUtil.signaturePolicy(policy)){
            return signaturePolicy;
        } else if(PolicyTypeUtil.timestampPolicy(policy)){
            return timestampPolicy;
        } else if(PolicyTypeUtil.x509CertificateBinding(policy)){
            return x509TokenPolicy;
        } else if(PolicyTypeUtil.usernameTokenPolicy(policy)){
            return usernameTokenPolicy;
        } else if(PolicyTypeUtil.secureConversationTokenKeyBinding(policy)){
            return MessageConstants.SCT_NAME;
        }
        return null;
    }
    
    public static void setCanonicalizationMethod(SignaturePolicy.FeatureBinding spFB, AlgorithmSuite algorithmSuite){
        if(algorithmSuite != null && algorithmSuite.getAdditionalProps().contains(Constants.InclusiveC14N)){
            spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.INCLUSIVE);
        } else{
            spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
        }
        
        if(algorithmSuite != null && algorithmSuite.getAdditionalProps().contains(Constants.InclusiveC14NWithCommentsForCm)){
            spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS);
        } else if(algorithmSuite != null && algorithmSuite.getAdditionalProps().contains(Constants.ExclusiveC14NWithCommentsForCm)){
            spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS);
        }
    }
    
    public static SecurityPolicyVersion getSPVersion(PolicyAssertion pa){
        String nsUri = pa.getName().getNamespaceURI();
        SecurityPolicyVersion spVersion = PolicyUtil.getSecurityPolicyVersion(nsUri);
        return spVersion;
    }
}
