/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

/*
 * $Id: SecurityPluginUtil.java,v 1.2 2010-10-21 15:35:44 snajper Exp $
 */

package com.sun.xml.rpc.security;

import java.util.Iterator;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;

import javax.xml.namespace.QName;

import java.io.ByteArrayInputStream;

import javax.security.auth.callback.CallbackHandler;

import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.soap.SOAPFaultException;
//import javax.xml.rpc.handler.soap.SOAPMessageContext;

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


import com.sun.xml.rpc.client.StreamingSenderState;
import com.sun.xml.rpc.server.StreamingHandlerState;

import com.sun.xml.rpc.soap.message.SOAPMessageContext;
import com.sun.xml.wss.impl.policy.SecurityPolicy;

import com.sun.xml.wss.impl.configuration.StaticApplicationContext;
import com.sun.xml.wss.impl.config.SecurityConfigurationXmlReader;
import com.sun.xml.wss.impl.config.ApplicationSecurityConfiguration;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;

public class SecurityPluginUtil {

    String port = null;

    private CallbackHandler _callbackHandler = null;
    private SecurityEnvironment _securityEnvironment = null;

    private ApplicationSecurityConfiguration _sConfig = null;

    private static final String CONTEXT_OPERATION = "context.operation.name";

    public SecurityPluginUtil (String config, String port, boolean isStub)
    throws Exception {
        if (config != null) {
            // look for and remove version tag
            int versionStart = config.indexOf('[');
            if (versionStart != -1) {
                int versionEnd = config.indexOf(']');
                // versionEnd better not be -1!
                // In future versions we can treat
                // the config string differently depending
                // on the version string.
                config = config.substring(versionEnd + 1);
            }

            this.port = port;

            _sConfig = SecurityConfigurationXmlReader.createApplicationSecurityConfiguration(
				         new ByteArrayInputStream(config.getBytes()));

            _callbackHandler = (CallbackHandler)Class.forName(_sConfig.getSecurityEnvironmentHandler(),
                                  true, Thread.currentThread().getContextClassLoader()).newInstance();
            _securityEnvironment = new DefaultSecurityEnvironmentImpl(_callbackHandler);

        }
    }

    private void copyToMessageContext (SOAPMessageContext messageContext, ProcessingContext context)
    throws Exception {
        messageContext.setMessage(context.getSOAPMessage());

        Iterator i = context.getExtraneousProperties().keySet().iterator();
        while (i.hasNext()) {
            String name  = (String) i.next();
            Object value = context.getExtraneousProperties().get (name);
            messageContext.setProperty(name, value);
        }
    }

    private void copyToProcessingContext (ProcessingContext context, SOAPMessageContext messageContext)
    throws Exception {
        context.setSOAPMessage (messageContext.getMessage());

        Iterator i = messageContext.getPropertyNames();
        while (i.hasNext()) {
			String name  = (String) i.next();
			Object value = messageContext.getProperty (name);

			context.setExtraneousProperty (name, value);
		}
    }

    private StaticApplicationContext getPolicyContext () {
        // assumed to contain single nested container
        ApplicationSecurityConfiguration config =
            (ApplicationSecurityConfiguration) _sConfig.
	        getAllTopLevelApplicationSecurityConfigurations().iterator().next();

        StaticApplicationContext iContext = (StaticApplicationContext) config.getAllContexts().next();

        StaticApplicationContext sContext = new StaticApplicationContext (iContext);
        sContext.setPortIdentifier (port);

	return sContext;
    }

    public void _preHandlingHook (StreamingSenderState state)
    throws Exception {
        try {
             SOAPMessageContext messageContext = state.getMessageContext();
             SOAPMessage message = state.getResponse().getMessage();

             String operation = (String)messageContext.getProperty(CONTEXT_OPERATION);

             StaticApplicationContext sContext = getPolicyContext ();
             sContext.setOperationIdentifier (operation);

             SecurityPolicy policy = _sConfig.getSecurityConfiguration (sContext);

	     ProcessingContext context = new ProcessingContext ();
	     copyToProcessingContext (context, messageContext);

	     context.setPolicyContext (sContext);

             if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                 context.setSecurityPolicy(
                    ((DeclarativeSecurityConfiguration)policy).receiverSettings());
             } else {
                 context.setSecurityPolicy(policy);
             }

	     context.setSecurityEnvironment (_securityEnvironment);
	     context.isInboundMessage (true);
             
             if (_sConfig.retainSecurityHeader()) {
                 context.retainSecurityHeader(true);
             }
             
             if (_sConfig.resetMustUnderstand()) {
                 context.resetMustUnderstand(true);
             }

             SecurityRecipient.validateMessage (context);

             copyToMessageContext(messageContext, context);

        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
            throw getSOAPFaultException(soapFaultException);
	} catch (com.sun.xml.wss.XWSSecurityException xwse) {
            QName qname = null;

            if (xwse.getCause() instanceof PolicyViolationException)
                qname = MessageConstants.WSSE_RECEIVER_POLICY_VIOLATION;
            else
                qname = MessageConstants.WSSE_FAILED_AUTHENTICATION;

	        com.sun.xml.wss.impl.WssSoapFaultException wsfe =
		                SecurableSoapMessage.newSOAPFaultException(
			                    qname, xwse.getMessage(), xwse);
                //TODO: MISSING-LOG
	        throw getSOAPFaultException(wsfe);
     	}
    }

    public boolean _preRequestSendingHook (StreamingSenderState state)
    throws Exception {
        try {
             SOAPMessageContext messageContext = state.getMessageContext();
             SOAPMessage message = state.getRequest().getMessage();

             String operation = getOperationName(message);
             messageContext.setProperty(CONTEXT_OPERATION, operation);

             StaticApplicationContext sContext = getPolicyContext();
             sContext.setOperationIdentifier (operation);

             SecurityPolicy policy = _sConfig.getSecurityConfiguration (sContext);

             ProcessingContext context = new ProcessingContext ();
             copyToProcessingContext (context, messageContext);

             context.setPolicyContext (sContext);

             if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                 context.setSecurityPolicy(
                    ((DeclarativeSecurityConfiguration)policy).senderSettings());
             } else {
                 context.setSecurityPolicy(policy);
             }

             context.setSecurityEnvironment (_securityEnvironment);
	     context.isInboundMessage (false);

             SecurityAnnotator.secureMessage (context);

             copyToMessageContext(messageContext, context);
        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
            throw getSOAPFaultException(soapFaultException);
        } catch (com.sun.xml.wss.XWSSecurityException xwse) {
                // log the exception here
	        throw new JAXRPCException(xwse);
        }

        return true;
    }

    private static final String ENCRYPTED_BODY_QNAME = 
        "{" + MessageConstants.XENC_NS + "}" + MessageConstants.ENCRYPTED_DATA_LNAME;

    public boolean preHandlingHook (StreamingHandlerState state)
    throws Exception {
        try {
            SOAPMessageContext messageContext = state.getMessageContext();
            SOAPMessage message = state.getRequest().getMessage();

            StaticApplicationContext sContext = new StaticApplicationContext (getPolicyContext());
            ProcessingContext context = new ProcessingContext();

            copyToProcessingContext (context, messageContext);
            String operation = getOperationName (message);

            if (operation.equals(ENCRYPTED_BODY_QNAME) && _sConfig.hasOperationPolicies()) {

	       // get enclosing port level configuration
               if (MessageConstants.debug) {
                   System.out.println("context in plugin= " + sContext.toString()); 
               }
	       ApplicationSecurityConfiguration config =
	           (ApplicationSecurityConfiguration) 
                       _sConfig.getSecurityPolicies(sContext).next();

	       if (config != null) {
                   context.setPolicyContext (sContext);
                   context.setSecurityPolicy (config);
	       } else {
                   ApplicationSecurityConfiguration config0 =
              	       (ApplicationSecurityConfiguration) _sConfig.
        	   getAllTopLevelApplicationSecurityConfigurations().iterator().next();

                   //sContext.setPortIdentifier ("");
                    context.setPolicyContext (sContext);
                    context.setSecurityPolicy (config0);
               }
           } else {
	       sContext.setOperationIdentifier(operation);
               messageContext.setProperty(CONTEXT_OPERATION, operation);
	       SecurityPolicy policy = _sConfig.getSecurityConfiguration (sContext);

	       context.setPolicyContext (sContext);

               if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                   context.setSecurityPolicy(
                       ((DeclarativeSecurityConfiguration)policy).receiverSettings());
               } else {
                   context.setSecurityPolicy(policy);
               }
	   }

           context.setSecurityEnvironment (_securityEnvironment);
	   context.isInboundMessage (true);

           if (_sConfig.retainSecurityHeader()) {
                 context.retainSecurityHeader(true);
           }
           
           SecurityRecipient.validateMessage (context);

           messageContext.setProperty(CONTEXT_OPERATION, getOperationName (message));

           copyToMessageContext (messageContext, context);
        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
            state.getResponse().setFailure(true);
            throw getSOAPFaultException(soapFaultException);

        } catch (com.sun.xml.wss.XWSSecurityException xwse) {
            QName qname = null;

            if (xwse.getCause() instanceof PolicyViolationException)
                qname = MessageConstants.WSSE_RECEIVER_POLICY_VIOLATION;
            else
                qname = MessageConstants.WSSE_FAILED_AUTHENTICATION;

	        com.sun.xml.wss.impl.WssSoapFaultException wsfe =
		                 SecurableSoapMessage.newSOAPFaultException(
			                       qname, xwse.getMessage(), xwse);
            //TODO: MISSING-LOG
            state.getResponse().setFailure(true);

            throw getSOAPFaultException(wsfe);
        }

        return true;
    }

    public void postResponseWritingHook (StreamingHandlerState state)
    throws Exception {
        try {
	    SOAPMessageContext messageContext = state.getMessageContext();
	    SOAPMessage message = state.getResponse().getMessage();

	    ProcessingContext context = new ProcessingContext ();

	    copyToProcessingContext(context, messageContext);

	    if (state.getResponse().isFailure()) {
	        DumpFilter.process (context);
	        return;
	    }

	    String operation = (String) messageContext.getProperty(CONTEXT_OPERATION);

            StaticApplicationContext sContext = new StaticApplicationContext (getPolicyContext());
            sContext.setOperationIdentifier (operation);

	    SecurityPolicy policy = _sConfig.getSecurityConfiguration (sContext);
	    context.setPolicyContext (sContext);

            if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                context.setSecurityPolicy(
                   ((DeclarativeSecurityConfiguration)policy).senderSettings());
            } else {
                context.setSecurityPolicy(policy);
            }

            context.setSecurityEnvironment (_securityEnvironment);
	    context.isInboundMessage (false);

            SecurityAnnotator.secureMessage (context);

            copyToMessageContext(messageContext, context);
        } catch (com.sun.xml.wss.impl.WssSoapFaultException soapFaultException) {
             throw getSOAPFaultException(soapFaultException);
        } catch (com.sun.xml.wss.XWSSecurityException xwse) {
             //TODO: MISSING-LOG
             com.sun.xml.wss.impl.WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                        xwse.getMessage(), xwse);
             throw getSOAPFaultException(wsfe);
        }
    }

    public void prepareMessageForMUCheck (SOAPMessage message)
    throws Exception {
        setMUValue(message, "0");
    }

    public void restoreMessageAfterMUCheck (SOAPMessage message)
    throws Exception {
        setMUValue(message, "1");
    }

    private void setMUValue (SOAPMessage message, String value)
    throws Exception {
        SOAPPart sp = message.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPHeader sh = se.getHeader();

        if (sh != null) {

            SOAPElement secHeader = null;
            Node currentChild = sh.getFirstChild();

            while (currentChild != null && !(currentChild.getNodeType() ==
			Node.ELEMENT_NODE)){
                currentChild = currentChild.getNextSibling();
            }

            if (currentChild != null /* && currentChild instanceof SOAPElement*/) {
                if (MessageConstants.WSSE_SECURITY_LNAME.equals(
                        currentChild.getLocalName()) &&
                    MessageConstants.WSSE_NS.equals(
                        currentChild.getNamespaceURI())) {

                    secHeader = (SOAPElement) currentChild;
                }
            }

            if (secHeader != null) {
                // change the mustUnderstand value to false
                Attr attr = secHeader.getAttributeNodeNS(
                        MessageConstants.SOAP_1_1_NS,
                        "mustUnderstand");

                if (attr != null)
                    secHeader.setAttributeNS(attr.getNamespaceURI(), attr
                            .getName(), value);
            }
        }
    }

    public SOAPFaultException getSOAPFaultException (WssSoapFaultException sfe) {
        return new SOAPFaultException(
            sfe.getFaultCode(),
            sfe.getFaultString(),
            sfe.getFaultActor(),
            sfe.getDetail());
    }

    /**
     * Handles rpc-lit, rpc-encoded, doc-lit wrap/non-wrap, doc-encoded modes as follows:
     *
     * (1) rpc-lit, rpc-enc, doc-lit (wrap), doc-enc: First child of SOAPBody to contain Operation Identifier
     * (2) doc-lit (non-wrap): Operation Identifier constructed as concatenated string
     *                         of tag names of childs of SOAPBody
     */
    private String getOperationName (SOAPMessage message)
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

		return operation.substring(0, operation.length()-1);
    }
}

