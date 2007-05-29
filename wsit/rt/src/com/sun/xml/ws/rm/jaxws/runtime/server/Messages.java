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

package com.sun.xml.ws.rm.jaxws.runtime.server;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * @author Bhakti Mehta
 */
enum Messages {
    INCORRECT_ADDRESSING_HEADERS, // 0 args
    ACKNOWLEDGEMENT_MESSAGE_EXCEPTION, //0 args
    CREATESEQUENCE_HEADER_PROBLEM ,//0 args
    ACKSTO_NOT_EQUAL_REPLYTO ,//2 args
    SECURITY_TOKEN_AUTHORIZATION_ERROR, // 2 args
    SECURITY_REFERENCE_ERROR ,//1 arg
    NULL_SECURITY_TOKEN ,//0 arg
    TERMINATE_SEQUENCE_EXCEPTION, // 0 arg
    INVALID_LAST_MESSAGE, //0 args
    LAST_MESSAGE_EXCEPTION ,//0 args
    INVALID_ACK_REQUESTED ,//0 args
    ACK_REQUESTED_EXCEPTION, //0 args
    INVALID_SEQ_ACKNOWLEDGEMENT, //0args
    SEQ_ACKNOWLEDGEMENT_EXCEPTION , //0 args
    INVALID_CREATE_SEQUENCE_RESPONSE , //0 args
    CREATE_SEQUENCE_CORRELATION_ERROR , //0 args
    SECURITY_TOKEN_MISMATCH, //0 args
    NOT_RELIABLE_SEQ_OR_PROTOCOL_MESSAGE ,//0args
    NON_RM_REQUEST_OR_MISSING_WSA_ACTION_HEADER, //0 args
    INVALID_OR_MISSING_TO_ON_CS_MESSAGE, //0 args
    COULD_NOT_RESET_MESSAGE //2 args
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(Messages.class.getName());

    public String toString() {
        return format();
    }

     /** Loads a string resource and formats it with specified arguments. */
    public String format( Object... args ) {
        return MessageFormat.format( rb.getString(name()), args );
    }
}
