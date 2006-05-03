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

package com.sun.xml.wss;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.soap.SOAPMessage;
import javax.security.auth.callback.CallbackHandler;


/**
 *<code>XWSSProcessor</code> interface defines methods for
 *<UL>
 *    <LI> Securing an outbound <code>SOAPMessage</Code>
 *    <LI> Verifying the security in an inbound <code>SOAPMessage</code>
 *</UL>
 *
 * An <code>XWSSProcessor</code> can add/verify Security in a 
 * <code>SOAPMessage</code> as defined by the OASIS WSS 1.0 specification.
 */
public interface XWSSProcessor {

    /**
     *  Adds Security to an outbound <code>SOAPMessage</Code> according to
     *  the Security Policy inferred from the <code>SecurityConfiguration</code>
     *  with which this <code>XWSSProcessor</code> was initialized.
     *
     * @param  messageCntxt the SOAP <code>ProcessingContext</code> containing 
     *         the outgoing  <code>SOAPMessage</code> to be secured
     *
     * @return the resultant Secure <code>SOAPMessage</code>
     *
     * @exception XWSSecurityException if there was an error in securing 
     *            the message.
     */
    public  SOAPMessage secureOutboundMessage(
        ProcessingContext messageCntxt) 
        throws XWSSecurityException;

    /**
     *  Verifies Security in an inbound <code>SOAPMessage</Code> according to
     *  the Security Policy inferred from the <code>SecurityConfiguration</code>
     *  with which this <code>XWSSProcessor</code> was initialized.
     *
     * @param  messageCntxt the SOAP <code>ProcessingContext</code> containing the 
     *         outgoing  <code>SOAPMessage</code> to be secured
     *
     * @return the resultant <code>SOAPMessage</code> after successful
     *         verification of security in the message
     * 
     *
     * @exception XWSSecurityException if there was an unexpected error 
     *     while verifying the message.OR if the security in the incoming     
     * message violates the Security policy that was applied to the message.
     * @exception WssSoapFaultException when security in the incoming message
     *     is in direct violation of the OASIS WSS specification. 
     *     When a WssSoapFaultException is thrown the getFaultCode() method on it
     *     will return a <code>QName</code> which would correspond to the 
     *     WSS defined fault.
     */
    public SOAPMessage verifyInboundMessage(
        ProcessingContext messageCntxt) 
        throws XWSSecurityException;


    /**
     * Create a Processing Context initialized with the given SOAPMessage
     * @param msg the SOAPMessage with which to initialize the ProcessingContext
     * @return  A ProcessingContext instance.
     */
    public ProcessingContext createProcessingContext(SOAPMessage msg) throws XWSSecurityException ;
}
