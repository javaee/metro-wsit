/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package simple.server1;

import org.xmlsoap.ping.Ping;
import org.xmlsoap.ping.PingResponseBody;


@javax.jws.WebService (endpointInterface="simple.server1.IPingService")
public class PingImpl implements IPingService {
    
    public PingResponseBody ping(Ping ping){
        
        PingResponseBody resp = new PingResponseBody();
        
        String scenario = ping.getScenario();
        System.out.println("scenario = " + scenario);
        resp.setScenario(scenario);
        
        String origin = ping.getOrigin();
        System.out.println("origin = " + origin);
        resp.setOrigin(origin);
        
        String text = ping.getText();
        System.out.println("text = " + text);
        resp.setText(text);
        
        return resp;
    }  
}
