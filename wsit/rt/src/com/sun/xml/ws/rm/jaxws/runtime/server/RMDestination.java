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
package com.sun.xml.ws.rm.jaxws.runtime.server;

import com.sun.xml.ws.rm.InvalidSequenceException;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.InboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.jaxws.runtime.RMProvider;
import com.sun.xml.ws.rm.jaxws.runtime.SequenceConfig;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An RMDestination represents a Collection of Inbound RMSequences.
 */
public class RMDestination extends RMProvider {

    private static final RMDestination RM_DESTINATION_INSTANCE = new RMDestination();
    private static final RmLogger LOGGER = RmLogger.getLogger(RMDestination.class);

    /*
     * Contains all the <code>OutboundSequences</code> managed
     * by this <code>RMProvider</code>.
     */
    private Hashtable<String, ServerOutboundSequence> outboundMap = new Hashtable<String, ServerOutboundSequence>();
    /*
     * Contains all the <code>InboundSequences</code> managed
     * by this <code>RMProvider</code>.  For an <code>RMDestination</code>
     * these are the "primary" sequences and <code>outboundMap</code> 
     * represents their "companion" sequences.
     */
    private Hashtable<String, ServerInboundSequence> inboundMap = new Hashtable<String, ServerInboundSequence>();
    //TODO - make an intelligent choice for  wake-up interval.
    private SequenceReaper reaper = new SequenceReaper(5000, inboundMap);

    public static RMDestination getRMDestination() {
        return RM_DESTINATION_INSTANCE;
    }

    private RMDestination() {
    }

    public void terminateSequence(String id) throws InvalidSequenceException {
        InboundSequence seq = inboundMap.get(id);

        if (seq == null) {
            throw new InvalidSequenceException(LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(id), id);
        }

        OutboundSequence out = seq.getOutboundSequence();
        synchronized (this) {
            if (seq != null) {
                inboundMap.remove(id);
            }

            if (inboundMap.isEmpty()) {
                reaper.stop();
            }
        }

        if (out != null) {
            String outid = out.getId();
            if (outid != null) {
                outboundMap.remove(outid);
            }
        }
    }

    //TODO add endpoint address argument to this method and corresponding
    //member in ServerInboundSequence
    public ServerInboundSequence createSequence(
            String inboundId,
            String outboundId,
            SequenceConfig config) throws RMException {

        ServerInboundSequence seq = new ServerInboundSequence(inboundId, outboundId, config);

        synchronized (this) {
            inboundMap.put(seq.getId(), seq);

            if (inboundMap.size() == 1) {
                reaper.start();
            }
        }

        ServerOutboundSequence outbound = (ServerOutboundSequence) seq.getOutboundSequence();
        String id = outbound.getId();
        if (id != null) {
            outboundMap.put(id, outbound);
        }

        return seq;
    }

    /**
     * SequenceReaper is a timer with a single task that periodically checks the map
     * of active ServerInboundSequences for expired ones an peremptorily terminates them.
     */
    private class SequenceReaper extends Timer {

        private long frequency;
        private Map<String, ServerInboundSequence> map;
        private TimerTask timerTask;

        public void start() {
            timerTask = new TimerTask() {

                public void run() {
                    //go though all the sequences and shut down any that
                    //are expired.
                    HashSet<String> keysToRemove = new HashSet<String>();
                    for (String key : map.keySet()) {
                        ServerInboundSequence sis = map.get(key);
                        synchronized (sis) {
                            if (sis.isExpired()) {
                                // TODO: L10N                                
                                LOGGER.fine("Terminating expired sequence: " + sis.getId());
                                keysToRemove.add(key);
                            }
                        }
                    }

                    for (String str : keysToRemove) {
                        try {
                            terminateSequence(str);
                        } catch (Exception e) {
                            // TODO: L10N
                            LOGGER.warning("Exception occured when terminating sequence: " + str, e);
                        }
                    }
                }
            };

            schedule(timerTask, new Date(System.currentTimeMillis() + frequency), frequency);
        }

        public void stop() {
            timerTask.cancel();
        }

        public SequenceReaper(long frequency, Map<String, ServerInboundSequence> map) {
            //make the Timer Thread a daemon.
            super(true);
            this.map = map;
            this.frequency = frequency;
        }
    }

    /**
     * Look up <code>OutboundSequence</code> with given id.
     *
     * @param The sequence id
     */
    public ServerOutboundSequence getOutboundSequence(String id) {
        return outboundMap.get(id);
    }

    /**
     * Look up <code>OutboundSequence</code> with given id.
     *
     * @param The sequence id
     */
    public ServerInboundSequence getInboundSequence(String id) {
        return inboundMap.get(id);
    }
}
