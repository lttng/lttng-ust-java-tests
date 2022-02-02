/*
 * Copyright (C) 2015, EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.lttng.ust.agent.benchmarks.jul.handler;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lttng.ust.agent.utils.TestPrintExtension;

/**
 * Base abstract class for JUL benchmarks. Sub-classes can setup parameters to
 * test different types of log handlers.
 */
@ExtendWith(TestPrintExtension.class)
@Tag("agent:jul")
@Tag("domain:jul")
@Tag("benchmark")
public abstract class JulHandlerBenchmarkBase {

    // ------------------------------------------------------------------------
    // Configurable test parameters
    // ------------------------------------------------------------------------

    /** Nb of runs per test, result will be averaged */
    private static final int NB_RUNS = 10;

    /** Trace/log events per run */
    private static final int NB_ITER = 100000;

    /** Which tests to run (for different number of threads) */
    private static final int[] NB_THREADS = {1, 1, 2, 3, 4, 5, 6, 7, 8};

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    protected Logger logger;
    protected Handler handler;

    // ------------------------------------------------------------------------
    // Maintenance methods
    // ------------------------------------------------------------------------

    /**
     * Base test setup
     */
    @BeforeEach
    public void setup() {
        /* Set up the logger */
        logger = Logger.getLogger("Test logger");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);

        /* Sub-classes' @Before will setup the Handler */
    }

    /**
     * Base test teardown
     */
    @AfterEach
    public void teardown() {
        if (handler != null) {
            logger.removeHandler(handler);
            handler.close();
        }
        handler = null;
        logger = null;
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Main test class for running the benchmark
     */
    @Test
    public void runBenchmark() {
        if (logger != null && handler != null) {
            logger.addHandler(handler);
        }

        System.out.println();
        System.out.println("Running benchmark: " + this.getClass().getCanonicalName());
        for (int i : NB_THREADS) {
            runTest(logger, i);
        }
    }

    private static void runTest(Logger log, int nbThreads) {
        long total = 0;
        for (int i = 0; i < NB_RUNS; i++) {
            Runner runner = new Runner(nbThreads, NB_ITER, log);

            long start = System.nanoTime();
            runner.run();
            long end = System.nanoTime();

            total += (end - start);
        }
        long average = (total / NB_RUNS);
        System.out.println(nbThreads + " threads, average = " + average / NB_ITER + " ns/loop");
    }

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------

    private static class Runner implements Runnable {

        private final List<Worker> workers = new LinkedList<>();
        private final List<Thread> workerThreads = new LinkedList<>();

        public Runner(int nbThreads, int nbIter, Logger log) {

            for (int id = 0; id < nbThreads; id++) {
                Worker curWorker = new Worker(id, nbIter, log);
                workers.add(curWorker);
                workerThreads.add(new Thread(curWorker, "worker " + id));
            }
        }

        @Override
        public void run() {
            workerThreads.forEach(Thread::start);

            workerThreads.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        private static class Worker implements Runnable {

            private final Logger log;
            private final int threadId;
            private final int nbIter;

            @SuppressWarnings("unused")
            private volatile int value = 0;

            public Worker(int threadId, int nbIter, Logger log) {
                this.log = log;
                this.threadId = threadId;
                this.nbIter = nbIter;
            }

            @Override
            public void run() {
                for (int i = 0; i < nbIter; i++) {
                    value = i;
                    if (log != null) {
                        log.info("Thread " + threadId + ", iteration " + i);
                    }
                }
            }
        }
    }
}
