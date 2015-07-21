package org.lttng.ust.agent.jul.benchmarks.handler;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.junit.Before;

public class FileHandlerBenchmark extends AbstractJulBenchmark {

    @Before
    public void testSetup() throws SecurityException, IOException {
        handler = new FileHandler("/tmp/log", false);
        handler.setFormatter(new SimpleFormatter());
    }
}
