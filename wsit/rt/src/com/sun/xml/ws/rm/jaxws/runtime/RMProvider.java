/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

/*
 * RMProvider.java
 *
 * @author Mike Grogan
 * Created on November 25, 2005, 9:50 AM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime;
import java.util.Hashtable;
import com.sun.xml.ws.rm.Message;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;


/**
 * RMProvider is a base class for <code>RMSource</code> and 
 * <code>RMDestination</code> that provides storage for Lists of
 * <code>InboundSequences</code> and <code>OutboundSequences</code> and
 * handles the processing of messages coming from the network.
 *
 */
public abstract class RMProvider<INBOUNDSEQUENCE extends InboundSequence,
                                 OUTBOUNDSEQUENCE extends OutboundSequence> {

    protected ProcessingFilter filter = null;
    
    /**
     */
    public RMProvider() {
    }
    
    /**
     * Sets and instance of ProcessingFilter to be used for diagnostic/debugging
     * purposes.  
     * 
     * @param filter The ProcessingFilter to be used.
     */
    public void setProcessingFilter(ProcessingFilter filter) {
        this.filter = filter;
    }
    
    /**
     * Returns the ProcessingFilter instance that has been designated for use for
     * diagnostic/debugging purposes.
     *
     * @return The filter that has been set by calling <code>setProcessingFilter</code>.
     * Returns null if no ProcessingFilter has been set.
     */
    public ProcessingFilter getProcessingFilter() {
        return filter;
    }
    /*
     * Contains all the <code>OutboundSequences</code> managed
     * by this <code>RMProvider</code>.  For an <code>RMSource</code>
     * these are the "primary" sequences and <code>inboundMap</code> 
     * represents their "companion" sequences.
     */
    protected Hashtable<String, OUTBOUNDSEQUENCE> outboundMap =
            new Hashtable<String, OUTBOUNDSEQUENCE>();
    
    /*
     * Contains all the <code>InboundSequences</code> managed
     * by this <code>RMProvider</code>.  For an <code>RMDestination</code>
     * these are the "primary" sequences and <code>inboundMap</code> 
     * represents their "companion" sequences.
     */
    protected Hashtable<String, INBOUNDSEQUENCE> inboundMap = 
            new Hashtable<String, INBOUNDSEQUENCE>();
    
    /**
     * Look up <code>OutboundSequence</code> with given id.
     *
     * @param The sequence id
     */
    public OUTBOUNDSEQUENCE getOutboundSequence(String id) {
        return outboundMap.get(id);
    }
    
    /**
     * Look up <code>OutboundSequence</code> with given id.
     *
     * @param The sequence id
     */
    public INBOUNDSEQUENCE getInboundSequence(String id) {
        return inboundMap.get(id);
    }
    /*
     * Instance of a Helper class to handle inbound messages based 
     * on their WS-RM protocol headers
     */
    protected InboundMessageProcessor messageProcessor = 
            new InboundMessageProcessor(this);
    
    /*
     * Process normal application message using <code>InboundMessageProcessor</code>
     * field.
     *
     * @param mess The inbound message.
     *
    */
    public void processInboundMessage(Message mess, 
                                    Marshaller marshaller,
                                    Unmarshaller unmarshaller) throws RMException{
        messageProcessor.processMessage(mess, marshaller, unmarshaller);
    }

    
    public InboundMessageProcessor getInboundMessageProcessor(){
        return messageProcessor;
    }
    
    
}
