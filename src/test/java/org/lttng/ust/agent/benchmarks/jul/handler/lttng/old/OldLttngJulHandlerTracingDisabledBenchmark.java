package org.lttng.ust.agent.benchmarks.jul.handler.lttng.old;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;
import org.lttng.ust.agent.utils.LttngSession;
import org.lttng.ust.agent.utils.LttngSession.Domain;

@SuppressWarnings("deprecation")
public class OldLttngJulHandlerTracingDisabledBenchmark extends JulHandlerBenchmarkBase {

    private LttngSession session;

    @Before
    public void testSetup() {
        LTTngAgent.getLTTngAgent();

        session = new LttngSession(null, Domain.JUL);
        assertTrue(session.enableEvents("non-event"));
        assertTrue(session.start());
    }

    @After
    public void testTeardown() {
        assertTrue(session.stop());
        session.close();

        LTTngAgent.dispose();
    }
}
