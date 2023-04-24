package com.zhao.myreader.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class KillableThreadPoolExecutor extends ThreadPoolExecutor {

    private final Map<Runnable, Thread> executingThreads;

    public KillableThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, String threadNamePrefix) {
        //super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingDeque<Runnable>(), ThreadFactoryBuilder.create().build());
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new LinkedBlockingDeque<Runnable>());
        executingThreads = new HashMap<>(maximumPoolSize);
    }

    @Override
    protected synchronized void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        executingThreads.put(r, t);
    }

    @Override
    protected synchronized void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (executingThreads.containsKey(r)) {
            executingThreads.remove(r);
        }
    }

    @Override
    public synchronized List<Runnable> shutdownNow() {
        List<Runnable> runnables = super.shutdownNow();
        for (Thread t : executingThreads.values()) {
            t.stop();
        }
        return runnables;
    }
}