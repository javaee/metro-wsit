package com.sun.xml.ws.commons;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class NamedThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public NamedThreadFactory(String namePrefix) {
        super();
        SecurityManager securityManager = System.getSecurityManager();
        this.group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix + "-thread-";
    }

    public Thread newThread(Runnable task) {
        Thread newThread = new Thread(group, task, namePrefix + threadNumber.getAndIncrement());
        if (newThread.getPriority() != Thread.NORM_PRIORITY) {
            newThread.setPriority(Thread.NORM_PRIORITY);
        }
        return newThread;
    }
}
