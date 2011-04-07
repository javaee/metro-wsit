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

package com.sun.xml.ws.tx.at;

import javax.xml.namespace.QName;

public interface WSATConstants {

    // general
    static final String SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope";
    static final String MUST_UNDERSTAND = "mustUnderstand";
    static final String MESSAGE_ID = "MessageID";
    // addressing
    static final String WSADDRESSING_NS_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    static final String WSA = "wsa";
    static final String REFERENCE_PARAMETERS = "ReferenceParameters";
    static final String TO = "To";
    static final String ADDRESS = "Address";
    static final String REPLY_TO = "ReplyTo";
    static final String FROM = "From";
    static final String FAULT_TO = "FaultTo";
    static final String ACTION = "Action";
    // coor
    static final String WSCOOR10_NS_URI = "http://schemas.xmlsoap.org/ws/2004/10/wscoor";
    static final String CURRENT_WSCOOR = WSCOOR10_NS_URI;
    static final String WSCOOR = "wscoor";
    static final String COORDINATION_TYPE = "CoordinationType";
    static final String REGISTRATION_SERVICE = "RegistrationService";
    static final String EXPIRES = "Expires";
    static final String IDENTIFIER = "Identifier";
    static final String PARTICIPANT_PROTOCOL_SERVICE = "ParticipantProtocolService";
    static final String PROTOCOL_IDENTIFIER = "ProtocolIdentifier";
    static final String REGISTER = "Register";
    static final String REGISTER_RESPONSE = "RegisterResponse";
    static final String COORDINATION_CONTEXT = "CoordinationContext";
    static final String COORDINATOR_PROTOCOL_SERVICE = "CoordinatorProtocolService";
    static final QName WSCOOR_CONTEXT_QNAME = new QName(WSCOOR10_NS_URI, COORDINATION_CONTEXT);
    static final QName WSCOOR_REGISTER_QNAME = new QName(WSCOOR10_NS_URI, REGISTER);

    static final String WSCOOR11_NS_URI = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06";
    static final QName WSCOOR11_CONTEXT_QNAME = new QName(WSCOOR11_NS_URI, COORDINATION_CONTEXT);
    static final QName WSCOOR11_REGISTER_QNAME = new QName(WSCOOR11_NS_URI, REGISTER);

    // at
    static final String HTTP_SCHEMAS_XMLSOAP_ORG_WS_2004_10_WSAT = "http://schemas.xmlsoap.org/ws/2004/10/wsat";
    static final String WSAT10_NS_URI = "http://schemas.xmlsoap.org/ws/2004/10/wsat";
    static final String WSAT = "wsat";
    static final String DURABLE_2PC = "Durable2PC";
    static final String VOLATILE_2PC = "Volatile2PC";
    public static final String PREPARE = "Prepare";
    public static final String COMMIT = "Commit";
    public static final String ROLLBACK = "Rollback";
    static final String PREPARED = "Prepared";
    static final String READONLY = "ReadOnly";
    static final String COMMITTED = "Committed";
    static final String ABORTED = "Aborted";
    static final String REPLAY = "Replay";
    static final String HTTP_SCHEMAS_XMLSOAP_ORG_WS_2004_10_WSAT_DURABLE_2PC = WSAT10_NS_URI + "/" + DURABLE_2PC;
    static final String HTTP_SCHEMAS_XMLSOAP_ORG_WS_2004_10_WSAT_VOLATILE_2PC = WSAT10_NS_URI + "/" + VOLATILE_2PC;
    public String WLA_WSAT_NS_URI = "http://com.sun.xml.ws.tx.at/ws/2008/10/wsat";
    static final String WSAT_WSAT = "wsat-wsat";
    static final String WSAT_CONTEXT_ROOT = "__wstx-services";
    static final String TXID = "txId";
    static final QName TXID_QNAME = new QName(WLA_WSAT_NS_URI, TXID, WSAT_WSAT);
    static final String BRANCHQUAL = "branchQual";
    static final QName BRANCHQUAL_QNAME = new QName(WLA_WSAT_NS_URI, BRANCHQUAL, WSAT_WSAT);
    static final String ROUTING = "routing";
    static final QName ROUTING_QNAME = new QName(WLA_WSAT_NS_URI, ROUTING, WSAT_WSAT);
    // outbound endpoints
    public static final String WSAT_COORDINATORPORTTYPEPORT = "/"+ WSAT_CONTEXT_ROOT +"/CoordinatorPortType";
    public static final String WSAT_REGISTRATIONCOORDINATORPORTTYPEPORT = "/"+ WSAT_CONTEXT_ROOT +"/RegistrationPortTypeRPC";
    // inbound endpoints
    public static final String WSAT_REGISTRATIONREQUESTERPORTTYPEPORT = "/"+ WSAT_CONTEXT_ROOT +"/RegistrationRequesterPortType";
    //RegistrationRequesterPortTypeRPC";
    public static final String WSAT_PARTICIPANTPORTTYPEPORT = "/"+ WSAT_CONTEXT_ROOT +"/ParticipantPortType";
    // logger
    public static final String DEBUG_WSAT = "DebugWSAT";

    static final String WSAT11_NS_URI = "http://docs.oasis-open.org/ws-tx/wsat/2006/06";
    static final String WSAT11_DURABLE_2PC = WSAT11_NS_URI + "/" + DURABLE_2PC;
    static final String WSAT11_VOLATILE_2PC = WSAT11_NS_URI + "/" + VOLATILE_2PC;
    public static final String WSAT11_REGISTRATIONCOORDINATORPORTTYPEPORT = "/"+ WSAT_CONTEXT_ROOT +"/RegistrationPortTypeRPC11";
    public static final String WSAT11_PARTICIPANTPORTTYPEPORT = "/"+ WSAT_CONTEXT_ROOT +"/ParticipantPortType11";
    public static final String WSAT11_COORDINATORPORTTYPEPORT = "/"+ WSAT_CONTEXT_ROOT +"/CoordinatorPortType11";
    // inbound endpoints
    public static final String WSAT11_REGISTRATIONREQUESTERPORTTYPEPORT = "/"+ WSAT_CONTEXT_ROOT +"/RegistrationRequesterPortType11";
    //  TM
    public static final String TXPROP_WSAT_FOREIGN_RECOVERY_CONTEXT = "com.sun.xml.ws.tx.foreignContext";
    // tube request map storage
    public static final String WSAT_TRANSACTION = "wsat.transaction";
    public static final String WSAT_TRANSACTION_XID = "wsat.transaction.xid";
}
