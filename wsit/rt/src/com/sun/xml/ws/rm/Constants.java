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

package com.sun.xml.ws.rm;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;

/**
 * Class contains  constants for faults defined by the 02/2005 version of the
 * WS-RM specification.
 * @author Bhakti Mehta
 */
public class Constants {

    public static final String WS_RM_NAMESPACE = "http://schemas.xmlsoap.org/ws/2005/02/rm";

    public static final QName MESSAGE_NUMBER_ROLLOVER_QNAME = new QName(WS_RM_NAMESPACE,"MessageNumberRollover");

    public static final String MESSAGE_NUMBER_ROLLOVER_TEXT = "The maximum value %s for "+ WS_RM_NAMESPACE +":MessageNumber has been exceeded";

    public static final QName UNKNOWN_SEQUENCE_QNAME = new QName(WS_RM_NAMESPACE,"UnknownSequence");

    public static final String UNKNOWN_SEQUENCE_TEXT = "The message contains an unknown sequence id %s ";
    /**
     * Name of Sender fault defined by SOAP 1.2.
     */
    public static final QName SOAP12_SENDER_QNAME = SOAPConstants.SOAP_SENDER_FAULT;

    public static final String PROTOCOL_PACKAGE_NAME="com.sun.xml.ws.rm.protocol";

    public static final QName CREATE_SEQUENCE_REFUSED_QNAME = new QName(WS_RM_NAMESPACE,"CreateSequenceRefused");

    public static final QName SEQUENCE_TERMINATED_QNAME = new QName(WS_RM_NAMESPACE,"SequenceTerminated");

    public static final String CREATE_SEQUENCE_REFUSED_TEXT = "The create sequence request has been refused by RM Destination %s ";

    public static final String SEQUENCE_TERMINATED_TEXT = "The sequence has been terminated because of an unrecoverable error";

    public static final String CREATE_SEQUENCE_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence";

    public static final String TERMINATE_SEQUENCE_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/TerminateSequence";

    public static final String ACK_REQUESTED_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/AckRequested";

    public static final String LAST_MESSAGE_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/LastMessage";

    public static final String CREATE_SEQUENCE_RESPONSE_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse";

    public static final String SEQUENCE_ACKNOWLEDGEMENT_ACTION="http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement";
    /*
    * Policy namespaces.
    */
    public  static final String version = "http://schemas.xmlsoap.org/ws/2005/02/rm/policy";

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

}