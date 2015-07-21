package org.lttng.ust.agent.jul.benchmarks.handler.lttng.old;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.jul.benchmarks.handler.AbstractJulBenchmark;
import org.lttng.ust.agent.jul.benchmarks.utils.LttngSessionControl;

@SuppressWarnings("deprecation")
public class OldLttngJulHandlerTracingDisabledBenchmark extends AbstractJulBenchmark {

    private LTTngAgent agent;

    @Before
    public void testSetup() throws IOException {
        agent = LTTngAgent.getLTTngAgent();

        assertTrue(LttngSessionControl.setupJulSessionNoEvents());
    }

    @After
    public void testTeardown() {
        assertTrue(LttngSessionControl.stopSession());
        assertTrue(LttngSessionControl.destroySession());

        agent.dispose();
    }
}
