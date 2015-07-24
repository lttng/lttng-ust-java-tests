package org.lttng.ust.agent.benchmarks.jul.handler.lttng.old;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.benchmarks.jul.handler.AbstractJulBenchmark;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

@SuppressWarnings("deprecation")
public class OldLttngJulHandlerTracingDisabledBenchmark extends AbstractJulBenchmark {

    @Before
    public void testSetup() throws IOException {
        LTTngAgent.getLTTngAgent();

        assertTrue(LttngSessionControl.setupSession(null, Domain.JUL));
    }

    @After
    public void testTeardown() {
        assertTrue(LttngSessionControl.stopSession(null));
        assertTrue(LttngSessionControl.destroySession(null));

        LTTngAgent.dispose();
    }
}
