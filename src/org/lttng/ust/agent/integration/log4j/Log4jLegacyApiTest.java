package org.lttng.ust.agent.integration.log4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.log4j.LttngLogAppender;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

@SuppressWarnings("deprecation")
public class Log4jLegacyApiTest {

    private static final Domain DOMAIN = Domain.LOG4J;

    private static final String EVENT_NAME_A = "EventA";
    private static final String EVENT_NAME_B = "EventB";

    private Logger loggerA;
    private Logger loggerB;

    @BeforeClass
    public static void classSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        try {
            LttngLogAppender testAppender = new LttngLogAppender();
            testAppender.close();
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
    public static void classCleanup() {
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

        Log4jEnabledEventsTest.send10Events(loggerA);
        Log4jEnabledEventsTest.send10Events(loggerB);

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

        Log4jEnabledEventsTest.send10Events(loggerA);
        Log4jEnabledEventsTest.send10Events(loggerB);

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

        Log4jEnabledEventsTest.send10Events(loggerA);
        Log4jEnabledEventsTest.send10Events(loggerB);

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(10, output.size());

        assertTrue(LttngSessionControl.destroySession(null));

        ILttngHandler handler = getAgentHandler();
        assertEquals(10, handler.getEventCount());
    }

    /**
     * Get the singleton Log4j Handler currently managed by the LTTngAgent. It
     * is not public, so we need reflection to access it.
     *
     * @return The agent's Log4j handler
     */
    private ILttngHandler getAgentHandler() {
        try {
            Field log4jAppenderField = LTTngAgent.class.getDeclaredField("log4jAppender");
            log4jAppenderField.setAccessible(true);
            return (ILttngHandler) log4jAppenderField.get(LTTngAgent.getLTTngAgent());
        } catch (ReflectiveOperationException | SecurityException e) {
            fail();
            return null;
        }
    }

}

