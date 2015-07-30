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
import org.junit.runner.RunWith;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.tools.utils.LttngUtils;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.utils.TestPrintRunner;

/**
 * Enabled events test for the LTTng-UST JUL log handler, using the legacy API.
 */
@RunWith(TestPrintRunner.class)
@SuppressWarnings("deprecation")
public class JulLegacyApiTest {

    private static final Domain DOMAIN = Domain.JUL;

    private static final String EVENT_NAME_A = "EventA";
    private static final String EVENT_NAME_B = "EventB";

    private ILttngSession session;

    private Logger loggerA;
    private Logger loggerB;

    /**
     * Class setup
     */
    @BeforeClass
    public static void julClassSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        assumeTrue(LttngUtils.checkForJulLibrary());
        assumeTrue(LttngUtils.checkForLttngTools(Domain.JUL));

        LttngToolsHelper.destroyAllSessions();
    }

    /**
     * Class cleanup
     */
    @AfterClass
    public static void julClassCleanup() {
        LttngToolsHelper.deleteAllTraces();
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

        session = ILttngSession.newCommandLineSession(null, DOMAIN);
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

        JulTestUtils.send10EventsTo(loggerA);
        JulTestUtils.send10EventsTo(loggerB);

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertTrue(output.isEmpty());

        ILttngHandler handler = getAgentHandler();
        assertEquals(0, handler.getEventCount());
    }

    /**
     * Test tracing with all events enabled (-j -a) in the tracing session.
     */
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

    /**
     * Test tracing with a subset of events enabled in the tracing session.
     */
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

