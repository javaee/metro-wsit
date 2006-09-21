/*
 * RetryTimer.java
 *
 * Created on September 20, 2006, 12:12 PM
 * @author Mike Grogan
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;
import com.sun.xml.ws.rm.RMException;
import java.util.Timer;
import java.util.TimerTask;
/**
 *  RetryTimer replaces RMSource$RetryThread.  It uses a java.util.Timer
 *  whose TimerTask executes the doMaintenanceTasks method in each
 *  ClientOutboundSequence.
 */
public class RetryTimer  {
    
    private final RMSource source;
    private Timer timer = null;
    private int sequences = 0;
    
    /**
     *
     */
    public RetryTimer(RMSource source) {
        this.source = source;
    }
    
    /**
  No need to synchronize stop and start because
     * they are only called from inside the bodies of RMSource.start and
     * RMSource.stop
     */
    public /*synchronized*/ void start() {
        
        if (timer != null) {
            throw new IllegalStateException();
        }
        timer = new Timer();
        timer.schedule(new RetryTask(),
                source.getRetryInterval(),
                source.getRetryInterval());
        
    }
    
    /**
     *
     */
    public /*synchronized*/ void stop() {
        
        if (timer == null) {
            throw new IllegalStateException();
        }
        timer.cancel();
        timer = null;
    }
    
    
    private class RetryTask extends TimerTask {
        public void run() {
            try {
                source.doMaintenanceTasks();
            } catch (RMException e) {
                //TODO Log with FINE granularity
            }
        }
    }
}



