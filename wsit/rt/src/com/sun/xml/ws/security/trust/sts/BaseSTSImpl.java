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

package com.sun.xml.ws.security.trust.sts;

import com.sun.xml.ws.api.security.trust.BaseSTS;
import com.sun.xml.ws.api.security.trust.WSTrustContract;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.config.STSConfiguration;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.impl.policy.Constants;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustFactory;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.impl.DefaultSTSConfiguration;
import com.sun.xml.ws.security.trust.impl.DefaultTrustSPMetadata;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.XWSSecurityException;

import java.util.Iterator;
import javax.xml.namespace.QName;

import javax.xml.transform.TransformerException;

import javax.xml.ws.WebServiceException;
import javax.xml.transform.Source;
import javax.xml.ws.handler.MessageContext;
//import javax.xml.ws.BindingType;
//import javax.xml.ws.RespectBinding;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;

/**
 * The Base class of an STS implementation. This could be used to implement
 * the actual STS. The sub class could override the methods of this class to
 * customize the implementation.
 */
//@RespectBinding
//@BindingType
public abstract class BaseSTSImpl implements BaseSTS {
    /**
     * The default value of the timeout for the tokens issued by this STS
     */
    public static final int DEFAULT_TIMEOUT = 36000;
    
    public static final String DEFAULT_ISSUER = "SampleSunSTS";
    /**
     * The xml element tag for STS Configuration
     */
    public static final String STS_CONFIGURATION = "STSConfiguration";
    /**
     * The default implementation class for the Trust contract. This
     * class issues SAML tokens.
     */
    public static final String DEFAULT_IMPL = 
            "com.sun.xml.ws.security.trust.impl.IssueSamlTokenContractImpl";
    /**
     * The default value for AppliesTo if appliesTo is not specified.
     */
    public static final String DEFAULT_APPLIESTO = "default";
    /**
     * The String AppliesTo
     */
    public static final String APPLIES_TO = "AppliesTo";
    /**
     * The String LifeTime that is used to specify lifetime of the tokens 
     * issued by this STS.
     */
    public static final String LIFETIME = "LifeTime";
    /**
     * The String CertAlias that is used in the configuration.
     * This identifies the alias of the Service that this STS serves.
     */
    public static final String ALIAS = "CertAlias";
    /**
     * The String encrypt-issued-key
     */
    public static final String ENCRYPT_KEY = "encryptIssuedKey";
    /**
     * The String encrypt-issued-token
     */
    public static final String ENCRYPT_TOKEN = "encryptIssuedToken";
    /**
     * The String Contract.
     */
    public static final String CONTRACT = "Contract";
    
    public static final String ISSUER = "Issuer";
    /**
     * The String TokenType.
     */
    public static final String TOKEN_TYPE = "TokenType";
    
    /**
     * The String KeyType.
     */
    public static final String KEY_TYPE = "KeyType";
    
    /**
     * The String ServiceProviders.
     */
    public static final String SERVICE_PROVIDERS = "ServiceProviders";
    /**
     * The String endPoint.
     */
    public static final String END_POINT = "endPoint";
    
    private static final QName Q_EK = new QName("",ENCRYPT_KEY);
    
    private static final QName Q_ET = new QName("",ENCRYPT_TOKEN);
    
    private static final QName Q_EP = new QName("",END_POINT);

    protected WSTrustVersion wstVer = WSTrustVersion.WS_TRUST_10;


  /** Implementation of the invoke method of the Provider interface
   *
   *  @param  rstElement The message comprising of RequestSecurityToken.
   *  @return The response message comprising of RequestSecurityTokenResponse
   *  @throws WebServiceException if there is an error processing request.
   *          The cause of the WebServiceException may be set to a subclass
   *          of ProtocolException to control the protocol level
   *          representation of the exception.
  **/
    public Source invoke(final Source rstElement){
        final STSConfiguration config = getConfiguration();
        Source rstrEle = null;
        try{
            // Get RequestSecurityToken
            final WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance(wstVer);
            final RequestSecurityToken rst = eleFac.createRSTFrom(rstElement);         
            
            String appliesTo = null;
            final AppliesTo applTo = rst.getAppliesTo();
            if(applTo != null){
                appliesTo = WSTrustUtil.getAppliesToURI(applTo);
            }
            
            if (appliesTo == null){
                appliesTo = DEFAULT_APPLIESTO;
            }
            
            if(rst.getRequestType().toString().equals(wstVer.getIssueRequestTypeURI())){
                rstrEle = issue(config, appliesTo, eleFac, rst);                
            }else if(rst.getRequestType().toString().equals(wstVer.getCancelRequestTypeURI())){
                rstrEle = cancel(config, appliesTo, eleFac, rst);
            }else if(rst.getRequestType().toString().equals(wstVer.getRenewRequestTypeURI())){
                rstrEle = renew(config, appliesTo, eleFac, rst);
            }else if(rst.getRequestType().toString().equals(wstVer.getValidateRequestTypeURI())){
                rstrEle = validate(config, appliesTo, eleFac, rst);
            }            
        } catch (Exception ex){
            //ex.printStackTrace();
            throw new WebServiceException(ex);
        }
        
        return rstrEle;
    }
    
     /** The actual STS class should override this method to return the 
     *  correct MessageContext
     * 
     * @return The MessageContext
     */
    protected abstract MessageContext getMessageContext();

    STSConfiguration getConfiguration() {
        final MessageContext msgCtx = getMessageContext();
        //final CallbackHandler handler = (CallbackHandler)msgCtx.get(WSTrustConstants.STS_CALL_BACK_HANDLER);
        final SecurityEnvironment secEnv = (SecurityEnvironment)msgCtx.get(WSTrustConstants.SECURITY_ENVIRONMENT);
        WSTrustVersion wstVersion = (WSTrustVersion)msgCtx.get(WSTrustConstants.WST_VERSION);
        String authnCtxClass = (String)msgCtx.get(WSTrustConstants.AUTHN_CONTEXT_CLASS);
        if (wstVersion != null){
            wstVer = wstVersion;
        }
        //Get Runtime STSConfiguration
        STSConfiguration rtConfig = WSTrustFactory.getRuntimeSTSConfiguration();
        if (rtConfig != null){
            if (rtConfig.getCallbackHandler() == null){
                rtConfig.getOtherOptions().put(WSTrustConstants.SECURITY_ENVIRONMENT, secEnv);
            }
            if (wstVersion == null){
                wstVersion = (WSTrustVersion)rtConfig.getOtherOptions().get(WSTrustConstants.WST_VERSION);
                if (wstVersion != null){
                    wstVer = wstVersion;
                }
            }
           
            rtConfig.getOtherOptions().put(WSTrustConstants.WST_VERSION, wstVer);
            
            return rtConfig;
        }
        
        // Get default STSConfiguration
        DefaultSTSConfiguration config = new DefaultSTSConfiguration();
        config.getOtherOptions().put(WSTrustConstants.SECURITY_ENVIRONMENT, secEnv);
        //config.setCallbackHandler(handler);
        final Iterator iterator = (Iterator)msgCtx.get(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS);
        if (iterator == null){
            throw new WebServiceException("STS configuration information is not available");
        }
        
        while(iterator.hasNext()) {
            final PolicyAssertion assertion = (PolicyAssertion)iterator.next();
            if (!STS_CONFIGURATION.equals(assertion.getName().getLocalPart())) {
                continue;
            }
            config.setEncryptIssuedToken(Boolean.parseBoolean(assertion.getAttributeValue(Q_ET)));
            config.setEncryptIssuedKey(Boolean.parseBoolean(assertion.getAttributeValue(Q_EK)));
            final Iterator<PolicyAssertion> stsConfig =
                    assertion.getNestedAssertionsIterator();
            while(stsConfig.hasNext()){
                final PolicyAssertion serviceSTSPolicy = stsConfig.next();
                if(LIFETIME.equals(serviceSTSPolicy.getName().getLocalPart())){
                    config.setIssuedTokenTimeout(Integer.parseInt(serviceSTSPolicy.getValue()));
                    
                    continue;
                }
                if(CONTRACT.equals(serviceSTSPolicy.getName().getLocalPart())){
                    config.setType(serviceSTSPolicy.getValue());
                    continue;
                }
                if(ISSUER.equals(serviceSTSPolicy.getName().getLocalPart())){
                    config.setIssuer(serviceSTSPolicy.getValue());
                    continue;
                }
                
                if(SERVICE_PROVIDERS.equals(serviceSTSPolicy.getName().getLocalPart())){
                    final Iterator<PolicyAssertion> serviceProviders =
                    serviceSTSPolicy.getNestedAssertionsIterator();
                    String endpointUri = null;
                    while(serviceProviders.hasNext()){
                        final PolicyAssertion serviceProvider = serviceProviders.next();
                        endpointUri = serviceProvider.getAttributeValue(Q_EP);
                        if (endpointUri == null){
                             endpointUri = serviceProvider.getAttributeValue(new QName("", END_POINT.toLowerCase()));
                        }
                        final DefaultTrustSPMetadata data = new DefaultTrustSPMetadata(endpointUri);
                        final Iterator<PolicyAssertion> spConfig = serviceProvider.getNestedAssertionsIterator();
                        while(spConfig.hasNext()){
                            final PolicyAssertion policy = spConfig.next();
                            if(ALIAS.equals(policy.getName().getLocalPart())){
                                data.setCertAlias(policy.getValue());
                            }else if (TOKEN_TYPE.equals(policy.getName().getLocalPart())){
                                data.setTokenType(policy.getValue());
                            }else if (KEY_TYPE.equals(policy.getName().getLocalPart())){
                                data.setKeyType(policy.getValue());
                            }
                        }
                        
                        config.addTrustSPMetadata(data, endpointUri);
                    }
                }
            }
        }
        config.getOtherOptions().put(WSTrustConstants.WST_VERSION, wstVer);
        
        if(authnCtxClass != null){
            config.getOtherOptions().put(WSTrustConstants.AUTHN_CONTEXT_CLASS, authnCtxClass);
        }
        config.getOtherOptions().putAll(msgCtx);
      
        return config;
    }

    private Source issue(final STSConfiguration config, final String appliesTo, 
            final WSTrustElementFactory eleFac, final BaseSTSRequest rst) 
            throws WSTrustException, TransformerException {
        
        // Create the RequestSecurityTokenResponse message
        final WSTrustContract<BaseSTSRequest, BaseSTSResponse> contract = WSTrustFactory.newWSTrustContract(config, 
                appliesTo);
        final IssuedTokenContext context = new IssuedTokenContextImpl();
        try {
            context.setRequestorSubject(SubjectAccessor.getRequesterSubject(getMessageContext()));            
        } catch (XWSSecurityException ex) {
            throw new WSTrustException("error getting subject",ex);
        }

        final BaseSTSResponse response = contract.issue(rst, context);
        
    /*    Token samlToken = rstr.getRequestedSecurityToken().getToken();
        rstr.getRequestedSecurityToken().setAny(null);
        Element samlEle = (Element)samlToken.getTokenValue();
        Element rstrEle = eleFac.toElement(rstr);
        Document doc = rstrEle.getOwnerDocument();
        samlEle = (Element)doc.importNode(samlEle, true);
        NodeList list = rstrEle.getElementsByTagNameNS("*", "RequestedSecurityToken");
        Element rdstEle = (Element)list.item(0);
        rdstEle.appendChild(samlEle); 
        
        return new DOMSource(eleFac.toElement(rstr)); */
        return eleFac.toSource(response);
    }

    private Source cancel(final STSConfiguration config,
            final String appliesTo, final WSTrustElementFactory eleFac,
            final BaseSTSRequest rst) {
        return null;
    }
    
    private Source renew(final STSConfiguration config,final String appliesTo, 
            final WSTrustElementFactory eleFac, final RequestSecurityToken rst) 
            throws WSTrustException {
        Source rstrEle;

        // Create the RequestSecurityTokenResponse message
        final WSTrustContract<BaseSTSRequest, BaseSTSResponse> contract = WSTrustFactory.newWSTrustContract(config, 
                appliesTo);
        final IssuedTokenContext context = new IssuedTokenContextImpl();
        
        final BaseSTSResponse rstr = contract.renew(rst, context);

        rstrEle = eleFac.toSource(rstr);
        return rstrEle;
    }
    
    private Source validate(final STSConfiguration config,final String appliesTo, 
            final WSTrustElementFactory eleFac, final BaseSTSRequest rst) 
            throws WSTrustException {
        Source rstrEle;

        // Create the RequestSecurityTokenResponse message
        final WSTrustContract<BaseSTSRequest, BaseSTSResponse> contract = WSTrustFactory.newWSTrustContract(config, 
                appliesTo);
        final IssuedTokenContext context = new IssuedTokenContextImpl();
        
        final BaseSTSResponse rstr = contract.validate(rst, context);

        rstrEle = eleFac.toSource(rstr);
        return rstrEle;
    }        
}
