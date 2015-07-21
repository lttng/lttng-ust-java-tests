package org.lttng.ust.agent.jul.benchmarks.handler.lttng.old;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.jul.LTTngJUL;
import org.lttng.ust.agent.jul.LTTngLogHandler;
import org.lttng.ust.agent.jul.benchmarks.handler.AbstractJulBenchmark;
import org.lttng.ust.agent.jul.benchmarks.utils.LttngSessionControl;

@SuppressWarnings("deprecation")
public class OldLttngJulHandlerTracingEnabledBenchmark extends AbstractJulBenchmark {

    private LTTngAgent agent;
    private LTTngLogHandler oldHandler;

    @Before
    public void testSetup() throws IOException {
        agent = LTTngAgent.getLTTngAgent();

        /*
         * The "old API" works by attaching a handler managed by the agent to
         * the root JUL logger. This causes problem here, because we use
         * logger.setUserParentHandler(false), which does not trigger the
         * handler as would be expected.
         *
         * Instead we will retrieve this handler through reflection, and attach
         * it to our own logger here for the duration of the test.
         */
        try {
            Field julRootField = LTTngAgent.class.getDeclaredField("julRoot");
            julRootField.setAccessible(true);
            LTTngJUL lf = (LTTngJUL) julRootField.get(null); // static field

            Field handlerField = LTTngJUL.class.getDeclaredField("handler");
            handlerField.setAccessible(true);
            oldHandler = (LTTngLogHandler) handlerField.get(lf);

            logger.addHandler(oldHandler);

        } catch (ReflectiveOperationException e) {
            fail();
        }

        assertTrue(LttngSessionControl.setupJulSessionAllEvents());
    }

    @After
    public void testTeardown() {
        assertTrue(LttngSessionControl.stopSession());
        assertTrue(LttngSessionControl.destroySession());

        logger.removeHandler(oldHandler);
        agent.dispose();
    }
}
