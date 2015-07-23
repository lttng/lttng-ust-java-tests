package org.lttng.ust.agent.benchmarks.jul.handler.lttng.old;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.benchmarks.jul.handler.AbstractJulBenchmark;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

@SuppressWarnings("deprecation")
public class OldLttngJulHandlerTracingEnabledBenchmark extends AbstractJulBenchmark {

    private LttngLogHandler agentHandler;

    @Before
    public void testSetup() throws IOException {
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

        assertTrue(LttngSessionControl.setupSessionAllEvents(null, Domain.JUL));
    }

    @After
    public void testTeardown() {
        assertTrue(LttngSessionControl.stopSession());
        assertTrue(LttngSessionControl.destroySession());

        logger.removeHandler(agentHandler);
        LTTngAgent.dispose();
    }
}
