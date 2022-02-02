/*
 * Copyright (C) 2016, EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;
import org.lttng.tools.ILttngSession;
import org.lttng.ust.agent.filter.FilterChangeNotifier;
import org.lttng.ust.agent.integration.filter.FilterListenerITBase.TestFilterListener;
import org.lttng.ust.agent.session.EventRule;
import org.lttng.ust.agent.utils.EventRuleFactory;
import org.lttng.ust.agent.utils.TestPrintExtension;

/**
 * For the filter change notifications to work, several setup steps are
 * required:
 *
 * <ul>
 * <li>Initialize the Java agent register it to the sessiond [Agent]</li>
 * <li>Instantiate a filer change listener, and register it to the notifier
 * [Listener]</li>
 * <li>Apply some event rule changes in the tracing session (lttng enable-event,
 * etc.) [Session]</li>
 * </ul>
 *
 * <p>
 * Then on teardown, the following steps are expected:
 * </p>
 *
 * <ul>
 * <li>Dispose the Java agent, closing the connection to the sessiond [Agent]
 * </li>
 * <li>Destroy the tracing session, removing tracked events [Session]</li>
 * </ul>
 *
 * (and then the filter change listener should be de-registered from the
 * notifier. If it is deregistered earlier, then obviously no notifications
 * would be received thereafter).
 *
 * <p>
 * Within these two sets, each step can happen in any order. This results in 6 x
 * 2 = 12 possibilities. The goal of this test class it to test these 12
 * possibilities.
 * </p>
 */
@ExtendWith(TestPrintExtension.class)
@SuppressWarnings("javadoc")
public abstract class FilterListenerOrderingITBase {

    protected static final String EVENT_NAME_A = "EventA";
    private static final String EVENT_NAME_B = "EventB";

    protected EventRuleFactory eventRuleFactory;

    private ILttngSession session;
    private TestFilterListener listener;

    /**
     * Base class cleanup
     */
    @AfterEach
    public void baseTeardown() {
        /*
         * Deregister the listener (should always be done after all the other
         * steps).
         */
        FilterChangeNotifier.getInstance().unregisterListener(listener);
        listener = null;
    }

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    protected abstract ILttngSession.Domain getDomain();

    protected abstract void registerAgent();

    private void registerListener() {
        listener = new TestFilterListener();
        FilterChangeNotifier.getInstance().registerListener(listener);
    }

    private void enableRulesInSession() {
        session = ILttngSession.createCommandLineSession(null, getDomain());
        session.enableEvent(EVENT_NAME_A, null, false, null);
        session.enableEvent(EVENT_NAME_B, null, false, null);
    }

    protected abstract void deregisterAgent();

    private void destroySession() {
        session.close();
        session = null;
    }

    protected EventRuleFactory getEventRuleFactory() {
        if (eventRuleFactory == null) {
            eventRuleFactory = new EventRuleFactory(getDomain());
        }
        return eventRuleFactory;
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Check that the expected event rules are present after setup but before
     * teardown.
     */
    private void checkOngoingConditions() {
        Set<EventRule> exptectedRules = Stream.of(
                getEventRuleFactory().createRule(EVENT_NAME_A),
                getEventRuleFactory().createRule(EVENT_NAME_B))
                .collect(Collectors.toSet());

        assertEquals(2, listener.getNbNotifications());
        assertEquals(exptectedRules, listener.getCurrentRules());
    }

    /**
     * Check that the expected event rules are present after/during teardown.
     */
    private void checkFinalConditions() {
        Set<EventRule> expectedRules = Collections.EMPTY_SET;

        assertEquals(4, listener.getNbNotifications());
        assertEquals(expectedRules, listener.getCurrentRules());
    }

    @Test
    public void testAgentListenerSession_AgentSession() {
        registerAgent();
        registerListener();
        enableRulesInSession();

        checkOngoingConditions();

        deregisterAgent();
        destroySession();

        checkFinalConditions();
    }

    @Test
    public void testAgentSessionListener_AgentSession() {
        registerAgent();
        enableRulesInSession();
        registerListener();

        checkOngoingConditions();

        deregisterAgent();
        destroySession();

        checkFinalConditions();
    }

    @Test
    public void testListenerAgentSession_AgentSession() {
        registerListener();
        registerAgent();
        enableRulesInSession();

        checkOngoingConditions();

        deregisterAgent();
        destroySession();

        checkFinalConditions();
    }

    @Test
    public void testListenerSessionAgent_AgentSession() {
        registerListener();
        enableRulesInSession();
        registerAgent();

        checkOngoingConditions();

        deregisterAgent();
        destroySession();

        checkFinalConditions();
    }

    @Test
    public void testSessionAgentListener_AgentSession() {
        enableRulesInSession();
        registerAgent();
        registerListener();

        checkOngoingConditions();

        deregisterAgent();
        destroySession();

        checkFinalConditions();
    }

    @Test
    public void testSessionListenerAgent_AgentSession() {
        enableRulesInSession();
        registerListener();
        registerAgent();

        checkOngoingConditions();

        deregisterAgent();
        destroySession();

        checkFinalConditions();
    }



    @Test
    public void testAgentListenerSession_SessionAgent() {
        registerAgent();
        registerListener();
        enableRulesInSession();

        checkOngoingConditions();

        destroySession();
        checkFinalConditions();
        deregisterAgent();
        checkFinalConditions();
    }

    @Test
    public void testAgentSessionListener_SessionAgent() {
        registerAgent();
        enableRulesInSession();
        registerListener();

        checkOngoingConditions();

        destroySession();
        checkFinalConditions();
        deregisterAgent();
        checkFinalConditions();
    }

    @Test
    public void testListenerAgentSession_SessionAgent() {
        registerListener();
        registerAgent();
        enableRulesInSession();

        checkOngoingConditions();

        destroySession();
        checkFinalConditions();
        deregisterAgent();
        checkFinalConditions();
    }

    @Test
    public void testListenerSessionAgent_SessionAgent() {
        registerListener();
        enableRulesInSession();
        registerAgent();

        checkOngoingConditions();

        destroySession();
        checkFinalConditions();
        deregisterAgent();
        checkFinalConditions();
    }

    @Test
    public void testSessionAgentListener_SessionAgent() {
        enableRulesInSession();
        registerAgent();
        registerListener();

        checkOngoingConditions();

        destroySession();
        checkFinalConditions();
        deregisterAgent();
        checkFinalConditions();
    }

    @Test
    public void testSessionListenerAgent_SessionAgent() {
        enableRulesInSession();
        registerListener();
        registerAgent();

        checkOngoingConditions();

        destroySession();
        checkFinalConditions();
        deregisterAgent();
        checkFinalConditions();
    }

}
