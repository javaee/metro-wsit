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

package com.sun.xml.ws.rm;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;

/**
 * Class contains  constants for faults defined by the 02/2005 version of the
 * WS-RM specification.
 * @author Bhakti Mehta
 */
public class Constants {
    
    
   

    
    
    /**
     * Name of Sender fault defined by SOAP 1.2.
     */
    public static final QName SOAP12_SENDER_QNAME = SOAPConstants.SOAP_SENDER_FAULT;

    /*public static final String PROTOCOL_PACKAGE_NAME="com.sun.xml.ws.rm.protocol";*/



    /*public static final String CREATE_SEQUENCE_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence";

    public static final String TERMINATE_SEQUENCE_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/TerminateSequence";

    public static final String ACK_REQUESTED_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/AckRequested";

    public static final String LAST_MESSAGE_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/LastMessage";

    public static final String CREATE_SEQUENCE_RESPONSE_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse";

    public static final String SEQUENCE_ACKNOWLEDGEMENT_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement";*/
    /*
    * Policy namespaces.
    */
    public  static final String version = RMVersion.WSRM10.policyNamespaceUri;

    public static final String microsoftVersion = "http://schemas.microsoft.com/net/2005/02/rm/policy";

    public  static final String sunVersion = "http://sun.com/2006/03/rm";
    
    public  static final String sunClientVersion = "http://sun.com/2006/03/rm/client";
    
     /*
     * RequestContext / MessageContext property names
     */
    public static final String sequenceProperty = "com.sun.xml.ws.sequence";
    
    public static final String messageNumberProperty = "com.sun.xml.ws.messagenumber";
    
    public static final String createSequenceProperty = "com.sun.xml.ws.createsequence";
    /**
     * Constants used by RMSource.createSequence
     */
    public static final String createSequencePayload = 
            "<sun:createSequence xmlns:sun=\"http://com.sun/createSequence\"/>";
    
    public static final String createSequenceNamespace = 
            "http://com.sun/createSequence";
    
    
    //INTERNATIONALIZE THESE
    
    public static final String MESSAGE_NUMBER_ROLLOVER_TEXT = "The maximum value, %s,  for MessageNumber has been exceeded";
    
     
     public static final String UNKNOWN_SEQUENCE_TEXT = "The message contains an unknown sequence id,  %s ";
     
     public static final String CREATE_SEQUENCE_REFUSED_TEXT = "The create sequence request has been refused by RM Destination -- %s ";

     public static final String SEQUENCE_TERMINATED_TEXT = "The sequence has been terminated because of an unrecoverable error";


}