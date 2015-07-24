package org.lttng.ust.agent.integration.jul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

public class JulEnabledEventsTest {

    private static final Domain DOMAIN = Domain.JUL;

    private static final String SESSION_NAME = JulEnabledEventsTest.class.getSimpleName();

    private static final String EVENT_NAME_A = "EventA";
    private static final String EVENT_NAME_B = "EventAB";
    private static final String EVENT_NAME_C = "EventABC";
    private static final String EVENT_NAME_D = "EventABCD";

    private Logger loggerA;
    private Logger loggerB;
    private Logger loggerC;
    private Logger loggerD;

    private LttngLogHandler handlerA;
    private LttngLogHandler handlerB;
    private LttngLogHandler handlerC;

    @BeforeClass
    public static void classSetup() {
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
    public static void classCleanup() {
        LttngSessionControl.deleteAllTracee();
    }

    @Before
    public void setup() throws SecurityException, IOException {
        // TODO Wipe all existing LTTng sessions?

        loggerA = Logger.getLogger(EVENT_NAME_A);
        loggerB = Logger.getLogger(EVENT_NAME_B);
        loggerC = Logger.getLogger(EVENT_NAME_C);
        loggerD = Logger.getLogger(EVENT_NAME_D);

        loggerA.setLevel(Level.ALL);
        loggerB.setLevel(Level.ALL);
        loggerC.setLevel(Level.ALL);
        loggerD.setLevel(Level.ALL);

        handlerA = new LttngLogHandler();
        handlerB = new LttngLogHandler();
        handlerC = new LttngLogHandler();

        loggerA.addHandler(handlerA);
        loggerB.addHandler(handlerB);
        loggerC.addHandler(handlerC);
    }

    @After
    public void teardown() {
        /* Just in case the test failed */
        LttngSessionControl.tryDestroySession(SESSION_NAME);

        loggerA.removeHandler(handlerA);
        loggerB.removeHandler(handlerB);
        loggerC.removeHandler(handlerC);

        handlerA.close();
        handlerB.close();
        handlerC.close();

        loggerA = null;
        loggerB = null;
        loggerC = null;
        loggerD = null;
        handlerA = null;
        handlerB = null;
        handlerC = null;
    }

    /**
     * Test sending events on the Java side, but no events enabled in the
     * tracing session. There should be nothing in the resulting trace, and
     * handlers should not have logged anything.
     */
    @Test
    public void testNoEvents() {
        assertTrue(LttngSessionControl.setupSession(SESSION_NAME, DOMAIN));

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession(SESSION_NAME));

        List<String> output = LttngSessionControl.viewSession(SESSION_NAME);
        assertNotNull(output);
        assertTrue(output.isEmpty());

        assertTrue(LttngSessionControl.destroySession(SESSION_NAME));

        assertEquals(0, handlerA.getEventCount());
        assertEquals(0, handlerB.getEventCount());
        assertEquals(0, handlerC.getEventCount());
    }

    /**
     * Test sending events on the Java side, and all events enabled in the
     * tracing session. All handlers should have sent their events.
     */
    @Test
    public void testAllEvents() {
        assertTrue(LttngSessionControl.setupSessionAllEvents(SESSION_NAME, DOMAIN));

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession(SESSION_NAME));

        List<String> output = LttngSessionControl.viewSession(SESSION_NAME);
        assertNotNull(output);
        assertEquals(30, output.size()); // loggerD has no handler attached

        assertTrue(LttngSessionControl.destroySession(SESSION_NAME));

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
    }

    /**
     * Test sending events on the Java side, with only some of them enabled in
     * the tracing session. Only the subset that is enabled should be received.
     */
    @Test
    public void testSomeEvents() {
        assertTrue(LttngSessionControl.setupSession(SESSION_NAME, DOMAIN,
                EVENT_NAME_A, EVENT_NAME_C, EVENT_NAME_D));

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession(SESSION_NAME));

        List<String> output = LttngSessionControl.viewSession(SESSION_NAME);
        assertNotNull(output);
        assertEquals(20, output.size());

        assertTrue(LttngSessionControl.destroySession(SESSION_NAME));

        assertEquals(10, handlerA.getEventCount());
        assertEquals(0, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
    }

    /**
     * Test with all events enabled (-a), plus some other events added manually.
     * Events should still be retained, but there should be no duplicates.
     */
    @Test
    public void testAllEventsAndSome() {
        assertTrue(LttngSessionControl.setupSessionAllEvents(SESSION_NAME, DOMAIN));
        assertTrue(LttngSessionControl.enableEvents(SESSION_NAME, DOMAIN,
                EVENT_NAME_A, EVENT_NAME_B));

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession(SESSION_NAME));

        List<String> output = LttngSessionControl.viewSession(SESSION_NAME);
        assertNotNull(output);
        assertEquals(30, output.size());

        assertTrue(LttngSessionControl.destroySession(SESSION_NAME));

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
    }

    /**
     * Same as {@link #testSomeEvents()}, but some events were enabled first,
     * then disabled. Makes sure the enabled-event refcounting works properly.
     */
    @Test
    public void testSomeEventsAfterDisabling() {
        assertTrue(LttngSessionControl.setupSession(SESSION_NAME, DOMAIN,
                EVENT_NAME_A, EVENT_NAME_C, EVENT_NAME_D));

        assertTrue(LttngSessionControl.disableEvents(SESSION_NAME, DOMAIN,
                EVENT_NAME_C));

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession(SESSION_NAME));

        List<String> output = LttngSessionControl.viewSession(SESSION_NAME);
        assertNotNull(output);
        assertEquals(10, output.size());

        assertTrue(LttngSessionControl.destroySession(SESSION_NAME));

        assertEquals(10, handlerA.getEventCount());
        assertEquals(0, handlerB.getEventCount());
        assertEquals(0, handlerC.getEventCount());
    }

    /**
     * Test enabling an event prefix, which means an event name ending with a *,
     * to match all events starting with this name.
     */
    @Test
    public void testEventPrefix() {
        assertTrue(LttngSessionControl.setupSession(SESSION_NAME, DOMAIN,
                "EventAB*")); // should match event/loggers B and C, but not A.

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession(SESSION_NAME));

        List<String> output = LttngSessionControl.viewSession(SESSION_NAME);
        assertNotNull(output);
        assertEquals(20, output.size());

        assertTrue(LttngSessionControl.destroySession(SESSION_NAME));

        assertEquals(0, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
    }

    /**
     * Same as {@link #testEventPrefix()}, but with multiple prefixes that
     * overlap. There should not be any duplicate events in the trace or in the
     * handlers.
     */
    @Test
    public void testEventPrefixOverlapping() {
        assertTrue(LttngSessionControl.setupSession(SESSION_NAME, DOMAIN,
                "EventAB*", "EventABC*")); // should still match B and C

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession(SESSION_NAME));

        List<String> output = LttngSessionControl.viewSession(SESSION_NAME);
        assertNotNull(output);
        assertEquals(20, output.size());

        assertTrue(LttngSessionControl.destroySession(SESSION_NAME));

        assertEquals(0, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
    }

    /**
     * Test with all events enabled (-a), plus an event prefix. Once again,
     * there should be no duplicates.
     */
    @Test
    public void testAllEventsAndPrefix() {
        assertTrue(LttngSessionControl.setupSessionAllEvents(SESSION_NAME, DOMAIN));
        assertTrue(LttngSessionControl.enableEvents(SESSION_NAME, DOMAIN,
                "EventAB*")); // should match B and C

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession(SESSION_NAME));

        List<String> output = LttngSessionControl.viewSession(SESSION_NAME);
        assertNotNull(output);
        assertEquals(30, output.size());

        assertTrue(LttngSessionControl.destroySession(SESSION_NAME));

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
    }

    private static void send10Events(Logger logger) {
        String a = new String("a");
        Object[] params = { a, new String("b"), new Object() };

        // Levels are FINE, FINER, FINEST, INFO, SEVERE, WARNING
        logger.fine("A fine level message");
        logger.finer("A finer level message");
        logger.finest("A finest level message");
        logger.info("A info level message");
        logger.severe("A severe level message");
        logger.warning("A warning level message");
        logger.warning("Another warning level message");
        logger.log(Level.WARNING, "A warning message using Logger.log()");
        logger.log(Level.INFO, "A message with one parameter", a);
        logger.log(Level.INFO, "A message with parameters", params);
    }

}
