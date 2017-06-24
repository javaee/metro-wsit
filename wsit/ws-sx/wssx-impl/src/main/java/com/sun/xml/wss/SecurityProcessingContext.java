/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.wss;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.soap.SOAPMessage;

/**
 * This interface represents a Context that is used by the XWS-Security 2.0 Runtime to
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
 */
public interface SecurityProcessingContext {
    /**
     * copy operator
     * 
     * @param ctx1 the ProcessingContext to which to copy
     * @param ctx2 the ProcessingContext from which to copy
     * @throws XWSSecurityException if there was an error during the copy operation
     */
    void copy(SecurityProcessingContext ctx1, SecurityProcessingContext ctx2) throws XWSSecurityException;

    /**
     * This method is used for internal purposes
     */
    int getConfigType();

    /**
     * Properties extraneously defined by XWSS runtime - can contain
     * application's runtime context (like JAXRPCContext etc)
     * 
     * 
     * @return Map of extraneous properties
     */
    Map getExtraneousProperties();

    /**
     * 
     * 
     * @return the value for the named extraneous property.
     */
    Object getExtraneousProperty(String name);

    /**
     * 
     * 
     * @return the CallbackHandler set for the context
     */
    CallbackHandler getHandler();

    /**
     * 
     * 
     * @return message identifier for the Message in the context
     */
    String getMessageIdentifier();

    /**
     * 
     * 
     * @return StaticPolicyContext associated with this ProcessingContext, null otherwise
     */
    StaticPolicyContext getPolicyContext();

    /**
     * 
     * 
     * @return the SOAPMessage from the context
     */
    SOAPMessage getSOAPMessage();

    /**
     * 
     * 
     * @return The SecurityEnvironment Handler set for the context
     */
    SecurityEnvironment getSecurityEnvironment();

    /**
     * 
     * 
     * @return SecurityPolicy for this context
     */
    SecurityPolicy getSecurityPolicy();

    /**
     * 
     * 
     * @return message flow direction, true if incoming, false otherwise
     */
    boolean isInboundMessage();

    /**
     * set the message flow direction (to true if inbound, false if outbound)
     * 
     * @param inBound message flow direction
     */
    void isInboundMessage(boolean inBound);

    /**
     * remove the named extraneous property if present
     * 
     * @param name the Extraneous Property to be removed
     */
    void removeExtraneousProperty(String name);

    /**
     * This method is used for internal purposes
     */
    void reset();

    /**
     * This method is used for internal purposes
     */
    void setConfigType(int type);

    /**
     * set the extraneous property into the context
     * Extraneous Properties are properties extraneously defined by XWSS runtime
     * and can contain application's runtime context (like JAXRPCContext etc)
     * 
     * @param name the property name
     * @param value the property value
     */
    void setExtraneousProperty(String name, Object value);

    /**
     * set the CallbackHandler for the context
     * 
     * @param handler The CallbackHandler
     */
    void setHandler(CallbackHandler handler);

    /**
     * Allow for message identifier to be generated externally
     * 
     * @param identifier the Message Identifier value
     */
    void setMessageIdentifier(String identifier);

    /**
     * set the StaticPolicyContext for this ProcessingContext.
     * 
     * @param context StaticPolicyContext for this context
     */
    void setPolicyContext(StaticPolicyContext context);

    /**
     * set the SOAP Message into the ProcessingContext.
     * 
     * @param message SOAPMessage
     * @throws XWSSecurityException if there was an error in setting the SOAPMessage
     */
    void setSOAPMessage(SOAPMessage message) throws XWSSecurityException;

    /**
     * set the SecurityEnvironment Handler for the context
     * 
     * @param handler The SecurityEnvironment Handler
     */
    void setSecurityEnvironment(SecurityEnvironment handler);

    /**
     * set the SecurityPolicy for the context
     * 
     * @param securityPolicy SecurityPolicy
     * @throws XWSSecurityException if the securityPolicy is of invalid type
     */
    void setSecurityPolicy(SecurityPolicy securityPolicy) throws XWSSecurityException;
    
    
}
