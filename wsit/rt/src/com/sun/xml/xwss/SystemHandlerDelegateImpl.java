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

package com.sun.xml.xwss;



import com.sun.xml.security.jaxws.JAXWSMessage;
import com.sun.xml.messaging.saaj.soap.ExpressMessageFactoryImpl;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import java.net.URL;

import java.io.InputStream;

import java.util.HashMap;
import java.util.Iterator;

import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;

import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPFault;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.WebServiceException;
import com.sun.xml.ws.spi.runtime.MessageContext;
import com.sun.xml.ws.spi.runtime.SOAPMessageContext;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;

import javax.xml.namespace.QName;


import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.SecurityRecipient;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.PolicyViolationException;
import com.sun.xml.wss.impl.PolicyTypeUtil;

import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.configuration.StaticApplicationContext;
import com.sun.xml.wss.impl.config.ApplicationSecurityConfiguration;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;

//import com.sun.xml.messaging.saaj.soap.ExpressMessageFactoryImpl;

import javax.servlet.ServletContext;
//import src.com.sun.xml.xwss.XOPProcessor;

public class SystemHandlerDelegateImpl implements SystemHandlerDelegate {

    private HashMap<String, SecurityConfiguration> service_to_configMap = new HashMap<String, SecurityConfiguration>();
    
    private SecurityConfiguration config = null;
    
    private static final String MESSAGE_SECURITY_CONFIGURATION =
            SecurityConfiguration.MESSAGE_SECURITY_CONFIGURATION;
    
    private static final String CONTEXT_WSDL_OPERATION =
            "com.sun.xml.ws.wsdl.operation";
    
    private MessageFactory soap11MF = null;
    private MessageFactory soap12MF = null;
    private ExpressMessageFactoryImpl expMF = null;
    //private MessageFactory expMF = null;

    private static final String FAILURE =
            "com.sun.xml.ws.shd.failure";
    
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static boolean nonOpt = false;
    //if jaxws has to construct the soap message.
    static{
        try{
            String value = System.getProperty("jaxws.soapmessage","false");
            if(value != null && value.length() > 0){
                nonOpt = Boolean.parseBoolean(value);
            }
        }catch(Exception ex){
        }
    }
    
    public SystemHandlerDelegateImpl() {
        
        try{
            soap11MF = MessageFactory.newInstance();
            soap12MF = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            expMF = new ExpressMessageFactoryImpl();
            //expMF = MessageFactory.newInstance();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
        
    }
    
    
    private String getServiceName(MessageContext messageContext) {
        QName serviceQName =
                (QName)messageContext.get(MessageContext.WSDL_SERVICE);
        String serviceName = null;
        if (serviceQName != null) {
            serviceName = serviceQName.getLocalPart();
        } else {
            serviceName = "server";
        }
        return serviceName;
    }
    
    private InputStream getServerConfigStream(String serviceName,
            MessageContext messageContext) {
        String serverConfig = "/WEB-INF/" + serviceName + "_" + "security_config.xml";
        ServletContext context =
                (ServletContext)messageContext.get(
                "javax.xml.ws.servlet.context");
        if(context == null) {
            //try to locate server_security_config.xml from classpath
            
            return null;
        } else {
            return context.getResourceAsStream(serverConfig);
        }
    }
    
    private URL getServerConfig(String serviceName,
            MessageContext messageContext) {
        String serverConfig = "/WEB-INF/" + serviceName + "_" + "security_config.xml";
        Object context = messageContext.get("javax.xml.ws.servlet.context");
        if(context == null) {
            //try to locate server_security_config.xml from classpath
            //This should work for both EJB Jar's and JDK6 Endpoints
            String configUrl = "META-INF/" + serviceName + "_" + "security_config.xml";
            return SecurityUtil.loadFromClasspath(configUrl);
        } else {
            return SecurityUtil.loadFromContext(serverConfig, context);
        }
    }
    
     

    private void debugProperties(MessageContext messageContext) {
        if (!MessageConstants.debug) {
            return;
        }
        
        System.out.println( "MESSAGE_OUTBOUND_PROPERTY " +
                messageContext.get(messageContext.MESSAGE_OUTBOUND_PROPERTY));
        
        System.out.println("SERVICE=" + messageContext.get(MessageContext.WSDL_SERVICE));
        System.out.println("INTERFACE=" + messageContext.get(MessageContext.WSDL_INTERFACE));
        System.out.println("PORT=" + messageContext.get(MessageContext.WSDL_PORT));
        System.out.println("OPERATION=" + messageContext.get(MessageContext.WSDL_OPERATION));
        System.out.println("ServletContext=" +
                messageContext.get("javax.xml.ws.servlet.context"));
        
    }
    
    private void setSecurityConfiguration(MessageContext messageContext)
    throws XWSSecurityException {
        
        if (config != null) {
            if(!config.isEmpty()){
                messageContext.put(MESSAGE_SECURITY_CONFIGURATION, config);
            }
        } else {
            String serviceName = getServiceName(messageContext);
            SecurityConfiguration scf =
                    service_to_configMap.get(serviceName);
            
            if (scf != null) {
                if (!scf.isEmpty()) {
                    messageContext.put(
                            MESSAGE_SECURITY_CONFIGURATION, scf);
                    return;
                }
            }
            
            // scf was null
            synchronized (service_to_configMap) {
                SecurityConfiguration _sc = service_to_configMap.get(serviceName);
                if(_sc == null){
                    scf = new SecurityConfiguration(
                            getServerConfig(serviceName, messageContext));
                    if(!scf.isEmpty()){
                        service_to_configMap.put(serviceName, scf);
                    }
                }else{
                    scf = _sc;
                }
            }
            if (!scf.isEmpty()) {
                messageContext.put(
                        MESSAGE_SECURITY_CONFIGURATION, scf);
                return;
            }
            
            //scf is Empty
            synchronized (service_to_configMap) {
                if(config == null){
                    config = new SecurityConfiguration(
                            getServerConfig("server", messageContext));
                }
            }
            if (!config.isEmpty()) {
                messageContext.put(MESSAGE_SECURITY_CONFIGURATION, config);
            }
        }
    }
    
    public boolean processRequest(MessageContext messageContext)
    throws RuntimeException {
         
        if (MessageConstants.debug) {
            System.out.println("....ProcessRequest of SystemHandlerDelegate ..."
                    + messageContext);
        }
        debugProperties(messageContext);
        
        Boolean outBound = (Boolean)
        messageContext.get(messageContext.MESSAGE_OUTBOUND_PROPERTY);
        // hack for the TODO above
        boolean client = (outBound == null) ? true : outBound.booleanValue();
        
        
        try {
            if (client) {
                return secureRequest((SOAPMessageContext)messageContext);
            } else {
                setSecurityConfiguration(messageContext);
                return validateRequest((SOAPMessageContext) messageContext);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void processResponse(MessageContext messageContext)
    throws RuntimeException {
        if (MessageConstants.debug) {
            System.out.println(".....ProcessResponse of SystemHandlerDelegate ..."
                    + messageContext);
        }
        debugProperties(messageContext);
        
        Boolean outBound =
                (Boolean)messageContext.get(
                messageContext.MESSAGE_OUTBOUND_PROPERTY);
        // hack for the TODO above
        boolean server = (outBound == null) ? true : outBound.booleanValue();
        boolean client = !server;
        
        
        try {
            if (client) {
                validateResponse((SOAPMessageContext)messageContext);
            } else {
                setSecurityConfiguration(messageContext);
                secureResponse((SOAPMessageContext)messageContext);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private StaticApplicationContext getPolicyContext(
            SOAPMessageContext messageContext) {
        // assumed to contain single nested container
        SecurityConfiguration sConfig = (SecurityConfiguration)
        messageContext.get(MESSAGE_SECURITY_CONFIGURATION);
        
        ApplicationSecurityConfiguration config =
                ((SecurityConfiguration)sConfig).getSecurityConfiguration();
        
        StaticApplicationContext iContext =
                (StaticApplicationContext)config.getAllContexts().next();
        StaticApplicationContext sContext =
                new StaticApplicationContext(iContext);
        
        String port = null;
        QName portQname =
                (QName)messageContext.get(messageContext.WSDL_PORT);
        
        if (portQname == null) {
            port = "";
        } else {
            port = portQname.toString();
        }
        
        sContext.setPortIdentifier(port);
        
        return sContext;
    }
    
    private void copyToMessageContext(SOAPMessageContext messageContext,
            ProcessingContext context) throws Exception {
        if (MessageConstants.debug) {
            System.out.println("Setting into messageContext ....");
        }
        messageContext.setMessage(context.getSOAPMessage());
        
        Iterator i = context.getExtraneousProperties().keySet().iterator();
        while (i.hasNext()) {
            String name  = (String) i.next();
            Object value = context.getExtraneousProperties().get(name);
            messageContext.put(name, value);
        }
    }
    
    private void copyToProcessingContext(ProcessingContext context,
            SOAPMessageContext messageContext) throws Exception {
        
        
        Iterator<Map.Entry<String, Object>> i = messageContext.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, Object> entry = i.next();
            String name  = entry.getKey();
            Object value = entry.getValue();
            context.setExtraneousProperty(name, value);
        }
        
    }
    
    // client side incoming request handling code
    public void validateResponse(SOAPMessageContext messageContext)
    throws Exception {
        boolean isSOAP12 = false;
        try {
            SecurityConfiguration sConfig = (SecurityConfiguration)
            messageContext.get(MESSAGE_SECURITY_CONFIGURATION);
            
            if (sConfig == null) {
                return;
            }
            
            SOAPMessage message =  messageContext.getMessage();
            
            // hack to get the SOAP version
            if (message.getClass().getName().
                    equals(
                    "com.sun.xml.messaging.saaj.soap.ver1_2.Message1_2Impl")) {
                isSOAP12 = true;
            }
            
            //QName operation = (QName)messageContext.get(CONTEXT_WSDL_OPERATION);
            String operation = (String)messageContext.get(CONTEXT_WSDL_OPERATION);
            
            StaticApplicationContext sContext =
                    getPolicyContext(messageContext);
            sContext.setOperationIdentifier(operation);
            
            ApplicationSecurityConfiguration config =
                    sConfig.getSecurityConfiguration();
            
            SecurityPolicy policy = config.getSecurityConfiguration(sContext);
            
            ProcessingContext context = new ProcessingContext();
            copyToProcessingContext(context, messageContext);
            context.setPolicyContext(sContext);
            context.setSOAPMessage(messageContext.getMessage());
            if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                context.setSecurityPolicy(
                        ((DeclarativeSecurityConfiguration)policy).
                        receiverSettings());
            } else {
                context.setSecurityPolicy(policy);
            }
            
            context.setSecurityEnvironment(sConfig.getSecurityEnvironment());
            context.isInboundMessage(true);
            
            if (config.retainSecurityHeader()) {
                context.retainSecurityHeader(true);
            }
            
            if (config.resetMustUnderstand()) {
                context.resetMustUnderstand(true);
            }
            SecurityRecipient.validateMessage(context);
            
            copyToMessageContext(messageContext, context);
            if (messageContext.get("javax.security.auth.Subject") != null) {
                messageContext.setScope("javax.security.auth.Subject", MessageContext.Scope.APPLICATION); 
            }
            
        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
            throw getSOAPFaultException(soapFaultException, isSOAP12);
        } catch (com.sun.xml.wss.XWSSecurityException xwse) {
            QName qname = null;
            
            if (xwse.getCause() instanceof PolicyViolationException)
                qname = MessageConstants.WSSE_RECEIVER_POLICY_VIOLATION;
            else
                qname = MessageConstants.WSSE_FAILED_AUTHENTICATION;
            
            com.sun.xml.wss.impl.WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    qname, xwse.getMessage(), xwse);
            throw getSOAPFaultException(wsfe, isSOAP12);
        }
    }
    
    // client side request sending hook
    public boolean secureRequest(SOAPMessageContext messageContext)
    throws Exception {
        boolean isSOAP12 = false;
        ProcessingContext context = null;
        try {
            SecurityConfiguration sConfig = (SecurityConfiguration)
            messageContext.get(MESSAGE_SECURITY_CONFIGURATION);
            
            if (sConfig == null) {
                return true;
            }
            
            
            QName operationQName =   (QName)messageContext.get(messageContext.WSDL_OPERATION);
            String operation =  null;
            
            if (operationQName == null) {
                
                SOAPMessage message =  messageContext.getMessage();
                operation = getOperationName(message);
            } else {
                operation = operationQName.toString();
            }
            
            messageContext.put(CONTEXT_WSDL_OPERATION, operation);
            
            StaticApplicationContext sContext =
                    getPolicyContext(messageContext);
            sContext.setOperationIdentifier(operation);
            
            ApplicationSecurityConfiguration config =
                    sConfig.getSecurityConfiguration();
            
            SecurityPolicy policy = config.getSecurityConfiguration(sContext);
            
            context = new ProcessingContext();
            //setting optimized flag
            
            copyToProcessingContext(context, messageContext);
            
            
            context.setPolicyContext(sContext);
            
            if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                context.setSecurityPolicy(
                        ((DeclarativeSecurityConfiguration)policy).senderSettings());
            } else {
                context.setSecurityPolicy(policy);
            }
            
            context.setSecurityEnvironment(sConfig.getSecurityEnvironment());
            context.isInboundMessage(false);
            setSOAPMessage(messageContext,context, config.isOptimized());
            SecurityAnnotator.secureMessage(context);
            
            copyToMessageContext(messageContext, context);
        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
            addFault(soapFaultException,messageContext.getMessage(),isSOAP12);
        } catch (com.sun.xml.wss.XWSSecurityException xwse) {
            // log the exception here
            throw new WebServiceException(xwse);
        }
        
        return true;
    }
    
    private static final String ENCRYPTED_BODY_QNAME =
            "{" + MessageConstants.XENC_NS + "}" + MessageConstants.ENCRYPTED_DATA_LNAME;
    
    // server side incoming request handling hook
    public boolean validateRequest(SOAPMessageContext messageContext)
    throws Exception {
        boolean isSOAP12 = false;
        ProcessingContext context = null;
        try {
            SecurityConfiguration sConfig = (SecurityConfiguration)
            messageContext.get(MESSAGE_SECURITY_CONFIGURATION);
            
            if (sConfig == null) {
                return true;
            }
            
            SOAPMessage message =  messageContext.getMessage();
            
            // hack to get the SOAP version
            if (message.getClass().getName().
                    equals(
                    "com.sun.xml.messaging.saaj.soap.ver1_2.Message1_2Impl")) {
                isSOAP12 = true;
            }
            
            StaticApplicationContext sContext =
                    new StaticApplicationContext(getPolicyContext(messageContext));
            
            context = new ProcessingContext();
            copyToProcessingContext(context, messageContext);
            context.setSOAPMessage(messageContext.getMessage());
            String operation = getOperationName(message);
            
            ApplicationSecurityConfiguration _sConfig =
                    sConfig.getSecurityConfiguration();
            
            if (operation.equals(ENCRYPTED_BODY_QNAME) &&
                    _sConfig.hasOperationPolicies()) {
                // get enclosing port level configuration
                if (MessageConstants.debug) {
                    System.out.println("context in plugin= " +
                            sContext.toString());
                }
                ApplicationSecurityConfiguration config =
                        (ApplicationSecurityConfiguration)
                        _sConfig.getSecurityPolicies(sContext).next();
                
                if (config != null) {
                    context.setPolicyContext(sContext);
                    context.setSecurityPolicy(config);
                } else {
                    ApplicationSecurityConfiguration config0 =
                            (ApplicationSecurityConfiguration) _sConfig.
                            getAllTopLevelApplicationSecurityConfigurations().
                            iterator().next();
                    
                    //sContext.setPortIdentifier ("");
                    context.setPolicyContext(sContext);
                    context.setSecurityPolicy(config0);
                }
            } else {
                sContext.setOperationIdentifier(operation);
                messageContext.put(CONTEXT_WSDL_OPERATION, operation);
                SecurityPolicy policy =
                        _sConfig.getSecurityConfiguration(sContext);
                
                context.setPolicyContext(sContext);
                
                if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                    context.setSecurityPolicy(
                            ((DeclarativeSecurityConfiguration)policy).
                            receiverSettings());
                } else {
                    context.setSecurityPolicy(policy);
                }
            }
            
            context.setSecurityEnvironment(sConfig.getSecurityEnvironment());
            context.isInboundMessage(true);
            
            if (_sConfig.retainSecurityHeader()) {
                context.retainSecurityHeader(true);
            }
            SecurityRecipient.validateMessage(context);
            String operationName = getOperationName(message);
            
            messageContext.put(CONTEXT_WSDL_OPERATION, operationName);
            
            copyToMessageContext(messageContext, context);
            if (messageContext.get("javax.security.auth.Subject") != null) {
                messageContext.setScope("javax.security.auth.Subject",MessageContext.Scope.APPLICATION); 
            }
            
        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
            
            messageContext.put(FAILURE, TRUE);
            addFault(soapFaultException,messageContext.getMessage(),isSOAP12);
            return false;
            
        } catch (com.sun.xml.wss.XWSSecurityException xwse) {
            QName qname = null;
            
            if (xwse.getCause() instanceof PolicyViolationException)
                qname = MessageConstants.WSSE_RECEIVER_POLICY_VIOLATION;
            else
                qname = MessageConstants.WSSE_FAILED_AUTHENTICATION;
            
            com.sun.xml.wss.impl.WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    qname, xwse.getMessage(), xwse);
            
            
            messageContext.put(FAILURE, TRUE);
            addFault(wsfe,messageContext.getMessage(),isSOAP12);
            
            return false;
        }
        
        return true;
    }
    
    // server side response writing hook
    public void secureResponse(SOAPMessageContext messageContext)
    throws Exception {
        boolean isSOAP12 = false;
        try {
            
            SecurityConfiguration sConfig = (SecurityConfiguration)
            messageContext.get(MESSAGE_SECURITY_CONFIGURATION);
            
            if (sConfig == null) {
                return;
            }
            ProcessingContext context = new ProcessingContext();
            copyToProcessingContext(context, messageContext);
            
            if (messageContext.get(FAILURE) == TRUE) {
                DumpFilter.process(context);
                // reset the FAILURE flag
                messageContext.put(FAILURE, FALSE);
                return;
            }
            
            String operation =
                    (String)messageContext.get(CONTEXT_WSDL_OPERATION);
            StaticApplicationContext sContext =
                    new StaticApplicationContext(getPolicyContext(messageContext));
            sContext.setOperationIdentifier(operation);
            
            ApplicationSecurityConfiguration _sConfig =
                    sConfig.getSecurityConfiguration();
            
            SecurityPolicy policy = _sConfig.getSecurityConfiguration(sContext);
            context.setPolicyContext(sContext);
            
            
            if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                context.setSecurityPolicy(((DeclarativeSecurityConfiguration)policy).senderSettings());
            } else {
                context.setSecurityPolicy(policy);
            }
            
            context.setSecurityEnvironment(sConfig.getSecurityEnvironment());
            context.isInboundMessage(false);
            setSOAPMessage(messageContext,context,_sConfig.isOptimized());
            SecurityAnnotator.secureMessage(context);
            copyToMessageContext(messageContext, context);
        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
            throw getSOAPFaultException(soapFaultException, isSOAP12);
        } catch (com.sun.xml.wss.XWSSecurityException xwse) {
            com.sun.xml.wss.impl.WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            throw getSOAPFaultException(wsfe, isSOAP12);
        }
    }
    
    private static SOAPFactory sf11 =  null;
    private static SOAPFactory sf12 =  null;
    static {
        try {
            sf11 = SOAPFactory.newInstance();
            sf12 = SOAPFactory.newInstance(
                    SOAPConstants.SOAP_1_2_PROTOCOL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public SOAPFaultException getSOAPFaultException(
            WssSoapFaultException sfe, boolean isSOAP12) {
        
        SOAPFault fault = null;
        try {
            if (isSOAP12) {
                fault = sf12.createFault(sfe.getFaultString(),SOAPConstants.SOAP_SENDER_FAULT);
                
                fault.appendFaultSubcode(sfe.getFaultCode());
            } else {
                fault = sf11.createFault(sfe.getFaultString(), sfe.getFaultCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("com.sun.xml.rpc.security.SystemHandlerDelegateImpl: Internal Error while trying to create a SOAPFault");
        }
        return new SOAPFaultException(fault);
    }
    
    /**
     * Handles rpc-lit, rpc-encoded, doc-lit wrap/non-wrap, doc-encoded modes as follows:
     *
     * (1) rpc-lit, rpc-enc, doc-lit (wrap), doc-enc: First child of SOAPBody to contain
     *  Operation Identifier
     * (2) doc-lit (non-wrap): Operation Identifier constructed as concatenated string
     *                         of tag names of childs of SOAPBody
     */
    private String getOperationName(SOAPMessage message)
    throws Exception {
        Node node = null;
        String key = null;
        SOAPBody body = null;
        
        if (message != null)
            body = message.getSOAPBody();
        else
            throw new XWSSecurityException(
                    "SOAPMessage in message context is null");
        
        if (body != null)
            node = body.getFirstChild();
        else
            throw new XWSSecurityException(
                    "No body element identifying an operation is found");
        
        StringBuffer tmp = new StringBuffer("");
        String operation = "";
        
        for (; node != null; node = node.getNextSibling())
            tmp.append("{" + node.getNamespaceURI() + "}" + node.getLocalName() + ":");
        operation = tmp.toString();
        if(operation.length()> 0){
            return operation.substring(0, operation.length()-1);
        }else{
            return operation;
        }
    }
    
    
    /**
     * Called before the method invocation.
     * @param messageContext contains property bag with the scope of the
     *                       properties
     */
    public void preInvokeEndpointHook(MessageContext messageContext) {
        
    }
    
    private void setSOAPMessage(com.sun.xml.ws.spi.runtime.SOAPMessageContext msgContext,ProcessingContext pc,boolean optimized) throws Exception{
        
        if(msgContext.isAlreadySoap()){
            if (MessageConstants.debug) {
                System.out.println("ALREADY SOAP");
            }
            pc.setSOAPMessage(msgContext.getMessage());
            return;
        }
        if(nonOpt){
            pc.setSOAPMessage(msgContext.getMessage());
            return;
        }
        
        String fiValue = (String) msgContext.get("com.sun.xml.ws.client.ContentNegotiation");
        
        if(fiValue != null &&  fiValue.length() > 0 ){
            if("optimistic".equals(fiValue)){
                SOAPMessage msg = expMF.createMessage(msgContext,false);
                if (MessageConstants.debug) {
                    System.out.println("CONSTRUCT SOAP");
                }
                pc.setSOAPMessage(msg);
                return;
            }
        }
        
        MessagePolicy mpolicy = (MessagePolicy)pc.getSecurityPolicy();
        if (MessageConstants.debug) {
            System.out.println("SOAP Message is "+optimized);
            System.out.println("SOAP Message is "+mpolicy.getOptimizedType());
        }
        if(!optimized || mpolicy.getOptimizedType() == MessageConstants.NOT_OPTIMIZED){
            SOAPMessage msg = expMF.createMessage(msgContext,false);
            
            if (MessageConstants.debug) {
                System.out.println("CONSTRUCT SOAP");
            }
            pc.setSOAPMessage(msg);
        }else{
            if (MessageConstants.debug) {
                System.out.println("CONSTRUCT SOAP EXPRESS");
            }
            SOAPMessage msg = expMF.createMessage(msgContext);
            pc.setSOAPMessage(msg);
            pc.setConfigType(mpolicy.getOptimizedType());
        }
        return;
    }
    
    public void addFault(com.sun.xml.wss.impl.WssSoapFaultException sfe,SOAPMessage soapMessage,boolean isSOAP12)throws SOAPException{
        SOAPBody body = soapMessage.getSOAPBody();
        body.removeContents();
        soapMessage.removeAllAttachments();
        QName faultCode = sfe.getFaultCode();
        Name faultCodeName = null;
        
        if(faultCode == null){
            faultCode = new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE,"Client");
        }
        if(isSOAP12){
            SOAPFault fault = body.addFault(SOAPConstants.SOAP_SENDER_FAULT,sfe.getMessage());
            fault.appendFaultSubcode(faultCode);
        }else{
            body.addFault(faultCode, sfe.getMessage());
        }
        NodeList list = soapMessage.getSOAPPart().getEnvelope().getElementsByTagNameNS(MessageConstants.WSSE_NS,MessageConstants.WSSE_SECURITY_LNAME);
        if(list.getLength() > 0){
            Node node = list.item(0);
            node.getParentNode().removeChild(node);
        }
    }
}
