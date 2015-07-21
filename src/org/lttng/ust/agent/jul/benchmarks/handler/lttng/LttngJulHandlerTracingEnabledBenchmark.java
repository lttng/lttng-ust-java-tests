package org.lttng.ust.agent.jul.benchmarks.handler.lttng;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.jul.LTTngLogHandler;
import org.lttng.ust.agent.jul.benchmarks.handler.AbstractJulBenchmark;
import org.lttng.ust.agent.jul.benchmarks.utils.LttngSessionControl;

public class LttngJulHandlerTracingEnabledBenchmark extends AbstractJulBenchmark {

    @Before
    public void testSetup() throws IOException {
        handler = new LTTngLogHandler(true);

        assertTrue(LttngSessionControl.setupJulSessionAllEvents());
    }

    @After
    public void testTeardown() {
        assertTrue(LttngSessionControl.stopSession());
        assertTrue(LttngSessionControl.destroySession());
    }
}
