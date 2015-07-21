package org.lttng.ust.agent.jul.benchmarks;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.junit.Before;

public class DummyHandlerBenchmark extends AbstractJulBenchmark {

    @Before
    public void testSetup() {
        handler = new DummyHandler();
    }

    private static class DummyHandler extends Handler {

        public DummyHandler() {
            super();
        }

        @Override
        public void close() throws SecurityException {}

        @Override
        public void flush() {}

        @Override
        public void publish(LogRecord record) {}

    }
}
