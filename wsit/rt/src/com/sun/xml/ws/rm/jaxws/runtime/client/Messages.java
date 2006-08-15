package com.sun.xml.ws.rm.jaxws.runtime.client;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * @author Bhakti Mehta
 */
enum Messages {
    UNCHANGEABLE_ENDPOINT_ADDRESS, // 0 args
    INVALID_DEST_URI, //1 arg
    INVALID_ACKS_TO_URI //1 arg

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
