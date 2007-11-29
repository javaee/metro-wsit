/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rm.jaxws.util;

import com.sun.xml.ws.rm.RMMessage;

/**
 * Implementing classes provide access to all RM headers on Inbound and
 * Outbound messages in JAX-WS pipeline and provide a way to simulate dropped
 * requests and responses in a JAX-WS client.
 * <p>
 * Each method takes a com.sun.xml.ws.rm.Message argument.  The RM headers
 * can be accessed using the getSequenceElement, getAckRequestedElement and
 * getSequenceAcknowledgement methods, which return com.sun.xml.ws.rm.v200502
 * objects.
 * <p>
 * The return values of handleClientRequestMessage and 
 * handleEndpointResponseMessage methods determine whether the request or response messages 
 * message should be processed or "lost".
 * <p>
 * The RM system will look for the name of a class that implements 
 * ProcessingFilter by calling the static method:
 * <p>  
 * <t><t> ProcessingFilter com.sun.xml.rm.jaxws.runtime.RMProvider.getProcessingFilter().
 * <p>
 * If non-null, the returned ProcessingFilter will be used.  The instance of ProcessingFilter
 * can be set by a test application using the static method:
 * <p>
 * <t><t> void com.sun.xml.rm.jaxws.runtime.RMProvider.setProcessingFilter(ProcessingFilter filter);
 * 
 * 
 */
public interface ProcessingFilter {

    /**
     * Use to inspect the RM headers on a client request message.  
     * The return value determines whether the message is processed normally 
     * or "lost".
     *
     * @param mess The request message.
     * @return true if processing should continue, 
     *         false if message should be "lost"
     */
    public boolean handleClientRequestMessage(RMMessage mess);

    /**
     * Use to inspect the RM headers on a client response message.  
     *
     * @param mess The response message.
     * @return true if processing should continue, 
     *         false if message should be "lost"
     */
    public boolean handleClientResponseMessage(RMMessage mess);

    /**
     * Use to inspect the RM headers on an Endpoint request message.  
     * The return value determines whether the message is processed normally 
     * or "lost".
     *
     * @param mess The request message.
     *   
     */
    public void handleEndpointRequestMessage(RMMessage mess);

    /**
     * Use to inspect the RM headers on an Endpoint response message.  
     *
     * @param mess The response message.
     */
    public boolean handleEndpointResponseMessage(RMMessage mess);

    /**
     * Use to inspect or modify headers of outgoing message just before they are
     * marshalled.  Use message.getSequenceElement(), 
     * message.getSequenceAcknowledgementElement and message.getAckRequested element to
     * obtain the headers.
     *
     * @param The message to be modified.
     */
    public void handleOutboundHeaders(RMMessage mess);
}
