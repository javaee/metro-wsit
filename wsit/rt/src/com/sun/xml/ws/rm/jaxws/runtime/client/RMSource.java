
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rm.jaxws.runtime.client;

import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.RMProvider;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * An RMSource represents a Collection of RMSequences with a
 * common acksTo endpoint.
 */
public class RMSource extends RMProvider {

    private static final RmLogger LOGGER = RmLogger.getLogger(RMSource.class);
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
        if (seq != null && outboundMap.keySet().contains(seq.getId())) {
            seq.disconnect();
            removeOutboundSequence(seq.getId());
        }
    }

    public synchronized void addOutboundSequence(ClientOutboundSequence seq) {
        LOGGER.fine(LocalizationMessages.WSRM_2011_ADDING_SEQUENCE_MESSAGE(seq.getId()));

        boolean firstSequence = outboundMap.isEmpty();
        outboundMap.put(seq.getId(), seq);

        ClientInboundSequence iseq = (ClientInboundSequence) seq.getInboundSequence();
        String iseqid = null;
        if (iseq != null && null != (iseqid = iseq.getId())) {
            inboundMap.put(iseqid, iseq);
        }
        if (firstSequence) {
            retryTimer.start();
        }
    }

    public synchronized void removeOutboundSequence(ClientOutboundSequence seq) {
        LOGGER.fine(LocalizationMessages.WSRM_2012_REMOVING_SEQUENCE_MESSAGE(seq.getId()));

        String id = seq.getId();
        ClientInboundSequence iseq = (ClientInboundSequence) seq.getInboundSequence();
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
            throw LOGGER.logException(new IllegalArgumentException(LocalizationMessages.WSRM_2013_NO_SUCH_OUTBOUND_SEQUENCE(id)), Level.FINE);
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
            //TODO - At the same time, it would be prettier to get something other than a fault
            // TODO L10N + exception handling
            LOGGER.warning("Sending CreateSequence failed", e);
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
            // TODO L10N + exception handling
            LOGGER.warning("Attempt to disconnect sequence [" + seq.getId() + "] failed with exception.", e);
        }

        seq.setId(sequenceID);
        ClientInboundSequence iseq = (ClientInboundSequence) seq.getInboundSequence();

        if (companionSequenceID != null) {
            if (iseq == null || iseq.getId() == null) {
                throw LOGGER.logException(new IllegalArgumentException(LocalizationMessages.WSRM_2014_NO_TWOWAY_OPERATION()), Level.FINE);
            }
            iseq.setId(companionSequenceID);
        } else if (iseq != null && iseq.getId() != null) {
            throw LOGGER.logException(new IllegalArgumentException(LocalizationMessages.WSRM_2015_NO_INBOUND_SEQUENCE_ID_SPECIFIED()), Level.FINE);
        }

        if (outboundMap.get(sequenceID) != null) {
            throw LOGGER.logException(new IllegalArgumentException(LocalizationMessages.WSRM_2016_SEQUENCE_ALREADY_EXISTS(sequenceID)), Level.FINE);
        }

        if (companionSequenceID != null && inboundMap.get(companionSequenceID) != null) {
            throw LOGGER.logException(new IllegalArgumentException(LocalizationMessages.WSRM_2016_SEQUENCE_ALREADY_EXISTS(companionSequenceID)), Level.FINE);
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
