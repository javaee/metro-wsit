/*
 * ProtocolMessageHelper.java
 *
 * @author Mike Grogan
 * Created on August 17, 2007, 3:26 PM
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;

/**
 * ProtocolMessageHelper is used to execute synchronous protocol message exchanges using
 * the asynchronous Tubeline architecture.
 */
public class ProtocolMessageHelper {
    
    private final Engine engine;
    private final Tube tube;
    
    public ProtocolMessageHelper(Tube tube) {
        this.tube = tube;
    
        Fiber currentFiber = Fiber.current();
        if (currentFiber == null) {
            throw new IllegalStateException("No current fiber.");
        }
        
        engine = currentFiber.owner;
    };
    
    /**
     * Synchronously executes the protocol exchange.  The implementation sends
     * the request through a clone of the stored Tubeline and blocks until a
     * response is received.
     */
    public Packet process(Packet request) throws RMException {
       
        try {
        //we will use a fresh Fiber and Tube for each request.  We need to do this
        //because there may be another request being procesed by the original tube.
        //This can happen when this ProtocolMessageHelper is being used to resend
        //messages or send AckRequested's from the maintenance thread.  These
        //things might happen while other requests are being processe.'
        Fiber fiber = engine.createFiber();
        Tube tubeline = TubeCloner.clone(tube);
        return fiber.runSync(tubeline, request);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
       
    };
    
}