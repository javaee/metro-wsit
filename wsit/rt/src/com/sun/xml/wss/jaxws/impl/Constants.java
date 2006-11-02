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

package com.sun.xml.wss.jaxws.impl;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */
public abstract class Constants {
    protected static final String OPERATION_SCOPE = "operation-policy-scope".intern();
    protected static final String BINDING_SCOPE = "binding-policy-scope".intern();
    protected static final String rstSCTURI = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/SCT".intern();
    protected static final String rstrSCTURI = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/SCT".intern();
    protected static final String rstTrustURI = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue".intern();
    protected static final String rstrTrustURI = "http://schemas.xmlsoap.org/ws/2005/02/trust/RSTR/Issue".intern();
    protected static final String wsaURI = "http://schemas.xmlsoap.org/ws/2004/08/addressing".intern();
    protected static final String SC_ASSERTION = "SecureConversationAssertion".intern();
    protected static final QName ACTION_HEADER = new QName(wsaURI,"Action");
    protected final static QName _SecureConversationToken_QNAME =
            new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SecureConversationToken");
    protected static final String SECURITY_POLICY_2005_07_NAMESPACE=
            "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy".intern();
    protected static final String TRUST_2005_02_NAMESPACE ="http://schemas.xmlsoap.org/ws/2005/02/trust".intern();
    private static final String ADDRESSING_POLICY_NAMESPACE_URI =
            "http://schemas.xmlsoap.org/ws/2004/09/policy/addressing";
    protected static final String XENC_NS = "http://www.w3.org/2001/04/xmlenc#";
    protected static final String ENCRYPTED_DATA_LNAME = "EncryptedData";

    protected static final QName MESSAGE_ID_HEADER = new QName(wsaURI,"MessageID");
    protected static final List<PolicyAssertion> EMPTY_LIST = Collections.emptyList();
    
    protected static QName bsOperationName =
            new QName("http://schemas.xmlsoap.org/ws/2005/02/trust","RequestSecurityToken");

    protected static final String SUN_WSS_SECURITY_SERVER_POLICY_NS="http://schemas.sun.com/2006/03/wss/server";
    protected static final String SUN_WSS_SECURITY_CLIENT_POLICY_NS="http://schemas.sun.com/2006/03/wss/client";
    protected static final String RM_CREATE_SEQ= "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequence";
    protected static final String RM_CREATE_SEQ_RESP= "http://schemas.xmlsoap.org/ws/2005/02/rm/CreateSequenceResponse";
    protected static final String RM_SEQ_ACK = "http://schemas.xmlsoap.org/ws/2005/02/rm/SequenceAcknowledgement";
    protected static final String RM_TERMINATE_SEQ = "http://schemas.xmlsoap.org/ws/2005/02/rm/TerminateSequence";
    protected static final String RM_LAST_MESSAGE= "http://schemas.xmlsoap.org/ws/2005/02/rm/LastMessage";
    protected static final String JAXWS_21_MESSAGE = "JAXWS_2_1_MESSAGE";

    
}



