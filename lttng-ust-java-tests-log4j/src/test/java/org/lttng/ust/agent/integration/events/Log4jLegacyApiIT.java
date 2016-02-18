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

package org.lttng.ust.agent.integration.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.utils.Log4jTestUtils;
import org.lttng.ust.agent.utils.TestPrintRunner;

/**
 * Enabled events test for the LTTng-UST Log4j log handler, using the legacy
 * API.
 */
@RunWith(TestPrintRunner.class)
@SuppressWarnings("deprecation")
public class Log4jLegacyApiIT {

    private static final Domain DOMAIN = Domain.LOG4J;

    private static final String EVENT_NAME_A = "EventA";
    private static final String EVENT_NAME_B = "EventB";

    private ILttngSession session;

    private Logger loggerA;
    private Logger loggerB;

    /**
     * Class setup
     */
    @BeforeClass
    public static void log4jClassSetup() {
        Log4jTestUtils.testClassSetup();
    }

    /**
     * Class cleanup
     */
    @AfterClass
    public static void log4jClassCleanup() {
        Log4jTestUtils.testClassCleanup();
    }

    /**
     * Test setup
     */
    @Before
    public void setup() {
        loggerA = Logger.getLogger(EVENT_NAME_A);
        LTTngAgent.getLTTngAgent();
        loggerB = Logger.getLogger(EVENT_NAME_B);

        loggerA.setLevel(Level.ALL);
        loggerB.setLevel(Level.ALL);

        session = ILttngSession.createSession(null, DOMAIN);
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        session.close();

        LTTngAgent.dispose();

        loggerA = null;
        loggerB = null;
    }

    /**
     * Test tracing with no events enabled in the tracing session.
     */
    @Test
    public void testNoEvents() {
        assertTrue(session.start());

        Log4jTestUtils.send10Events(loggerA);
        Log4jTestUtils.send10Events(loggerB);

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertTrue(output.isEmpty());

        ILttngHandler handler = getAgentHandler();
        assertEquals(0, handler.getEventCount());
    }

    /**
     * Test tracing with all events enabled (-l -a) in the tracing session.
     */
    @Test
    public void testAllEvents() {
        assertTrue(session.enableAllEvents());
        assertTrue(session.start());

        Log4jTestUtils.send10Events(loggerA);
        Log4jTestUtils.send10Events(loggerB);

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(20, output.size());

        ILttngHandler handler = getAgentHandler();
        assertEquals(20, handler.getEventCount());
    }

    /**
     * Test tracing with a subset of events enabled in the tracing session.
     */
    @Test
    public void testSomeEvents() {
        assertTrue(session.enableEvents(EVENT_NAME_A));
        assertTrue(session.start());

        Log4jTestUtils.send10Events(loggerA);
        Log4jTestUtils.send10Events(loggerB);

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertEquals(10, output.size());

        ILttngHandler handler = getAgentHandler();
        assertEquals(10, handler.getEventCount());
    }

    /**
     * Get the singleton Log4j Handler currently managed by the LTTngAgent. It
     * is not public, so we need reflection to access it.
     *
     * @return The agent's Log4j handler
     */
    private static ILttngHandler getAgentHandler() {
        try {
            Field log4jAppenderField = LTTngAgent.class.getDeclaredField("log4jAppender");
            log4jAppenderField.setAccessible(true);
            return (ILttngHandler) log4jAppenderField.get(LTTngAgent.getLTTngAgent());
        } catch (ReflectiveOperationException | SecurityException e) {
            fail(e.getMessage());
            return null;
        }
    }

}

