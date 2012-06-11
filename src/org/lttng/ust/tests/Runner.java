package org.lttng.ust.tests;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Runner implements Runnable {

    private final List<Worker> workers;
    private final List<Thread> workerThreads;

    public Runner(int nbThreads, int nbIter, Logger log)
            throws SecurityException, IOException {
        Worker curWorker;

        this.workers = new LinkedList<>();
        this.workerThreads = new LinkedList<>();

        for (int id = 0; id < nbThreads; id++) {
            curWorker = new Worker(id, nbIter, log);
            workers.add(curWorker);
            workerThreads.add(new Thread(curWorker, "worker " + id));
        }
    }

    @Override
    public void run() {
        // System.out.println("Starting");
        for (Thread curThread : workerThreads) {
            curThread.start();
        }

        for (Thread curThread : workerThreads) {
            try {
                curThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // System.out.println("Finished");
    }
}
