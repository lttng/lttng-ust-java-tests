/*
 * Copyright (C) 2015, EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.lttng.ust.agent.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.utils.TestPrintRunner;

/**
 * Base abstract class to implement all sorts of integration tests verifying the
 * presence of enabled events in resulting traces.
 */
@RunWith(TestPrintRunner.class)
public abstract class EnabledEventsITBase {

    protected static final String EVENT_NAME_A = "EventA";
    protected static final String EVENT_NAME_B = "EventAB";
    protected static final String EVENT_NAME_C = "EventABC";
    protected static final String EVENT_NAME_D = "EventABCD";

    private ILttngSession session;

    /* Fields defined by the sub-class */
    protected ILttngHandler handlerA;
    protected ILttngHandler handlerB;
    protected ILttngHandler handlerC;

    protected abstract Domain getDomain();

    protected abstract void sendEventsToLoggers();

    /**
     * Base test setup
     */
    @Before
    public void testSetup() {
        session = ILttngSession.createSession(null, getDomain());
    }

    /**
     * Base test teardown
     */
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
