package org.lttng.ust.agent.integration.jul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

public class JulEnabledEventsTest {

    private static final Domain DOMAIN = Domain.JUL;

    private static final String EVENT_NAME_A = "EventA";
    private static final String EVENT_NAME_B = "EventAB";
    private static final String EVENT_NAME_C = "EventABC";
    private static final String EVENT_NAME_D = "EventABCD";

    private Logger loggerA;
    private Logger loggerB;
    private Logger loggerC;
    private Logger loggerD;

    private Handler handlerA;
    private Handler handlerB;
    private Handler handlerC;

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
        boolean ret2 = LttngSessionControl.stopSession();
        boolean ret3 = LttngSessionControl.destroySession();
        assumeTrue(ret1 && ret2 && ret3);
    }

    @Before
    public void setup() throws SecurityException, IOException {
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
        loggerC.addHandler(handlerB);
    }

    @After
    public void teardown() {
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
     * tracing session. There should be nothing in the resulting trace.
     */
    @Test
    public void testNoEvents() {
        assertTrue(LttngSessionControl.setupSession(null, DOMAIN));

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession());

        List<String> output = LttngSessionControl.viewSession();
        assertNotNull(output);
        assertTrue(output.isEmpty());

        assertTrue(LttngSessionControl.destroySession());
    }

    /**
     * Test sending events on the Java side, and all events enabled in the
     * tracing session. All handlers should have sent their events.
     */
    @Test
    public void testAllEvents() {
        assertTrue(LttngSessionControl.setupSessionAllEvents(null, DOMAIN));

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession());

        List<String> output = LttngSessionControl.viewSession();
        assertNotNull(output);
        assertEquals(20, output.size()); // loggerC has no handler attached

        assertTrue(LttngSessionControl.destroySession());
    }

    /**
     * Test sending events on the Java side, with only some of them enabled in
     * the tracing session. Only the subset that is enabled should be received.
     */
    @Test
    public void testSomeEvents() {
        assertTrue(LttngSessionControl.setupSession(null, DOMAIN,
                EVENT_NAME_A, EVENT_NAME_D));

        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);

        assertTrue(LttngSessionControl.stopSession());

        List<String> output = LttngSessionControl.viewSession();
        assertNotNull(output);
        assertEquals(10, output.size()); // loggerC has no handler attached

        assertTrue(LttngSessionControl.destroySession());
    }

    private static void send10Events(Logger logger) {
        String a = new String("a");
        Object[] params = {a, new String("b"), new Object()};

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
