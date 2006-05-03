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

package com.sun.xml.wss.impl.misc;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.soap.SOAPMessage;
import javax.security.auth.callback.CallbackHandler;


import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;
import com.sun.xml.wss.impl.config.SecurityConfigurationXmlReader;

import com.sun.xml.wss.impl.SecurityRecipient;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.ProcessingContext;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;


public class XWSSProcessor2_0Impl implements XWSSProcessor {

    private DeclarativeSecurityConfiguration declSecConfig = null;
    private CallbackHandler handler = null;
    private SecurityEnvironment secEnv = null;
    
    protected XWSSProcessor2_0Impl(
        InputStream securityConfig, CallbackHandler handler) 
        throws XWSSecurityException {
        try {
            declSecConfig = 
                SecurityConfigurationXmlReader.createDeclarativeConfiguration(securityConfig);
            this.handler = handler;
            secEnv = new DefaultSecurityEnvironmentImpl(handler);
        }catch (Exception e) {
            // log
            throw new XWSSecurityException(e);
        }
    }


    protected XWSSProcessor2_0Impl(
        InputStream securityConfig) throws XWSSecurityException {
        throw new UnsupportedOperationException("Operation Not Supported");
    }

    public SOAPMessage secureOutboundMessage(
        ProcessingContext context) 
        throws XWSSecurityException {

        //resolve the policy first
        MessagePolicy resolvedPolicy = null;

        if (declSecConfig != null) {
            resolvedPolicy = declSecConfig.senderSettings();
        } else {
            //log
            throw new XWSSecurityException("Security Policy Unknown");
        }
                                                                                                      
        if (resolvedPolicy == null) {
            // log that no outbound security specified ?
            return context.getSOAPMessage();
        }

        if (context.getHandler() == null  && context.getSecurityEnvironment() == null) {
            context.setSecurityEnvironment(secEnv);
        }

        context.setSecurityPolicy(resolvedPolicy);
 
        try {
            SecurityAnnotator.secureMessage(context);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }

        try {
            SOAPMessage msg = context.getSOAPMessage();
            //System.out.println("\n Secure Message Start .........\n\n");
            //msg.writeTo(System.out);
            //System.out.println("\n Secure Message End .........\n\n");
            return msg;
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }

    }

    public SOAPMessage verifyInboundMessage(
        ProcessingContext context) 
        throws XWSSecurityException {

        MessagePolicy resolvedPolicy = null;

        if (declSecConfig != null) {
            resolvedPolicy = declSecConfig.receiverSettings();
        } else {
            //log
            throw new XWSSecurityException("Security Policy Unknown");
        }

        if (context.getHandler() == null  && context.getSecurityEnvironment() == null) {
            context.setSecurityEnvironment(secEnv);
        }

        context.setSecurityPolicy(resolvedPolicy);
        try {
            SecurityRecipient.validateMessage(context);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }

        try {
            SOAPMessage msg = context.getSOAPMessage();
            //System.out.println("\n Verified Message Start .........\n\n");
            //msg.writeTo(System.out);
            //System.out.println("\n Verified Message End .........\n\n");
            return msg;
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }

    }

    public ProcessingContext createProcessingContext(SOAPMessage msg) throws XWSSecurityException {
        ProcessingContext cntxt = new ProcessingContext();
        cntxt.setSOAPMessage(msg);
        return cntxt;
    }
}
