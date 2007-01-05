package com.sun.xml.ws.rm.protocol;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * @author Bhakti Mehta
 * @author Mike Grogan
 */
enum Messages {
    
    ACKREQUESTED_TOSTRING_STRING, //2 args
    ACKNOWLEDGEMENT_MESSAGE, //2 args
    BOTH_ACKS_AND_NACKS_MESSAGE, //0 args
    UPPERBOUND_LESSTHAN_LOWERBOUND_MESSAGE, //0 args
    SEQUENCE_ACKNOWLEDGEMENT_TOSTRING_STRING, //2 args
    SEQUENCE_TOSTRING_STRING //3 args

    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(com.sun.xml.ws.rm.protocol.Messages.class.getName());

    public String toString() {
        return format();
    }

     /** Loads a string resource and formats it with specified arguments. */
    public String format( Object... args ) {
        return MessageFormat.format( com.sun.xml.ws.rm.protocol.Messages.rb.getString(name()), args );
    }
}
