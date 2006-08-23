/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package xwss.s2.server;


@javax.jws.WebService (endpointInterface="xwss.s2.server.IPingService")
@javax.xml.ws.BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
public class PingImpl implements IPingService {
    
   public String ping(String message) {
        System.out.println("The message is here : " + message);
        return message;
    }                    
}
