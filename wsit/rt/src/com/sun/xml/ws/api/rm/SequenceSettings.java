/*
 * SequenceSettings.java
 *
 *
 * @author Mike Grogan
 * Created on January 19, 2007, 8:59 AM
 *
 */

package com.sun.xml.ws.api.rm;
import java.io.Serializable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.rm.RMConstants;

/**
 * Initialization data for a sequence, which can be persisted
 * and used to reinitialize a sequence.
 */
public class SequenceSettings implements Serializable {
    
    public SequenceSettings() {
    }
    
    /**
     * AcksTo URI for the sequence
     */
    public String acksTo;

    /**
     * For OutboundSequences, determines whether destination guarantees ordered delivery.
     */
    public boolean ordered;

    /**
     * Number of milliseconds after which destination may terminate sequence.
     */
    public long inactivityTimeout;


    /**
     * Indicates whether flow control is enabled.
     */
    public boolean flowControl;
    
    /**
     * Number of messages that destination will buffer pending delivery.
     */
    public int bufferSize;

    /**
     * The SOAPVersion which will be passed on to the protocol elements
     * populated from the Pipe
     */
    public SOAPVersion soapVersion;
    
    
    /**
     * Length of time between resends
     */
    public long resendInterval;
    
    /**
     * Length of time between ackRequests.
     */
    public long ackRequestInterval;
    
    
    /**
     * Lenth of time that RMClientPipe.preDestroy will block while
     * waiting for unacknowledged messages to arrive.
     */
    public long closeTimeout;


    /**
     * Do we suppress duplicates at the endpoint?
     */
    public boolean allowDuplicates;
    
    /**
     * RMConstants enum value using correct addressing version.
     */
    public RMConstants constants ;

    /**
     * SequenceId for the sequence.  This field is not assumed to be populated
     * in the (@link SequenceConfig} subclass.
     */
    public String sequenceId;
    
    
     /**
     * SequenceId for the companion sequence, if any.  This field is not assumed 
     * to be populated in the (@link SequenceConfig} subclass.
     */
    public String companionSequenceId;
}
