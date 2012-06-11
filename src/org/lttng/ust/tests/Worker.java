package org.lttng.ust.tests;

import java.util.logging.Logger;

import org.lttng.ust.LTTngUst;

public class Worker implements Runnable {

    private final Logger log;
    private final int threadId;
    private final int nbIter;
    private long curCount;

    public Worker(int threadId, int nbIter, Logger log) {
        this.log = log;
        this.threadId = threadId;
        this.nbIter = nbIter;
        curCount = 0;
    }

    @Override
    public void run() {
        for (int i = 0; i < nbIter; i++) {
//            log.info("Thread " + threadId + ", iteration " + i);
            LTTngUst.tracepointIntInt("Thread/Iteration", threadId, i);
            curCount += i;
        }
//        System.out.println(curCount);
    }

}
