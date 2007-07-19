/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.security.impl.policy;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.policy.AlgorithmSuiteValue;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import javax.xml.namespace.QName;
/**
 *
 * @author K.Venugopal@sun.com Abhijit.Das@Sun.COM
 */
public class PolicyUtil {
    
    /** Creates a new instance of PolicyUtil */
    public PolicyUtil() {
    }
    
    public static boolean isSecurityPolicyNS(PolicyAssertion pa) {
        if ( Constants.SECURITY_POLICY_NS.equals(pa.getName().getNamespaceURI()) ) {
            return true;
        }
        return false;
    }
    
    public static boolean isSunPolicyNS(PolicyAssertion pa) {
        if ( Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS.equals(pa.getName().getNamespaceURI()) ||
                Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS.equals(pa.getName().getNamespaceURI())) {
            return true;
        }
        return false;
    }
    
    public static boolean isAddressingNS(PolicyAssertion pa) {
        if ( AddressingVersion.MEMBER.getNsUri().equals(pa.getName().getNamespaceURI()) ) {
            return true;
        }
        if ( AddressingVersion.W3C.getNsUri().equals(pa.getName().getNamespaceURI()) ) {
            return true;
        }
        return false;
    }
    
    public static boolean isTrustNS(PolicyAssertion pa) {
        if ( Constants.TRUST_NS.equals(pa.getName().getNamespaceURI()) ) {
            return true;
        }
        return false;
    }
    
    public static boolean isUtilityNS(PolicyAssertion pa) {
        if ( Constants.UTILITY_NS.equals(pa.getName().getNamespaceURI()) ) {
            return true;
        }
        return false;
    }
    
    public static boolean isXpathNS(PolicyAssertion pa) {
        if ( Constants.XPATH_NS.equals(pa.getName().getNamespaceURI()) ) {
            return true;
        }
        return false;
    }
    
    public static boolean isAlgorithmAssertion(PolicyAssertion pa){
        if ( isSecurityPolicyNS(pa) ) {
            if(pa.getName().getLocalPart().equals(AlgorithmSuite)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isToken(PolicyAssertion pa){
        if ( !isSecurityPolicyNS(pa) ) {
            return false;
        }
        
        if(pa.getName().getLocalPart().equals(EncryptionToken) ) {
            return true;
        }else if(pa.getName().getLocalPart().equals(SignatureToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(InitiatorToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(HttpsToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(IssuedToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(KerberosToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(ProtectionToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(RecipientToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(SupportingTokens)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(SC10SecurityContextToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(SamlToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(UsernameToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(X509Token)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(SecureConversationToken)) {
            return true;
        }else if(pa.getName().getLocalPart().equals(TransportToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isBootstrapPolicy(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion) ) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(BootstrapPolicy)) {
            return true;
        }
        return false;
    }
    
    public static boolean isTarget(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion) ) {
            return false;
        }
        
        String name = assertion.getName().getLocalPart();
        if(name.equals(EncryptedParts) ||
                name.equals(SignedParts) ||
                name.equals(SignedElements) ||
                name.equals(EncryptedElements)) {
            return true;
        }
        return false;
    }
    
    public static boolean isXPath(PolicyAssertion assertion ) {
        if ( !isSecurityPolicyNS(assertion) ) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(XPath) ) {
            return true;
        }
        return false;
    }
    
    public static boolean isXPathFilter20(PolicyAssertion assertion ) {
        if ( !isXpathNS(assertion) ) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(XPathFilter20) ) {
            return true;
        }
        return false;
    }
    
    public static boolean isRequiredKey(PolicyAssertion assertion) {
        return false;
    }
    
    public static boolean isTokenType(PolicyAssertion assertion){
        
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(WssX509V1Token10)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(WssX509V3Token10)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(WssX509Pkcs7Token10)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(WssX509PkiPathV1Token10)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(WssX509V1Token11)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(WssX509V3Token11)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(WssX509Pkcs7Token11)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(WssX509PkiPathV1Token11)) {
            return true;
        }
        return false;
    }
    
    public static boolean isTokenReferenceType(PolicyAssertion assertion) {
        
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(RequireKeyIdentifierReference)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(RequireThumbprintReference)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(RequireEmbeddedTokenReference)) {
            return true;
        } else if ( assertion.getName().getLocalPart().equals(RequireIssuerSerialReference)) {
            return true;
        }
        return false;
    }
    
    public static boolean isUsernameTokenType(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(WssUsernameToken10) ||
                assertion.getName().getLocalPart().equals(WssUsernameToken11)) {
            return true;
        }
        return false;
    }
    
    public static boolean isHttpsToken(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(Constants.HttpsToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSecurityContextToken(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        
        if(token.getName().getLocalPart().equals(Constants.SecurityContextToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSecurityContextTokenType(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        String localPart = token.getName().getLocalPart();
        if(localPart.equals(SC10SecurityContextToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isKerberosToken(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        
        if(token.getName().getLocalPart().equals(Constants.KerberosToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isKerberosTokenType(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        String localPart = token.getName().getLocalPart();
        if(localPart.equals(WssKerberosV5ApReqToken11)) {
            return true;
        }else if(localPart.equals(WssGssKerberosV5ApReqToken11)){
            return true;
        }
        return false;
    }
    
    public static boolean isRelToken(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        
        if(token.getName().getLocalPart().equals(Constants.RelToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isRelTokenType(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        String localPart = token.getName().getLocalPart();
        if(localPart.equals(WssRelV10Token10)) {
            return true;
        }else if(localPart.equals(WssRelV10Token11)){
            return true;
        }else if(localPart.equals(WssRelV20Token10)){
            return true;
        }else if(localPart.equals(WssRelV20Token11)){
            return true;
        }
        return false;
    }
    
    public static boolean isIncludeTimestamp(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(IncludeTimestamp)) {
            return true;
        }
        return false;
    }
    
    public static boolean isEncryptBeforeSign(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(EncryptBeforeSigning)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSignBeforeEncrypt(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(SignBeforeEncrypting)) {
            return true;
        }
        return false;
    }
    
    public static boolean isContentOnlyAssertion(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(OnlySignEntireHeadersAndBody)) {
            return true;
        }
        return false;
    }
    
    public static boolean isMessageLayout(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(Layout)) {
            return true;
        }
        return false;
    }
    
    public static boolean isEncryptParts(PolicyAssertion assertion ){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(EncryptedParts)) {
            return true;
        }
        return false;
    }
    
    public static boolean isEncryptedElements(PolicyAssertion assertion ){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(EncryptedElements)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSignedParts(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(SignedParts)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSignedElements(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(SignedElements)) {
            return true;
        }
        return false;
    }
    
    
    public static boolean isSignedSupportingToken(PolicyAssertion policyAssertion) {
        if ( !isSecurityPolicyNS(policyAssertion )) {
            return false;
        }
        
        if(policyAssertion.getName().getLocalPart().equals(SignedSupportingTokens)) {
            return true;
        }
        return false;
    }
    
    public static boolean isEndorsedSupportingToken(PolicyAssertion policyAssertion) {
        if ( !isSecurityPolicyNS(policyAssertion )) {
            return false;
        }
        
        if(policyAssertion.getName().getLocalPart().equals(EndorsingSupportingTokens)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSignedEndorsingSupportingToken(PolicyAssertion policyAssertion) {
        if ( !isSecurityPolicyNS(policyAssertion )) {
            return false;
        }
        
        if(policyAssertion.getName().getLocalPart().equals(SignedEndorsingSupportingTokens)) {
            return true;
        }
        return false;
    }
    
    
    public static boolean isBinding(PolicyAssertion policyAssertion) {
        
        if ( !isSecurityPolicyNS(policyAssertion )) {
            return false;
        }
        
        String name = policyAssertion.getName().getLocalPart();
        if(name.equals(SymmetricBinding) ||
                name.equals(AsymmetricBinding) ||
                name.equals(TransportBinding)) {
            
            return true;
        }
        return false;
    }
    
    public static boolean isUsernameToken(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        
        if(token.getName().getLocalPart().equals(UsernameToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSamlToken(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        
        if(token.getName().getLocalPart().equals(SamlToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSamlTokenType(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        String localPart = token.getName().getLocalPart();
        if(localPart.equals(WssSamlV10Token10)) {
            return true;
        }else if(localPart.equals(WssSamlV10Token11)){
            return true;
        }else if(localPart.equals(WssSamlV11Token10)){
            return true;
        }else if(localPart.equals(WssSamlV20Token11)){
            return true;
        }else if(localPart.equals(WssSamlV11Token11)){
            return true;
        }
        return false;
    }
    
    public static boolean isIssuedToken(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        
        if(token.getName().getLocalPart().equals(IssuedToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSecureConversationToken(PolicyAssertion token) {
        if ( !isSecurityPolicyNS(token )) {
            return false;
        }
        
        if(token.getName().getLocalPart().equals(SecureConversationToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isX509Token(PolicyAssertion policyAssertion) {
        if ( !isSecurityPolicyNS(policyAssertion )) {
            return false;
        }
        
        if(policyAssertion.getName().getLocalPart().equals(X509Token)) {
            return true;
        }
        return false;
    }
    
    public static boolean isAsymmetricBinding(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(AsymmetricBinding)) {
            return true;
        }
        return false;
    }
    
    public static boolean isAsymmetricBinding(QName assertion){
        if ( assertion.getLocalPart().equals(Constants.AsymmetricBinding) &&
                assertion.getNamespaceURI().equals(Constants.SECURITY_POLICY_NS)) {
            return true;
        }
        return false;
    }
    
    public static boolean isTransportBinding(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(TransportBinding)) {
            return true;
        }
        return false;
    }
    
    public static boolean isTransportBinding(QName assertion){
        if ( assertion.getLocalPart().equals(Constants.TransportBinding) &&
                assertion.getNamespaceURI().equals(Constants.SECURITY_POLICY_NS)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSymmetricBinding(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(SymmetricBinding)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSymmetricBinding(QName assertion){
        if ( assertion.getLocalPart().equals(Constants.SymmetricBinding) &&
                assertion.getNamespaceURI().equals(Constants.SECURITY_POLICY_NS)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSupportingTokens(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(isSignedSupportingToken(assertion) || isEndorsedSupportingToken(assertion)||
                isSignedEndorsingSupportingToken(assertion) || isSupportingToken(assertion)){
            return true;
        }
        return false;
    }
    
    
    public static boolean isSupportingToken(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(SupportingTokens)){
            return true;
        }
        return false;
    }
    
    
    public static boolean isSupportClientChallenge(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(MustSupportClientChallenge)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSupportServerChallenge(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(MustSupportServerChallenge)) {
            return true;
        }
        return false;
    }
    
    public static boolean isWSS10PolicyContent(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(MustSupportRefKeyIdentifier)) {
            return true;
        }else if( assertion.getName().getLocalPart().equals(MustSupportRefIssuerSerial)) {
            return true;
        }else if(assertion.getName().getLocalPart().equals(RequireExternalUriReference)) {
            return true;
        }else if(assertion.getName().getLocalPart().equals(RequireEmbeddedTokenReference)) {
            return true;
        }
        return false;
    }
    
    public static boolean isWSS11PolicyContent(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(MustSupportRefKeyIdentifier)) {
            return true;
        }else if( assertion.getName().getLocalPart().equals(MustSupportRefIssuerSerial)) {
            return true;
        }else if(assertion.getName().getLocalPart().equals(MustSupportRefThumbprint)) {
            return true;
        }else if(assertion.getName().getLocalPart().equals(MustSupportRefEncryptedKey)) {
            return true;
        }else if(assertion.getName().getLocalPart().equals(RequireSignatureConfirmation)) {
            return true;
        }else if(assertion.getName().getLocalPart().equals(RequireExternalUriReference)) {
            return true;
        }else if(assertion.getName().getLocalPart().equals(RequireEmbeddedTokenReference)) {
            return true;
        }
        return false;
    }
    
    
    public static boolean isRequireClientEntropy(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(RequireClientEntropy)) {
            return true;
        }
        return false;
    }
    
    public static boolean isRequireServerEntropy(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(RequireServerEntropy)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSupportIssuedTokens(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion )) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(MustSupportIssuedTokens)) {
            return true;
        }
        return false;
    }
    
    public static boolean isIssuer(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(Issuer)) {
            return true;
        }
        return false;
    }
    
    
    public static boolean isWSS10(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        if(assertion.getName().getLocalPart().equals(Wss10)) {
            return true;
        }
        return false;
    }
    
    public static boolean isWSS11(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(Wss11)) {
            return true;
        }
        return false;
    }
    
    public static boolean isTrust10(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(Trust10)) {
            return true;
        }
        return false;
    }
    
    public static boolean isBody(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(Constants.Body)){
            return true;
        }
        return false;
    }
    
    public static boolean isRequireDerivedKeys(PolicyAssertion assertion ) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( RequireDerivedKeys.toString().equals(assertion.getName().getLocalPart().toString())) {
            return true;
        }
        return false;
    }
    
    public static AlgorithmSuiteValue isValidAlgorithmSuiteValue(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return null;
        }
        
        if ( assertion.getName().getLocalPart().equals(Basic256) ) {
            return AlgorithmSuiteValue.Basic256;
        } else if ( assertion.getName().getLocalPart().equals(Basic192)) {
            return AlgorithmSuiteValue.Basic192;
        } else if ( assertion.getName().getLocalPart().equals(Basic128)) {
            return AlgorithmSuiteValue.Basic128;
        } else if ( assertion.getName().getLocalPart().equals(TripleDes )) {
            return AlgorithmSuiteValue.TripleDes;
        } else if ( assertion.getName().getLocalPart().equals(Basic256Rsa15)) {
            return AlgorithmSuiteValue.Basic256Rsa15;
        } else if ( assertion.getName().getLocalPart().equals(Basic192Rsa15)) {
            return AlgorithmSuiteValue.Basic192Rsa15;
        } else if ( assertion.getName().getLocalPart().equals(Basic128Rsa15)) {
            return AlgorithmSuiteValue.Basic128Rsa15;
        } else if ( assertion.getName().getLocalPart().equals(TripleDesRsa15)) {
            return AlgorithmSuiteValue.TripleDesRsa15;
        } else if ( assertion.getName().getLocalPart().equals(Basic256Sha256)) {
            return AlgorithmSuiteValue.Basic256Sha256;
        } else if ( assertion.getName().getLocalPart().equals(Basic192Sha256)) {
            return AlgorithmSuiteValue.Basic192Sha256;
        } else if ( assertion.getName().getLocalPart().equals(Basic128Sha256)) {
            return AlgorithmSuiteValue.Basic128Sha256;
        } else if ( assertion.getName().getLocalPart().equals(TripleDesSha256)) {
            return AlgorithmSuiteValue.TripleDesSha256;
        } else if ( assertion.getName().getLocalPart().equals(Basic256Sha256Rsa15)) {
            return AlgorithmSuiteValue.Basic256Sha256Rsa15;
        } else if ( assertion.getName().getLocalPart().equals(Basic192Sha256Rsa15)) {
            return AlgorithmSuiteValue.Basic192Sha256Rsa15;
        } else if ( assertion.getName().getLocalPart().equals(Basic128Sha256Rsa15)) {
            return AlgorithmSuiteValue.Basic128Sha256Rsa15;
        } else if ( assertion.getName().getLocalPart().equals(TripleDesSha256Rsa15)) {
            return AlgorithmSuiteValue.TripleDesSha256Rsa15;
        }
        return null;
    }
    
    public static boolean isInclusiveC14N(PolicyAssertion assertion ) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(InclusiveC14N)) {
            return true;
        }
        return false;
        
    }
    
    public static boolean isInclusiveC14NWithComments(PolicyAssertion assertion ) {
        if(!isSunPolicyNS(assertion)){
            return false;
        }
        if ( assertion.getName().getLocalPart().equals(InclusiveC14NWithComments)) {
            return true;
        }
        return false;
    }
    
    public static boolean isInclusiveC14NWithCommentsForTransforms(PolicyAssertion assertion ) {
        
        if(!isSunPolicyNS(assertion)){
            return false;
        }
        if ( assertion.getName().getLocalPart().equals(InclusiveC14NWithComments)) {
            if("true".equals(assertion.getAttributeValue(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS, "forTransforms"))))
                return true;
        }
        return false;
    }
    
    public static boolean isInclusiveC14NWithCommentsForCm(PolicyAssertion assertion ) {
        
        if(!isSunPolicyNS(assertion)){
            return false;
        }
        if ( assertion.getName().getLocalPart().equals(InclusiveC14NWithComments)) {
            if("true".equals(assertion.getAttributeValue(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS, "forCm"))))
                return true;
        }
        return false;
    } 
    
    public static boolean isExclusiveC14NWithComments(PolicyAssertion assertion ) {
        if(!isSunPolicyNS(assertion)){
            return false;
        }
        if ( assertion.getName().getLocalPart().equals(ExclusiveC14NWithComments)) {
            return true;
        }
        return false;
    }
    
    public static boolean isExclusiveC14NWithCommentsForTransforms(PolicyAssertion assertion ) {
        if(!isSunPolicyNS(assertion)){
            return false;
        }
        if ( assertion.getName().getLocalPart().equals(ExclusiveC14NWithComments)) {
            if("true".equals(assertion.getAttributeValue(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS, "forTransforms"))))
                return true;
        }
        return false;
    }
    
    public static boolean isExclusiveC14NWithCommentsForCm(PolicyAssertion assertion ) {
        if(!isSunPolicyNS(assertion)){
            return false;
        }
        if ( assertion.getName().getLocalPart().equals(ExclusiveC14NWithComments)) {
            if("true".equals(assertion.getAttributeValue(new QName(SUN_WSS_SECURITY_SERVER_POLICY_NS, "forCm"))))
                return true;
        }
        return false;
    }
    
    public static boolean isSTRTransform10(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(STRTransform10)) {
            return true;
        }
        return false;
    }
    
    public static boolean isInitiatorToken(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(InitiatorToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isRecipientToken(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(RecipientToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isProtectTokens(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(ProtectTokens)) {
            return true;
        }
        return false;
    }
    
    public static boolean isEncryptSignature(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(EncryptSignature)) {
            return true;
        }
        return false;
    }
    
    public static boolean isCreated(PolicyAssertion assertion) {
        if ( !isUtilityNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(Created)) {
            return true;
        }
        return false;
    }
    
    public static boolean isExpires(PolicyAssertion assertion) {
        if (!isUtilityNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(Expires)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSignatureToken(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(SignatureToken)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isEncryptionToken(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(EncryptionToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isProtectionToken(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(ProtectionToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isAddress(PolicyAssertion assertion ) {
        if ( !isAddressingNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(Address)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isRequestSecurityTokenTemplate(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(RequestSecurityTokenTemplate)) {
            return true;
        }
        return false;
    }
    
    public static boolean isRequireExternalUriReference(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(RequireExternalUriReference)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isRequireExternalReference(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(RequireExternalReference)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isRequireInternalReference(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(RequireInternalReference)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isEndpointReference(PolicyAssertion assertion) {
        if ( !isAddressingNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(EndpointReference)) {
            return true;
        }
        return false;
    }
    
    public static boolean isLax(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(Lax)) {
            return true;
        }
        return false;
    }
    
    public static boolean isLaxTsFirst(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(LaxTsFirst)) {
            return true;
        }
        return false;
    }
    
    public static boolean isLaxTsLast(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(LaxTsLast)) {
            return true;
        }
        return false;
    }
    
    public static boolean isStrict(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(Strict)) {
            return true;
        }
        return false;
    }
    
    public static boolean isKeyType(PolicyAssertion assertion) {
        if ( !isTrustNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(KeyType)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isKeySize(PolicyAssertion assertion) {
        if ( !isTrustNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(KeySize)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isUseKey(PolicyAssertion assertion) {
        if ( !isTrustNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(UseKey)) {
            return true;
        }
        
        return false;
    }
    
    public static boolean isEncryption(PolicyAssertion assertion) {
        if ( !isTrustNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(Encryption)) {
            return true;
        }
        return false;
    }
    
    public static boolean isProofEncryption(PolicyAssertion assertion) {
        if ( !isTrustNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(ProofEncryption)) {
            return true;
        }
        return false;
    }
    
    public static boolean isLifeTime(PolicyAssertion assertion) {
        if ( !isTrustNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(Lifetime)) {
            return true;
        }
        return false;
    }
    
    public static boolean isHeader(PolicyAssertion assertion) {
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        if ( assertion.getName().getLocalPart().equals(HEADER)) {
            return true;
        }
        return false;
    }
    
    public static boolean isRequireKeyIR(PolicyAssertion assertion) {
        if (!isSecurityPolicyNS(assertion)) {
            return false;
        }
        if ( assertion.getName().getLocalPart().equals(RequireKeyIdentifierReference)) {
            return true;
        }
        return false;
    }
    
    public static boolean isSignWith(PolicyAssertion assertion) {
        if(!isTrustNS(assertion)){
            return false;
        }
        if(SignWith.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    public static boolean isRequestType(PolicyAssertion assertion) {
        if(!isTrustNS(assertion)){
            return false;
        }
        if(RequestType.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    public static boolean isSignatureAlgorithm(PolicyAssertion assertion) {
        if(!isTrustNS(assertion)){
            return false;
        }
        if(SignatureAlgorithm.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    public static boolean isComputedKeyAlgorithm(PolicyAssertion assertion) {
        if(!isTrustNS(assertion)){
            return false;
        }
        if(ComputedKeyAlgorithm.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    public static boolean isCanonicalizationAlgorithm(PolicyAssertion assertion) {
        if(!isTrustNS(assertion)){
            return false;
        }
        if(CanonicalizationAlgorithm.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    public static boolean isEncryptionAlgorithm(PolicyAssertion assertion) {
        if(!isTrustNS(assertion)){
            return false;
        }
        if(EncryptionAlgorithm.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    public static boolean isAuthenticationType(PolicyAssertion assertion) {
        if(!isTrustNS(assertion)){
            return false;
        }
        if(AuthenticationType.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    public static boolean isSC10SecurityContextToken(PolicyAssertion assertion){
        if ( !isSecurityPolicyNS(assertion)) {
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(Constants.SC10SecurityContextToken)) {
            return true;
        }
        return false;
    }
    
    public static boolean isConfigPolicyAssertion(PolicyAssertion assertion){
        String uri = assertion.getName().getNamespaceURI();
        if(SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS.equals(uri) || SUN_TRUST_CLIENT_SECURITY_POLICY_NS.equals(uri) ||
                SUN_SECURE_SERVER_CONVERSATION_POLICY_NS.equals(uri) || SUN_TRUST_SERVER_SECURITY_POLICY_NS.equals(uri) ||
                SUN_WSS_SECURITY_CLIENT_POLICY_NS.equals(uri) ||SUN_WSS_SECURITY_SERVER_POLICY_NS.equals(uri) ){
            return true;
        }
        return false;
    }
    
    public static boolean isTrustTokenType(PolicyAssertion assertion) {
        if(!isTrustNS(assertion)){
            return false;
        }
        if(TokenType.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    public static boolean isPortType(PolicyAssertion assertion) {
        if ( !isAddressingNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(PortType)) {
            return true;
        }
        return false;
    }
    
    public  static boolean isReferenceParameters(PolicyAssertion assertion) {
        if ( !isAddressingNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(ReferenceParameters)) {
            return true;
        }
        return false;
    }
    
    public static boolean isReferenceProperties(PolicyAssertion assertion) {
        if ( !isAddressingNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(ReferenceProperties)) {
            return true;
        }
        return false;
    }
    
    public static boolean isServiceName(PolicyAssertion assertion) {
        if ( !isAddressingNS(assertion)) {
            return false;
        }
        
        if ( assertion.getName().getLocalPart().equals(ServiceName)) {
            return true;
        }
        return false;
    }
    
    public static boolean isRequiredElements(PolicyAssertion assertion){
        if(isSecurityPolicyNS(assertion)){
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(RequiredElements)){
            return true;
        }
        return false;
    }
    
    public static boolean isClaimsElement(PolicyAssertion assertion){
        if(!isTrustNS(assertion)){
            return false;
        }
        if(Claims.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    public static boolean isEntropyElement(PolicyAssertion assertion){
        if(!isTrustNS(assertion)){
            return false;
        }
        if(Entropy.equals(assertion.getName().getLocalPart())){
            return true;
        }
        return false;
    }
    
    
    public static boolean hasPassword(PolicyAssertion assertion){
        if(!isSecurityPolicyNS(assertion)){
            return false;
        }
        
        if(assertion.getName().getLocalPart().equals(NoPassword)){
            return true;
        }
        return false;
    }
}
