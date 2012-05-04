package org.lttng.ust.tests;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Runner implements Runnable {

    private final List<Worker> workers;
    private final List<Thread> workerThreads;

    public Runner(int nbThreads, Logger log) throws SecurityException,
            IOException {
        Worker curWorker;

        workers = new LinkedList<>();
        workerThreads = new LinkedList<>();

        for (int i = 0; i < nbThreads; i++) {
            curWorker = new Worker(i, log);
            workers.add(curWorker);
            workerThreads.add(new Thread(curWorker, "worker " + i));
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
