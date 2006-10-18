/*
 * Copyright (c) 2006 Sun Microsystems, Inc.
 * All rights reserved.
 */
package wssc.sc28.server;
import javax.xml.ws.Holder;


@javax.jws.WebService (endpointInterface="wssc.sc28.server.IPingService")
@javax.xml.ws.BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
public class PingImpl implements IPingService {
    
   public void ping( Holder<String> scenario,
         Holder<String> origin,
         Holder<String> text){
        System.out.println("The message is here : " + scenario.value + " " +origin.value + " " + text.value);

    }                    
}
