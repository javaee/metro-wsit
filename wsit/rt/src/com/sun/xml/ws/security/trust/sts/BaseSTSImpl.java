/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.security.trust.sts;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.impl.policy.Constants;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustContract;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustFactory;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.impl.STSConfiguration;
import com.sun.xml.ws.security.trust.impl.TrustSPMetadata;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import java.util.Iterator;
import javax.xml.namespace.QName;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.transform.dom.DOMSource;

import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceException;
import javax.xml.transform.Source;
import javax.xml.ws.handler.MessageContext;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import org.w3c.dom.*;

/**
 * The Base class of an STS implementation. This could be used to implement 
 * the actual STS. The sub class could override the methods of this class to 
 * customize the implementation.
 */
public abstract class BaseSTSImpl implements Provider<Source> {
    /**
     * The default value of the timeout for the tokens issued by this STS
     */
    public static final int DEFAULT_TIMEOUT = 36000;
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
    /**
     * The String TokenType.
     */
    public static final String TOKEN_TYPE = "TokenType";
    /**
     * The String ServiceProviders.
     */
    public static final String SERVICE_PROVIDERS = "ServiceProviders";
    /**
     * The String endPoint.
     */
    public static final String END_POINT = "endPoint";
    
    
  /** Implementation of the invoke method of the Provider interface
   *
   *  @param  rstElement The message comprising of RequestSecurityToken.
   *  @return The response message comprising of RequestSecurityTokenResponse
   *  @throws WebServiceException if there is an error processing request.
   *          The cause of the WebServiceException may be set to a subclass
   *          of ProtocolException to control the protocol level
   *          representation of the exception.
  **/
    public Source invoke(Source rstElement){
        
        Source rstrEle = null;
        try{
            // Get RequestSecurityToken
            WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance();
            RequestSecurityToken rst = eleFac.createRSTFrom(rstElement);
            String tokenType = null;
            
            String appliesTo = null;
            AppliesTo ap = rst.getAppliesTo();
            if(ap != null){
                appliesTo = WSTrustUtil.getAppliesToURI(ap);
            }
            
            if (appliesTo == null){
                appliesTo = DEFAULT_APPLIESTO;
            }
            
            if(rst.getTokenType()!=null){
                tokenType = rst.getTokenType().toString();
            }
            STSConfiguration config = getConfiguration();
            if(rst.getRequestType().toString().equals(WSTrustConstants.ISSUE_REQUEST)){
                rstrEle = issue(config, appliesTo, eleFac, rst);                
            }else if(rst.getRequestType().toString().equals(WSTrustConstants.CANCEL_REQUEST)){
                rstrEle = cancel(config, appliesTo, eleFac, rst);
            }else if(rst.getRequestType().toString().equals(WSTrustConstants.RENEW_REQUEST)){
                rstrEle = renew(config, appliesTo, eleFac, rst);
            }else if(rst.getRequestType().toString().equals(WSTrustConstants.RENEW_REQUEST)){
                rstrEle = renew(config, appliesTo, eleFac, rst);
            }else if(rst.getRequestType().toString().equals(WSTrustConstants.KEY_EXCHANGE_REQUEST)){
                rstrEle = keyExchange(config, appliesTo, eleFac, rst);
            }else if(rst.getRequestType().toString().equals(WSTrustConstants.VALIDATE_REQUEST)){
                rstrEle = validate(config, appliesTo, eleFac, rst);
            }            
        } catch (Exception ex){
            ex.printStackTrace();
            throw new WebServiceException(ex);
        }
        
        return rstrEle;
    }

    STSConfiguration getConfiguration() {
        STSConfiguration config = new STSConfiguration();
        
        MessageContext msgCtx = getMessageContext();
        Iterator it = (Iterator)msgCtx.get(
                Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS);
        CallbackHandler handler = (CallbackHandler)msgCtx.get(WSTrustConstants.STS_CALL_BACK_HANDLER);
        String impl = DEFAULT_IMPL;
        String alias = null;
        boolean encKey=true;
        boolean encToken = false;
        int timeout = DEFAULT_TIMEOUT;
        String endpointUri = null;
        String tokenType = null;
        while(it.hasNext()) {
            PolicyAssertion as = (PolicyAssertion)it.next();
            if (!STS_CONFIGURATION.equals(as.getName().getLocalPart())) {
                continue;
            }
            encKey = Boolean.parseBoolean(as.getAttributeValue(new QName("",ENCRYPT_KEY)));
            encToken = Boolean.parseBoolean(as.getAttributeValue(new QName("",ENCRYPT_TOKEN)));
            Iterator<PolicyAssertion> stsConfig =
                    as.getNestedAssertionsIterator();
            while(stsConfig.hasNext()){
                PolicyAssertion serviceSTSPolicy = stsConfig.next();
                if(LIFETIME.equals(serviceSTSPolicy.getName().getLocalPart())){
                    timeout = Integer.parseInt(serviceSTSPolicy.getValue());
                    continue;
                }
                if(CONTRACT.equals(serviceSTSPolicy.getName().getLocalPart())){
                    impl = serviceSTSPolicy.getValue();
                    continue;
                }
                if(SERVICE_PROVIDERS.equals(serviceSTSPolicy.getName().getLocalPart())){
                    Iterator<PolicyAssertion> serviceProviders =
                    serviceSTSPolicy.getNestedAssertionsIterator();
                    while(serviceProviders.hasNext()){
                        PolicyAssertion serviceProvider = serviceProviders.next();
                        endpointUri = serviceProvider.getAttributeValue(new QName("",END_POINT));
                        TrustSPMetadata data = new TrustSPMetadata(endpointUri, null);
                        Iterator<PolicyAssertion> spConfig = serviceProvider.getNestedAssertionsIterator();
                        while(spConfig.hasNext()){
                            PolicyAssertion pa = spConfig.next();
                            if(ALIAS.equals(pa.getName().getLocalPart())){
                                alias = pa.getValue();
                            }else if (TOKEN_TYPE.equals(pa.getName().getLocalPart())){
                                tokenType = pa.getValue();
                            }
                        }
                        data.setType(impl);
                        data.setEncryptIssuedToken(encToken);
                        data.setEncryptIssuedKey(encKey);
                        data.setCertAlias(alias);
                        data.setIssuedTokenTimeout(timeout);
                        //data.setCallbackHandlerName("common.STSCallbackHandler");
                        data.setCallbackHandler(handler);
                        config.addTrustSPMetadata(data, endpointUri);
                    }
                }
            }
        }
      
        return config;
    }

    /** The actual STS class should override this method to return the 
     *  correct MessageContext
     * 
     * @return The MessageContext
     */
    protected abstract MessageContext getMessageContext();

    private Source issue(final STSConfiguration config,final String appliesTo, 
            final WSTrustElementFactory eleFac, final RequestSecurityToken rst) 
            throws WSTrustException {
        
        // Create the RequestSecurityTokenResponse message
        WSTrustContract contract = WSTrustFactory.newWSTrustContract(config, 
                appliesTo);
        IssuedTokenContext context = new IssuedTokenContextImpl();
        
        RequestSecurityTokenResponse rstr = contract.issue(rst, context, null);
        
        Token samlToken = rstr.getRequestedSecurityToken().getToken();
        rstr.getRequestedSecurityToken().setToken(null);
        Element samlEle = (Element)samlToken.getTokenValue();
        Element rstrEle = eleFac.toElement(rstr);
        Document doc = rstrEle.getOwnerDocument();
        samlEle = (Element)doc.importNode(samlEle, true);
        NodeList list = rstrEle.getElementsByTagNameNS("*", "RequestedSecurityToken");
        Element rdstEle = (Element)list.item(0);
        rdstEle.appendChild(samlEle);
        
        return new DOMSource(rstrEle);
    }

    private Source cancel(final STSConfiguration config,
            final String appliesTo, final WSTrustElementFactory eleFac,
            final RequestSecurityToken rst) {
        return null;
    }
    
    private Source renew(final STSConfiguration config,final String appliesTo, 
            final WSTrustElementFactory eleFac, final RequestSecurityToken rst) 
            throws WSTrustException {
        Source rstrEle;

        // Create the RequestSecurityTokenResponse message
        WSTrustContract contract = WSTrustFactory.newWSTrustContract(config, 
                appliesTo);
        IssuedTokenContext context = new IssuedTokenContextImpl();
        
        RequestSecurityTokenResponse rstr = contract.renew(rst, context);

        rstrEle = eleFac.toSource(rstr);
        return rstrEle;
    }
    
    private Source keyExchange(final STSConfiguration config,final String appliesTo, 
            final WSTrustElementFactory eleFac, final RequestSecurityToken rst) 
            throws WSTrustException {
        return null;
    }    

    private Source validate(final STSConfiguration config,final String appliesTo, 
            final WSTrustElementFactory eleFac, final RequestSecurityToken rst) 
            throws WSTrustException {
        Source rstrEle;

        // Create the RequestSecurityTokenResponse message
        WSTrustContract contract = WSTrustFactory.newWSTrustContract(config, 
                appliesTo);
        IssuedTokenContext context = new IssuedTokenContextImpl();
        
        RequestSecurityTokenResponse rstr = contract.validate(rst, context);

        rstrEle = eleFac.toSource(rstr);
        return rstrEle;
    }        
}
