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

package org.lttng.ust.agent.integration.context;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.context.ContextInfoManager;
import org.lttng.ust.agent.context.IContextInfoRetriever;
import org.lttng.ust.agent.utils.TestPrintRunner;

/**
 * To obtain application contexts in a trace, three steps are required:
 *
 * <ul>
 * <li>Having the Java agent register to the sessiond (Agent)</li>
 * <li>Registering the application-provided context info retriever (Retriever)</li>
 * <li>Enabling the contexts in the tracing session (Session)</li>
 * </ul>
 *
 * These three steps however can occur in any order ; this means there are 6
 * possible cases. The goal of this class is to test all these cases.
 */
@RunWith(TestPrintRunner.class)
public abstract class AppContextOrderingITBase {

    protected static final String EVENT_NAME = "EventName";

    private static final IContextInfoRetriever RETRIEVER = ContextInfoRetrieverStubs.STRING_RETRIEVER;
    private static final String RETRIEVER_NAME = "MyRetriever";
    private static final String CONTEXT_NAME = ContextInfoRetrieverStubs.CONTEXT_NAME;
    private static final String CONTEXT_VALUE = ContextInfoRetrieverStubs.STRING_VALUE;

    protected ILttngHandler logHandler;

    private ContextInfoManager cim;
    private ILttngSession session;

    protected abstract Domain getDomain();
    protected abstract void sendEventsToLoggers();

    /**
     * Base test setup
     */
    @Before
    public void testSetup() {
        try {
            cim = ContextInfoManager.getInstance();
        } catch (SecurityException | IOException e) {
            /* The native library is not available! */
            fail(e.getMessage());
        }
        session = ILttngSession.createSession(null, getDomain());
    }

    /**
     * Base test cleanup
     */
    @After
    public void testCleanup() {
        session.close();
        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME));
    }

    /**
     * Base class cleanup
     */
    @AfterClass
    public static void julClassCleanup() {
        LttngToolsHelper.deleteAllTraces();
    }

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    /**
     * Instantiate the log handler for the corresponding logging API. This will
     * also spawn the agent and have it register to the sessiond, so it
     * corresponds to the "Agent" step.
     *
     * This method should set the 'logHandler' field accordingly.
     */
    protected abstract void registerAgent();

    /**
     * Register the context info retriever to UST. This corresponds to the
     * "Retriever" step.
     */
    private void registerRetriever() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME, RETRIEVER));
    }

    /**
     * Enable the contexts in the tracing session. This corresponds to the "Session" step.
     */
    private void enableContextInSessions() {
        assertTrue(session.enableAllEvents());
        assertTrue(session.enableAppContext(RETRIEVER_NAME, CONTEXT_NAME));
    }

    /**
     * Start tracing, send events from the application, and verify that the
     * output contains the expected context information.
     *
     * This should be called only after all 3 steps above are done.
     */
    private void traceSendEventsAndVerify() {
        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        String traceRetriverName = RETRIEVER_NAME.replace('.', '_');
        String traceContextName = CONTEXT_NAME.replace('.', '_');

        String expectedString = "_app_" + traceRetriverName + "_" + traceContextName + " = { string = \"" + CONTEXT_VALUE + "\" } }";
        output.forEach(line -> assertTrue(line.contains(expectedString)));
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test the sequence Agent -> Retriever -> Session
     */
    @Test
    public void testAgentRetrieverSession() {
        registerAgent();
        registerRetriever();
        enableContextInSessions();

        traceSendEventsAndVerify();
    }

    /**
     * Test the sequence Agent -> Session -> Retriever
     */
    @Test
    public void testAgentSessionRetriever() {
        registerAgent();
        enableContextInSessions();
        registerRetriever();

        traceSendEventsAndVerify();
    }

    /**
     * Test the sequence Retriever -> Agent -> Session
     */
    @Test
    public void testRetrieverAgentSession() {
        registerRetriever();
        registerAgent();
        enableContextInSessions();

        traceSendEventsAndVerify();
    }

    /**
     * Test the sequence Retriever -> Session -> Agent
     */
    @Test
    public void testRetrieverSessionAgent() {
        registerAgent();
        registerRetriever();
        enableContextInSessions();

        traceSendEventsAndVerify();
    }

    /**
     * Test the sequence Session -> Agent -> Retriever
     */
    @Test
    public void testSessionAgentRetriever() {
        registerAgent();
        registerRetriever();
        enableContextInSessions();

        traceSendEventsAndVerify();
    }

    /**
     * Test the sequence Session -> Retriever -> Agent
     */
    @Test
    public void testSessionRetrieverAgent() {
        registerAgent();
        registerRetriever();
        enableContextInSessions();

        traceSendEventsAndVerify();
    }
}
