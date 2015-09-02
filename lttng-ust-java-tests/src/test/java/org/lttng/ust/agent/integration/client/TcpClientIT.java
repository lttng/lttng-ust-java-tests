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

package org.lttng.ust.agent.integration.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.ust.agent.ILttngAgent;
import org.lttng.ust.agent.client.LttngTcpSessiondClient;
import org.lttng.ust.agent.session.EventRule;
import org.lttng.ust.agent.session.LogLevelSelector;
import org.lttng.ust.agent.session.LogLevelSelector.LogLevelType;
import org.lttng.ust.agent.utils.ILogLevelStrings;

/**
 * Tests for the TCP client only, without using an agent.
 *
 * This test suite requires that a *root* session daemon is running on the
 * system. Since we have to explicitly tell the TCP client which sessiond to
 * connect to, we have to hard-code it in here.
 *
 * @author Alexandre Montplaisir
 */
public class TcpClientIT {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final LogLevelSelector LOG_LEVEL_UNSPECIFIED = new LogLevelSelector(Integer.MIN_VALUE, 0);

    private static final String EVENT_NAME_A = "eventA";
    private static final String EVENT_NAME_B = "eventB";
    private static final String EVENT_NAME_C = "eventC";
    private static final String EVENT_NAME_ALL = "*";

    /* Test configuration */
    private static final int DOMAIN_VALUE = ILttngAgent.Domain.JUL.value();
    private static final ILttngSession.Domain SESSION_DOMAIN = ILttngSession.Domain.JUL;
    private static final boolean ROOT_SESSIOND = true;

    private static TcpClientDebugListener clientListener;
    private static LttngTcpSessiondClient client;
    private static Thread clientThread;

    private ILttngSession session;

    // ------------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------------

    /**
     * Class setup
     */
    @BeforeClass
    public static void setupClass() {
        LttngToolsHelper.destroyAllSessions();

        clientListener = new TcpClientDebugListener();
        client = new LttngTcpSessiondClient(clientListener, DOMAIN_VALUE, ROOT_SESSIOND);

        clientThread = new Thread(client);
        clientThread.start();

        assumeTrue("Timed out waiting for root sessiond", client.waitForConnection(5));
    }

    /**
     * Class teardown
     */
    @AfterClass
    public static void teardownClass() {
        if (client != null) {
            client.close();
        }
        if (clientThread != null) {
            try {
                clientThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Test setup
     */
    @Before
    public void setup() {
        session = ILttngSession.createSession(null, SESSION_DOMAIN);
        clientListener.clearAllCommands();
    }

    /**
     * Test teardown
     */
    @After
    public void teardown() {
        session.close();
    }


    private static ILogLevelStrings getLogLevelStrings() {
        return ILogLevelStrings.JUL_LOGLEVEL_STRINGS;
    }

    /**
     * Check that two lists contain the exact same element (including
     * duplicates), but their order does not matter.
     */
    private static <T extends Comparable<T>> boolean containSameElements(List<T> list1, List<T> list2) {
        List<T> newlist1 = new ArrayList<>(list1);
        List<T> newlist2 = new ArrayList<>(list2);
        Collections.sort(newlist1);
        Collections.sort(newlist2);
        return (newlist1.equals(newlist2));

    }

    // ------------------------------------------------------------------------
    // Test cases
    // ------------------------------------------------------------------------

    /**
     * Test enabling one event.
     */
    @Test
    public void testEnableEvent() {
        session.enableEvent(EVENT_NAME_A, null, false, null);

        List<EventRule> expectedCommands = Collections.singletonList(
                new EventRule(EVENT_NAME_A, LOG_LEVEL_UNSPECIFIED, null));

        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();
        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test an "enable-event -a" command.
     */
    @Test
    public void testEnableAllEvents() {
        session.enableAllEvents();

        List<EventRule> expectedCommands = Collections.singletonList(
                new EventRule(EVENT_NAME_ALL, LOG_LEVEL_UNSPECIFIED, null));
        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling then disabling one event.
     */
    @Test
    public void testEnableThenDisableOneEvent() {
        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.disableEvents(EVENT_NAME_A);

        List<EventRule> expectedEnableCommands = Collections.singletonList(
                new EventRule(EVENT_NAME_A, LOG_LEVEL_UNSPECIFIED, null));
        List<String> expectedDisableCommands = Collections.singletonList(EVENT_NAME_A);

        assertEquals(expectedEnableCommands, clientListener.getEnabledEventCommands());
        assertTrue(containSameElements(expectedDisableCommands, clientListener.getDisabledEventCommands()));
    }

    /**
     * Test enabling some events manually, then disabling all events (-a).
     */
    @Test
    public void testEnableSomeThenDisableAll() {
        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        session.enableEvent(EVENT_NAME_C, null, false, null);
        session.disableAllEvents();

        List<EventRule> expectedEnableCommands = Arrays.asList(
                new EventRule(EVENT_NAME_A, LOG_LEVEL_UNSPECIFIED, null),
                new EventRule(EVENT_NAME_B, LOG_LEVEL_UNSPECIFIED, null),
                new EventRule(EVENT_NAME_C, LOG_LEVEL_UNSPECIFIED, null));
        /*
         * A "disable-event -a" will send one command for each enabled event.
         * The order may be different though.
         */
        List<String> expectedDisableCommands = Arrays.asList(
                EVENT_NAME_A, EVENT_NAME_B, EVENT_NAME_C);

        assertEquals(expectedEnableCommands, clientListener.getEnabledEventCommands());
        assertTrue(containSameElements(expectedDisableCommands, clientListener.getDisabledEventCommands()));
    }

    /**
     * Test enabling then (enable-event -a) then disabling all (disable-event -a) events.
     */
    @Test
    public void testEnableAllThenDisableAll() {
        session.enableAllEvents();
        session.disableAllEvents();

        List<EventRule> expectedEnableCommands = Arrays.asList(
                new EventRule(EVENT_NAME_ALL, LOG_LEVEL_UNSPECIFIED, null));
        List<String> expectedDisableCommands = Arrays.asList(
                EVENT_NAME_ALL);

        assertEquals(expectedEnableCommands, clientListener.getEnabledEventCommands());
        assertTrue(containSameElements(expectedDisableCommands, clientListener.getDisabledEventCommands()));
    }

    /**
     * Test specifying an event with a --loglevel option.
     */
    @Test
    public void testEnableEventLogLevelRange() {
        LogLevelSelector lls = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);

        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);

        List<EventRule> expectedCommands = Collections.singletonList(
                new EventRule(EVENT_NAME_A, lls, null));
        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling an event with a --loglevel-only option.
     */
    @Test
    public void testEnableEventLogLevelSingle() {
        LogLevelSelector lls = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_SINGLE);

        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), true, null);

        List<EventRule> expectedCommands = Collections.singletonList(
                new EventRule(EVENT_NAME_A, lls, null));
        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling an event twice, for the same loglevel, with --loglevel followed by --loglevel-only.
     */
    @Ignore("See http://bugs.lttng.org/issues/913")
    @Test
    public void testEnableEventsLogLevelRangeAndSingle() {
        LogLevelSelector lls1 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);
        LogLevelSelector lls2 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_SINGLE);

        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), true, null);

        List<EventRule> expectedCommands = Arrays.asList(
                new EventRule(EVENT_NAME_A, lls1, null),
                new EventRule(EVENT_NAME_A, lls2, null)
                );
        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling an event twice, for the same loglevel, with --loglevel--only followed by --loglevel.
     */
    @Ignore("See http://bugs.lttng.org/issues/913")
    @Test
    public void testEnableEventsLogLevelSingleAndRange() {
        LogLevelSelector lls1 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_SINGLE);
        LogLevelSelector lls2 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);

        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), true, null);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);

        List<EventRule> expectedCommands = Arrays.asList(
                new EventRule(EVENT_NAME_A, lls1, null),
                new EventRule(EVENT_NAME_A, lls2, null)
                );
        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling the same event, same loglevel, but different loglevel types
     * (--loglevel vs --loglevel-only) in two separate sessions.
     */
    @Test
    public void testEnableEventsLogLevelRangeAndSingleDiffSessions() {
        try (ILttngSession session2 = ILttngSession.createSession(null, SESSION_DOMAIN);) {

            LogLevelSelector lls1 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);
            LogLevelSelector lls2 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_SINGLE);

            session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);
            session2.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), true, null);

            List<EventRule> expectedCommands = Arrays.asList(new EventRule(EVENT_NAME_A, lls1, null),
                    new EventRule(EVENT_NAME_A, lls2, null));
            List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

            assertEquals(expectedCommands, actualCommands);
        }
    }
}
