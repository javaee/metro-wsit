package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.xml.ws.api.ha.HaInfo;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.commons.ha.StickyKey;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.JaxwsApplicationMessage;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import org.glassfish.ha.store.api.BackingStore;

final class UnackedMessageReplicationManager implements ReplicationManager<String, ApplicationMessage> {

    private static class ApplicationMessageState implements Serializable {

        private final byte[] data;
        private final int nextResendCount;
        private final String correlationId;
        private final String wsaAction;
        private final String sequenceId;
        private final long messageNumber;

        public ApplicationMessageState(ApplicationMessage _message) {
            if (!(_message instanceof JaxwsApplicationMessage)) {
                throw new IllegalArgumentException("Unsupported message class: " + _message.getClass().getName());
            }
            JaxwsApplicationMessage message = (JaxwsApplicationMessage) _message;
            this.data = message.toBytes();
            this.nextResendCount = message.getNextResendCount();
            this.correlationId = message.getCorrelationId();
            this.wsaAction = message.getWsaAction();
            this.sequenceId = message.getSequenceId();
            this.messageNumber = message.getMessageNumber();
        }

        public ApplicationMessage toMessage() {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            return JaxwsApplicationMessage.newInstance(bais, nextResendCount, correlationId, wsaAction, sequenceId, messageNumber);
        }
    }
    private BackingStore<StickyKey, ApplicationMessageState> unackedMesagesBs;

    public UnackedMessageReplicationManager(final String uniqueEndpointId) {
        this.unackedMesagesBs = HighAvailabilityProvider.INSTANCE.createBackingStore(HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY), uniqueEndpointId + "_UNACKED_MESSAGES_BS", StickyKey.class, ApplicationMessageState.class);
    }

    public ApplicationMessage load(String key) {
        ApplicationMessageState state = HighAvailabilityProvider.loadFrom(unackedMesagesBs, new StickyKey(key), null);
        return state.toMessage();
    }

    public void save(final String key, final ApplicationMessage value, final boolean isNew) {
        ApplicationMessageState ams = new ApplicationMessageState(value);
        HaInfo haInfo = HaContext.currentHaInfo();
        if (haInfo != null) {
            HighAvailabilityProvider.saveTo(unackedMesagesBs, new StickyKey(key, haInfo.getKey()), ams, isNew);
        } else {
            final StickyKey stickyKey = new StickyKey(key);
            final String replicaId = HighAvailabilityProvider.saveTo(unackedMesagesBs, stickyKey, ams, isNew);
            HaContext.updateHaInfo(new HaInfo(stickyKey.getHashKey(), replicaId, false));
        }
    }

    public void remove(String key) {
        HighAvailabilityProvider.removeFrom(unackedMesagesBs, new StickyKey(key));
    }

    public void close() {
        HighAvailabilityProvider.close(unackedMesagesBs);
    }

    public void destroy() {
        HighAvailabilityProvider.destroy(unackedMesagesBs);
    }
}
