/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.wss;

import com.sun.xml.wss.impl.*;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

import javax.xml.soap.SOAPMessage;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;

import javax.security.auth.callback.CallbackHandler;
import com.sun.xml.wss.impl.misc.*;
/**
 * This class represents a Context that is used by the XWS-Security Runtime to
 * apply/verify Security Policies on an Outgoing/Incoming SOAP Message.
 * The context contains among other things
 * <UL>
 *   <LI>The SOAP Message to be operated upon
 *   <LI>The Message direction (incoming or outgoing)
 *   <LI>The security policy to be applied by XWS-Security on the message
 *   <LI>A randomly generated Message-Identifier that can be used for request-response correlation,
 *    by a CallbackHandler, the handles <code>DynamicPolicyCallback</code>
 *   <LI>A list of properties associated with the calling Application Runtime, that can be used to
 *    make Dynamic Policy decisions.
 *   <LI>A concrete implementation of the SecurityEnvironment interface OR a CallbackHandler
 * </UL>
 *
 */
public class ProcessingContext implements SecurityProcessingContext {
    
    /*
     * Example:
     *
     * A policy configured as follows:
     *
     *           <RequireSignature/>
     *           <Encrypt/>
     *
     * where, the key used in signature has to be used for response
     * encryption.
     *
     * A callback handler can cache the key information during
     * signature processing (while handling CertificateValidation)
     * based on message and policy identifiers, such that encryption
     * policy handling will cause the cached key to be used.
     */
    
    /*
     * Unique randomnly generated message identifier. Application
     * callback handlers can use this for policy correlation on
     * per message basis.
     */
    String messageIdentifier;
    
    /*
     * Policy identification context that is based on information in
     * configuration files. Truely, security context need not be
     * identified as static or dynamic - such resolution is required
     * in view of support for declarative security configuration.
     *
     * Application callback handlers can use static context in conjunction
     * with certain runtime context to make policy resolution decisions.
     */
    StaticPolicyContext context;
    
    /*
     * Resolved declarative security policy. The IL (integration layer)
     * is responsible for resolving configured security policy; alternately
     * the context can be populated with a policy container and specific
     * policy identifying context, requiring lazy accurate policy resolution
     * to happen in Annotator/Recipient.
     */
    SecurityPolicy securityPolicy;
    
    /*
     * Context indicating direction of message flow depending upon which
     * Annotator or Recipient is 'invoked'
     */
    boolean inBoundMessage = false;
    
    /*
     * Application security environment callback handler
     *
     * @see UsernameCallback (XWSS v1.0)
     * @see PasswordCallback (XWSS v1.0)
     * @see SignatureKeyCallback (XWSS v1.0)
     * @see DecrptionKeyCallback (XWSS v1.0)
     * @see DynamicPolicyCallback (XWSS v2.0)
     * @see EncryptionKeyCallback (XWSS v1.0)
     * @see PasswordValidationCallback (XWSS v1.0)
     * @see CertificateValidationCallback (XWSS v1.0)
     * @see SignatureVerificationCallback (XWSS v1.0)
     */
    CallbackHandler callbackHandler = null;
    
    /*
     * The SecurityEnvironment handler
     */
    SecurityEnvironment environmentHandler= null;
    
    /*
     * XWSS representation for SOAPMessage
     *
     * @see SecurableSoapMessage
     */
    protected SecurableSoapMessage secureMessage = null;
    
    /*
     * Extraneous property list
     */
    protected Map properties = null;
    
    // flag to indicate optimized security option
    // under JAXWS 2.0
    int configType = MessageConstants.NOT_OPTIMIZED;
    
    protected MessageLayout securityHeaderLayout = MessageLayout.Lax;
    
    public static final String OPERATION_RESOLVER="OperationResolver";

    private boolean retainSecHeader= false;
    private boolean resetMU = false;
    private boolean isClient = false;
    
    private boolean isExpired = false;
    
    /**
     *Default constructor
     */
    public ProcessingContext() {}
    
    /**
     * Constructor
     * @param context the static policy context for this request
     * @param securityPolicy the SecurityPolicy to be applied for this request
     * @param message the SOAPMessage
     * @throws XWSSecurityException if there was an error in creating the ProcessingContext
     */
    public ProcessingContext(StaticPolicyContext context,
            SecurityPolicy securityPolicy,
            SOAPMessage message)
            throws XWSSecurityException {
        
        generateMessageId();
        
        setPolicyContext(context);
        
        setSecurityPolicy(securityPolicy);
        
        setSOAPMessage(message);
    }

    public void resetMustUnderstand(boolean b) {
        this.resetMU = b;
    }
    
    public boolean resetMustUnderstand() {
        return this.resetMU;
    }
    /*
     * Note: Avoiding check on policy types since Annotator/Recipient
     * w'd do the same. Helps FPC to throw exception if WSSPolicy
     * instance is not set.
     */
    
    /**
     * set the SecurityPolicy for the context
     * @param securityPolicy SecurityPolicy
     * @throws XWSSecurityException if the securityPolicy is of invalid type
     */
    public void setSecurityPolicy(SecurityPolicy securityPolicy)
    throws XWSSecurityException {
        this.securityPolicy = securityPolicy;
    }
    
    /**
     * @return SecurityPolicy for this context
     */
    public SecurityPolicy getSecurityPolicy() {
        return this.securityPolicy;
    }
    
    /**
     * set the StaticPolicyContext for this ProcessingContext.
     * @param context StaticPolicyContext for this context
     */
    public void setPolicyContext(StaticPolicyContext context) {
        this.context = context;
    }
    
    /**
     * @return StaticPolicyContext associated with this ProcessingContext, null otherwise
     */
    public StaticPolicyContext getPolicyContext() {
        return this.context;
    }
    
    /**
     * set the SOAP Message into the ProcessingContext.
     * @param message SOAPMessage
     * @throws XWSSecurityException if there was an error in setting the SOAPMessage
     */
    public void setSOAPMessage(SOAPMessage message)
    throws XWSSecurityException {
        
        secureMessage = new SecurableSoapMessage();
        secureMessage.setSOAPMessage(message);
        setOptimized();
    }
    
    
    /**
     * @return the SOAPMessage from the context
     */
    public SOAPMessage getSOAPMessage() {
        return secureMessage.getSOAPMessage();
    }
    
    /* (non-Javadoc)
     * @return SecurableSoapMessage
    public SecurableSoapMessage getSecurableSoapMessage() {
        return secureMessage;
    }
     */
    
    /**
     * set the CallbackHandler for the context
     * @param handler The CallbackHandler
     */
    public void setHandler(CallbackHandler handler) {
        this.callbackHandler = handler;
        this.environmentHandler = new DefaultSecurityEnvironmentImpl(handler);
    }
    
    /**
     * set the SecurityEnvironment Handler for the context
     * @param handler The SecurityEnvironment Handler
     */
    public void setSecurityEnvironment(SecurityEnvironment handler) {
        this.environmentHandler = handler;
    }
    
    /**
     * @return the CallbackHandler set for the context
     */
    public CallbackHandler getHandler() {
        return this.callbackHandler;
    }
    
    /**
     * @return The SecurityEnvironment Handler set for the context
     */
    public SecurityEnvironment getSecurityEnvironment() {
        return this.environmentHandler;
    }
    
    /**
     * Properties extraneously defined by XWSS runtime - can contain
     * application's runtime context (like JAXRPCContext etc)
     *
     * @return Map of extraneous properties
     */
    public Map getExtraneousProperties() {
        if (properties == null) {
            properties = new HashMap();
        }
        return this.properties;
    }
    
    /**
     * set the message flow direction (to true if inbound, false if outbound)
     * @param inBound message flow direction
     */
    public void isInboundMessage(boolean inBound) {
        this.inBoundMessage = inBound;
    }
    
    /**
     * @return message flow direction, true if incoming, false otherwise
     */
    public boolean isInboundMessage() {
        return this.inBoundMessage;
    }
    
    /**
     * Allow for message identifier to be generated externally
     * @param identifier the Message Identifier value
     */
    public void setMessageIdentifier(String identifier) {
        this.messageIdentifier = identifier;
    }
    
    /**
     * @return message identifier for the Message in the context
     */
    public String getMessageIdentifier() {
        return this.messageIdentifier;
    }
    
    
    /**
     * set the extraneous property into the context
     * Extraneous Properties are properties extraneously defined by XWSS runtime
     * and can contain application's runtime context (like JAXRPCContext etc)
     * @param name the property name
     * @param value the property value
     */
    @SuppressWarnings("unchecked")
    public void setExtraneousProperty(String name, Object value) {
        getExtraneousProperties().put(name, value);
    }
    
    /**
     * @return the value for the named extraneous property.
     */
    public Object getExtraneousProperty(String name) {
        return getExtraneousProperties().get(name);
    }
    
    /**
     * remove the named extraneous property if present
     * @param name the Extraneous Property to be removed
     */
    public void removeExtraneousProperty(String name) {
        getExtraneousProperties().remove(name);
    }
    
    
    /*
     * @param p1
     * @param p2
     */
    @SuppressWarnings("unchecked")
    public static  void copy(Map p1, Map p2) {
        p1.putAll(p2);
    }
    
    
    /**
     * copy operator
     * @param ctx1 the ProcessingContext to which to copy
     * @param ctx2 the ProcessingContext from which to copy
     * @throws XWSSecurityException if there was an error during the copy operation
     */
    public  void copy(ProcessingContext ctx1, ProcessingContext ctx2)
    throws XWSSecurityException {
        if(ctx2 == null)
            return;
        ctx1.setHandler(ctx2.getHandler());
        ctx1.setSecurityEnvironment(ctx2.getSecurityEnvironment());
        ctx1.setMessageIdentifier(ctx2.getMessageIdentifier());
        if (ctx2.getSecurityPolicy() != null) {
            ctx1.setSecurityPolicy(ctx2.getSecurityPolicy());
        }
        ctx1.isInboundMessage(ctx2.isInboundMessage());
        ctx1.setSecureMessage(ctx2.getSecureMessage());
        //ctx1.getExtraneousProperties().putAll(ctx2.getExtraneousProperties());
        this.properties = ctx2.getExtraneousProperties();
        ctx1.setPolicyContext(ctx2.getPolicyContext());
        ctx1.setConfigType(ctx2.getConfigType());
        ctx1.retainSecurityHeader(ctx2.retainSecurityHeader());
        ctx1.resetMustUnderstand(ctx2.resetMustUnderstand());
        ctx1.isClient(ctx2.isClient());
        ctx1.isExpired(ctx2.isExpired());
    }
    
    /*
     * Internal auto-generating message identifier implementation
     */
    private void generateMessageId() {
        Random rnd = new Random();
        long longRandom = rnd.nextLong();
        this.messageIdentifier = String.valueOf(longRandom);
    }
    
    /**
     * This method is used for internal purposes
     */
    public void reset(){
    }
    
    /**
     * This method is used for internal purposes
     */
    public int getConfigType() {
        return this.configType;
    }
    
    /**
     * This method is used for internal purposes
     */
    public void setConfigType(int type) {
        this.configType = type;
        setOptimized();
    }
    
    protected SecurableSoapMessage getSecureMessage() {
        return secureMessage;
    }
    
    protected void setSecureMessage(SecurableSoapMessage msg) {
        secureMessage = msg;
    }
    
    
    private void setOptimized(){
        if(this.secureMessage != null){
            if(this.configType == MessageConstants.NOT_OPTIMIZED ){
                this.secureMessage.setOptimized(false);
            }else{
                this.secureMessage.setOptimized(true);
            }
        }
    }
    
    public void copy(SecurityProcessingContext ctx1, SecurityProcessingContext ctx2) throws XWSSecurityException {
        throw new UnsupportedOperationException("Not yet supported");
    }
    
    public void setSecurityHeaderLayout(MessageLayout layout){
        this.securityHeaderLayout = layout;
    }
    
    public MessageLayout getSecurityHeaderLayout(){
        return this.securityHeaderLayout;
    }
    
    /*
     *@return the Retain Security Header Config Property
     */
    public boolean retainSecurityHeader() {
        return retainSecHeader;
    }
    
    /*
     *@param arg, set the retainSecurityHeader flag. 
     */
    public void retainSecurityHeader(boolean arg) {
        this.retainSecHeader = arg;
    }
    
    public void isClient(boolean isClient){
        this.isClient = isClient;
    }
    
    public boolean isClient(){
        return isClient;
    }
    
    public boolean isExpired(){
        return this.isExpired;
    }
    
    public void isExpired(boolean value){
        this.isExpired = value;
    }

}
