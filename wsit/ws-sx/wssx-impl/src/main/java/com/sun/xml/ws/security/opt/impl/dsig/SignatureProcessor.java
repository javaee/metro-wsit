/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * SignatureProcessor.java
 *
 * Created on August 10, 2006, 2:56 PM
 */

package com.sun.xml.ws.security.opt.impl.dsig;

import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.impl.util.NamespaceAndPrefixMapper;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBSignContext;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.opt.signature.LogStringsMessages;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader;

import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import java.security.Key;
import java.util.List;
import java.util.ArrayList;


/**
 *
 * @author Ashutosh.Shahi@sun.com
 */

public class SignatureProcessor {
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE);
    
    /** Creates a new instance of SignatureProcessor */
    public SignatureProcessor() {
    }
    
    /**
     * 
     * performs the signature
     * @param context JAXBFilterProcessingContext
     * @return errorCode
     * @throws XWSSecurityException
     */
    @SuppressWarnings("unchecked")
    public static int sign(JAXBFilterProcessingContext context) throws XWSSecurityException {
        try{
            SignaturePolicy signaturePolicy  = (SignaturePolicy)context.getSecurityPolicy();
            ((NamespaceContextEx)context.getNamespaceContext()).addSignatureNS();
            WSSPolicy keyBinding = (WSSPolicy)signaturePolicy.getKeyBinding();
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST, "KeyBinding is "+keyBinding);
            }
            
            Key signingKey = null;

            SignatureElementFactory signFactory = new SignatureElementFactory();
            
            KeyInfo keyInfo = null;
            SecurityHeader securityHeader = context.getSecurityHeader();
            
            //Get the Signing key and KeyInfo from TokenProcessor
            TokenProcessor tokenProcessor = new TokenProcessor(signaturePolicy, context);
            BuilderResult builderResult = tokenProcessor.process();
            signingKey = builderResult.getDataProtectionKey();
            keyInfo = builderResult.getKeyInfo();
            
            if (keyInfo != null || !keyBinding.isOptional()){
                SignedInfo signedInfo = signFactory.constructSignedInfo(context);
                JAXBSignContext signContext = new JAXBSignContext(signingKey);
                signContext.setURIDereferencer(DSigResolver.getInstance());
                XMLSignature signature = signFactory.constructSignature(signedInfo, keyInfo, signaturePolicy.getUUID());            
                signContext.put(MessageConstants.WSS_PROCESSING_CONTEXT, context);            
                NamespaceAndPrefixMapper npMapper = new NamespaceAndPrefixMapper(context.getNamespaceContext(), context.getDisableIncPrefix());
                signContext.put(NamespaceAndPrefixMapper.NS_PREFIX_MAPPER, npMapper);            
                signContext.putNamespacePrefix(MessageConstants.DSIG_NS, MessageConstants.DSIG_PREFIX);
                signature.sign(signContext);
            
                JAXBSignatureHeaderElement jaxBSign = new JAXBSignatureHeaderElement((com.sun.xml.ws.security.opt.crypto.dsig.Signature)signature,context.getSOAPVersion());
                securityHeader.add(jaxBSign);
            
                //For SignatureConfirmation
                List scList = (ArrayList)context.getExtraneousProperty("SignatureConfirmation");
                if(scList != null){
                    scList.add(Base64.encode(signature.getSignatureValue().getValue()));
                } 
            }
            //End SignatureConfirmation specific code
            
        } catch(XWSSecurityException xe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1701_SIGN_FAILED(), xe);
            throw xe;
        } catch(Exception ex){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1701_SIGN_FAILED(), ex);
            throw new XWSSecurityException(ex);
        }        
        return 0;
    }
    
}
