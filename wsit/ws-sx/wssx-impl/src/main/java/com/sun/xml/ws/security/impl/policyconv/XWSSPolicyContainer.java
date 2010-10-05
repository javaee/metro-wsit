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

import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.ws.security.policy.MessageLayout;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.Target;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author Abhijit.Das@Sun.COM
 */
public class XWSSPolicyContainer {
    private boolean isServer;
    private boolean isIncoming;

    private boolean encPoliciesContain(QName qName, List<SecurityPolicy> encPolicies) {
        if (qName.equals(Target.BODY_QNAME)) {
            return false;
        }
        for (SecurityPolicy sp : encPolicies) {
            if (PolicyTypeUtil.encryptionPolicy(sp)) {
                EncryptionPolicy.FeatureBinding fb = (EncryptionPolicy.FeatureBinding) ((EncryptionPolicy) sp).getFeatureBinding();
                ArrayList targets = fb.getTargetBindings();
                for (int i = 0; i < targets.size(); i++) {
                    Target t = (Target) targets.get(i);
                    if (t.getType() == Target.TARGET_TYPE_VALUE_QNAME) {
                        if (qName.equals(t.getQName())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void fixEncryptedTargetsInSignature(MessagePolicy msgPolicy,boolean isWSS11) {
        boolean encryptBeforeSign = false;        
        boolean seenEncryptPolicy = false;
        boolean seenSignPolicy = false;
        List<SecurityPolicy> encPolicies = new ArrayList<SecurityPolicy>();

        for (Object policy : msgPolicy.getPrimaryPolicies()) {

            if (policy instanceof SecurityPolicy) {
                SecurityPolicy secPolicy = (SecurityPolicy) policy;
                if (PolicyTypeUtil.signaturePolicy(secPolicy)) {
                    seenSignPolicy = true;
                    if (!seenEncryptPolicy && isIncoming) {
                        encryptBeforeSign = true;
                    }
                } else if (PolicyTypeUtil.encryptionPolicy(secPolicy)) {
                    seenEncryptPolicy  = true;
                    if (!seenSignPolicy && !isIncoming) {
                        encryptBeforeSign = true;
                    }
                    encPolicies.add(secPolicy);
                }
            }
        }
        if (encryptBeforeSign) {
            for (Object policy : msgPolicy.getPrimaryPolicies()) {
                boolean containsEncryptedHeader  =false;
                if (policy instanceof SecurityPolicy) {
                    SecurityPolicy secPolicy = (SecurityPolicy) policy;
                    if (PolicyTypeUtil.signaturePolicy(secPolicy)) {
                        SignaturePolicy.FeatureBinding sfb = (SignaturePolicy.FeatureBinding) ((SignaturePolicy) secPolicy).getFeatureBinding();
                        ArrayList targets = sfb.getTargetBindings();
                        for (int i = 0; i < targets.size(); i++) {
                            Target t = (Target) targets.get(i);
                            if (t.getType() == Target.TARGET_TYPE_VALUE_QNAME) {
                                if (encPoliciesContain(t.getQName(), encPolicies)) {                                    
                                    if(isWSS11){
                                        if (!containsEncryptedHeader) {
                                            QName encHeaderQName = new QName("http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd", "EncryptedHeader");
                                            t.setQName(encHeaderQName);
                                            containsEncryptedHeader = true;
                                        } else {
                                            targets.remove(i);
                                        }
                                    }else {
                                        if (!containsEncryptedHeader) {
                                            QName encDataQName = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "EncryptedData");
                                             t.setQName(encDataQName);
                                            containsEncryptedHeader = true;
                                        } else {
                                            targets.remove(i);
                                        }
                                    }                                    
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private enum Section {
        ClientIncomingPolicy,
        ClientOutgoingPolicy,
        ServerIncomingPolicy,
        ServerOutgoingPolicy
    };
    
    private Section section;
    private List<SecurityPolicy> policyList;
    private List<SecurityPolicy> effectivePolicyList;
    private MessageLayout mode;
    private int foundTimestamp = -1;
    
    private boolean modified = false;
    
    /** Creates a new instance of PolicyConverter */
    public XWSSPolicyContainer(MessageLayout mode, boolean isServer, boolean isIncoming) {
        this.mode = mode;
        this.isServer = isServer;
        this.isIncoming = isIncoming;
        setMessageMode(isServer, isIncoming);
        effectivePolicyList = new ArrayList<SecurityPolicy>();
    }
    public XWSSPolicyContainer(boolean isServer,boolean isIncoming) {
        setMessageMode(isServer, isIncoming);
        this.isServer = isServer;
        this.isIncoming = isIncoming;
        effectivePolicyList = new ArrayList<SecurityPolicy>();
    }
    public void setMessageMode(boolean isServer, boolean isIncoming) {
        if ( isServer && isIncoming) {
            section = Section.ServerIncomingPolicy;
        } else if ( isServer && !isIncoming) {
            section = Section.ServerOutgoingPolicy;
        } else if ( !isServer && isIncoming) {
            section = Section.ClientIncomingPolicy;
        } else if ( !isServer && !isIncoming) {
            section = Section.ClientOutgoingPolicy;
        }
    }
    public void setPolicyContainerMode(MessageLayout mode){
        this.mode = mode;
    }
    /**
     * Insert into policyList
     *
     *
     */
    public void insert(SecurityPolicy secPolicy ) {
        if(secPolicy == null){
            return;
        }
        if ( policyList == null ) {
            policyList = new ArrayList<SecurityPolicy>();
        }
        if ( isSupportingToken(secPolicy)) {
            switch (section) {
            case ServerOutgoingPolicy:
            case ClientIncomingPolicy:
                return;
            }
        }
        modified = true;
        policyList.add(secPolicy);
    }
    public MessagePolicy getMessagePolicy(boolean isWSS11)throws PolicyGenerationException {
        if ( modified ) {
            convert();
            modified = false;
        }
        MessagePolicy msgPolicy = new MessagePolicy();
        
        msgPolicy.appendAll(effectivePolicyList);
        removeEmptyPrimaryPolicies(msgPolicy);        
        fixEncryptedTargetsInSignature(msgPolicy,isWSS11);        
        return msgPolicy;
        
    }
    private void removeEmptyPrimaryPolicies(MessagePolicy msgPolicy) {
        for ( Object policy : msgPolicy.getPrimaryPolicies() ) {
            if ( policy instanceof SecurityPolicy) {
                SecurityPolicy secPolicy = (SecurityPolicy)policy;
                if ( PolicyTypeUtil.signaturePolicy(secPolicy)) {
                    if (((SignaturePolicy.FeatureBinding)((SignaturePolicy)secPolicy).getFeatureBinding()).getTargetBindings().size() == 0 ) {
                        msgPolicy.remove(secPolicy);
                    }
                } else if ( PolicyTypeUtil.encryptionPolicy(secPolicy)) {
                    if (((EncryptionPolicy.FeatureBinding)((EncryptionPolicy)secPolicy).getFeatureBinding()).getTargetBindings().size() == 0 ) {
                        msgPolicy.remove(secPolicy);
                    }
                }
            }
        }
    }
    
    /**
     * Insert SecurityPolicy after supporting tokens.
     *
     */
    //private void appendAfterToken(SecurityPolicy xwssPolicy , Section section) {
    private void appendAfterToken(SecurityPolicy xwssPolicy) {
        int pos = -1;
        for ( SecurityPolicy secPolicy : effectivePolicyList) {
            if ( isSupportingToken(secPolicy) || isTimestamp(secPolicy)) {
                continue;
            } else {
                pos = effectivePolicyList.indexOf(secPolicy);
                break;
            }
        }
        if ( pos != -1 ) {
            effectivePolicyList.add(pos, xwssPolicy);
        } else {
            effectivePolicyList.add(xwssPolicy);
        }
    }
    /**
     * Insert SecurityPolicy before supporting Tokens.
     *
     */
    private void prependBeforeToken(SecurityPolicy xwssPolicy ) {
        int pos = -1;
        for ( SecurityPolicy secPolicy : effectivePolicyList) {
            if ( !isSupportingToken(secPolicy)) {
                continue;
            } else {
                pos = effectivePolicyList.indexOf(secPolicy);
            }
        }
        if ( pos != -1 ) {
            effectivePolicyList.add(pos, xwssPolicy);
        } else {
            effectivePolicyList.add(xwssPolicy);
        }
    }
    /**
     *
     * Add Security policy.
     */
    private void append(SecurityPolicy xwssPolicy ) {
        effectivePolicyList.add(xwssPolicy);
    }
    /**
     * Add SecurityPolicy.
     *
     */
    private void prepend(SecurityPolicy xwssPolicy) {
        effectivePolicyList.add(0, xwssPolicy);
    }
    /**
     *
     * @return - true if xwssPolicy is SupportingToken policy else false.
     */
    private boolean isSupportingToken( SecurityPolicy xwssPolicy ) {
        if ( xwssPolicy == null ) {
            return false;
        }
        //UsernameToken, SAML Token Policy, X509Certificate, issued token
        if ( PolicyTypeUtil.authenticationTokenPolicy(xwssPolicy)) {
            MLSPolicy binding = ((AuthenticationTokenPolicy)xwssPolicy).getFeatureBinding();
            if ( PolicyTypeUtil.usernameTokenPolicy(binding) ||
                    PolicyTypeUtil.samlTokenPolicy(binding) ||
                    PolicyTypeUtil.x509CertificateBinding(binding) || 
                    PolicyTypeUtil.issuedTokenKeyBinding(binding)) {
                return true;
            }
        }
        return false;
    }
    /**
     *
     * @return - true if xwssPolicy is TimestampPolicy else false.
     */
    private boolean isTimestamp( SecurityPolicy xwssPolicy ) {
        if ( xwssPolicy != null && PolicyTypeUtil.timestampPolicy(xwssPolicy) ) {
            return true;
        }
        return false;
    }
    /**
     *
     * Lax mode
     */
    private void convertLax() {
        for ( SecurityPolicy xwssPolicy : policyList ) {
            if ( isTimestamp(xwssPolicy )) {
                foundTimestamp = policyList.indexOf(xwssPolicy);
                prepend(xwssPolicy);
                continue;
            }
            
            if ( !isSupportingToken(xwssPolicy)) {
                switch(section) {
                case ClientIncomingPolicy:
                    prepend(xwssPolicy);
                    break;
                case ClientOutgoingPolicy:
                    append(xwssPolicy);
                    break;
                case ServerIncomingPolicy:
                    appendAfterToken(xwssPolicy);
                    break;
                case ServerOutgoingPolicy:
                    append(xwssPolicy);
                    break;
                }
            } else if ( isSupportingToken(xwssPolicy) || isTimestamp(xwssPolicy)) {
                prepend(xwssPolicy);
                
             
            }
        }
    }
    /**
     *
     * Strict mode.
     */
    private void convertStrict() {
        for ( SecurityPolicy xwssPolicy : policyList ) {
            if ( isSupportingToken(xwssPolicy)) {
                prepend(xwssPolicy);
       
            } else if ( isTimestamp(xwssPolicy)) {
                prepend(xwssPolicy);
            } else {
                switch (section ) {
                case ClientIncomingPolicy:
                    appendAfterToken(xwssPolicy);
                    break;
                case ClientOutgoingPolicy:
                    append(xwssPolicy);
                    break;
                case ServerIncomingPolicy:
                    appendAfterToken(xwssPolicy);
                    break;
                case ServerOutgoingPolicy:
                    append(xwssPolicy);
                    break;
                }
            }
        }
    }
    /**
     * LaxTsFirst mode.
     *
     */
    private void convertLaxTsFirst() {
        convertLax();
        if ( foundTimestamp != -1 ) {
            switch (section ) {
            case ClientOutgoingPolicy:
                effectivePolicyList.add(0, effectivePolicyList.remove(foundTimestamp));
                break;
            case ServerOutgoingPolicy:
                effectivePolicyList.add(0, effectivePolicyList.remove(foundTimestamp));
                break;
            }
        }
        
    }
    /**
     * LaxTsLast mode.
     *
     */
    private void convertLaxTsLast() {
        convertLax();
        if ( foundTimestamp != -1 ) {
            switch (section) {
            case ClientOutgoingPolicy:
                effectivePolicyList.add(effectivePolicyList.size() -1, effectivePolicyList.remove(foundTimestamp));
                break;
            case ServerOutgoingPolicy:
                effectivePolicyList.add(effectivePolicyList.size() -1, effectivePolicyList.remove(foundTimestamp));
                break;
            }
        }
    }
    /**
     *
     * Convert WS-Security Policy to XWSS policy.
     */
    public void convert() {
        if ( MessageLayout.Lax == mode ) {
            convertLax();
        } else if ( MessageLayout.Strict == mode ) {
            convertStrict();
        } else if ( MessageLayout.LaxTsFirst == mode ) {
            convertLaxTsFirst();
        } else if ( MessageLayout.LaxTsLast == mode ) {
            convertLaxTsLast();
        }
    }
}

