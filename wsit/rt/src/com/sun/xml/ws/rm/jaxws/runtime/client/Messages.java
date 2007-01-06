package com.sun.xml.ws.rm.jaxws.runtime.client;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * @author Bhakti Mehta
 */
enum Messages {
    UNCHANGEABLE_ENDPOINT_ADDRESS, // 0 args
    INVALID_DEST_URI, //1 arg
    INVALID_ACKS_TO_URI, //1 arg
    UNEXPECTED_TRY_SEND_EXCEPTION,
    UNEXPECTED_WRAPPED_EXCEPTION,
    UNEXPECTED_PROCESS_EXCEPTION,
    UNEXPECTED_PREDESTROY_EXCEPTION,
    UNSUPPORTED_ADDRESSING_VERSION,
    HEARTBEAT_MESSAGE_EXCEPTION,
    HEARTBEAT_MESSAGE_MESSAGE, //2 arg
    ADDING_SEQUENCE_MESSAGE, //1 arg
    REMOVING_SEQUENCE_MESSAGE, //1 arg
    NO_SUCH_OUTBOUND_SEQUENCE, //1 arg
    NO_TWO_WAY_OPERATION,
    NO_INBOUND_SEQUENCE_ID_SPECIFIED,
    SEQUENCE_ALREADY_EXISTS //1 arg

    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(com.sun.xml.ws.rm.jaxws.runtime.client.Messages.class.getName());

    public String toString() {
        return format();
    }

     /** Loads a string resource and formats it with specified arguments. */
    public String format( Object... args ) {
        return MessageFormat.format( com.sun.xml.ws.rm.jaxws.runtime.client.Messages.rb.getString(name()), args );
    }
}
