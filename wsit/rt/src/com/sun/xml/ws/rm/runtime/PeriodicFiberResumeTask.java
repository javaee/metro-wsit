package com.sun.xml.ws.rm.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

final class PeriodicFiberResumeTask implements Runnable {

    private static final RmLogger LOGGER = RmLogger.getLogger(PeriodicFiberResumeTask.class);
    
    private static class FiberRegistration {

        private final long timestamp;
        @NotNull final Fiber fiber;
        final Packet packet;

        FiberRegistration(Fiber fiber, Packet packet) {
            this.timestamp = System.currentTimeMillis();
            this.fiber = fiber;
            this.packet = packet;
        }

        boolean expired(long period) {
            return System.currentTimeMillis() - timestamp >= period;
        }
    }
    private final Queue<FiberRegistration> fiberResumeQueue = new ConcurrentLinkedQueue<FiberRegistration>();
    private long resumePeriod;

    PeriodicFiberResumeTask(long period) {
        super();
        this.resumePeriod = period;
    }

    public void run() {
        while (!fiberResumeQueue.isEmpty() && fiberResumeQueue.peek().expired(resumePeriod)) {
            FiberRegistration registration = fiberResumeQueue.poll();            
            registration.fiber.resume(registration.packet);
            if (LOGGER.isLoggable(Level.FINER)) {                
                LOGGER.finer(LocalizationMessages.WSRM_1127_FIBER_RESUMED(registration.fiber.toString(), registration.packet.toString()));
            }
        }
    }

    /**
     * Registers given fiber for a resume
     * 
     * @param fiber a fiber to be resumed after preconfigured interval has passed
     * @return {@code true} if the fiber was successfully registered; {@code false} otherwise.
     */
    final boolean registerForResume(@NotNull Fiber fiber, Packet packet) {
        return fiberResumeQueue.offer(new FiberRegistration(fiber, packet));
    }
}
