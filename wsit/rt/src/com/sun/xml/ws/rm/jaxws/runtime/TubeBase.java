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
 * TubeBase.java
 *
 * @author Mike Grogan
 * Created on August 17, 2007, 1:28 PM
 *
 */
package com.sun.xml.ws.rm.jaxws.runtime;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.rm.RMException;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.RMMessage;

import com.sun.xml.ws.rm.localization.RmLogger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * BaseClass for <code>RMClientTube</code> and <code>RMServerTube</code>.  <coded>Tube</code>
 * methods are implemented in the subclasses.  The base class contains common code used
 *  by the JAX-WS runtime to communicate with the RM Providers.
 */
public abstract class TubeBase extends AbstractFilterTubeImpl {

    private static final RmLogger LOGGER = RmLogger.getLogger(TubeBase.class);
    /**
     * Either RMSource or RMDestination
     */
    private WSDLPort wsdlPort;
    private SequenceConfig config;
    private Unmarshaller unmarshaller;

    protected TubeBase(WSDLPort wsdlPort, WSBinding binding, Tube nextTube) {
        super(nextTube);

        this.wsdlPort = wsdlPort;
        this.config = new SequenceConfig(wsdlPort, binding.getAddressingVersion(), binding.getSOAPVersion());
        this.unmarshaller = createUnmarshaller(config.getRMVersion().jaxbContext);
    }

    protected TubeBase(TubeBase that, TubeCloner cloner) {
        super(that, cloner);

        this.wsdlPort = that.wsdlPort;
        this.config = that.config;
        this.unmarshaller = createUnmarshaller(config.getRMVersion().jaxbContext);
    }

    /**
     * Use methods of <code>OutboundSequence</code> field to store and write headers to
     * outbound message.
     *
     * @param packet Packet containing Outbound message
     * @return The wrapped message
     */
    protected RMMessage handleOutboundMessage(OutboundSequence outboundSequence, Packet packet, boolean isTwoWayRequest, boolean isOneWayResponse) throws RMException {
        //don't want to add this message to a sequence if one way response.
        RMMessage rmMessage = new RMMessage(packet.getMessage(), config.getRMVersion(), isOneWayResponse, isTwoWayRequest);

        Object messageNumberProperty = packet.invocationProperties.get(Constants.messageNumberProperty);
        if (messageNumberProperty instanceof Integer) {
            rmMessage.setMessageNumber((Integer) messageNumberProperty);
        }

        outboundSequence.processOutboundMessage(rmMessage);

        return rmMessage;
    }

    /**
     * Use methods of <code>RMProvider</code> field to store and write headers to
     * inbound message.
     *
     * @param packet Packet containing Outbound message
     * @return The wrapped message
     */
    protected RMMessage handleInboundMessage(Packet packet, RMProvider provider) throws RMException {
        Message message = packet.getMessage();
        RMMessage rmMessage = new RMMessage(message, config.getRMVersion());

        provider.getInboundMessageProcessor().processMessage(rmMessage, unmarshaller);
        return rmMessage;
    }

    protected final Unmarshaller getUnmarshaller() {
        return this.unmarshaller;
    }

    protected final WSDLPort getWsdlPort() {
        return this.wsdlPort;
    }

    protected final SequenceConfig getConfig() {
        return this.config;
    }

    private Unmarshaller createUnmarshaller(JAXBContext jaxbContext) {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            // TODO L10N            
            throw LOGGER.logSevereException(new IllegalStateException("Unable to create JAXB unmarshaller", e));
        }
    }
//    private static Marshaller createMarshaller(JAXBContext jaxbContext) {
//        try {
//            Marshaller marshaller = jaxbContext.createMarshaller();
//            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
//            return marshaller;
//        } catch (JAXBException e) {
//            // TODO L10N            
//            throw LOGGER.logSevereException(new IllegalStateException("Unable to create JAXB marshaller", e));
//        }
//
//    }
}
