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
 * ProcessingFilter.java
 *
 * Created on March 13, 2006, 4:15 PM
 *
 * @author Mike Grogan
 */

package com.sun.xml.ws.rm.jaxws.util;
import com.sun.xml.ws.rm.Message;

/**
 * Implementing classes provide access to all RM headers on Inbound and
 * Outbound messages in JAX-WS pipeline and provide a way to simulate dropped
 * requests and responses in a JAX-WS client.
 * <p>
 * Each method takes a com.sun.xml.ws.rm.Message argument.  The RM headers
 * can be accessed using the getSequenceElement, getAckRequestedElement and
 * getSequenceAcknowledgement methods, which return com.sun.xml.ws.rm.protocol
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
    public boolean handleClientRequestMessage(Message mess);
    
     /**
     * Use to inspect the RM headers on a client response message.  
     *
     * @param mess The response message.
     * @return true if processing should continue, 
     *         false if message should be "lost"
     */   
    public boolean handleClientResponseMessage(Message mess);
    
    
    /**
     * Use to inspect the RM headers on an Endpoint request message.  
     * The return value determines whether the message is processed normally 
     * or "lost".
     *
     * @param mess The request message.
     *   
     */                              
    public void handleEndpointRequestMessage(Message mess);
    
     /**
     * Use to inspect the RM headers on an Endpoint response message.  
     *
     * @param mess The response message.
     */   
    public boolean handleEndpointResponseMessage(Message mess);
    
    /**
     * Use to inspect or modify headers of outgoing message just before they are
     * marshalled.  Use message.getSequenceElement(), 
     * message.getSequenceAcknowledgementElement and message.getAckRequested element to
     * obtain the headers.
     *
     * @param The message to be modified.
     */
    public void handleOutboundHeaders(Message mess);
 
}
