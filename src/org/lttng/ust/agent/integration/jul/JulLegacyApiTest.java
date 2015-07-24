package org.lttng.ust.agent.integration.jul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

@SuppressWarnings("deprecation")
public class JulLegacyApiTest {

    private static final Domain DOMAIN = Domain.JUL;

    private static final String EVENT_NAME_A = "EventA";
    private static final String EVENT_NAME_B = "EventB";

    private Logger loggerA;
    private Logger loggerB;

    @BeforeClass
    public static void julClassSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        try {
            LttngLogHandler testHandler = new LttngLogHandler();
            testHandler.close();
        } catch (SecurityException | IOException e) {
            assumeTrue(false);
        }

        boolean ret1 = LttngSessionControl.setupSession(null, DOMAIN);
        boolean ret2 = LttngSessionControl.stopSession(null);
        /* "lttng view" also tests that Babeltrace is installed and working */
        List<String> contents = LttngSessionControl.viewSession(null);
        boolean ret3 = LttngSessionControl.destroySession(null);
        assumeTrue(ret1 && ret2 && ret3);
        assumeTrue(contents.isEmpty());
    }

    @AfterClass
    public static void julClassCleanup() {
        LttngSessionControl.deleteAllTracee();
    }

    @Before
    public void setup() {
        loggerA = Logger.getLogger(EVENT_NAME_A);
        LTTngAgent.getLTTngAgent();
        loggerB = Logger.getLogger(EVENT_NAME_B);

        loggerA.setLevel(Level.ALL);
        loggerB.setLevel(Level.ALL);
    }

    @After
    public void tearDown() {
        /* In case the test fails before destroying the session */
        LttngSessionControl.tryDestroySession(null);

        LTTngAgent.dispose();

        loggerA = null;
        loggerB = null;
    }

    @Test
    public void testNoEvents() {
        assertTrue(LttngSessionControl.setupSession(null, DOMAIN));

        JulEnabledEventsTest.send10Events(loggerA);
        JulEnabledEventsTest.send10Events(loggerB);

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertTrue(output.isEmpty());

        assertTrue(LttngSessionControl.destroySession(null));

        ILttngHandler handler = getAgentHandler();
        assertEquals(0, handler.getEventCount());
    }

    @Test
    public void testAllEvents() {
        assertTrue(LttngSessionControl.setupSessionAllEvents(null, DOMAIN));

        JulEnabledEventsTest.send10Events(loggerA);
        JulEnabledEventsTest.send10Events(loggerB);

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(20, output.size());

        assertTrue(LttngSessionControl.destroySession(null));

        ILttngHandler handler = getAgentHandler();
        assertEquals(20, handler.getEventCount());
    }

    @Test
    public void testSomeEvents() {
        assertTrue(LttngSessionControl.setupSession(null, DOMAIN,
                EVENT_NAME_A));

        JulEnabledEventsTest.send10Events(loggerA);
        JulEnabledEventsTest.send10Events(loggerB);

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(10, output.size());

        assertTrue(LttngSessionControl.destroySession(null));

        ILttngHandler handler = getAgentHandler();
        assertEquals(10, handler.getEventCount());
    }

    /**
     * Get the singleton JUL Handler currently managed by the LTTngAgent. It is
     * not public, so we need reflection to access it.
     *
     * @return The agent's JUL handler
     */
    private ILttngHandler getAgentHandler() {
        try {
            Field julHandlerField = LTTngAgent.class.getDeclaredField("julHandler");
            julHandlerField.setAccessible(true);
            return (ILttngHandler) julHandlerField.get(LTTngAgent.getLTTngAgent());
        } catch (ReflectiveOperationException | SecurityException e) {
            fail();
            return null;
        }
    }

}

