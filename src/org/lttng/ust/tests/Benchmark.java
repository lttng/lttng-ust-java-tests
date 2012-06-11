package org.lttng.ust.tests;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.lttng.ust.LTTngUst;

public class Benchmark {

    /** Nb of runs per test, result will be averaged */
    private static final int NB_RUNS = 5;

    /** Trace/log events per run */
    private static final int NB_ITER = 1000000;

    /** Which tests to run (for different number of threads) */
    private static final int[] nbThreads = {1, 1, 2, 3, 4, 5, 6, 7, 8};

    private static Runner runner;
    private static Logger log;

    public static void main(String[] args) throws SecurityException, IOException {

        final Handler fh;

        /* Set up the logger */
        log = Logger.getLogger("Test logger");
        log.setUseParentHandlers(false);
        fh = new FileHandler("/tmp/log", false);
        fh.setFormatter(new SimpleFormatter());
//        log.addHandler(fh);
        log.setLevel(Level.ALL);

        /* Set up the tracer */
        LTTngUst.init();
        System.out.println("Press a key to start.");
        System.in.read();

        for (int i : nbThreads) {
            runTest(i);
        }
    }


    private static void runTest(int nbThreads) throws SecurityException, IOException {
        long start, end, average, total = 0;
        for (int i = 0; i < NB_RUNS; i++) {
            runner = new Runner(nbThreads, NB_ITER, log);

            start = System.nanoTime();
            runner.run();
            end = System.nanoTime();

            total += (end - start);
        }
        average = total / NB_RUNS;
        System.out.println(nbThreads + " threads, average = " + average / NB_ITER + " ns/event");
    }
}
