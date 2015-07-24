package org.lttng.ust.agent.benchmarks.jul.handler.lttng;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.benchmarks.jul.handler.AbstractJulBenchmark;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

public class LttngJulHandlerTracingEnabledBenchmark extends AbstractJulBenchmark {

    @Before
    public void testSetup() throws IOException {
        handler = new LttngLogHandler();

        assertTrue(LttngSessionControl.setupSessionAllEvents(null, Domain.JUL));
    }

    @After
    public void testTeardown() {
        assertTrue(LttngSessionControl.stopSession(null));
        assertTrue(LttngSessionControl.destroySession(null));
    }
}
