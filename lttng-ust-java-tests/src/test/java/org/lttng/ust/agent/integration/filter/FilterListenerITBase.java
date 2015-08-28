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

package org.lttng.ust.agent.integration.filter;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lttng.tools.ILttngSession;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.filter.FilterNotificationManager;
import org.lttng.ust.agent.filter.IFilterChangeListener;
import org.lttng.ust.agent.session.EventRule;
import org.lttng.ust.agent.session.LogLevelFilter;
import org.lttng.ust.agent.session.LogLevelFilter.LogLevelType;
import org.lttng.ust.agent.utils.ILogLevelStrings;
import org.lttng.ust.agent.utils.TestPrintRunner;

/**
 * Base test class for {@link IFilterChangeListener} tests.
 *
 * @author Alexandre Montplaisir
 */
@RunWith(TestPrintRunner.class)
public abstract class FilterListenerITBase {

    protected static final LogLevelFilter LOG_LEVEL_UNSPECIFIED = new LogLevelFilter(Integer.MIN_VALUE, 0);

    private static final String EVENT_NAME_A = "eventA";
    private static final String EVENT_NAME_B = "eventB";
    private static final String EVENT_NAME_C = "eventC";

    private ILttngSession session;
    private TestFilterListener listener;
    private ILttngHandler handler;

    protected abstract ILttngSession.Domain getSessionDomain();
    protected abstract ILttngHandler getLogHandler() throws SecurityException, IOException;
    protected abstract ILogLevelStrings getLogLevelStrings();

    /**
     * Test setup
     *
     * @throws SecurityException
     * @throws IOException
     */
    @Before
    public void setup() throws SecurityException, IOException {
        handler = getLogHandler();
        listener = new TestFilterListener();
        FilterNotificationManager.getInstance().registerListener(listener);
        session = ILttngSession.createSession(null, getSessionDomain());
    }

    /**
     * Test teardown
     */
    @After
    public void teardown() {
        session.close();
        FilterNotificationManager.getInstance().unregisterListener(listener);
        handler.close();
    }

    /**
     * Test not sending any commands.
     */
    @Test
    public void testNoRules() {
        Set<EventRule> rules = Collections.EMPTY_SET;
        listener.setParameters(0, rules);
        /* Don't enable any events */

        assertTrue(listener.waitForAllNotifications());
        assertTrue(listener.checkRules());
    }

    /**
     * Test sending one event rule.
     */
    @Test
    public void testOneRule() {
        Set<EventRule> rules = Collections.singleton(
                new EventRule(EVENT_NAME_A, LOG_LEVEL_UNSPECIFIED, null));

        listener.setParameters(1, rules);

        session.enableEvent(EVENT_NAME_A, null, false, null);

        assertTrue(listener.waitForAllNotifications());
        assertTrue(listener.checkRules());
    }

    /**
     * Test sending many event rules.
     */
    @Test
    public void testManyRules() {
        Set<EventRule> rules = Stream
                .of(new EventRule(EVENT_NAME_A, LOG_LEVEL_UNSPECIFIED, null),
                        new EventRule(EVENT_NAME_B, LOG_LEVEL_UNSPECIFIED, null),
                        new EventRule(EVENT_NAME_C, LOG_LEVEL_UNSPECIFIED, null))
                .collect(Collectors.toSet());

        listener.setParameters(3, rules);

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        session.enableEvent(EVENT_NAME_C, null, false, null);

        assertTrue(listener.waitForAllNotifications());
        assertTrue(listener.checkRules());
    }

    /**
     * Test enabling then disabling some events.
     */
    @Test
    public void testManyRulesDisableSome() {
        Set<EventRule> rules = Collections.singleton(
                new EventRule(EVENT_NAME_A, LOG_LEVEL_UNSPECIFIED, null));

        listener.setParameters(4, rules);

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        session.enableEvent(EVENT_NAME_C, null, false, null);
        session.disableEvents(EVENT_NAME_B);
        session.disableEvents(EVENT_NAME_C);

        assertTrue(listener.waitForAllNotifications());
        assertTrue(listener.checkRules());
    }

    /**
     * Test enabling some rules, then calling disable-event -a.
     */
    @Test
    public void testManyRulesDisableAll() {
        Set<EventRule> rules = Collections.EMPTY_SET;

        /*
         * We should receive 6 notifications, because a "disable-event -a" sends
         * one for each event that was enabled.
         */
        listener.setParameters(6, rules);

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        session.enableEvent(EVENT_NAME_C, null, false, null);
        session.disableAllEvents();

        assertTrue(listener.waitForAllNotifications());
        assertTrue(listener.checkRules());
    }

    /**
     * Test enabling the same event name with various values of loglevels.
     */
    @Ignore("Does not work as expected atm, see http://bugs.lttng.org/issues/913")
    @Test
    public void testSameEventsDiffLogLevels() {
        LogLevelFilter llf1 = new LogLevelFilter(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);
        LogLevelFilter llf2 = new LogLevelFilter(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_SINGLE);
        LogLevelFilter llf3 = new LogLevelFilter(getLogLevelStrings().infoInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);

        Set<EventRule> rules = Stream.of(
                    new EventRule(EVENT_NAME_A, llf1, null),
                    new EventRule(EVENT_NAME_A, llf2, null),
                    new EventRule(EVENT_NAME_A, llf3, null))
                .collect(Collectors.toSet());

        listener.setParameters(3, rules);

        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), true, null);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().infoName(), false, null);

        assertTrue(listener.waitForAllNotifications());
        assertTrue(listener.checkRules());
    }

    /**
     * Test enabling the same event name with various filters.
     */
    @Ignore("Filters are not tracked yet")
    @Test
    public void testSameEventsDiffFilters() {
        String filterA = "filterA";
        String filterB = "filterB";

        Set<EventRule> rules = Stream.of(
                    new EventRule(EVENT_NAME_A, LOG_LEVEL_UNSPECIFIED, null),
                    new EventRule(EVENT_NAME_A, LOG_LEVEL_UNSPECIFIED, filterA),
                    new EventRule(EVENT_NAME_A, LOG_LEVEL_UNSPECIFIED, filterB))
                .collect(Collectors.toSet());

        listener.setParameters(3, rules);

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, filterA);
        session.enableEvent(EVENT_NAME_C, null, false, filterB);

        assertTrue(listener.waitForAllNotifications());
        assertTrue(listener.checkRules());
    }

    /**
     * The filter listener used for tests.
     *
     * <p>
     * Usage:
     * <ul>
     * <li>Specify the expected number of notifications and end rules with
     * {@link #setParameters}.</li>
     * <li>Send the commands to LTTng (using {@link ILttngSession} for example.
     * </li>
     * <li>Call {@link #waitForAllNotifications()}.</li>
     * <li>Verify that {@link #checkRules()} returns true.</li>
     * </ul>
     * </p>
     */
    private static class TestFilterListener implements IFilterChangeListener {

        private final Set<EventRule> currentRules = new HashSet<>();
        private CountDownLatch remainingExpectedNotifs;
        private Set<EventRule> expectedRules;

        public TestFilterListener() {}

        @Override
        public void eventRuleAdded(EventRule rule) {
            currentRules.add(rule);
            remainingExpectedNotifs.countDown();
        }

        @Override
        public void eventRuleRemoved(EventRule rule) {
            currentRules.remove(rule);
            remainingExpectedNotifs.countDown();
        }

        public void setParameters(int expectedNotifications, Set<EventRule> expectedRulesAtEnd) {
            this.remainingExpectedNotifs = new CountDownLatch(expectedNotifications);
            this.expectedRules = expectedRulesAtEnd;
        }

        public boolean waitForAllNotifications() {
            System.out.println("Waiting for all notifications to arrive...");
            try {
                return remainingExpectedNotifs.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                return false;
            }
        }

        public boolean checkRules() {
            return ((remainingExpectedNotifs.getCount() == 0) && currentRules.equals(expectedRules));
        }
    }

}
