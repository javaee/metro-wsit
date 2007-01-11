package com.sun.xml.ws.rm.jaxws.runtime;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * @author Bhakti Mehta
 * @author Mike Grogan
 */
enum Messages {
    TIMEOUT_IN_WAITFORACKS_STRING //1 arg
  
                    
    ;

    private static final ResourceBundle rb = ResourceBundle.getBundle(com.sun.xml.ws.rm.jaxws.runtime.Messages.class.getName());

    public String toString() {
        return format();
    }

     /** Loads a string resource and formats it with specified arguments. */
    public String format( Object... args ) {
        return MessageFormat.format( com.sun.xml.ws.rm.jaxws.runtime.Messages.rb.getString(name()), args );
    }
}
