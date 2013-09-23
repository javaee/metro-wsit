package com.sun.xml.ws.rx.rm.runtime;

import java.util.logging.Level;

import com.oracle.webservices.api.rm.InboundAccepted;
import com.oracle.webservices.api.rm.InboundAcceptedAcceptFailed;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.runtime.transaction.TransactionPropertySet;

public class InboundAcceptedImpl extends InboundAccepted {
    private static final Logger LOGGER = Logger.getLogger(InboundAcceptedImpl.class);
    private final JaxwsApplicationMessage request;
    private final RuntimeContext rc;
    private Boolean accepted;

    public InboundAcceptedImpl(JaxwsApplicationMessage request, RuntimeContext rc) {
        this.request = request;
        this.rc = rc;
    }

    @Override
    public void setAccepted(Boolean accept) throws InboundAcceptedAcceptFailed {

        if (accept == null) {
            throw new IllegalArgumentException("Found supplied accept Boolean null.");
        }

        accepted = accept;

        TransactionPropertySet ps =
                request.getPacket().getSatellite(TransactionPropertySet.class);
        boolean txOwned = (ps != null && ps.isTransactionOwned());

        if (accept) {
            rc.destinationMessageHandler.acknowledgeApplicationLayerDelivery(request);

            if (txOwned) {
                if (rc.transactionHandler.isActive() || rc.transactionHandler.isMarkedForRollback()) {

                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Transaction status before commit: " + rc.transactionHandler.getStatusAsString());
                    }

                    try {
                        rc.transactionHandler.commit();
                    } catch (Throwable t) {
                        accepted = null;
                        throw new InboundAcceptedAcceptFailed();
                    }
                } else {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.severe("Unexpected transaction status before commit: " + rc.transactionHandler.getStatusAsString());
                    }
                    accepted = null;
                    throw new InboundAcceptedAcceptFailed();
                }
            } else {
                //Do nothing as we don't own the TX
            }
        } else {//accept == false
            if (txOwned) {
                if (rc.transactionHandler.isActive() || rc.transactionHandler.isMarkedForRollback()) {

                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Transaction status before rollback: " + rc.transactionHandler.getStatusAsString());
                    }

                    rc.transactionHandler.rollback();
                } else {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.severe("Unexpected transaction status before rollback: " + rc.transactionHandler.getStatusAsString());
                    }
                }
            } else {
                //Don't roll back as we don't own the TX but if active then mark for roll back
                if (rc.transactionHandler.userTransactionAvailable() &&
                        rc.transactionHandler.isActive()) {
                    rc.transactionHandler.setRollbackOnly();
                }
            }
        }
    }

    @Override
    @Property("com.oracle.webservices.api.rm.inbound.accepted.accepted")
    public Boolean getAccepted() {
        return accepted;
    }

    @Override
    @Property("com.oracle.webservices.api.rm.inbound.accepted.rm.sequence.id")
    public String getRMSequenceId() {
        String seqID = (String)request.getPacket().invocationProperties.get(ServerTube.SEQUENCE_PROPERTY);
        return seqID;
    }

    @Override
    @Property("com.oracle.webservices.api.rm.inbound.accepted.rm.message.number")
    public long getRMMessageNumber() {
        long msgNumber = (Long)request.getPacket().invocationProperties.get(ServerTube.MESSAGE_NUMBER_PROPERTY);
        return msgNumber;
    }
}
