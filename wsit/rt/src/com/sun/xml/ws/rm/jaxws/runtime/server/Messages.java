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
