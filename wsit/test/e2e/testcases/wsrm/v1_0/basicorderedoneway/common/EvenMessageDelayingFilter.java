package wsrm.v1_0.basicorderedoneway.common;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.runtime.testing.PacketFilter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EvenMessageDelayingFilter extends PacketFilter {

    private static final Logger LOGGER = Logger.getLogger(EvenMessageDelayingFilter.class.getName());
    private static final long MESSAGE_NUMBER_TO_BLOCK = 2;
    private static final long RESUMING_MESSAGE_NUMBER = 5;
    private static final CountDownLatch LATCH = new CountDownLatch(1);
    
    private static final AtomicLong CURRENT_MSG_ID = new AtomicLong(1);

    public EvenMessageDelayingFilter() {
        super(RmVersion.WSRM10);
    }

    @Override
    public Packet filterClientRequest(Packet request) throws Exception {
        long msgId = -1;
        try {
//            msgId = this.getMessageId(request);
            msgId = CURRENT_MSG_ID.getAndIncrement();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected exception occured when trying to retrieve message id.", ex);
        }

        if (msgId == RESUMING_MESSAGE_NUMBER) {
            LOGGER.info("Sending request resume signal");
            LATCH.countDown();
            LOGGER.info("Resume signal sent");
        } else if (msgId == MESSAGE_NUMBER_TO_BLOCK) {
            LOGGER.info(String.format("Blocking the request [ %d ] processing ...", msgId));
            LATCH.await();
            LOGGER.info(String.format("Request [ %d ]  resumed...", msgId));
        }
        return request;
    }

    @Override
    public Packet filterServerResponse(Packet response) throws Exception {
        return response;
    }
}
