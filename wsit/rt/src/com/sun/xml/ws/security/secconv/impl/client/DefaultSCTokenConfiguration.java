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
package com.sun.xml.ws.security.secconv.impl.client;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.security.secconv.client.SCTokenConfiguration;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.impl.policy.Trust10;
import com.sun.xml.ws.security.impl.policy.Trust13;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.Constants;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.jaxws.impl.SecurityClientTube;
import com.sun.xml.wss.provider.wsit.WSITClientAuthContext;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author Shyam Rao
 */
public class DefaultSCTokenConfiguration extends SCTokenConfiguration{

    private static final String SC_CLIENT_CONFIGURATION = "SCClientConfiguration";
    private static final String RENEW_EXPIRED_SCT = "renewExpiredSCT";
    private static final String REQUIRE_CANCEL_SCT = "requireCancelSCT";
    private static final String LIFETIME = "LifeTime";
    private static final String CONFIG_NAMESPACE = "";
    private Trust10 trust10 = null;
    private Trust13 trust13 = null;
    private SymmetricBinding symBinding = null;
    private int skl = 0;
    private boolean reqClientEntropy = true;
    //private boolean isExpired = false;
    private boolean checkTokenExpiry = true;
    private boolean clientOutboundMessage = true;
    private WSDLPort wsdlPort = null;
    private WSBinding wsBinding = null;
    private Tube clientSecurityTube = null;
    private Tube nextTube = null;
    private Packet packet = null;
    private WSITClientAuthContext wsitClientAuthContext = null;
    private AddressingVersion addVer = null;
    private Token scToken = null;
    private String tokenId = null;
    private MessagePolicy messagePolicy = null;
    private boolean addRenewPolicy = true;

    public DefaultSCTokenConfiguration(String protocol, SecureConversationToken scToken, final WSDLPort wsdlPort,
            final WSBinding binding, final Tube securityTube, final Packet packet, final AddressingVersion addVer, PolicyAssertion localToken, Tube nextTube) {
        this.protocol = protocol;
        this.scToken = scToken;
        this.wsdlPort = wsdlPort;
        this.wsBinding = binding;
        this.clientSecurityTube = securityTube;
        this.packet = packet;
        this.addVer = addVer;
        this.tokenId = null;
        this.nextTube = nextTube;
        parseAssertions(scToken, localToken);
    }

    public DefaultSCTokenConfiguration(String protocol, SecureConversationToken scToken, final WSDLPort wsdlPort,
            final WSBinding binding, final Pipe securityPipe, final Packet packet, final AddressingVersion addVer, PolicyAssertion localToken){
        this.protocol = protocol;
        this.scToken = scToken;
        this.wsdlPort = wsdlPort;
        this.wsBinding = binding;
        this.clientSecurityTube = null;
        this.packet = packet;
        this.addVer = addVer;
        this.tokenId = ((Token)scToken).getTokenId();
        parseAssertions(scToken, localToken);
    }

    public DefaultSCTokenConfiguration(String protocol, SecureConversationToken scToken, final WSDLPort wsdlPort,
            final WSBinding binding, final WSITClientAuthContext wsitClientAuthContext, final Packet packet, final AddressingVersion addVer, PolicyAssertion localToken){
        this.protocol = protocol;
        this.scToken = scToken;
        this.wsdlPort = wsdlPort;
        this.wsBinding = binding;
        this.wsitClientAuthContext = wsitClientAuthContext;
        this.packet = packet;
        this.addVer = addVer;
        this.tokenId = ((Token)scToken).getTokenId();
        parseAssertions(scToken, localToken);
    }

    public DefaultSCTokenConfiguration(String protocol, final MessagePolicy messagePolicy){
        super(protocol);
        this.messagePolicy = messagePolicy;
    }

    public DefaultSCTokenConfiguration(String protocol, final MessagePolicy messagePolicy, boolean addRenewPolicy){
        this(protocol, messagePolicy);
        this.addRenewPolicy = addRenewPolicy;
    }

    public DefaultSCTokenConfiguration(String protocol, String tokenId, boolean checkTokenExpiry){
        super(protocol);
        this.tokenId = tokenId;
        this.checkTokenExpiry = checkTokenExpiry;
    }

    public DefaultSCTokenConfiguration(String protocol, String tokenId, boolean checkTokenExpiry, boolean clientOutboundMessage){
        this(protocol, tokenId, checkTokenExpiry);
        this.clientOutboundMessage = clientOutboundMessage;
    }
    
    public DefaultSCTokenConfiguration(DefaultSCTokenConfiguration that, String tokenId) { 
        this.protocol = that.protocol;
        this.scToken = that.scToken;
        this.wsdlPort = that.wsdlPort;
        this.wsBinding = that.wsBinding;
        this.clientSecurityTube = that.clientSecurityTube;
        this.wsitClientAuthContext = that.wsitClientAuthContext;
        this.packet = that.packet;
        this.addVer = that.addVer;        
        this.nextTube = that.nextTube;      
        this.tokenId = tokenId;
        this.checkTokenExpiry = that.checkTokenExpiry;
        this.clientOutboundMessage = that.clientOutboundMessage;
        this.messagePolicy = that.messagePolicy;
        this.addRenewPolicy = that.addRenewPolicy;
        this.reqClientEntropy = that.reqClientEntropy;
        this.symBinding = that.symBinding;
        this.skl = that.skl;
        this.scToken = that.scToken;
        this.wsdlPort = that.wsdlPort;
        this.wsBinding = that.wsBinding;
        this.renewExpiredSCT = that.renewExpiredSCT;
        this.requireCancelSCT = that.requireCancelSCT;
        this.scTokenTimeout = that.scTokenTimeout;
        
    }

    private void parseAssertions(SecureConversationToken scToken, PolicyAssertion localToken){

        final AssertionSet assertions = scToken.getBootstrapPolicy().getAssertionSet();
        for(PolicyAssertion policyAssertion : assertions){
            SecurityPolicyVersion spVersion  =
                    PolicyUtil.getSecurityPolicyVersion(policyAssertion.getName().getNamespaceURI());
            if(PolicyUtil.isTrust13(policyAssertion, spVersion)){
                this.trust13 = (Trust13)policyAssertion;
            }else if(PolicyUtil.isTrust10(policyAssertion, spVersion)){
                this.trust10 = (Trust10)policyAssertion;
            }else if(PolicyUtil.isSymmetricBinding(policyAssertion, spVersion)){
                this.symBinding = (SymmetricBinding)policyAssertion;
            }
        }
        if(symBinding!=null){
            final AlgorithmSuite algoSuite = symBinding.getAlgorithmSuite();
            skl = algoSuite.getMinSKLAlgorithm();
        }

        if(trust10 != null){
            final Set trustReqdProps = trust10.getRequiredProperties();
            reqClientEntropy = trustReqdProps.contains(Constants.REQUIRE_CLIENT_ENTROPY);
        }
        if(trust13 != null){
            final Set trustReqdProps = trust13.getRequiredProperties();
            reqClientEntropy = trustReqdProps.contains(Constants.REQUIRE_CLIENT_ENTROPY);
        }

        if(localToken != null){
            if (SC_CLIENT_CONFIGURATION.equals(localToken.getName().getLocalPart())) {
                final Map<QName,String> attrs = localToken.getAttributes();
                this.renewExpiredSCT = Boolean.parseBoolean(attrs.get(new QName(CONFIG_NAMESPACE, RENEW_EXPIRED_SCT)));
                this.requireCancelSCT = Boolean.parseBoolean(attrs.get(new QName(CONFIG_NAMESPACE, REQUIRE_CANCEL_SCT)));
            }
            final Iterator<PolicyAssertion> sctConfig = localToken.getNestedAssertionsIterator();
            while(sctConfig.hasNext()){
                final PolicyAssertion sctConfigPolicy = sctConfig.next();
                if(LIFETIME.equals(sctConfigPolicy.getName().getLocalPart())){
                    this.scTokenTimeout = Integer.parseInt(sctConfigPolicy.getValue());
                    break;
                }
            }
        }
    }

    public String getTokenId(){
        return tokenId;
    }

    public boolean checkTokenExpiry(){
        return this.checkTokenExpiry;
    }

    public boolean isClientOutboundMessage(){
        return this.clientOutboundMessage;
    }

    public MessagePolicy getMessagePolicy(){
        return this.messagePolicy;
    }

    public boolean addRenewPolicy(){
        return this.addRenewPolicy;
    }

    public boolean getReqClientEntropy(){
        return this.reqClientEntropy;
    }

    public boolean isSymmetricBinding(){
        if(symBinding == null){
            return false;
        }
        return true;
    }

    public int getKeySize(){
        return this.skl;
    }

    public Token getSCToken(){
        return this.scToken;
    }

    public WSDLPort getWSDLPort(){
        return this.wsdlPort;
    }

    public WSBinding getWSBinding(){
        return this.wsBinding;
    }

    public Tube getClientTube(){
        return this.clientSecurityTube;
    }

    public WSITClientAuthContext getWSITClientAuthContext(){
        return this.wsitClientAuthContext;
    }

    public Tube getNextTube(){
        return this.nextTube;
    }

    public Packet getPacket(){
        return this.packet;
    }

    public AddressingVersion getAddressingVersion(){
        return this.addVer;
    }
}

