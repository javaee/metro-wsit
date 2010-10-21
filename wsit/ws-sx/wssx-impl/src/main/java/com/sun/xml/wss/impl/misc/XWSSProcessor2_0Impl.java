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

package com.sun.xml.wss.impl.misc;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import java.io.InputStream;

import javax.xml.soap.SOAPMessage;
import javax.security.auth.callback.CallbackHandler;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;
import com.sun.xml.wss.impl.config.SecurityConfigurationXmlReader;

import com.sun.xml.wss.impl.SecurityRecipient;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.SecurityEnvironment;


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
            secEnv = new DefaultSecurityEnvironmentImpl(this.handler);
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
        
        if (declSecConfig.retainSecurityHeader()) {
            context.retainSecurityHeader(true);
        }
        
        if (declSecConfig.resetMustUnderstand()) {
            context.resetMustUnderstand(true);
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
