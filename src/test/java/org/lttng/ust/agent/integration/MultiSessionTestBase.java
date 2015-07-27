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
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.utils.LttngSession;
import org.lttng.ust.agent.utils.LttngSession.Domain;

/**
 * Base abstract class for tests with multiple concurrent tracing sessions
 */
public abstract class MultiSessionTestBase {

    protected static final String EVENT_NAME_A = "EventA";
    protected static final String EVENT_NAME_B = "EventAB";
    protected static final String EVENT_NAME_C = "EventABC";
    protected static final String EVENT_NAME_D = "EventABCD";

    private LttngSession session1;
    private LttngSession session2;
    private LttngSession session3;

    /* Fields defined by the sub-class */
    protected ILttngHandler handlerA;
    protected ILttngHandler handlerB;
    protected ILttngHandler handlerC;
    protected ILttngHandler handlerD;

    protected abstract Domain getDomain();

    protected abstract void sendEventsToLoggers();

    /**
     * Base test setup
     */
    @Before
    public void testSetup() {
        session1 = new LttngSession(null, getDomain());
        session2 = new LttngSession(null, getDomain());
        session3 = new LttngSession(null, getDomain());
    }

    /**
     * Base test teardown
     */
    @After
    public void testTeardown() {
        session1.close();
        session2.close();
        session3.close();

        handlerA.close();
        handlerB.close();
        handlerC.close();
        handlerD.close();

        handlerA = null;
        handlerB = null;
        handlerC = null;
        handlerD = null;
    }

    /**
     * Test with no events in any session.
     */
    @Test
    public void testNoEvents() {
        assertTrue(session1.start());
        assertTrue(session2.start());
        assertTrue(session3.start());

        sendEventsToLoggers();

        assertTrue(session1.stop());
        assertTrue(session2.stop());
        assertTrue(session3.stop());

        List<String> output1 = session1.view();
        List<String> output2 = session2.view();
        List<String> output3 = session3.view();
        assertNotNull(output1);
        assertNotNull(output2);
        assertNotNull(output3);
        assertTrue(output1.isEmpty());
        assertTrue(output2.isEmpty());
        assertTrue(output3.isEmpty());

        assertEquals(0, handlerA.getEventCount());
        assertEquals(0, handlerB.getEventCount());
        assertEquals(0, handlerC.getEventCount());
        assertEquals(0, handlerD.getEventCount());
    }

    /**
     * Test with all events enabled in one session only. Everything should be
     * sent through JNI, but only that session should keep the trace events.
     */
    @Test
    public void testAllEventsOneSession() {
        assertTrue(session1.enableAllEvents());
        assertTrue(session1.start());
        assertTrue(session2.start());
        assertTrue(session3.start());

        sendEventsToLoggers();

        assertTrue(session1.stop());
        assertTrue(session2.stop());
        assertTrue(session3.stop());

        List<String> output1 = session1.view();
        List<String> output2 = session2.view();
        List<String> output3 = session3.view();
        assertNotNull(output1);
        assertNotNull(output2);
        assertNotNull(output3);
        assertEquals(40, output1.size());
        assertTrue(output2.isEmpty());
        assertTrue(output3.isEmpty());

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
        assertEquals(10, handlerD.getEventCount());
    }

    /**
     * Test with all events enabled in all sessions. All traces and handlers
     * should see every event that was logged.
     */
    @Test
    public void testAllEventsAllSessions() {
        assertTrue(session1.enableAllEvents());
        assertTrue(session2.enableAllEvents());
        assertTrue(session3.enableAllEvents());
        assertTrue(session1.start());
        assertTrue(session2.start());
        assertTrue(session3.start());

        sendEventsToLoggers();

        assertTrue(session1.stop());
        assertTrue(session2.stop());
        assertTrue(session3.stop());

        List<String> output1 = session1.view();
        List<String> output2 = session2.view();
        List<String> output3 = session3.view();
        assertNotNull(output1);
        assertNotNull(output2);
        assertNotNull(output3);
        assertEquals(40, output1.size());
        assertEquals(40, output2.size());
        assertEquals(40, output3.size());

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
        assertEquals(10, handlerD.getEventCount());
    }

    /**
     * Test enabling some events in some sessions only.
     */
    @Test
    public void testSomeEvents() {
        assertTrue(session1.enableEvents(EVENT_NAME_A));
        assertTrue(session2.enableEvents(EVENT_NAME_B));
        assertTrue(session1.start());
        assertTrue(session2.start());
        assertTrue(session3.start());

        sendEventsToLoggers();

        assertTrue(session1.stop());
        assertTrue(session2.stop());
        assertTrue(session3.stop());

        List<String> output1 = session1.view();
        List<String> output2 = session2.view();
        List<String> output3 = session3.view();
        assertNotNull(output1);
        assertNotNull(output2);
        assertNotNull(output3);
        assertEquals(10, output1.size());
        assertEquals(10, output2.size());
        assertEquals(0, output3.size());

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(0, handlerC.getEventCount());
        assertEquals(0, handlerD.getEventCount());
    }

    /**
     * Test with all events enabled in one session, and some others in another.
     * All events should arrive where expected, with no duplicates.
     */
    @Test
    public void testAllEventsAndSome() {
        assertTrue(session1.enableAllEvents());
        assertTrue(session2.enableEvents(EVENT_NAME_D));
        assertTrue(session1.start());
        assertTrue(session2.start());
        assertTrue(session3.start());

        sendEventsToLoggers();

        assertTrue(session1.stop());
        assertTrue(session2.stop());
        assertTrue(session3.stop());

        List<String> output1 = session1.view();
        List<String> output2 = session2.view();
        List<String> output3 = session3.view();
        assertNotNull(output1);
        assertNotNull(output2);
        assertNotNull(output3);
        assertEquals(40, output1.size());
        assertEquals(10, output2.size());
        assertEquals(0, output3.size());

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
        assertEquals(10, handlerD.getEventCount());
    }

    /**
     * Test with enabling then disabling some events. Makes sure the refcounting
     * works properly.
     */
    @Test
    public void testSomeEventsAfterDisabling() {
        assertTrue(session1.enableEvents(EVENT_NAME_A, EVENT_NAME_B, EVENT_NAME_C));
        assertTrue(session2.enableEvents(EVENT_NAME_B, EVENT_NAME_C, EVENT_NAME_D));
        assertTrue(session3.enableEvents(EVENT_NAME_A));

        assertTrue(session1.disableEvents(EVENT_NAME_C));
        assertTrue(session2.disableEvents(EVENT_NAME_B, EVENT_NAME_C));
        assertTrue(session3.disableEvents(EVENT_NAME_A));

        assertTrue(session1.start());
        assertTrue(session2.start());
        assertTrue(session3.start());

        sendEventsToLoggers();

        assertTrue(session1.stop());
        assertTrue(session2.stop());
        assertTrue(session3.stop());

        List<String> output1 = session1.view();
        List<String> output2 = session2.view();
        List<String> output3 = session3.view();
        assertNotNull(output1);
        assertNotNull(output2);
        assertNotNull(output3);
        assertEquals(20, output1.size());
        assertEquals(10, output2.size());
        assertEquals(0, output3.size());

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(0, handlerC.getEventCount());
        assertEquals(10, handlerD.getEventCount());
    }

    /**
     * Test with a prefix in one session and a standard event in another.
     */
    @Test
    public void testPrefixAndEvent() {
        assertTrue(session1.enableEvents("EventAB*"));
        assertTrue(session3.enableEvents(EVENT_NAME_A));
        assertTrue(session1.start());
        assertTrue(session2.start());
        assertTrue(session3.start());

        sendEventsToLoggers();

        assertTrue(session1.stop());
        assertTrue(session2.stop());
        assertTrue(session3.stop());

        List<String> output1 = session1.view();
        List<String> output2 = session2.view();
        List<String> output3 = session3.view();
        assertNotNull(output1);
        assertNotNull(output2);
        assertNotNull(output3);
        assertEquals(30, output1.size());
        assertEquals(0, output2.size());
        assertEquals(10, output3.size());

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
        assertEquals(10, handlerD.getEventCount());
    }

    /**
     * Test with all events enabled in one session, and an event prefix in
     * another. Once again, there should be no duplicates.
     */
    @Test
    public void testAllEventsAndPrefix() {
        assertTrue(session1.enableAllEvents());
        assertTrue(session2.enableEvents("EventABC*"));
        assertTrue(session1.start());
        assertTrue(session2.start());
        assertTrue(session3.start());

        sendEventsToLoggers();

        assertTrue(session1.stop());
        assertTrue(session2.stop());
        assertTrue(session3.stop());

        List<String> output1 = session1.view();
        List<String> output2 = session2.view();
        List<String> output3 = session3.view();
        assertNotNull(output1);
        assertNotNull(output2);
        assertNotNull(output3);
        assertEquals(40, output1.size());
        assertEquals(20, output2.size());
        assertEquals(0, output3.size());

        assertEquals(10, handlerA.getEventCount());
        assertEquals(10, handlerB.getEventCount());
        assertEquals(10, handlerC.getEventCount());
        assertEquals(10, handlerD.getEventCount());
    }
}
