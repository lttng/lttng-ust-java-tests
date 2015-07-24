package org.lttng.ust.agent.integration.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

public abstract class EnabledEventsTest {

    protected static final String EVENT_NAME_A = "EventA";
    protected static final String EVENT_NAME_B = "EventAB";
    protected static final String EVENT_NAME_C = "EventABC";
    protected static final String EVENT_NAME_D = "EventABCD";

    /* Fields defined by the sub-class */
    protected ILttngHandler handlerA;
    protected ILttngHandler handlerB;
    protected ILttngHandler handlerC;

    protected abstract Domain getDomain();

    protected abstract void sendEventsToLoggers();

    @Before
    public void testSetup() {

    }

    @After
    public void testTeardown() {
        /* In case the test fails before destroying the session */
        LttngSessionControl.tryDestroySession(null);

        handlerA.close();
        handlerB.close();
        handlerC.close();

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
        assertTrue(LttngSessionControl.setupSession(null, getDomain()));

        sendEventsToLoggers();

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertTrue(output.isEmpty());

        assertTrue(LttngSessionControl.destroySession(null));

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
        assertTrue(LttngSessionControl.setupSessionAllEvents(null, getDomain()));

        sendEventsToLoggers();

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(30, output.size()); // loggerD has no handler attached

        assertTrue(LttngSessionControl.destroySession(null));

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
        assertTrue(LttngSessionControl.setupSession(null, getDomain(),
                EVENT_NAME_A, EVENT_NAME_C, EVENT_NAME_D));

        sendEventsToLoggers();

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(20, output.size());

        assertTrue(LttngSessionControl.destroySession(null));

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
        assertTrue(LttngSessionControl.setupSessionAllEvents(null, getDomain()));
        assertTrue(LttngSessionControl.enableEvents(null, getDomain(),
                EVENT_NAME_A, EVENT_NAME_B));

        sendEventsToLoggers();

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(30, output.size());

        assertTrue(LttngSessionControl.destroySession(null));

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
        assertTrue(LttngSessionControl.setupSession(null, getDomain(),
                EVENT_NAME_A, EVENT_NAME_C, EVENT_NAME_D));

        assertTrue(LttngSessionControl.disableEvents(null, getDomain(),
                EVENT_NAME_C));

        sendEventsToLoggers();

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(10, output.size());

        assertTrue(LttngSessionControl.destroySession(null));

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
        assertTrue(LttngSessionControl.setupSession(null, getDomain(),
                "EventAB*")); // should match event/loggers B and C, but not A.

        sendEventsToLoggers();

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(20, output.size());

        assertTrue(LttngSessionControl.destroySession(null));

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
        assertTrue(LttngSessionControl.setupSession(null, getDomain(),
                "EventAB*", "EventABC*")); // should still match B and C

        sendEventsToLoggers();

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(20, output.size());

        assertTrue(LttngSessionControl.destroySession(null));

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
        assertTrue(LttngSessionControl.setupSessionAllEvents(null, getDomain()));
        assertTrue(LttngSessionControl.enableEvents(null, getDomain(),
                "EventAB*")); // should match B and C

        sendEventsToLoggers();

        assertTrue(LttngSessionControl.stopSession(null));

        List<String> output = LttngSessionControl.viewSession(null);
        assertNotNull(output);
        assertEquals(30, output.size());

        assertTrue(LttngSessionControl.destroySession(null));

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
    }
}
