package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.xml.ws.rx.ha.ReplicationManager;
import com.sun.xml.ws.api.ha.HaInfo;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.commons.ha.StickyKey;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.JaxwsApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.JaxwsApplicationMessage.JaxwsApplicationMessageState;
import org.glassfish.ha.store.api.BackingStore;

final class UnackedMessageReplicationManager implements ReplicationManager<String, ApplicationMessage> {

    private BackingStore<StickyKey, JaxwsApplicationMessageState> unackedMesagesBs;

    public UnackedMessageReplicationManager(final String uniqueEndpointId) {
        this.unackedMesagesBs = HighAvailabilityProvider.INSTANCE.createBackingStore(
                HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY),
                uniqueEndpointId + "_UNACKED_MESSAGES_BS",
                StickyKey.class,
                JaxwsApplicationMessageState.class);
    }

    public ApplicationMessage load(String key) {
        JaxwsApplicationMessageState state = HighAvailabilityProvider.loadFrom(unackedMesagesBs, new StickyKey(key), null);
        return state.toMessage();
    }

    public void save(final String key, final ApplicationMessage value, final boolean isNew) {
        if (!(value instanceof JaxwsApplicationMessage)) {
            throw new IllegalArgumentException("Unsupported application message type: " + value.getClass().getName());
        }
        JaxwsApplicationMessageState ams = ((JaxwsApplicationMessage) value).getState();
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
