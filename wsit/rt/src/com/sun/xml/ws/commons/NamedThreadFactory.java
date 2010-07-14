package com.sun.xml.ws.commons;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class NamedThreadFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean createDeamenoThreads;

    public NamedThreadFactory(String namePrefix) {
        this (namePrefix, true);
    }

    public NamedThreadFactory(String namePrefix, boolean createDaemonThreads) {
        SecurityManager securityManager = System.getSecurityManager();
        this.group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix + "-thread-";
        this.createDeamenoThreads = createDaemonThreads;
    }

    public Thread newThread(Runnable task) {
        Thread newThread = new Thread(group, task, namePrefix + threadNumber.getAndIncrement());

        if (newThread.getPriority() != Thread.NORM_PRIORITY) {
            newThread.setPriority(Thread.NORM_PRIORITY);
        }

        if (createDeamenoThreads && !newThread.isDaemon()) {
            newThread.setDaemon(true);
        }

        return newThread;
    }
}
