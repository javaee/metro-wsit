/*
 * TokenProcessor.java
 *
 * Created on September 8, 2006, 10:44 AM
 */

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

package com.sun.xml.ws.security.opt.impl.dsig;

import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.api.keyinfo.TokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.DerivedKeyTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.IssuedTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.KerberosTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.SCTBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.SamlTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.KeyValueTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.SymmetricTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.X509TokenBuilder;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.keyinfo.KeyValueTokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.UsernameTokenBuilder;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.AlgorithmSuite;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;
import java.security.Key;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * TokenProcessor for Signature. Looks at the keyBinding and 
 * polulates BuilderResult with appropriate key and KeyInfo
 * @author Ashutosh.Shahi@sun.com
 */

public class TokenProcessor {
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    private Key  signingKey = null;
    //private KeyInfo siKI = null;
    private TokenBuilder builder = null;
    private WSSPolicy keyBinding = null;
    //private SignaturePolicy sp = null;
    private JAXBFilterProcessingContext context = null;
    
    /**
     * Creates a new instance of TokenProcessor
     * @param sp SignaturePolicy
     * @param context the ProcessingContext
     */
    public TokenProcessor(SignaturePolicy sp,JAXBFilterProcessingContext context) {
        //this.sp = sp;
        this.context = context;
        this.keyBinding = (WSSPolicy)sp.getKeyBinding();
    }
    
    /**
     * process the keyBinding and populate BuilderResult with appropriate key and KeyInfo
     * @return <CODE>BuilderResult</CODE> populated with appropriate values
     * @throws com.sun.xml.wss.XWSSecurityException 
     */
    public BuilderResult process()
    throws XWSSecurityException{
        
        String keyEncAlgo = XMLCipher.RSA_v1dot5;  //<--Harcoding of Algo
        String dataEncAlgo = MessageConstants.TRIPLE_DES_BLOCK_ENCRYPTION;
        
        AlgorithmSuite algSuite = context.getAlgorithmSuite();
        String tmp = null;
        if(algSuite != null){
            tmp = algSuite.getAsymmetricKeyAlgorithm();
        }
        if(tmp != null && !"".equals(tmp)){
            keyEncAlgo = tmp;
        }
        if(algSuite != null){
            tmp = algSuite.getEncryptionAlgorithm();
        }
        if(tmp != null && !"".equals(tmp)){
            dataEncAlgo = tmp;
        }
        
        if (PolicyTypeUtil.UsernameTokenBinding(keyBinding)) {            
            AuthenticationTokenPolicy.UsernameTokenBinding usernameTokenBinding = null;
            if ( context.getusernameTokenBinding() != null ) {
                usernameTokenBinding  = context.getusernameTokenBinding();
                context.setUsernameTokenBinding(null);
            } else {
                usernameTokenBinding =(AuthenticationTokenPolicy.UsernameTokenBinding)keyBinding;
            }      
            signingKey = usernameTokenBinding.getSecretKey();
            builder = new UsernameTokenBuilder(context,usernameTokenBinding);
            BuilderResult untResult = builder.process();            
            untResult.setDataProtectionKey(signingKey);
            return untResult;
            
        } else if(PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
            AuthenticationTokenPolicy.X509CertificateBinding certificateBinding = null;
            if ( context.getX509CertificateBinding() != null) {
                certificateBinding  = context.getX509CertificateBinding();
                context.setX509CertificateBinding(null);
            } else {
                certificateBinding  =(AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
            }
            
            PrivateKeyBinding privKBinding  = (PrivateKeyBinding)certificateBinding.getKeyBinding();
            signingKey = privKBinding.getPrivateKey();
            
            builder = new X509TokenBuilder(context,certificateBinding);
            BuilderResult xtbResult = builder.process();
            
            xtbResult.setDataProtectionKey(signingKey);
            return xtbResult;
        } else if(PolicyTypeUtil.kerberosTokenBinding(keyBinding)){
            AuthenticationTokenPolicy.KerberosTokenBinding krbBinding = null;
            if(context.getKerberosTokenBinding() != null){
                krbBinding = context.getKerberosTokenBinding();
                context.setKerberosTokenBinding(null);
            } else{
                krbBinding = (AuthenticationTokenPolicy.KerberosTokenBinding)keyBinding;
            }
            
            signingKey = krbBinding.getSecretKey();
            builder = new KerberosTokenBuilder(context, krbBinding);
            BuilderResult ktbResult = builder.process();
            ktbResult.setDataProtectionKey(signingKey);
            
            return ktbResult;
        } else if (PolicyTypeUtil.symmetricKeyBinding(keyBinding)) {
            SymmetricKeyBinding skb = null;
            if ( context.getSymmetricKeyBinding() != null) {
                skb = context.getSymmetricKeyBinding();
                context.setSymmetricKeyBinding(null);
            } else {
                skb = (SymmetricKeyBinding)keyBinding;
            }
            
            builder = new SymmetricTokenBuilder(skb, context, dataEncAlgo,keyEncAlgo);
            BuilderResult skbResult = builder.process();
            return skbResult;
        }  else if ( PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)) {
            DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding;
            ((NamespaceContextEx)context.getNamespaceContext()).addSCNS();
            builder = new DerivedKeyTokenBuilder(context, dtk);
            BuilderResult dtkResult = builder.process();
            return dtkResult;
        }  else if ( PolicyTypeUtil.issuedTokenKeyBinding(keyBinding)) {
            IssuedTokenBuilder itb = new IssuedTokenBuilder(context,(IssuedTokenKeyBinding)keyBinding);
            BuilderResult itbResult = itb.process();
            return itbResult;
        } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(keyBinding)) {
            ((NamespaceContextEx)context.getNamespaceContext()).addSCNS();
            SCTBuilder builder = new SCTBuilder(context,(SecureConversationTokenKeyBinding)keyBinding);
            BuilderResult sctResult = builder.process();
            return sctResult;
        } else if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {
            ((NamespaceContextEx)context.getNamespaceContext()).addSAMLNS();
            SamlTokenBuilder stb = new SamlTokenBuilder(context,(AuthenticationTokenPolicy.SAMLAssertionBinding)keyBinding,true);
            return stb.process();
        } else if (PolicyTypeUtil.keyValueTokenBinding(keyBinding)) {
            ((NamespaceContextEx)context.getNamespaceContext()).addSAMLNS();            
            KeyValueTokenBuilder builder = new KeyValueTokenBuilder(context,(AuthenticationTokenPolicy.KeyValueTokenBinding)keyBinding);
            BuilderResult kvtResult = builder.process();                        
            return kvtResult;            
         } else{
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1703_UNSUPPORTED_KEYBINDING_SIGNATUREPOLICY(keyBinding));
            throw new UnsupportedOperationException("Unsupported Key Binding"+keyBinding);
            
        }
    }
    
}
