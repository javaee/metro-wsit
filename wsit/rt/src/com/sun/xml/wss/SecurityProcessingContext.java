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

/*
 * SecurityProcessingContext.java
 *
 * Created on January 30, 2006, 4:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.soap.SOAPMessage;

/**
 *
 * @author Vbkumar.Jayanti@Sun.COM
 * @author K.Venugopal@sun.com
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
