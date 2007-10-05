/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package simple.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import javax.xml.ws.Holder;

import simple.client.PingService;
import simple.client.IPingService;


import org.xmlsoap.ping.Ping;

public class PingServiceClient {
    public static void main (String[] args) {
      try {
            PingService service = new PingService();
            IPingService stub = service.getPingPort(); 
            //IPingService stub = service.getIPingService();
            
            // use static stubs to override endpoint property of WSDL       
            String serviceHost = System.getProperty("endpoint.host");
            String servicePort = System.getProperty("endpoint.port");
            String serviceURLFragment = System.getProperty("service.url");
            String serviceURL = 
               "https://" + serviceHost + ":" + servicePort + serviceURLFragment;

            System.out.println("Service URL=" + serviceURL);
            
            ((BindingProvider)stub).getRequestContext().
                put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL);  
            
            stub.ping(new Holder("1"), new Holder("sun"), new Holder("Passed!"));
            
            // Ping again
            stub.ping(new Holder("1"), new Holder("sun"), new Holder("Passed again!"));
            
            
             // Ping the third time
            stub.ping(new Holder("1"), new Holder("sun"), new Holder("Passed again again!"));
            
            
        } catch (Exception ex) {
            System.out.println ("Caught Exception: " + ex.getMessage() );
            ex.printStackTrace();
        } 
    }
}
