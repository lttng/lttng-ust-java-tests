package org.lttng.ust.agent.benchmarks.jul.handler.lttng.old;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngSession;
import org.lttng.ust.agent.utils.LttngSession.Domain;

@SuppressWarnings("deprecation")
public class OldLttngJulHandlerTracingEnabledBenchmark extends JulHandlerBenchmarkBase {

    private LttngSession session;
    private LttngLogHandler agentHandler;

    @Before
    public void testSetup() {
        LTTngAgent agentInstance = LTTngAgent.getLTTngAgent();

        /*
         * The "old API" works by attaching a handler managed by the agent to
         * the root JUL logger. This causes problems here, because we use
         * logger.setUserParentHandler(false), which does not trigger the
         * handler as would be expected.
         *
         * Instead we will retrieve this handler through reflection, and attach
         * it to our own logger here for the duration of the test.
         */
        try {
            Field julHandlerField = LTTngAgent.class.getDeclaredField("julHandler");
            julHandlerField.setAccessible(true);
            agentHandler = (LttngLogHandler) julHandlerField.get(agentInstance);

            logger.addHandler(agentHandler);

        } catch (ReflectiveOperationException e) {
            fail();
        }

        session = new LttngSession(null, Domain.JUL);
        assertTrue(session.enableAllEvents());
        assertTrue(session.start());
    }

    @After
    public void testTeardown() {
        assertTrue(session.stop());
        session.close();

        logger.removeHandler(agentHandler);
        LTTngAgent.dispose();
    }
}
