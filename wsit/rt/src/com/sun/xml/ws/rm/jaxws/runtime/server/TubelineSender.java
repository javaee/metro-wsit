/*
 * TubelineSender.java
 *
 * @author Mike Grogan
 *
 * Created on August 27, 2007, 10:57 AM
 *
 */
package com.sun.xml.ws.rm.jaxws.runtime.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.rm.MessageSender;
import com.sun.xml.ws.rm.localization.RmLogger;

public class TubelineSender implements MessageSender, Fiber.CompletionCallback {

    private static final RmLogger LOGGER = RmLogger.getLogger(TubelineSender.class);
    private Fiber fiber;
    private Fiber parentFiber;
    private Packet requestPacket;
    private SOAPVersion soapVersion;
    private AddressingVersion addressingVersion;
    private RMServerTube tube;

    public TubelineSender(
            RMServerTube tube,
            Packet packet,
            SOAPVersion soapVersion,
            AddressingVersion addressingVersion) {

        this.requestPacket = packet;
        this.parentFiber = Fiber.current();
        this.fiber = parentFiber.owner.createFiber();
        this.soapVersion = soapVersion;
        this.addressingVersion = addressingVersion;
        this.tube = tube;
    }

    public void onCompletion(@NotNull Packet packet) {
        try {
            tube.postProcess(packet);
            parentFiber.resume(packet);
        } catch (Throwable t) {
            // TODO L10N
            LOGGER.severe("Unexcpected error occured, proceeding with handling the exception", t);
            onCompletion(t);
        }
    }

    public void onCompletion(@NotNull Throwable throwable) {
        Message mess = Messages.create(throwable, soapVersion);
        Packet packet = requestPacket.createServerResponse(
                mess,
                addressingVersion,
                soapVersion,
                addressingVersion.getDefaultFaultAction());
        parentFiber.resume(packet);
        // TODO: Should I really catch another Throwable here and just log & discard it?
    }

    public void send() {
        fiber.start(TubeCloner.clone(tube.nextTube()), requestPacket, this);
    }
}
