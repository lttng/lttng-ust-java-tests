package org.lttng.ust.agent.integration.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.utils.LttngSession;
import org.lttng.ust.agent.utils.LttngSession.Domain;

public abstract class EnabledEventsTest {

    protected static final String EVENT_NAME_A = "EventA";
    protected static final String EVENT_NAME_B = "EventAB";
    protected static final String EVENT_NAME_C = "EventABC";
    protected static final String EVENT_NAME_D = "EventABCD";

    private LttngSession session;

    /* Fields defined by the sub-class */
    protected ILttngHandler handlerA;
    protected ILttngHandler handlerB;
    protected ILttngHandler handlerC;

    protected abstract Domain getDomain();

    protected abstract void sendEventsToLoggers();

    @Before
    public void testSetup() {
        session = new LttngSession(null, getDomain());
    }

    @After
    public void testTeardown() {
        session.close();

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
        assertTrue(session.start());

        sendEventsToLoggers();

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertTrue(output.isEmpty());

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
        assertTrue(session.enableAllEvents());
        assertTrue(session.start());

        sendEventsToLoggers();

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(30, output.size()); // loggerD has no handler attached

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
        assertTrue(session.enableEvents(EVENT_NAME_A, EVENT_NAME_C, EVENT_NAME_D));
        assertTrue(session.start());

        sendEventsToLoggers();

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(20, output.size());

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
        assertTrue(session.enableAllEvents());
        assertTrue(session.enableEvents(EVENT_NAME_A, EVENT_NAME_B));
        assertTrue(session.start());

        sendEventsToLoggers();

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(30, output.size());

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
        assertTrue(session.enableEvents(EVENT_NAME_A, EVENT_NAME_C, EVENT_NAME_D));
        assertTrue(session.disableEvents(EVENT_NAME_C));
        assertTrue(session.start());

        sendEventsToLoggers();

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(10, output.size());

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
        // should match event/loggers B and C, but not A.
        assertTrue(session.enableEvents("EventAB*"));
        assertTrue(session.start());

        sendEventsToLoggers();

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(20, output.size());

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
        // should still match B and C
        assertTrue(session.enableEvents("EventAB*", "EventABC*"));
        assertTrue(session.start());

        sendEventsToLoggers();

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(20, output.size());

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
        assertTrue(session.enableAllEvents());
        assertTrue(session.enableEvents("EventAB*"));
        assertTrue(session.start());

        sendEventsToLoggers();

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(30, output.size());

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
    }
}
