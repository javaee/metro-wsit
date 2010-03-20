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

package com.sun.xml.wss;

import javax.xml.soap.SOAPMessage;


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
