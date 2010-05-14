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
package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.EncryptedSupportingTokens;
import com.sun.xml.ws.security.policy.EndorsingEncryptedSupportingTokens;
import com.sun.xml.ws.security.policy.EndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedEncryptedSupportingTokens;
import com.sun.xml.ws.security.policy.SignedEndorsingEncryptedSupportingTokens;
import com.sun.xml.ws.security.policy.SignedEndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.WSSAssertion;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.KeyBindingBase;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.Vector;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.namespace.QName;
import static com.sun.xml.wss.impl.policy.mls.Target.SIGNATURE_CONFIRMATION;

/**
 *
 * @author K.Venugopal@sun.com
 */
public abstract class BindingProcessor {

    protected String protectionOrder = Binding.SIGN_ENCRYPT;
    protected boolean isServer = false;
    protected boolean isIncoming = false;
    protected SignaturePolicy primarySP = null;
    protected EncryptionPolicy primaryEP = null;
    //current secondary encryption policy
    protected EncryptionPolicy sEncPolicy = null;
    protected SignaturePolicy sSigPolicy = null;
    protected XWSSPolicyContainer container = null;
    protected Vector<SignedParts> signedParts = null;
    protected Vector<EncryptedParts> encryptedParts = null;
    protected Vector<SignedElements> signedElements = null;
    protected Vector<EncryptedElements> encryptedElements = null;
    protected PolicyID pid = null;
    protected TokenProcessor tokenProcessor = null;
    protected IntegrityAssertionProcessor iAP = null;
    protected EncryptionAssertionProcessor eAP = null;
    private WSSAssertion wss11 = null;
    private boolean isIssuedTokenAsEncryptedSupportingToken = false;
    protected boolean foundEncryptTargets = false;

    /** Creates a new instance of BindingProcessor */
    public BindingProcessor() {
        this.pid = new PolicyID();
    }

    /*
    WSIT Configuration should not allow protect primary signature
    property to be set if we determine there will be no signature.
     */
    protected void protectPrimarySignature() throws PolicyException {
        if (primarySP == null) {
            return;
        }
        boolean encryptSignConfirm = (isServer && !isIncoming) || (!isServer && isIncoming);
        if (Binding.ENCRYPT_SIGN.equals(protectionOrder)) {
            EncryptionPolicy ep = getSecondaryEncryptionPolicy();
            EncryptionPolicy.FeatureBinding epFB = (EncryptionPolicy.FeatureBinding) ep.getFeatureBinding();
            EncryptionTarget et = eAP.getTargetCreator().newURIEncryptionTarget(primarySP.getUUID());
            SecurityPolicyUtil.setName(et, primarySP);
            epFB.addTargetBinding(et);
            if (foundEncryptTargets && (isWSS11() && requireSC()) && encryptSignConfirm && getBinding().getSignatureProtection()) {
                eAP.process(SIGNATURE_CONFIRMATION, epFB);
            }
        } else {
            EncryptionPolicy.FeatureBinding epFB = (EncryptionPolicy.FeatureBinding) primaryEP.getFeatureBinding();
            EncryptionTarget et = eAP.getTargetCreator().newURIEncryptionTarget(primarySP.getUUID());
            SecurityPolicyUtil.setName(et, primarySP);
            epFB.addTargetBinding(et);
            if (foundEncryptTargets && (isWSS11() && requireSC()) && encryptSignConfirm && getBinding().getSignatureProtection()) {
                eAP.process(SIGNATURE_CONFIRMATION, epFB);
            }
        }
    }

    protected void protectTimestamp(TimestampPolicy tp) {
        if (primarySP != null) {
            SignatureTarget target = iAP.getTargetCreator().newURISignatureTarget(tp.getUUID());
            iAP.getTargetCreator().addTransform(target);
            SecurityPolicyUtil.setName(target, tp);
            SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding) primarySP.getFeatureBinding();
            spFB.addTargetBinding(target);
        }
    }

    //TODO:WS-SX Spec:If we have a secondary signature should it protect the token too ?
    protected void protectToken(WSSPolicy token, SecurityPolicyVersion spVersion) {
        if (primarySP == null) {
            return;
        }
        if ((isServer && isIncoming) || (!isServer && !isIncoming)) {//token protection is from client to service only
            protectToken(token, false, spVersion);
        }
    }

    protected void protectToken(WSSPolicy token, boolean ignoreSTR, SecurityPolicyVersion spVersion) {
        String uuid = (token != null) ? (token.getUUID()) : null;
        String uid = null;
        String includeToken = ((KeyBindingBase) token).getIncludeToken();
        boolean strIgnore = false;
        QName qName = null;

        //dont compute STR Transform when the include token type is always or always to recipient
       if (includeToken.endsWith("Always") || includeToken.endsWith("AlwaysToRecipient") || includeToken.endsWith("Once")) {
            strIgnore = true;
        }

        if (PolicyTypeUtil.UsernameTokenBinding(token)) {
            uid = ((AuthenticationTokenPolicy.UsernameTokenBinding) token).getUUID();
            if (uid == null) {
                uid = pid.generateID();
                ((AuthenticationTokenPolicy.UsernameTokenBinding) token).setSTRID(uid);
            }
            // includeToken = ((AuthenticationTokenPolicy.UsernameTokenBinding) kb).getIncludeToken();
            strIgnore = true;
            qName = new QName(MessageConstants.WSSE_NS, MessageConstants.USERNAME_TOKEN_LNAME);
        } else if (PolicyTypeUtil.x509CertificateBinding(token)) {
            uid = ((AuthenticationTokenPolicy.X509CertificateBinding) token).getSTRID();
            if (uid == null) {
                uid = pid.generateID();
                ((AuthenticationTokenPolicy.X509CertificateBinding) token).setSTRID(uid);
            }
            qName = new QName(MessageConstants.WSSE_NS, MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME);
        } else if (PolicyTypeUtil.samlTokenPolicy(token)) {
            //uid = ((AuthenticationTokenPolicy.SAMLAssertionBinding) token).getSTRID();
            uid = generateSAMLSTRID();
            //if(uid == null){
            // uid = pid.generateID();
            ((AuthenticationTokenPolicy.SAMLAssertionBinding) token).setSTRID(uid);
            //}
            qName = new QName(MessageConstants.WSSE_NS, MessageConstants.SAML_ASSERTION_LNAME);
        } else if (PolicyTypeUtil.issuedTokenKeyBinding(token)) {
            IssuedTokenKeyBinding itb = ((IssuedTokenKeyBinding) token);
            uid = itb.getSTRID();
            if (MessageConstants.WSSE_SAML_v1_1_TOKEN_TYPE.equals(itb.getTokenType()) ||
                    MessageConstants.WSSE_SAML_v2_0_TOKEN_TYPE.equals(itb.getTokenType())) {
                uid = generateSAMLSTRID();
                itb.setSTRID(uid);
                uuid = uid;
            }
            if (uid == null) {
                uid = pid.generateID();
                itb.setSTRID(uid);
            }
        } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(token)) {
            SecureConversationTokenKeyBinding sctBinding = (SecureConversationTokenKeyBinding) token;
           //sctBinding TODO ::Fix this incomplete code
        }

        //when the include token is Never , the sig. reference should refer to the security token reference of KeyInfo
        // also in case of saml token we have to use the id #_SAML, so ,
        if (includeToken.endsWith("Never") || PolicyTypeUtil.samlTokenPolicy(token) || PolicyTypeUtil.issuedTokenKeyBinding(token)) {
            uuid = uid;
        }
        //TODO:: Handle DTK and IssuedToken.
        if (!ignoreSTR) {
            if (uuid != null) {
                SignatureTargetCreator stc = iAP.getTargetCreator();
                SignatureTarget st = stc.newURISignatureTarget(uuid);
                if (strIgnore != true) {
                    stc.addSTRTransform(st);
                }else {
                    stc.addTransform(st);
                }
                SignaturePolicy.FeatureBinding fb = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding) primarySP.getFeatureBinding();
                st.setPolicyName(qName);
                fb.addTargetBinding(st);
            }
        } else {
            SignatureTargetCreator stc = iAP.getTargetCreator();
            SignatureTarget st = null;
            if (PolicyTypeUtil.derivedTokenKeyBinding(token)) {
                WSSPolicy kbd = ((DerivedTokenKeyBinding) token).getOriginalKeyBinding();
                if (PolicyTypeUtil.symmetricKeyBinding(kbd)) {
                    st = stc.newURISignatureTarget(uuid);
                } else {
                    st = stc.newURISignatureTarget(uuid);
                }
            } else {
                st = stc.newURISignatureTarget(uuid);
            }
            if (st != null) {  //when st is null, request simply goes with out signing the token;
                if (strIgnore != true) {
                    stc.addSTRTransform(st);
                } else {
                    stc.addTransform(st);
                }
                SignaturePolicy.FeatureBinding fb = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding) primarySP.getFeatureBinding();
                st.setPolicyName(qName);
                fb.addTargetBinding(st);
            }
        }
    }

    protected abstract EncryptionPolicy getSecondaryEncryptionPolicy() throws PolicyException;

    private String generateSAMLSTRID() {
        StringBuilder sb = new StringBuilder();
        sb.append("SAML");
        sb.append(pid.generateID());
        return sb.toString();
    }

    protected void addPrimaryTargets() throws PolicyException {
        SignaturePolicy.FeatureBinding spFB = null;
        if (primarySP != null) {
            spFB = (SignaturePolicy.FeatureBinding) primarySP.getFeatureBinding();
        }
        EncryptionPolicy.FeatureBinding epFB = null;
        if (primaryEP != null) {
            epFB = (EncryptionPolicy.FeatureBinding) primaryEP.getFeatureBinding();
        }

        if (spFB != null) {
            if (spFB.getCanonicalizationAlgorithm() == null || spFB.getCanonicalizationAlgorithm().equals("")) {
                spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
            }

            //TODO:: Merge SignedElements.

            for (SignedElements se : signedElements) {
                iAP.process(se, spFB);
            }
            /*
            If Empty SignParts is present then remove rest of the SignParts
            as we will be signing all HEADERS and Body. Question to WS-SX:
            Are SignedParts headers targeted to ultimate reciever role.
             */
            for (SignedParts sp : signedParts) {
                if (SecurityPolicyUtil.isSignedPartsEmpty(sp)) {
                    signedParts.removeAllElements();
                    signedParts.add(sp);
                    break;
                }
            }
            for (SignedParts sp : signedParts) {
                iAP.process(sp, spFB);
            }

            if (isWSS11() && requireSC()) {
                iAP.process(SIGNATURE_CONFIRMATION, spFB);
            }
        }

        if (epFB != null) {
            for (EncryptedParts ep : encryptedParts) {
                foundEncryptTargets = true;
                eAP.process(ep, epFB);
            }

            for (EncryptedElements encEl : encryptedElements) {
                foundEncryptTargets = true;
                eAP.process(encEl, epFB);
            }
        }
    }

    protected boolean requireSC() {
        if (wss11 != null && wss11.getRequiredProperties() != null) {
            if (wss11.getRequiredProperties().contains(WSSAssertion.REQUIRE_SIGNATURE_CONFIRMATION)) {
                return true;
            }
        }
        return false;
    }

    protected abstract Binding getBinding();

    public void processSupportingTokens(SupportingTokens st) throws PolicyException {

        SupportingTokensProcessor stp = new SupportingTokensProcessor((SupportingTokens) st,
                tokenProcessor, getBinding(), container, primarySP, getEncryptionPolicy(), pid);
        stp.process();
    }

    public void processSupportingTokens(SignedSupportingTokens st) throws PolicyException {

        SignedSupportingTokensProcessor stp = new SignedSupportingTokensProcessor(st,
                tokenProcessor, getBinding(), container, primarySP, getEncryptionPolicy(), pid);
        stp.process();
    }

    public void processSupportingTokens(EndorsingSupportingTokens est) throws PolicyException {

        EndorsingSupportingTokensProcessor stp = new EndorsingSupportingTokensProcessor(est,
                tokenProcessor, getBinding(), container, primarySP, getEncryptionPolicy(), pid);
        stp.process();
    }

    public void processSupportingTokens(SignedEndorsingSupportingTokens est) throws PolicyException {
        SignedEndorsingSupportingTokensProcessor stp = new SignedEndorsingSupportingTokensProcessor(est,
                tokenProcessor, getBinding(), container, primarySP, getEncryptionPolicy(), pid);
        stp.process();

    }

    public void processSupportingTokens(SignedEncryptedSupportingTokens sest) throws PolicyException {
        SignedEncryptedSupportingTokensProcessor setp = new SignedEncryptedSupportingTokensProcessor(sest,
                tokenProcessor, getBinding(), container, primarySP, getEncryptionPolicy(), pid);
        setp.process();
        isIssuedTokenAsEncryptedSupportingToken(setp.isIssuedTokenAsEncryptedSupportingToken());
    }

    public void processSupportingTokens(EncryptedSupportingTokens est) throws PolicyException {
        EncryptedSupportingTokensProcessor etp = new EncryptedSupportingTokensProcessor(est,
                tokenProcessor, getBinding(), container, primarySP, getEncryptionPolicy(), pid);
        etp.process();
        isIssuedTokenAsEncryptedSupportingToken(etp.isIssuedTokenAsEncryptedSupportingToken());
    }

    public void processSupportingTokens(EndorsingEncryptedSupportingTokens est) throws PolicyException {
        EndorsingEncryptedSupportingTokensProcessor etp = new EndorsingEncryptedSupportingTokensProcessor(est,
                tokenProcessor, getBinding(), container, primarySP, getEncryptionPolicy(), pid);
        etp.process();
        isIssuedTokenAsEncryptedSupportingToken(etp.isIssuedTokenAsEncryptedSupportingToken());
    }

    public void processSupportingTokens(SignedEndorsingEncryptedSupportingTokens est) throws PolicyException {
        SignedEndorsingEncryptedSupportingTokensProcessor etp = new SignedEndorsingEncryptedSupportingTokensProcessor(est,
                tokenProcessor, getBinding(), container, primarySP, getEncryptionPolicy(), pid);
        etp.process();
        isIssuedTokenAsEncryptedSupportingToken(etp.isIssuedTokenAsEncryptedSupportingToken());
    }

    protected SignaturePolicy getSignaturePolicy() {
        if (Binding.SIGN_ENCRYPT.equals(getBinding().getProtectionOrder())) {
            return primarySP;
        } else {
            return sSigPolicy;
        }
    }

    private EncryptionPolicy getEncryptionPolicy() throws PolicyException {
        if (Binding.SIGN_ENCRYPT.equals(getBinding().getProtectionOrder())) {
            return primaryEP;
        } else {
            return getSecondaryEncryptionPolicy();
        }
    }

    protected abstract void close();

    public boolean isWSS11() {
        if (wss11 != null) {
            return true;
        }
        return false;
    }

    public void setWSS11(WSSAssertion wss11) {
        this.wss11 = wss11;
    }

    public boolean isIssuedTokenAsEncryptedSupportingToken() {
        return this.isIssuedTokenAsEncryptedSupportingToken;
    }

    private void isIssuedTokenAsEncryptedSupportingToken(boolean value) {
        this.isIssuedTokenAsEncryptedSupportingToken = value;
    }
}
