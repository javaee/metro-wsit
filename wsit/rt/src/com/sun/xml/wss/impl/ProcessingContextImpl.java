/*
 * $Id: ProcessingContextImpl.java,v 1.1 2006-05-03 22:57:37 arungupta Exp $
 */

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

package com.sun.xml.wss.impl;

import com.sun.xml.ws.security.IssuedTokenContext;
import java.util.Map;
import java.util.Random;
import java.util.Hashtable;
import java.util.HashMap;

import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;

import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.WSSAssertion;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;

import org.w3c.dom.Element;

public class ProcessingContextImpl extends ProcessingContext {
    
    protected WSSAssertion wssAssertion = null;

    protected Hashtable issuedTokenContextMap = null;

    // Security runtime would populate received client creds into it
    // when it is an incoming Trust or SC message
    private static final String TRUST_CLIENT_CREDENTIALS = "TrustClientCredentialHolder";
    private static final String ISSUED_SAML_TOKEN = "IssuedSAMLToken";
    private static final String SAMLID_VS_KEY_CACHE = "SAMLID_VS_KEY_CACHE";

    // Hack required for DecryptionProcessor
    protected AlgorithmSuite algoSuite = null;

    protected IssuedTokenContext secureConversationContext = null;
    protected IssuedTokenContext trustContext = null;

    protected MessagePolicy inferredSecurityPolicy = new MessagePolicy();

    private OperationResolver operationResolver = null;
    private boolean isTrustMsg = false;
    
    /**
     *Default constructor
     */
    public ProcessingContextImpl() {}

    /**
     *constructor
     */
    public ProcessingContextImpl(Map invocationProps) {
       properties = invocationProps; 
    }
    
    /**
     * Constructor
     * @param context the static policy context for this request
     * @param securityPolicy the SecurityPolicy to be applied for this request
     * @param message the SOAPMessage
     * @throws XWSSecurityException if there was an error in creating the ProcessingContext
     */
    public ProcessingContextImpl(StaticPolicyContext context,
            SecurityPolicy securityPolicy,
            SOAPMessage message)
            throws XWSSecurityException {
        super(context, securityPolicy, message);    
    }
    
     /**
     * copy operator
     * @param ctx1 the ProcessingContext to which to copy
     * @param ctx2 the ProcessingContext from which to copy
     * @throws XWSSecurityException if there was an error during the copy operation
     */
    public  void copy(ProcessingContext ctxx1, ProcessingContext ctxx2)
    throws XWSSecurityException {
        if (ctxx2 instanceof ProcessingContextImpl) {
            ProcessingContextImpl ctx1 = (ProcessingContextImpl)ctxx1;
            ProcessingContextImpl ctx2 = (ProcessingContextImpl)ctxx2;
            super.copy(ctx1, ctx2);
            ctx1.setIssuedTokenContextMap(ctx2.getIssuedTokenContextMap()); 
            ctx1.setAlgorithmSuite(ctx2.getAlgorithmSuite()); 
            ctx1.setSecureConversationContext(ctx2.getSecureConversationContext());
            ctx1.setWSSAssertion(ctx2.getWSSAssertion());
            ctx1.inferredSecurityPolicy = ctx2.getInferredSecurityPolicy();
            ctx1.setOperationResolver(ctx2.getOperationResolver());
            ctx1.isTrustMessage(ctx2.isTrustMessage());
       }else {
           super.copy(ctxx1, ctxx2);
       }
    }
    
    public void setIssuedTokenContextMap(Hashtable issuedTokenContextMap ) {
        this.issuedTokenContextMap = issuedTokenContextMap;
    }
    
    public Hashtable getIssuedTokenContextMap() {
        return issuedTokenContextMap;
    }
    
    /* (non-Javadoc)
     * @return SecurableSoapMessage
     */
    public SecurableSoapMessage getSecurableSoapMessage() {
        return secureMessage;
    }

    public IssuedTokenContext getIssuedTokenContext(String policyID) {
        if (issuedTokenContextMap == null) {
            //throw new RuntimeException("Internal Error: IssuedTokenContext(s) not initialized in ProcessingContext");
            return null;
        }
        return (IssuedTokenContext)issuedTokenContextMap.get(policyID);
    }

    public void setIssuedTokenContext(IssuedTokenContext issuedTokenContext, String policyID) {
        if ( issuedTokenContextMap == null ) {
            //TODO: This is temporary for testing
            // Once integrated we must throw an RT exception from here
            issuedTokenContextMap = new Hashtable();
        } 
        issuedTokenContextMap.put(policyID, issuedTokenContext);
    }

    public void setTrustCredentialHolder(IssuedTokenContext ctx) {
        getExtraneousProperties().put(TRUST_CLIENT_CREDENTIALS, ctx);
    }

    public IssuedTokenContext getTrustCredentialHolder() {
        return (IssuedTokenContext)getExtraneousProperties().get(TRUST_CLIENT_CREDENTIALS);
    }

    public Element getIssuedSAMLToken() {
         return (Element)getExtraneousProperties().get(ISSUED_SAML_TOKEN);
    }

    public void setIssuedSAMLToken(Element elem) {
         getExtraneousProperties().put(ISSUED_SAML_TOKEN, elem);
    }

    public void setSecureConversationContext(IssuedTokenContext ctx) {
        secureConversationContext = ctx;
    }

    public IssuedTokenContext getSecureConversationContext() {
        return secureConversationContext;
    }

    public void setTrustContext(IssuedTokenContext ctx) {
        trustContext = ctx;
    }

    public IssuedTokenContext getTrustContext() {
        return trustContext;
    }

   //TODO:Having to add AlgorithmSuite here because we need
   // it in the KeyResolver (Encryption)
   public AlgorithmSuite getAlgorithmSuite() {
       return algoSuite;
   }

   public void setAlgorithmSuite(AlgorithmSuite suite) {
       algoSuite = suite;
   }

    public void setWSSAssertion(WSSAssertion wssAssertion){
        this.wssAssertion = wssAssertion;
    }
                                                                                
    public WSSAssertion getWSSAssertion(){
        return wssAssertion;
    }

    public MessagePolicy getInferredSecurityPolicy() {
        return inferredSecurityPolicy;
    }

    public HashMap getSamlIdVSKeyCache() {
        if (getExtraneousProperties().get(SAMLID_VS_KEY_CACHE) == null) {
            getExtraneousProperties().put(SAMLID_VS_KEY_CACHE, new HashMap());
        }
        return (HashMap)getExtraneousProperties().get(SAMLID_VS_KEY_CACHE);
    }

    public void setOperationResolver(OperationResolver operationResolver){
          this.operationResolver = operationResolver;
    }
 
    public OperationResolver getOperationResolver(){
        return operationResolver;
    }
 
    public void isTrustMessage(boolean isTrust){
        this.isTrustMsg = isTrust;
    }

    public boolean isTrustMessage(){
        return isTrustMsg;
    }


}
