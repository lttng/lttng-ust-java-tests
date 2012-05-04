package org.lttng.ust.tests;

import java.util.logging.Logger;

import org.lttng.ust.LTTngUst;

public class Worker implements Runnable {

    private final Logger log;
    private final int threadNumber;
    private long curCount;

    public Worker(int nb, Logger log) {
        this.log = log;
        threadNumber = nb;
        curCount = 0;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            // log.info("Thread " + threadNumber + ", iteration " + i);
            LTTngUst.tracepointIntInt("Thread/Iteration", threadNumber, i);
            curCount += i;
        }
        // System.out.println(curCount);
    }

}
