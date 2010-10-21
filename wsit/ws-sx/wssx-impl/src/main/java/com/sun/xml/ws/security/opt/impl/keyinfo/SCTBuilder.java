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

package com.sun.xml.ws.security.opt.impl.keyinfo;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.api.reference.DirectReference;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextTokenInfo;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import java.security.Key;
import java.util.logging.Level;
import javax.crypto.spec.SecretKeySpec;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SCTBuilder extends TokenBuilder{
    private SecureConversationTokenKeyBinding sctBinding = null;
    /** Creates a new instance of SCTBuilder */
    public SCTBuilder(JAXBFilterProcessingContext context,SecureConversationTokenKeyBinding kb) {
        super(context);
        this.sctBinding = kb;      
    }
    /**
     * 
     * @return BuilderResult
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public BuilderResult process() throws XWSSecurityException {
        BuilderResult sctResult = new BuilderResult();       
        String dataEncAlgo = SecurityUtil.getDataEncryptionAlgo(context);       
        String sctPolicyId = sctBinding.getUUID();
        //Look for SCT in TokenCache
        SecurityElement sct = context.getSecurityHeader().getChildElement(sctPolicyId);
        IssuedTokenContext ictx = context.getSecureConversationContext();
        String sctVersion = sctBinding.getIncludeToken();
        boolean includeToken = (sctBinding.INCLUDE_ALWAYS.equals( sctVersion) ||
                                sctBinding.INCLUDE_ALWAYS_TO_RECIPIENT.equals( sctVersion) ||
                                sctBinding.INCLUDE_ALWAYS_VER2.equals( sctVersion) ||
                                sctBinding.INCLUDE_ALWAYS_TO_RECIPIENT_VER2.equals( sctVersion)
                                );
        com.sun.xml.ws.security.SecurityContextToken sct1 = null;
        if (sct == null) {
            sct1 =(com.sun.xml.ws.security.SecurityContextToken)ictx.getSecurityToken();
            if (sct1 == null) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1809_SCT_NOT_FOUND());
                throw new XWSSecurityException("SecureConversation Token not Found");
            }
            sct  = context.getSecurityHeader().getChildElement(sct1.getWsuId());
            if(sct == null){
                sct1 = com.sun.xml.wss.impl.misc.SecurityUtil.getSCT(sct1, context.getSOAPVersion());
                if(includeToken){
                    if(context.getSecurityPolicyVersion().equals(MessageConstants.SECURITYPOLICY_12_NS)){
                        context.getSecurityHeader().add((SecurityContextToken13)sct1);
                    }else{
                        context.getSecurityHeader().add((SecurityContextToken)sct1);
                    }
                } 
                if(context.getSecurityPolicyVersion().equals(MessageConstants.SECURITYPOLICY_12_NS)){
                    sct = (SecurityContextToken13)sct1;
                }else{
                    sct = (SecurityContextToken)sct1;
                }                
            }
            //Add ext elements;
        }
   
        String sctWsuId = sct.getId();
        if (sctWsuId == null) {
            sct.setId(context.generateID());
            sctWsuId = sct.getId();
        }               
        Key dataProtectionKey = null;       
        DirectReference directRef = elementFactory.createDirectReference();
        if(includeToken){
            directRef.setURI("#"+sctWsuId);
        } else{
            directRef.setURI(sct1.getIdentifier().toString());  
        }       
        if (!sctBinding.INCLUDE_ALWAYS_TO_RECIPIENT.equals(sctBinding.getIncludeToken()) ||
                !sctBinding.INCLUDE_ALWAYS.equals(sctBinding.getIncludeToken())) {
            if(context.getSecurityPolicyVersion().equals(MessageConstants.SECURITYPOLICY_12_NS)){
                directRef.setValueType(MessageConstants.SCT_13_VALUETYPE);                
            }else{
                directRef.setValueType(MessageConstants.SCT_VALUETYPE);                
            }
        }
     
        if(sct1.getInstance() != null && !context.isExpired()){
            ((com.sun.xml.ws.security.opt.impl.reference.DirectReference)directRef).setAttribute(
                    context.getWSSCVersion(context.getSecurityPolicyVersion()), "Instance", sct1.getInstance());
        }   
        byte[] proofKey = null;    
        if(sct1.getInstance() != null){
            if(context.isExpired()){
                proofKey = ictx.getProofKey();
            }else{
               if(ictx.getSecurityContextTokenInfo() != null){
                SecurityContextTokenInfo sctInstanceInfo = ictx.getSecurityContextTokenInfo();
                 proofKey = sctInstanceInfo.getInstanceSecret(sct1.getInstance());
               } else {
                   proofKey = ictx.getProofKey();
               }
            }
        }else{
            proofKey = ictx.getProofKey();
        }
        String jceAlgo = SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo);
        //dataProtectionKey = new SecretKeySpec(ictx.getProofKey(), jceAlgo);
        dataProtectionKey = new SecretKeySpec(proofKey, jceAlgo);
        buildKeyInfo(directRef,context.generateID());
        sctResult.setKeyInfo(super.keyInfo);
        sctResult.setDataProtectionKey(dataProtectionKey);
        return sctResult;
    }
}
