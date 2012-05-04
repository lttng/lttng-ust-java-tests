package org.lttng.ust.tests;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.lttng.ust.LTTngUst;

public class Benchmark {

    private static final int NB_RUNS = 5;
    private static final int NB_THREADS = 2;

    /**
     * @param args
     * @throws IOException
     * @throws SecurityException
     */
    public static void main(String[] args) throws SecurityException,
            IOException {
        Runner runner;
        long start, end, average, total = 0;

        final Logger log;
        final FileHandler fh;

        /* Set up the logger */
        log = Logger.getLogger("Test logger");
        log.setUseParentHandlers(false);
        fh = new FileHandler("/tmp/log", false);
        fh.setFormatter(new SimpleFormatter());
        // log.addHandler(fh);
        log.setLevel(Level.ALL);

        /* Set up the tracer */
        LTTngUst.init();
        System.out.println("Press a key to start.");
        System.in.read();

        for (int i = 0; i < NB_RUNS; i++) {
            runner = new Runner(NB_THREADS, log);

            start = System.nanoTime();
            runner.run();
            end = System.nanoTime();

            total += (end - start);
        }
        average = total / NB_RUNS;
        System.out.println("Average = " + average / 1000000 + " ms");
    }

}
