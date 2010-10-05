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
package com.sun.xml.ws.security.opt.impl.keyinfo;

import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BinarySecurityToken;
import com.sun.xml.ws.security.opt.api.keyinfo.BuilderResult;
import com.sun.xml.ws.security.opt.api.reference.DirectReference;
import com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier;
import com.sun.xml.ws.security.opt.impl.reference.X509Data;
import com.sun.xml.ws.security.opt.impl.reference.X509IssuerSerial;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class X509TokenBuilder extends TokenBuilder {

    AuthenticationTokenPolicy.X509CertificateBinding binding = null;

    /** Creates a new instance of X509TokenBuilder */
    public X509TokenBuilder(JAXBFilterProcessingContext context, AuthenticationTokenPolicy.X509CertificateBinding binding) {
        super(context);
        this.binding = binding;
    }

    /**
     * processes the token and obtain the keys
     * @return BuilderResult
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    @SuppressWarnings("unchecked")
    public BuilderResult process() throws XWSSecurityException {

        String x509id = binding.getUUID();
        if (x509id == null || x509id.equals("")) {
            x509id = context.generateID();
        }
        SecurityUtil.checkIncludeTokenPolicyOpt(context, binding, x509id);

        String referenceType = binding.getReferenceType();
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, LogStringsMessages.WSS_1851_REFERENCETYPE_X_509_TOKEN(referenceType));
        }
        BuilderResult result = new BuilderResult();
        if (referenceType.equals("Direct")) {
            BinarySecurityToken bst = createBinarySecurityToken(binding, binding.getX509Certificate());
            if (bst == null) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1802_WRONG_TOKENINCLUSION_POLICY(), "creating binary security token failed");
                throw new XWSSecurityException(LogStringsMessages.WSS_1802_WRONG_TOKENINCLUSION_POLICY());
            }
            DirectReference dr = buildDirectReference(bst.getId(), MessageConstants.X509v3_NS);
            buildKeyInfo(dr, binding.getSTRID());
        } else if (referenceType.equals("Identifier")) {
            BinarySecurityToken bst = createBinarySecurityToken(binding, binding.getX509Certificate());
            buildKeyInfoWithKI(binding, MessageConstants.X509SubjectKeyIdentifier_NS);
            try {
                if (binding.getSTRID() != null) {
                    SecurityElement bsToken = elementFactory.createBinarySecurityToken(null, binding.getX509Certificate().getEncoded());
                    SSEData data = new SSEData(bsToken, false, context.getNamespaceContext());
                    context.getSTRTransformCache().put(binding.getSTRID(), data);
                }
            } catch (CertificateEncodingException ce) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1814_ERROR_ENCODING_CERTIFICATE(), ce);
                throw new XWSSecurityException(LogStringsMessages.WSS_1814_ERROR_ENCODING_CERTIFICATE(), ce);
            }
        } else if (referenceType.equals(MessageConstants.THUMB_PRINT_TYPE)) {
            BinarySecurityToken bst = createBinarySecurityToken(binding, binding.getX509Certificate());
            KeyIdentifier ki = buildKeyInfoWithKI(binding, MessageConstants.ThumbPrintIdentifier_NS);
            try {
                if (binding.getSTRID() != null) {
                    SecurityElement bsToken = elementFactory.createBinarySecurityToken(null, binding.getX509Certificate().getEncoded());
                    SSEData data = new SSEData(bsToken, false, context.getNamespaceContext());
                    context.getSTRTransformCache().put(binding.getSTRID(), data);
                }
            } catch (CertificateEncodingException ce) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1814_ERROR_ENCODING_CERTIFICATE(), ce);
                throw new XWSSecurityException(LogStringsMessages.WSS_1814_ERROR_ENCODING_CERTIFICATE(), ce);
            }
        } else if (referenceType.equals(MessageConstants.X509_ISSUER_TYPE)) {
            BinarySecurityToken bst = createBinarySecurityToken(binding, binding.getX509Certificate());
            X509Certificate xCert = binding.getX509Certificate();
            X509IssuerSerial xis = elementFactory.createX509IssuerSerial(xCert.getIssuerDN().getName(), xCert.getSerialNumber());
            X509Data x509Data = elementFactory.createX509DataWithIssuerSerial(xis);
            buildKeyInfo(x509Data, binding.getSTRID());
            try {
                if (binding.getSTRID() != null) {
                    SecurityElement bsToken = elementFactory.createBinarySecurityToken(null, binding.getX509Certificate().getEncoded());
                    SSEData data = new SSEData(bsToken, false, context.getNamespaceContext());
                    context.getSTRTransformCache().put(binding.getSTRID(), data);
                }
            } catch (CertificateEncodingException ce) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1814_ERROR_ENCODING_CERTIFICATE(), ce);
                throw new XWSSecurityException(LogStringsMessages.WSS_1814_ERROR_ENCODING_CERTIFICATE(), ce);
            }
        } else {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1803_UNSUPPORTED_REFERENCE_TYPE(referenceType));
            throw new XWSSecurityException(LogStringsMessages.WSS_1803_UNSUPPORTED_REFERENCE_TYPE(referenceType));
        }
        result.setKeyInfo(keyInfo);
        return result;
    }
}
