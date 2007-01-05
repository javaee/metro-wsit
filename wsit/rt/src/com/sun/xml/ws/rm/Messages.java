package com.sun.xml.ws.rm;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * @author Bhakti Mehta
 * @author Mike Grogan
 */
enum Messages {
    MESSAGE_NUMBER_STRING, //1 arg
    SEQUENCE_STRING, //1 arg
    INVALID_INDEX_MESSAGE //1 arg

    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(com.sun.xml.ws.rm.Messages.class.getName());

    public String toString() {
        return format();
    }

     /** Loads a string resource and formats it with specified arguments. */
    public String format( Object... args ) {
        return MessageFormat.format( com.sun.xml.ws.rm.Messages.rb.getString(name()), args );
    }
}
