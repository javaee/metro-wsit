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

package com.sun.xml.xwss;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.PolicyViolationException;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.SecurityRecipient;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.config.ApplicationSecurityConfiguration;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;
import com.sun.xml.wss.impl.configuration.StaticApplicationContext;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.xwss.SecurityConfiguration;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes.Name;
import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import org.w3c.dom.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import org.w3c.dom.NodeList;


/**
 *
 * 
 */
public class XWSSServerTube extends AbstractFilterTubeImpl {

    private WSEndpoint endPoint;
    private WSDLPort port;
    
    private SecurityConfiguration config = null;
    
    protected  SOAPFactory soapFactory = null;
    protected  MessageFactory messageFactory = null;
    protected SOAPVersion soapVersion = null;
    protected boolean isSOAP12 = false;
    
    protected static final String FAILURE =
            "com.sun.xml.ws.shd.failure";
    
    protected static final String TRUE = "true";
    protected static final String FALSE = "false";
    protected static final String CONTEXT_WSDL_OPERATION =
            "com.sun.xml.ws.wsdl.operation";
    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
    
    /** Creates a new instance of XWSSServerPipe */
    public XWSSServerTube(WSEndpoint epoint, WSDLPort prt, Tube nextTube) {
        super(nextTube);
        endPoint = epoint;
        port = prt;
        try {
        config = new SecurityConfiguration(getServerConfig());
        } catch (XWSSecurityException ex) {
            throw new WebServiceException(ex);
        }
        soapVersion = endPoint.getBinding().getSOAPVersion();
        isSOAP12 = (soapVersion == SOAPVersion.SOAP_12) ? true : false;
        soapFactory = soapVersion.saajSoapFactory;
        messageFactory = soapVersion.saajMessageFactory;
    }
    
    public XWSSServerTube(XWSSServerTube that, TubeCloner cloner) {
        
        super(that,cloner);
        this.endPoint = that.endPoint;
        this.port = that.port;
        
        this.soapFactory = that.soapFactory;
        this.messageFactory = that.messageFactory;
        this.soapVersion = that.soapVersion;
        this.isSOAP12 = that.isSOAP12;
        this.config = that.config;
    }


    public void preDestroy() {
    }

     
    private InputStream getServerConfigStream() {
        QName serviceQName = endPoint.getServiceName();
        String serviceName = serviceQName.getLocalPart();
         
        String serverConfig = "/WEB-INF/" + serviceName + "_" + "security_config.xml";
        ServletContext context = endPoint.getContainer().getSPI(ServletContext.class);
        if(context == null) {
            return null;
        }
        InputStream in = context.getResourceAsStream(serverConfig);
        
        if (in == null) {
            serverConfig = "/WEB-INF/" + "server" + "_" + "security_config.xml";
            in = context.getResourceAsStream(serverConfig);
        }
        
        return in;
    }
    
    private URL getServerConfig() {
        QName serviceQName = endPoint.getServiceName();
        String serviceName = serviceQName.getLocalPart();
        
        
        Container container = endPoint.getContainer();
        
        Object ctxt = null;
        if (container != null) {
            try {
                final Class<?> contextClass = Class.forName(SERVLET_CONTEXT_CLASSNAME);
                ctxt = container.getSPI(contextClass);
            } catch (ClassNotFoundException e) {
                //log here at FINE Level : that the ServletContext was not found
            }
        }
        String serverName = "server";
        URL url = null;
        if (ctxt != null) {
            String serverConfig = "/WEB-INF/" + serverName + "_" + "security_config.xml";
            url =  SecurityUtil.loadFromContext(serverConfig, ctxt);
            if (url == null) {
                serverConfig = "/WEB-INF/" + serviceName + "_" + "security_config.xml";
                url = SecurityUtil.loadFromContext(serverConfig, ctxt);
            }
            
            if (url != null) {
                return url;
            }
        } else {
            //this could be an EJB or JDK6 endpoint
            //so let us try to locate the config from META-INF classpath
            String serverConfig = "META-INF/" + serverName + "_" + "security_config.xml";
            url = SecurityUtil.loadFromClasspath(serverConfig);
            if (url == null) {
                serverConfig = "META-INF/" + serviceName + "_" + "security_config.xml";
                url = SecurityUtil.loadFromClasspath(serverConfig);
            }
            
            if (url != null) {
                return url;
            }
        }
        return null;
    }
     
    
     private static final String ENCRYPTED_BODY_QNAME =
            "{" + MessageConstants.XENC_NS + "}" + MessageConstants.ENCRYPTED_DATA_LNAME;
    
    // server side incoming request handling hook
    public Packet validateRequest(Packet packet)
    throws Exception {
        
        if (config == null) {
            return  packet;
        }
        
        ProcessingContext context = null;
        SOAPMessage message =  packet.getMessage().readAsSOAPMessage();
        try {
            
            StaticApplicationContext sContext =
                    new StaticApplicationContext(getPolicyContext(packet));
            
            context = new ProcessingContextImpl(packet.invocationProperties);
            
            context.setSOAPMessage(message);
            
            String operation = getOperationName(message);
            
            ApplicationSecurityConfiguration _sConfig =
                    config.getSecurityConfiguration();
            
            if (operation.equals(ENCRYPTED_BODY_QNAME) &&
                    _sConfig.hasOperationPolicies()) {
                // get enclosing port level configuration
                if (MessageConstants.debug) {
                    System.out.println("context in plugin= " +
                            sContext.toString());
                }
                ApplicationSecurityConfiguration appconfig =
                        (ApplicationSecurityConfiguration)
                        _sConfig.getSecurityPolicies(sContext).next();
                
                if (appconfig != null) {
                    context.setPolicyContext(sContext);
                    context.setSecurityPolicy(appconfig);
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
                packet.invocationProperties.put(CONTEXT_WSDL_OPERATION, operation);
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
            
            context.setSecurityEnvironment(config.getSecurityEnvironment());
            context.isInboundMessage(true);
            
            if (_sConfig.retainSecurityHeader()) {
                context.retainSecurityHeader(true);
            }
            
            if (_sConfig.resetMustUnderstand()) {
                context.resetMustUnderstand(true);
            }
            
            SecurityRecipient.validateMessage(context);
            String operationName = getOperationName(message);
            
            packet.invocationProperties.put(CONTEXT_WSDL_OPERATION, operationName);
            packet.setMessage(Messages.create(context.getSOAPMessage()));
            /* TODO, how to change this
            if (packet.invocationProperties.get("javax.security.auth.Subject") != null) {
                packet.invocationProperties.("javax.security.auth.Subject",MessageContext.Scope.APPLICATION); 
            }*/
            return packet;
            
        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
            
            packet.invocationProperties.put(FAILURE, TRUE);
            addFault(soapFaultException,message,isSOAP12);
            packet.setMessage(Messages.create(message));
            return packet;
            
        } catch (com.sun.xml.wss.XWSSecurityException xwse) {
            QName qname = null;
            
            if (xwse.getCause() instanceof PolicyViolationException)
                qname = MessageConstants.WSSE_RECEIVER_POLICY_VIOLATION;
            else
                qname = MessageConstants.WSSE_FAILED_AUTHENTICATION;
            
            com.sun.xml.wss.impl.WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    qname, xwse.getMessage(), xwse);
            
            
            packet.invocationProperties.put(FAILURE, TRUE);
            addFault(wsfe,message,isSOAP12);
            packet.setMessage(Messages.create(message));
            
            return packet;
        }
        
    }
    
    // server side response writing hook
    public Packet secureResponse(Packet packet)
    throws Exception {
       
        if (config == null) {
            return packet;
        }
        try {
            ProcessingContext context = new ProcessingContextImpl(packet.invocationProperties);
           
            String operation =
                    (String)packet.invocationProperties.get(CONTEXT_WSDL_OPERATION);
            StaticApplicationContext sContext =
                    new StaticApplicationContext(getPolicyContext(packet));
            sContext.setOperationIdentifier(operation);
            
            ApplicationSecurityConfiguration _sConfig =
                    config.getSecurityConfiguration();
            
            SecurityPolicy policy = _sConfig.getSecurityConfiguration(sContext);
            context.setPolicyContext(sContext);
            
            if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                context.setSecurityPolicy(((DeclarativeSecurityConfiguration)policy).senderSettings());
            } else {
                context.setSecurityPolicy(policy);
            }
            
            context.setSecurityEnvironment(config.getSecurityEnvironment());
            context.isInboundMessage(false);
            context.setSOAPMessage(packet.getMessage().readAsSOAPMessage());
            SecurityAnnotator.secureMessage(context);
            packet.setMessage(Messages.create(context.getSOAPMessage()));
            return packet;
        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
            Message msg = Messages.create(getSOAPFault(soapFaultException));
            packet.setMessage(msg);
            return packet;
        } catch (com.sun.xml.wss.XWSSecurityException xwse) {
            com.sun.xml.wss.impl.WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            Message msg = Messages.create(getSOAPFault(wsfe));
            packet.setMessage(msg);
            return packet;
        }
    }
    
    private StaticApplicationContext getPolicyContext(Packet packet) {
        // assumed to contain single nested container
        ApplicationSecurityConfiguration appconfig =
                config.getSecurityConfiguration();
        
        StaticApplicationContext iContext =
                (StaticApplicationContext)appconfig.getAllContexts().next();
        StaticApplicationContext sContext =
                new StaticApplicationContext(iContext);
        
        QName portQname = null;
        if (port != null) {
            portQname = port.getName();
        } 
        String prt = null;
        
        if (portQname == null) {
            prt = "";
        } else {
            prt = portQname.toString();
        }
        
        sContext.setPortIdentifier(prt);
        return sContext;
    }
    
    public void addFault(
            com.sun.xml.wss.impl.WssSoapFaultException sfe,SOAPMessage soapMessage,boolean isSOAP12)
               throws SOAPException{
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
    
    protected SOAPFault getSOAPFault(WssSoapFaultException sfe) {
        
        SOAPFault fault = null;
        try {
            if (isSOAP12) {
                fault = soapFactory.createFault(sfe.getFaultString(),SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(sfe.getFaultCode());
            } else {
                fault = soapFactory.createFault(sfe.getFaultString(), sfe.getFaultCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Security Pipe: Internal Error while trying to create a SOAPFault");
        }
        return fault;
    }
    
    public SOAPFaultException getSOAPFaultException(
            WssSoapFaultException sfe, boolean isSOAP12) {
        
        SOAPFault fault = null;
        try {
            if (isSOAP12) {
                fault = soapFactory.createFault(sfe.getFaultString(),SOAPConstants.SOAP_SENDER_FAULT);
                
                fault.appendFaultSubcode(sfe.getFaultCode());
            } else {
                fault = soapFactory.createFault(sfe.getFaultString(), sfe.getFaultCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(this + ": Internal Error while trying to create a SOAPFault");
        }
        return new SOAPFaultException(fault);
    }

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

    @Override
    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new XWSSServerTube(this, cloner); 
    }
    
    @Override
    public NextAction processRequest(Packet packet){
        try {
            Packet ret = validateRequest(packet);
            if (TRUE.equals(ret.invocationProperties.get(FAILURE))) {
                return  doReturnWith(ret);
            }            
            return doInvoke(super.next, ret);
        } catch (Throwable t) {
            if (!(t instanceof WebServiceException)) {
                t = new WebServiceException(t);
            }
            return doThrow(t);
        }        
    }
     
    @Override
    public NextAction processResponse(Packet ret) {
        try{
            //could be oneway
            if(ret.getMessage() == null){
                return doReturnWith(ret);
            }
            Packet response = secureResponse(ret);
            return doReturnWith(response);
        }catch(Throwable t){
            if (!(t instanceof WebServiceException)) {
                t = new WebServiceException(t);
            }
            return doThrow(t);
        }
         
    }
    
}
