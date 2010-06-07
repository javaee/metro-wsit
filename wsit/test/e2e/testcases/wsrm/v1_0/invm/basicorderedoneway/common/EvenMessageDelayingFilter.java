package wsrm.v1_0.invm.basicorderedoneway.common;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.rx.testing.PacketFilter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class EvenMessageDelayingFilter extends PacketFilter {

    private static final Logger LOGGER = Logger.getLogger(EvenMessageDelayingFilter.class.getName());
    private static final long MESSAGE_NUMBER_TO_BLOCK = 2;
    private static final AtomicInteger BLOCK_COUNT = new AtomicInteger(0);
    private static final int MAX_BLOCK_COUNT = 3;

    public EvenMessageDelayingFilter() {
        super();
    }

    @Override
    public Packet filterClientRequest(Packet request) throws Exception {
        long msgId = this.getMessageId(request);

        if (msgId == MESSAGE_NUMBER_TO_BLOCK && BLOCK_COUNT.getAndIncrement() < MAX_BLOCK_COUNT) {
            LOGGER.info(String.format("Blocking the request [ %d ] processing ...", msgId));
            return null;
        }
        return request;
    }

    @Override
    public Packet filterServerResponse(Packet response) throws Exception {
        return response;
    }
}
