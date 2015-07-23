package org.lttng.ust.agent.benchmarks.jul.handler.lttng;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.benchmarks.jul.handler.AbstractJulBenchmark;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

public class LttngJulHandlerTracingDisabledBenchmark extends AbstractJulBenchmark {

    @Before
    public void testSetup() throws IOException {
        handler = new LttngLogHandler();

        assertTrue(LttngSessionControl.setupSession(null, Domain.JUL));
    }

    @After
    public void testTeardown() {
        assertTrue(LttngSessionControl.stopSession());
        assertTrue(LttngSessionControl.destroySession());
    }
}
