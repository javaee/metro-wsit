
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * RMSource.java
 *
 * @author Mike Grogan
 * Created on October 15, 2005, 6:24 PM
 */
package com.sun.xml.ws.rm.jaxws.runtime.client;

import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.RMProvider;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import com.sun.xml.ws.rm.Constants;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * An RMSource represents a Collection of RMSequences with a
 * common acksTo endpoint.
 */
public class RMSource extends RMProvider {

    private static final Logger logger = Logger.getLogger(RMSource.class.getName());
    private static final RMSource RM_SOURCE_INSTANCE = new RMSource();
    private static final byte[] CREATE_SEQUENCE_PAYLOAD = "<sun:createSequence xmlns:sun=\"http://com.sun/createSequence\"/>".getBytes();
    private RetryTimer retryTimer;

    /*
     * Contains all the <code>OutboundSequences</code> managed
     * by this <code>RMProvider</code>.  For an <code>RMSource</code>
     * these are the "primary" sequences and <code>inboundMap</code> 
     * represents their "companion" sequences.
     */
    private Hashtable<String, ClientOutboundSequence> outboundMap = new Hashtable<String, ClientOutboundSequence>();
    /*
     * Contains all the <code>InboundSequences</code> managed
     * by this <code>RMProvider</code>.
     */
    private Hashtable<String, ClientInboundSequence> inboundMap = new Hashtable<String, ClientInboundSequence>();

    public static RMSource getRMSource() {
        return RM_SOURCE_INSTANCE;
    }

    private RMSource() {
        retryTimer = new RetryTimer(this);
    }

    public synchronized void terminateSequence(ClientOutboundSequence seq) throws RMException {
        String id = seq.getId();
        if (seq != null && outboundMap.keySet().contains(id)) {
            seq.disconnect();
            removeOutboundSequence(id);
        }
    }

    public synchronized void addOutboundSequence(ClientOutboundSequence seq) {
        logger.fine(Messages.ADDING_SEQUENCE_MESSAGE.format(seq.getId()));

        boolean firstSequence = outboundMap.isEmpty();
        outboundMap.put(seq.getId(), seq);

        ClientInboundSequence iseq =
                (ClientInboundSequence) seq.getInboundSequence();

        String iseqid = null;

        if (iseq != null && null != (iseqid = iseq.getId())) {
            inboundMap.put(iseqid, iseq);
        }
        if (firstSequence) {
            retryTimer.start();
        }
    }

    public synchronized void removeOutboundSequence(ClientOutboundSequence seq) {

        logger.fine(Messages.REMOVING_SEQUENCE_MESSAGE.format(seq.getId()));

        String id = seq.getId();

        ClientInboundSequence iseq =
                (ClientInboundSequence) seq.getInboundSequence();

        String iseqid = null;
        if (iseq != null && null != (iseqid = iseq.getId())) {
            inboundMap.remove(iseqid);
        }
        outboundMap.remove(id);

        if (outboundMap.isEmpty()) {
            retryTimer.stop();
        }
    }

    private void removeOutboundSequence(String id) {

        ClientOutboundSequence seq = outboundMap.get(id);

        if (seq != null) {
            removeOutboundSequence(seq);
        } else {
            String message = Messages.NO_SUCH_OUTBOUND_SEQUENCE.format(id);
            IllegalArgumentException e = new IllegalArgumentException(message);
            logger.log(Level.FINE, message, e);
            throw e;
        }
    }

    /**
     * Do the necessary maintenance tasks for each <code>ClientInboundSequence</code>
     * managed by this RMSource.  This is done by calling the <code>doMaintenanceTasks</code>
     * method of each managed sequence.
     * 
     * @throws RMException Propogates <code>RMException</code> thrown by any of the managed
     * sequences.
     */
    public void doMaintenanceTasks() throws RMException {
        for (String key : outboundMap.keySet()) {
            ClientOutboundSequence seq = outboundMap.get(key);
            synchronized (seq) {
                //1. resend all incomplete messages
                //2. send ackRequested messages in any sequences
                //   in danger of timing out.
                seq.doMaintenanceTasks();
            }
        }
    }

    /**
     * Initialize a sequence using a CreateSequence handshake.  The
     * returned Sequence can be set in BindingProvider properies which will
     * result in the Sequence being used for the BindingProvider's request messages.
     *
     * @param client A Service hosting the endpoint
     * @param port The QName for the RM enpoint.
     * @return The ClientOutboundSequence.  null if the sequence could not be created
     *  
     */
    public ClientOutboundSequence createSequence(javax.xml.ws.Service service, QName portName) {
        Dispatch<Source> dispatch = service.createDispatch(
                portName,
                Source.class,
                Service.Mode.PAYLOAD,
                new javax.xml.ws.RespectBindingFeature());

        ByteArrayInputStream stream = new ByteArrayInputStream(CREATE_SEQUENCE_PAYLOAD);
        StreamSource source = new StreamSource(stream);

        try {
            dispatch.invoke(source);
        } catch (Exception e) {

        //dont care what happened processing the response message.  We are only
        //interested in the sequence that has been stored in the request context
        //
        //TODO - At the same time, it would be prettier to get something other than
        //a fault
        //TODO exception handling
        }

        ClientOutboundSequence seq = (ClientOutboundSequence) dispatch.getRequestContext().get(Constants.sequenceProperty);
        seq.setService(service);
        return seq;

    }

    /**
     * Initialize a sequence using an existing seuence id known to an RM endpoint.
     * The method is designed to be used after a startup to reinitialize a
     * sequence from persisted data.
     *
     * @param client A Service hosting the endpoint
     * @param port The QName for the RM enpoing.
     * @param sequencID The id to be used for the outbound sequence
     * @param companionSequenceID The id to be used for the companion inbound sequence,
     *              if any
     * @return The ClientOutboundSequence.  null if the sequence could not be created
     */
    public ClientOutboundSequence createSequence(
            Service service,
            QName portName,
            String sequenceID,
            String companionSequenceID) {


        //this will throw and exception if the specified sequence does not exist.
        //removeOutboundSequence(sequenceID);

        ClientOutboundSequence seq = createSequence(service, portName);
        if (seq == null) {
            return null;
        }

        try {
            seq.disconnect(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        seq.setId(sequenceID);

        ClientInboundSequence iseq =
                (ClientInboundSequence) seq.getInboundSequence();

        if (companionSequenceID != null) {

            if (iseq == null || iseq.getId() == null) {

                String message = Messages.NO_TWO_WAY_OPERATION.format();
                IllegalArgumentException e = new IllegalArgumentException(message);
                logger.log(Level.FINE, message, e);
                throw e;
            }
            iseq.setId(companionSequenceID);

        } else if (iseq != null && iseq.getId() != null) {

            String message = Messages.NO_INBOUND_SEQUENCE_ID_SPECIFIED.format();
            IllegalArgumentException e = new IllegalArgumentException(message);
            logger.log(Level.FINE, message, e);
            throw e;
        }

        if (outboundMap.get(sequenceID) != null) {

            String message = Messages.SEQUENCE_ALREADY_EXISTS.format(sequenceID);
            IllegalArgumentException e = new IllegalArgumentException(message);
            logger.log(Level.FINE, message, e);
            throw e;

        }

        if (companionSequenceID != null &&
                inboundMap.get(companionSequenceID) != null) {

            String message = Messages.SEQUENCE_ALREADY_EXISTS.format(companionSequenceID);
            IllegalArgumentException e = new IllegalArgumentException(message);
            logger.log(Level.FINE, message, e);
            throw e;

        }

        addOutboundSequence(seq);

        return seq;
    }

    /**
     * Look up <code>OutboundSequence</code> with given id.
     *
     * @param The sequence id
     */
    public ClientOutboundSequence getOutboundSequence(String id) {
        return outboundMap.get(id);
    }

    /**
     * Look up <code>OutboundSequence</code> with given id.
     *
     * @param The sequence id
     */
    public ClientInboundSequence getInboundSequence(String id) {
        return inboundMap.get(id);
    }
}
