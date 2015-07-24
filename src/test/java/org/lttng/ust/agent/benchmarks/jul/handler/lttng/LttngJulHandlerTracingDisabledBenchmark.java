package org.lttng.ust.agent.benchmarks.jul.handler.lttng;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.benchmarks.jul.handler.AbstractJulBenchmark;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngSession;
import org.lttng.ust.agent.utils.LttngSession.Domain;

public class LttngJulHandlerTracingDisabledBenchmark extends AbstractJulBenchmark {

    private LttngSession session;

    @Before
    public void testSetup() throws IOException {
        handler = new LttngLogHandler();

        session = new LttngSession(null, Domain.JUL);
        assertTrue(session.enableEvents("non-event"));
        assertTrue(session.start());
    }

    @After
    public void testTeardown() {
        assertTrue(session.stop());
        session.close();
    }
}
