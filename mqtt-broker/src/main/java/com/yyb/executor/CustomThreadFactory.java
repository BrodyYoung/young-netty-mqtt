package com.yyb.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class CustomThreadFactory implements ThreadFactory {

    private AtomicInteger counter = new AtomicInteger(0);
    private String threadName;

    public CustomThreadFactory(String threadName){
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r,threadName + "_" + this.counter.incrementAndGet());
    }
}
