package org.lttng.ust.agent.integration.jul;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

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
import org.lttng.ust.agent.utils.LttngSession;
import org.lttng.ust.agent.utils.LttngSession.Domain;
import org.lttng.ust.agent.utils.TestUtils;

@SuppressWarnings("deprecation")
public class JulLegacyApiTest {

    private static final Domain DOMAIN = Domain.JUL;

    private static final String EVENT_NAME_A = "EventA";
    private static final String EVENT_NAME_B = "EventB";

    private LttngSession session;

    private Logger loggerA;
    private Logger loggerB;

    @BeforeClass
    public static void julClassSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        assumeTrue(TestUtils.checkForJulLibrary());
        assumeTrue(TestUtils.checkForLttngTools(Domain.JUL));
    }

    @AfterClass
    public static void julClassCleanup() {
        LttngSession.deleteAllTracee();
    }

    @Before
    public void setup() {
        loggerA = Logger.getLogger(EVENT_NAME_A);
        LTTngAgent.getLTTngAgent();
        loggerB = Logger.getLogger(EVENT_NAME_B);

        loggerA.setLevel(Level.ALL);
        loggerB.setLevel(Level.ALL);

        session = new LttngSession(null, DOMAIN);
    }

    @After
    public void tearDown() {
        session.close();

        LTTngAgent.dispose();

        loggerA = null;
        loggerB = null;
    }

    @Test
    public void testNoEvents() {
        assertTrue(session.start());

        JulTestUtils.send10EventsTo(loggerA);
        JulTestUtils.send10EventsTo(loggerB);

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertTrue(output.isEmpty());

        ILttngHandler handler = getAgentHandler();
        assertEquals(0, handler.getEventCount());
    }

    @Test
    public void testAllEvents() {
        assertTrue(session.enableAllEvents());
        assertTrue(session.start());

        JulTestUtils.send10EventsTo(loggerA);
        JulTestUtils.send10EventsTo(loggerB);

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(20, output.size());

        ILttngHandler handler = getAgentHandler();
        assertEquals(20, handler.getEventCount());
    }

    @Test
    public void testSomeEvents() {
        assertTrue(session.enableEvents(EVENT_NAME_A));
        assertTrue(session.start());

        JulTestUtils.send10EventsTo(loggerA);
        JulTestUtils.send10EventsTo(loggerB);

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(10, output.size());

        ILttngHandler handler = getAgentHandler();
        assertEquals(10, handler.getEventCount());
    }

    /**
     * Get the singleton JUL Handler currently managed by the LTTngAgent. It is
     * not public, so we need reflection to access it.
     *
     * @return The agent's JUL handler
     */
    private static ILttngHandler getAgentHandler() {
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

