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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.lttng.tools.ILttngSession;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.filter.FilterChangeNotifier;
import org.lttng.ust.agent.filter.IFilterChangeListener;
import org.lttng.ust.agent.session.EventRule;
import org.lttng.ust.agent.session.LogLevelSelector;
import org.lttng.ust.agent.session.LogLevelSelector.LogLevelType;
import org.lttng.ust.agent.utils.EventRuleFactory;
import org.lttng.ust.agent.utils.ILogLevelStrings;
import org.lttng.ust.agent.utils.TestPrintExtension;

/**
 * Base test class for {@link IFilterChangeListener} tests.
 *
 * @author Alexandre Montplaisir
 */
@ExtendWith(TestPrintExtension.class)
public abstract class FilterListenerITBase {

    private static final String EVENT_NAME_A = "eventA";
    private static final String EVENT_NAME_B = "eventB";
    private static final String EVENT_NAME_C = "eventC";

    private ILttngSession session;
    private TestFilterListener listener;
    private ILttngHandler handler;

    protected EventRuleFactory eventRuleFactory;

    protected abstract ILttngSession.Domain getSessionDomain();
    protected abstract ILttngHandler getLogHandler() throws SecurityException, IOException;
    protected abstract ILogLevelStrings getLogLevelStrings();

    protected EventRuleFactory getEventRuleFactory() {
        if (eventRuleFactory == null) {
            eventRuleFactory = new EventRuleFactory(getSessionDomain());
        }
        return eventRuleFactory;
    }

    /**
     * Test setup
     *
     * @throws SecurityException
     * @throws IOException
     */
    @BeforeEach
    public void setup() throws SecurityException, IOException {
        handler = getLogHandler();
        listener = new TestFilterListener();
        FilterChangeNotifier.getInstance().registerListener(listener);
        session = ILttngSession.createSession(null, getSessionDomain());

        assertEquals(0, listener.getNbNotifications());
    }

    /**
     * Test teardown
     */
    @AfterEach
    public void teardown() {
        session.close();
        FilterChangeNotifier.getInstance().unregisterListener(listener);
        listener = null;
        handler.close();
    }

    /**
     * Test not sending any commands.
     */
    @Test
    public void testNoRules() {
        assertEquals(0, listener.getNbNotifications());
        assertEquals(Collections.EMPTY_SET, listener.getCurrentRules());
    }

    /**
     * Test sending one event rule.
     */
    @Test
    public void testOneRule() {
        Set<EventRule> rules = Collections.singleton(
                getEventRuleFactory().createRule(EVENT_NAME_A));

        session.enableEvent(EVENT_NAME_A, null, false, null);

        assertEquals(1, listener.getNbNotifications());
        assertEquals(rules, listener.getCurrentRules());
    }

    /**
     * Test sending many event rules.
     */
    @Test
    public void testManyRules() {
        Set<EventRule> rules = Stream.of(
                getEventRuleFactory().createRule(EVENT_NAME_A),
                getEventRuleFactory().createRule(EVENT_NAME_B),
                getEventRuleFactory().createRule(EVENT_NAME_C))
                .collect(Collectors.toSet());

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        session.enableEvent(EVENT_NAME_C, null, false, null);

        assertEquals(3, listener.getNbNotifications());
        assertEquals(rules, listener.getCurrentRules());
    }

    /**
     * Test enabling then disabling some events.
     */
    @Test
    public void testManyRulesDisableSome() {
        Set<EventRule> rules = Collections.singleton(
                getEventRuleFactory().createRule(EVENT_NAME_A));

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        session.enableEvent(EVENT_NAME_C, null, false, null);
        session.disableEvents(EVENT_NAME_B);
        session.disableEvents(EVENT_NAME_C);

        assertEquals(5, listener.getNbNotifications());
        assertEquals(rules, listener.getCurrentRules());
    }

    /**
     * Test enabling some rules, then calling disable-event -a.
     */
    @Test
    public void testManyRulesDisableAll() {
        Set<EventRule> rules = Collections.EMPTY_SET;

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        session.enableEvent(EVENT_NAME_C, null, false, null);
        session.disableAllEvents();

        /*
         * We should receive 6 notifications, because a "disable-event -a" sends
         * one for each event that was enabled.
         */
        assertEquals(6, listener.getNbNotifications());
        assertEquals(rules, listener.getCurrentRules());
    }

    /**
     * Test enabling the same event name with various values of loglevels.
     */
    @Test
    public void testSameEventsDiffLogLevels() {
        LogLevelSelector lls1 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);
        LogLevelSelector lls2 = new LogLevelSelector(getLogLevelStrings().warningInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_SINGLE);
        LogLevelSelector lls3 = new LogLevelSelector(getLogLevelStrings().infoInt(), LogLevelType.LTTNG_EVENT_LOGLEVEL_RANGE);

        Set<EventRule> rules = Stream.of(
                getEventRuleFactory().createRule(EVENT_NAME_A, lls1),
                getEventRuleFactory().createRule(EVENT_NAME_A, lls2),
                getEventRuleFactory().createRule(EVENT_NAME_A, lls3))
                .collect(Collectors.toSet());

        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), false, null);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().warningName(), true, null);
        session.enableEvent(EVENT_NAME_A, getLogLevelStrings().infoName(), false, null);

        assertEquals(3, listener.getNbNotifications());
        assertEquals(rules, listener.getCurrentRules());
    }

    /**
     * Test enabling the same event name with various filters.
     */
    @Test
    public void testSameEventsDiffFilters() {
        String filterA = "filterA";
        String filterB = "filterB";

        Set<EventRule> rules = Stream.of(
                getEventRuleFactory().createRule(EVENT_NAME_A),
                getEventRuleFactory().createRule(EVENT_NAME_B, getEventRuleFactory().LOG_LEVEL_UNSPECIFIED, filterA),
                getEventRuleFactory().createRule(EVENT_NAME_C, getEventRuleFactory().LOG_LEVEL_UNSPECIFIED, filterB))
                .collect(Collectors.toSet());

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, filterA);
        session.enableEvent(EVENT_NAME_C, null, false, filterB);

        assertEquals(3, listener.getNbNotifications());
        assertEquals(rules, listener.getCurrentRules());
    }

    /**
     * Test sending some notifications then detaching a listener. Subsequent
     * notifications should not be sent.
     */
    @Test
    public void testDetachingListener() {
        Set<EventRule> rules = Stream.of(
                getEventRuleFactory().createRule(EVENT_NAME_A),
                getEventRuleFactory().createRule(EVENT_NAME_B))
                .collect(Collectors.toSet());

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        FilterChangeNotifier.getInstance().unregisterListener(listener);
        session.enableEvent(EVENT_NAME_C, null, false, null);

        assertEquals(2, listener.getNbNotifications());
        assertEquals(rules, listener.getCurrentRules());
    }

    /**
     * Run a test with multiple listeners attached to the manager. All listeners
     * should receive all the data.
     */
    @Test
    public void testMultipleListeners() {
        FilterChangeNotifier fcn = FilterChangeNotifier.getInstance();
        TestFilterListener listener2 = new TestFilterListener();
        TestFilterListener listener3 = new TestFilterListener();
        fcn.registerListener(listener2);
        fcn.registerListener(listener3);

        Set<EventRule> rules = Stream.of(
                getEventRuleFactory().createRule(EVENT_NAME_A),
                getEventRuleFactory().createRule(EVENT_NAME_B))
            .collect(Collectors.toSet());

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        session.enableEvent(EVENT_NAME_C, null, false, null);
        session.disableEvents(EVENT_NAME_C);

        assertEquals(4, listener.getNbNotifications());
        assertEquals(rules, listener.getCurrentRules());

        assertEquals(4, listener2.getNbNotifications());
        assertEquals(rules, listener2.getCurrentRules());

        assertEquals(4, listener3.getNbNotifications());
        assertEquals(rules, listener3.getCurrentRules());

        fcn.unregisterListener(listener2);
        fcn.unregisterListener(listener3);
    }

    /**
     * Test with both attached and unattached listeners. The unattached ones
     * should not receive anything, but should not interfere with the other
     * ones.
     */
    @Test
    public void testUnattachedListeners() {
        FilterChangeNotifier fcn = FilterChangeNotifier.getInstance();
        TestFilterListener listener2 = new TestFilterListener();
        TestFilterListener listener3 = new TestFilterListener();
        /* We attach then detach listener2. We never attach listener3 */
        fcn.registerListener(listener2);
        fcn.unregisterListener(listener2);

        Set<EventRule> rules = Stream.of(
                getEventRuleFactory().createRule(EVENT_NAME_A),
                getEventRuleFactory().createRule(EVENT_NAME_B))
            .collect(Collectors.toSet());

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);

        assertEquals(2, listener.getNbNotifications());
        assertEquals(rules, listener.getCurrentRules());

        assertEquals(0, listener2.getNbNotifications());
        assertEquals(Collections.EMPTY_SET, listener2.getCurrentRules());

        assertEquals(0, listener3.getNbNotifications());
        assertEquals(Collections.EMPTY_SET, listener3.getCurrentRules());
    }

    /**
     * Test that a newly-registered listener correctly receives the "statedump",
     * which means all the rules currently active, upon registration.
     */
    @Test
    public void testStatedump() {
        FilterChangeNotifier fcn = FilterChangeNotifier.getInstance();
        TestFilterListener listener2 = new TestFilterListener();

        Set<EventRule> rules1 = Stream.of(
                getEventRuleFactory().createRule(EVENT_NAME_A),
                getEventRuleFactory().createRule(EVENT_NAME_B))
            .collect(Collectors.toSet());
        Set<EventRule> rules2 = Stream.of(
                getEventRuleFactory().createRule(EVENT_NAME_A),
                getEventRuleFactory().createRule(EVENT_NAME_C))
            .collect(Collectors.toSet());

        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
        fcn.registerListener(listener2);

        /* We should have received the "statedump" when registering */
        assertEquals(2, listener2.getNbNotifications());
        assertEquals(rules1, listener2.getCurrentRules());

        session.enableEvent(EVENT_NAME_C, null, false, null);
        session.disableEvents(EVENT_NAME_B);

        /* Subsequent changes should also be received */
        assertEquals(4, listener2.getNbNotifications());
        assertEquals(rules2, listener2.getCurrentRules());

        fcn.unregisterListener(listener2);
    }

    /**
     * The filter listener used for tests.
     */
    static class TestFilterListener implements IFilterChangeListener {

        private final Set<EventRule> currentRules = new HashSet<>();
        private volatile int currentNotifications = 0;

        public TestFilterListener() {}

        @Override
        public void eventRuleAdded(EventRule rule) {
            currentRules.add(rule);
            currentNotifications++;
        }

        @Override
        public void eventRuleRemoved(EventRule rule) {
            currentRules.remove(rule);
            currentNotifications++;
        }

        public int getNbNotifications() {
            return currentNotifications;
        }

        public Set<EventRule> getCurrentRules() {
            return currentRules;
        }
    }

}
