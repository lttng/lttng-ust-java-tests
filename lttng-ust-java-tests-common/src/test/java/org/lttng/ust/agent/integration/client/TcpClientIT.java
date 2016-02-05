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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.ust.agent.ILttngAgent;
import org.lttng.ust.agent.client.LttngTcpSessiondClient;
import org.lttng.ust.agent.session.EventRule;
import org.lttng.ust.agent.session.LogLevelSelector;
import org.lttng.ust.agent.session.LogLevelSelector.LogLevelType;
import org.lttng.ust.agent.utils.EventRuleFactory;
import org.lttng.ust.agent.utils.ILogLevelStrings;
import org.lttng.ust.agent.utils.TestPrintRunner;

/**
 * Tests for the TCP client only, without using an agent.
 *
 * This test suite requires that a *root* session daemon is running on the
 * system. Since we have to explicitly tell the TCP client which sessiond to
 * connect to, we have to hard-code it in here.
 *
 * @author Alexandre Montplaisir
 */
@RunWith(TestPrintRunner.class)
public class TcpClientIT {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final String EVENT_NAME_A = "eventA";
    private static final String EVENT_NAME_B = "eventB";
    private static final String EVENT_NAME_C = "eventC";

    private static final String CONTEXT_RETRIEVER_NAME_A = "retrieverA";
    private static final String CONTEXT_RETRIEVER_NAME_B = "retrieverB";
    private static final String CONTEXT_NAME_A = "contextA";
    private static final String CONTEXT_NAME_B = "contextB";

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
    // Event enabling/disabling test cases
    // ------------------------------------------------------------------------

    /**
     * Test enabling one event.
     */
    @Test
    public void testEnableEvent() {
        session.enableEvent(EVENT_NAME_A, null, false, null);

        List<EventRule> expectedCommands = Collections.singletonList(
                EventRuleFactory.createRule(EVENT_NAME_A));

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
                EventRuleFactory.createRuleAllEvents());
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
                EventRuleFactory.createRule(EVENT_NAME_A));
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
                EventRuleFactory.createRule(EVENT_NAME_A),
                EventRuleFactory.createRule(EVENT_NAME_B),
                EventRuleFactory.createRule(EVENT_NAME_C));
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

        List<EventRule> expectedEnableCommands = Arrays.asList(EventRuleFactory.createRuleAllEvents());
        List<String> expectedDisableCommands = Arrays.asList(EventRuleFactory.EVENT_NAME_ALL);

        assertEquals(expectedEnableCommands, clientListener.getEnabledEventCommands());
        assertTrue(containSameElements(expectedDisableCommands, clientListener.getDisabledEventCommands()));
    }

    /**
     * Test enabling then destroying the session (should send corresponding
     * disable event messages).
     */
    @SuppressWarnings("static-method")
    @Test
    public void testEnableEventThenDestroy() {
        try (ILttngSession session2 = ILttngSession.createSession(null, SESSION_DOMAIN);) {
            session2.enableEvent(EVENT_NAME_A, null, false, null);
            session2.enableEvent(EVENT_NAME_B, null, false, null);
        } // close(), aka destroy the session, sending "disable event" messages

        List<EventRule> expectedEnabledCommands = Arrays.asList(EventRuleFactory.createRule(EVENT_NAME_A), EventRuleFactory.createRule(EVENT_NAME_B));
        List<String> expectedDisabledCommands = Arrays.asList(EVENT_NAME_A, EVENT_NAME_B);

        assertEquals(expectedEnabledCommands, clientListener.getEnabledEventCommands());
        assertTrue(clientListener.getDisabledEventCommands().containsAll(expectedDisabledCommands));
    }

    /**
     * Test specifying an event with a --loglevel option.
     */
    @Test
    public void testEnableEventLogLevelRange() {
        LogLevelSelector lls = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);

        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);

        List<EventRule> expectedCommands = Collections.singletonList(
                EventRuleFactory.createRule(EVENT_NAME_A, lls));
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
                EventRuleFactory.createRule(EVENT_NAME_A, lls));
        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling an event twice, for the same loglevel, with --loglevel followed by --loglevel-only.
     */
    @Test
    public void testEnableEventsLogLevelRangeAndSingle() {
        LogLevelSelector lls1 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);
        LogLevelSelector lls2 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_SINGLE);

        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), true, null);

        List<EventRule> expectedCommands = Arrays.asList(
                EventRuleFactory.createRule(EVENT_NAME_A, lls1),
                EventRuleFactory.createRule(EVENT_NAME_A, lls2)
                );
        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling an event twice, for the same loglevel, with --loglevel-only followed by --loglevel.
     */
    @Test
    public void testEnableEventsLogLevelSingleAndRange() {
        LogLevelSelector lls1 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_SINGLE);
        LogLevelSelector lls2 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);

        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), true, null);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);

        List<EventRule> expectedCommands = Arrays.asList(
                EventRuleFactory.createRule(EVENT_NAME_A, lls1),
                EventRuleFactory.createRule(EVENT_NAME_A, lls2)
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

            List<EventRule> expectedCommands = Arrays.asList(
                    EventRuleFactory.createRule(EVENT_NAME_A, lls1),
                    EventRuleFactory.createRule(EVENT_NAME_A, lls2));
            List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

            assertEquals(expectedCommands, actualCommands);
        }
    }

    /**
     * Enable the same event multiple times with different filter strings.
     */
    @Test
    public void testEnableEventsDiffFilters() {
        final String filter1 = "filter1";
        final String filter2 = "filter2";

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_A, null, false, filter1);
        session.enableEvent(EVENT_NAME_A, null, false, filter2);

        List<EventRule> expectedCommands = Arrays.asList(
                EventRuleFactory.createRule(EVENT_NAME_A),
                EventRuleFactory.createRule(EVENT_NAME_A, EventRuleFactory.LOG_LEVEL_UNSPECIFIED, filter1),
                EventRuleFactory.createRule(EVENT_NAME_A, EventRuleFactory.LOG_LEVEL_UNSPECIFIED, filter2));
        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Enable the same event multiple times with different log levels and
     * filters.
     */
    @Test
    public void testEnableEventsLogLevelAndFilters() {
        final LogLevelSelector lls = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);
        final String filter = "filter1";

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);
        session.enableEvent(EVENT_NAME_A, null, false, filter);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, filter);

        List<EventRule> expectedCommands = Arrays.asList(
                EventRuleFactory.createRule(EVENT_NAME_A),
                EventRuleFactory.createRule(EVENT_NAME_A, lls),
                EventRuleFactory.createRule(EVENT_NAME_A, EventRuleFactory.LOG_LEVEL_UNSPECIFIED, filter),
                EventRuleFactory.createRule(EVENT_NAME_A, lls, filter));
        List<EventRule> actualCommands = clientListener.getEnabledEventCommands();

        assertEquals(expectedCommands, actualCommands);
    }

    // ------------------------------------------------------------------------
    // Application context enabling/disabling test cases
    // ------------------------------------------------------------------------

    /**
     * Test enabling one application context.
     */
    @Test
    public void testEnableAppContext() {
        session.enableAppContext(CONTEXT_RETRIEVER_NAME_A, CONTEXT_NAME_A);

        List<String> expectedCommands = Collections.singletonList(
                CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A);

        List<String> actualCommands = clientListener.getEnabledAppContextCommands();
        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling two application contexts sharing the same retriever name.
     */
    @Test
    public void testEnableAppContextsSameRetriever() {
        session.enableAppContext(CONTEXT_RETRIEVER_NAME_A, CONTEXT_NAME_A);
        session.enableAppContext(CONTEXT_RETRIEVER_NAME_A, CONTEXT_NAME_B);

        List<String> expectedCommands = Arrays.asList(
                CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A,
                CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_B);

        List<String> actualCommands = clientListener.getEnabledAppContextCommands();
        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling two application contexts sharing the same context name, but
     * with different retrievers. Unusual, but they should still be recognized
     * separately.
     */
    @Test
    public void testEnableAppContextsSameContext() {
        session.enableAppContext(CONTEXT_RETRIEVER_NAME_A, CONTEXT_NAME_A);
        session.enableAppContext(CONTEXT_RETRIEVER_NAME_B, CONTEXT_NAME_A);

        List<String> expectedCommands = Arrays.asList(
                CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A,
                CONTEXT_RETRIEVER_NAME_B + ':' + CONTEXT_NAME_A);

        List<String> actualCommands = clientListener.getEnabledAppContextCommands();
        assertEquals(expectedCommands, actualCommands);
    }

    /**
     * Test enabling one application context, then destroying the session. We
     * should receive the corresponding "context removed" message.
     */
    @Test
    @SuppressWarnings("static-method")
    public void testEnableAppContextThenDestroy() {
        try (ILttngSession session2 = ILttngSession.createSession(null, SESSION_DOMAIN);) {
            session2.enableAppContext(CONTEXT_RETRIEVER_NAME_A, CONTEXT_NAME_A);
        } // close(), aka destroy the session, sending "disable context" messages

        List<String> expectedEnabledCommands = Collections.singletonList(CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A);
        List<String> expectedDisabledCommands = Collections.singletonList(CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A);
        List<String> actualEnabledCommands = clientListener.getEnabledAppContextCommands();
        List<String> actualDisabledCommands = clientListener.getDisabledAppContextCommands();

        assertEquals(expectedEnabledCommands, actualEnabledCommands);
        assertEquals(expectedDisabledCommands, actualDisabledCommands);
    }

    /**
     * Test enabling the same application context in two different sessions.
     * Upon destroying one, we should only receive one "destroyed" message.
     */
    @Test
    public void testEnableSameAppContextTwoSessions() {
        List<String> expectedEnabledCommands = Arrays.asList(
                CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A,
                CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A);
        List<String> actualEnabledCommands;

        try (ILttngSession session2 = ILttngSession.createSession(null, SESSION_DOMAIN);) {
            session.enableAppContext(CONTEXT_RETRIEVER_NAME_A, CONTEXT_NAME_A);
            session2.enableAppContext(CONTEXT_RETRIEVER_NAME_A, CONTEXT_NAME_A);

            actualEnabledCommands = clientListener.getEnabledAppContextCommands();
            assertEquals(expectedEnabledCommands, actualEnabledCommands);
        } // close/destroy session2

        actualEnabledCommands = clientListener.getEnabledAppContextCommands();
        assertEquals(expectedEnabledCommands, actualEnabledCommands);

        List<String> expectedDisabledCommands = Collections.singletonList(CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A);
        List<String> actualDisabledCommands = clientListener.getDisabledAppContextCommands();

        assertEquals(expectedDisabledCommands, actualDisabledCommands);
    }

    /**
     * Test enabling two different application context in two different
     * sessions. Upon destroying one, we should receive the correct "destroyed"
     * message.
     */
    @Test
    public void testEnableDiffAppContextTwoSessions() {
        List<String> expectedEnabledCommands = Arrays.asList(
                CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A,
                CONTEXT_RETRIEVER_NAME_B + ':' + CONTEXT_NAME_B);
        List<String> actualEnabledCommands;

        try (ILttngSession session2 = ILttngSession.createSession(null, SESSION_DOMAIN);) {
            session.enableAppContext(CONTEXT_RETRIEVER_NAME_A, CONTEXT_NAME_A);
            session2.enableAppContext(CONTEXT_RETRIEVER_NAME_B, CONTEXT_NAME_B);

            actualEnabledCommands = clientListener.getEnabledAppContextCommands();
            assertEquals(expectedEnabledCommands, actualEnabledCommands);
        } // close/destroy session2

        actualEnabledCommands = clientListener.getEnabledAppContextCommands();
        assertEquals(expectedEnabledCommands, actualEnabledCommands);

        List<String> expectedDisabledCommands = Collections.singletonList(CONTEXT_RETRIEVER_NAME_B + ':' + CONTEXT_NAME_B);
        List<String> actualDisabledCommands = clientListener.getDisabledAppContextCommands();

        assertEquals(expectedDisabledCommands, actualDisabledCommands);
    }

    // ------------------------------------------------------------------------
    // Application context filtering
    // ------------------------------------------------------------------------

    /**
     * Test that enabling an event with a filter string referring to a context
     * should send an agent message about this context now being "enabled".
     *
     * This is because we will pass the context information to UST for the
     * filtering step, even if the actual context won't be present in the trace.
     */
    @SuppressWarnings("static-method")
    @Test
    public void testContextInFilterString() {
        try (ILttngSession session2 = ILttngSession.createSession(null, SESSION_DOMAIN);) {
            session2.enableEvent(EVENT_NAME_A, null, false, "$app." + CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A + "==\"bozo\"");

            List<String> expectedEnabledCommands = Collections.singletonList(CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A);
            assertEquals(expectedEnabledCommands, clientListener.getEnabledAppContextCommands());
        } // close(), aka destroy the session, sending "disable context" messages

        List<String> expectedDisabledCommands = Collections.singletonList(CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A);
        assertEquals(expectedDisabledCommands, clientListener.getDisabledAppContextCommands());
    }

    /**
     * Test that if we the context is both referred to by a filter string *and*
     * enabled directly, we receive *2* messages about this context being
     * enabled (and disabled on session teardown).
     */
    @SuppressWarnings("static-method")
    @Test
    public void testContextEnabledAndInFilterString() {
        try (ILttngSession session2 = ILttngSession.createSession(null, SESSION_DOMAIN);) {
            session2.enableEvent(EVENT_NAME_A, null, false, "$app." + CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A + "==\"bozo\"");
            session2.enableAppContext(CONTEXT_RETRIEVER_NAME_A, CONTEXT_NAME_A);

            List<String> expectedEnabledCommands = Collections.nCopies(2, CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A);
            assertEquals(expectedEnabledCommands, clientListener.getEnabledAppContextCommands());
        } // close(), aka destroy the session, sending "disable context" messages

        List<String> expectedDisabledCommands = Collections.nCopies(2, CONTEXT_RETRIEVER_NAME_A + ':' + CONTEXT_NAME_A);
        assertEquals(expectedDisabledCommands, clientListener.getDisabledAppContextCommands());
    }
}
