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
 * InboundMessageProcessor.java
 *
 * @author Mike Grogan
 * Created on October 31, 2005, 8:21 AM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.rm.*;
import com.sun.xml.ws.rm.v200502.AckRequestedElement;
import com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement;
import com.sun.xml.ws.rm.v200502.SequenceElement;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * 
 * InboundMessageProcessor examines the headers of inbound <code>Messages</code> and
 * based on the sequence id's and types of the headers, dispatches them to 
 * appropriate <code>ClientInboundSequence</code> or <code>ClientOutboundSequence</code> 
 * methods.
 */
public class InboundMessageProcessor {


    private  RMProvider provider;


    public InboundMessageProcessor(RMProvider provider) {
       
        this.provider = provider;
    }


    
     /**
     * For each inbound <code>Message</code>, invokes protocol logic dictated by the contents of the
     * WS-RM protocol headers on the message.
     * <ul>
     *  <li><b>Sequence Header</b><br>Adds the message to the instance data of this incoming sequence according using the
     *          Sequence Identifier and Message Number in the header.</li>
     *  <li><b>SequenceAcknowledgement Header</b><br>Invokes the <code>handleAckResponse</code> method of the companion 
     *          <code>ClientOutboundSequence</code> which marks acknowledged messages as delivered.</li>
     *  <li><b>AckRequested Header</b><br>Constructs a <code>SequenceAcknowledgementElement</code> reflecting the messages 
     *          belonging to this sequence that have been received.  Sets the resulting 
     *      
     * <code>SequenceAcknowledgementElement</code> in the state of the companion <code>ClientOutboundSequence</code>.</li>
     * </ul>
     * <br>
     * @param message The inbound <code>Message</code>.
     */

    public void processMessage(Message message,
                               Marshaller marshaller,
                               Unmarshaller unmarshaller) 
            throws RMException {

            
            try {
                
                /*
                 * Check for each RM header type and do the right thing in RMProvider
                 * depending on the type.
                 */

                InboundSequence inseq =null;

                Header header = message.getHeader("Sequence");
                if (header != null) {
                    //identify sequence and message number from data in header and add
                    //the message to the sequence at the specified index.
                    //TODO handle error condition seq == null
                    SequenceElement el = 
                                (SequenceElement)header.readAsJAXB(unmarshaller);
                    
                    message.setSequenceElement(el);
                    
                    String seqid = el.getId();

                    //add message to ClientInboundSequence
                    int messageNumber = (int)el.getNumber();
                    if (messageNumber == Integer.MAX_VALUE){
                        throw new MessageNumberRolloverException(String.format(Constants.MESSAGE_NUMBER_ROLLOVER_TEXT,messageNumber),messageNumber);
                    }
                    
                    inseq =   provider.getInboundSequence(seqid);
                    
                    if (inseq != null) {
                        inseq.set(messageNumber, message);
                    } else {
                        throw new InvalidSequenceException(String.format(Constants.UNKNOWN_SEQUENCE_TEXT,seqid),seqid);
                    }

                } 


                header = message.getHeader("SequenceAcknowledgement");
                if (header != null) {
                    
                    //determine OutboundSequence id from data in header and update
                    //state of that sequence according to the acks and nacks in the element 
                    SequenceAcknowledgementElement ackHeader =
                            (SequenceAcknowledgementElement)(header.readAsJAXB(unmarshaller));
                    
                    message.setSequenceAcknowledgementElement(ackHeader);
                    
                    OutboundSequence seq =
                            provider.getOutboundSequence(ackHeader.getId());

                    if (seq != null) {
                        seq.handleAckResponse(ackHeader);
                    }
                }
                
                
                header = message.getHeader("AckRequested");
                if (header != null) {
 
                    //dispatch to InboundSequence to construct response.
                    //TODO handle error condition no such sequence
                     AckRequestedElement el = 
                             (AckRequestedElement)header.readAsJAXB(unmarshaller);
                     
                     message.setAckRequestedElement(el);
                     
                     String id = el.getId();

                    InboundSequence seq =
                            provider.getInboundSequence(id);

                    if (seq != null) {
                        seq.handleAckRequested((AckRequestedElement)el, marshaller);
                    }

                } else {
                    //FIXME - We need to be checking whether this is a ServerInboundSequence
                    //in a port with a two-way operation.  This is the case where MS
                    //puts a SequenceAcknowledgement on every message.
                    
                    //Need to check this with the latest CTP
                    //Currently with Dec CTP the client message
                    //does not have AckRequested element
                    // but they are expecting a SequenceAcknowledgement
                    //Hack for now
                    
                    if (inseq != null) {
                        
                        inseq.handleAckRequested(null, marshaller);
                    } else {
                        //we can get here if there is no sequence header.  Perhaps this
                        //is a ClientInboundSequence where the OutboundSequence has no two-ways
                    }
                    
                }

                
            } catch (JAXBException e) {
                throw new RMException(e);
            }
        }

}
