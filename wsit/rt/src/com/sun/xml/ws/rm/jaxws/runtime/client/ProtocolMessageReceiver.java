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
 * ProtocolMessageReceiver.java
 *
 * @author Mike Grogan
 * Created on March 1, 2006, 12:50 PM
 *
 */
package com.sun.xml.ws.rm.jaxws.runtime.client;

import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.InvalidSequenceException;
import com.sun.xml.ws.rm.RMConstants;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.jaxws.runtime.OutboundSequence;
import com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement;
import com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement;
//import com.sun.xml.ws.transport.http.server.EndpointImpl;

import java.util.HashMap;

/**
 * Handles the contents of responses to RM Protocol requests with
 * non-anonymous AcksTo.
 */
public class ProtocolMessageReceiver {

    //Hardcoding the W3C version for now
    public static final String anonymous = RMConstants.W3C.getAnonymousURI().toString();
    /**
     * AcksTo URI used for non-anonymous responses... Currently one per process.
     * Set using the <code>start</code> method.  Defaults to anonymous.  When
     * start is called with non-anonymous argument, an HTTP listener is started to
     * process the messages.
     */
    private static String acksTo = anonymous;
    /**
     * Endpoint listening for protocol messages.
     */
//    private static EndpointImpl endpoint;
    /**
     * Map of  messageId String / CreateSequenceElement pairs that have beeen
     * passed to setCreateSequenceResponse.
     */
    private static HashMap<String, CreateSequenceResponseElement> knownIds =
            new HashMap<String, CreateSequenceResponseElement>();

    /**
     * Everything is static
     */
    private ProtocolMessageReceiver() {
    }

    /**
     * Accessor for the AcksTo field.
     */
    public static String getAcksTo() {
        return acksTo;
    }

    /*
     * Set the acksTo field to the specified URI and start the
     * Http listener listening at that URI.
     */
    public static void start(String newAcksTo) {
    /*       
    if (!acksTo.equals(anonymous) && !newAcksTo.equals(acksTo)) {
    throw new UnsupportedOperationException("Cannot change non-anonymous acksTo");
    }
    if (acksTo.equals(anonymous)) {
    acksTo = newAcksTo;      
    //start our endpoint listening on the given URI
    BindingID binding = BindingID.parse(SOAPBinding.SOAP12HTTP_BINDING) ;
    endpoint = new EndpointImpl(binding, new DummyProvider());
    endpoint.publish(acksTo);
    }
     */
    }

    public static void stop() {
    /*
    if (endpoint != null) {
    endpoint.stop();
    }
     */
    }

    public static void setCreateSequenceResponse(String messageId,
            CreateSequenceResponseElement csrElement) {

        synchronized (knownIds) {
            knownIds.put(messageId, csrElement);
            knownIds.notifyAll();
        }
    }

    public static CreateSequenceResponseElement getCreateSequenceResponse(String messageId) {
        CreateSequenceResponseElement ret = null;
        synchronized (knownIds) {
            if (!knownIds.keySet().contains(messageId)) {
                knownIds.put(messageId, null);
            }

            while (null == (ret = knownIds.get(messageId))) {
                try {
                    knownIds.wait();
                } catch (InterruptedException e) {
                    // TODO handle exception
                }
            }
        }
        return ret;
    }

    public static void handleAcknowledgement(SequenceAcknowledgementElement el) throws RMException {
        //probably no need for synchronization here.  The element was initialized at the 
        //endpoint using a sequenceid generated by the endpoint that made it back to
        //the client.  That means that getCreateSequenceResponse has returned long ago.
        String id = el.getId();
        OutboundSequence seq = RMSource.getRMSource().getOutboundSequence(id);
        if (id == null) {
            throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT, id), id);
        }

        seq.handleAckResponse(el);
    }
}
