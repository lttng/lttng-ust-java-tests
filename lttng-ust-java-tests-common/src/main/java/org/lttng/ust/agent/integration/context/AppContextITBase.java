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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.context.ContextInfoManager;
import org.lttng.ust.agent.utils.TestPrintRunner;

/**
 * Base abstract class to implement all sorts of integration tests verifying the
 * presence of enabled application contexts in resulting traces.
 */
@RunWith(TestPrintRunner.class)
public abstract class AppContextITBase {

    protected static final String EVENT_NAME = "EventName";

    protected static final String RETRIEVER_NAME_1 = "Retriever1";
    protected static final String RETRIEVER_NAME_2 = "Retriever2";

    private static final String CONTEXT_NAME = ContextInfoRetrieverStubs.CONTEXT_NAME;

    private ContextInfoManager cim;
    private ILttngSession session;

    /* Field defined by the sub-class */
    protected ILttngHandler logHandler;

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
     * Base test teardown
     */
    @After
    public void testTeardown() {
        session.close();

        logHandler.close();
        logHandler = null;

        /* In case some tests fail or forget to unregister their retrievers */
        cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1);
        cim.unregisterContextInfoRetriever(RETRIEVER_NAME_2);
    }

    // ------------------------------------------------------------------------
    // Context enabled/disabled tests
    // ------------------------------------------------------------------------

    /**
     * Utility method to check that a context is present in all events of a
     * trace output.
     */
    private static void testContextPresentInTrace(List<String> traceOutput, String retrieverName, String contextName, String contextValue) {
        String fullString = "_app_" + retrieverName + "_" + contextName + " = " + contextValue;
        traceOutput.forEach(line -> assertTrue(line.contains(fullString)));
    }

    /**
     * Utility method to check that a context is *absent* from all events of a
     * trace output
     */
    private static void testContextNotPresentInTrace(List<String> traceOutput, String retrieverName, String contextName) {
        String fullString = "_app_" + retrieverName + "_" + contextName;
        traceOutput.forEach(line -> assertFalse(line.contains(fullString)));
    }

    /**
     * Test that if no retrievers are declared, no context info is passed at
     * all.
     */
    @Test
    public void testNoContexts() {
        assertTrue(session.enableAllEvents());
        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        /* Test that there is no "_app" contexts in the output */
        output.forEach(line -> assertFalse(line.contains("_app")));
    }

    /**
     * Test that if a retriever is registered and provides a context, but this
     * context is not enabled in the tracing session, that it is not present in
     * the trace.
     */
    @Test
    public void testContextAvailableButNotEnabled() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.STRING_RETRIEVER));

        assertTrue(session.enableAllEvents());
        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        /* Test that there is no "_app" contexts in the output */
        output.forEach(line -> assertFalse(line.contains("_app")));

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test that if a context is enabled, but no retriever provides it, that the
     * retriever/context names are still mentioned in the event but no value is
     * provided.
     */
    @Test
    public void testContextNotAvailableButEnabled() {
        assertTrue(session.enableAllEvents());
        assertTrue(session.enableAppContext(RETRIEVER_NAME_1, CONTEXT_NAME));

        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        /* Test that context name is there but value is not */
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME, "{ none = { } } }");
    }

    /**
     * Test that if a context is enabled and provided by a retriever that it is
     * correctly present in the tracing session.
     */
    @Test
    public void testContextAvailableAndEnabled() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.STRING_RETRIEVER));

        assertTrue(session.enableAllEvents());
        assertTrue(session.enableAppContext(RETRIEVER_NAME_1, CONTEXT_NAME));

        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        /* Test that context name + value are present */
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ string = \"" + ContextInfoRetrieverStubs.STRING_VALUE + "\" }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test that one context is available by a retriever, but another is is
     * enabled in the session. Only the latter should be mentioned in events,
     * with no value.
     */
    @Test
    public void testContextsOneAvailableOtherEnabled() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.STRING_RETRIEVER));

        assertTrue(session.enableAllEvents());
        assertTrue(session.enableAppContext(RETRIEVER_NAME_2, CONTEXT_NAME));

        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        /* Test that only retriever-name-2 is present, with no value */
        testContextNotPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME);
        testContextPresentInTrace(output, RETRIEVER_NAME_2, CONTEXT_NAME, "{ none = { } } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test with two contexts provided in the application, but only one of them
     * is enabled in the session. Only that one should be present in the trace,
     * name and value.
     */
    @Test
    public void testContextsTwoAvailableOneEnabled() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.STRING_RETRIEVER));
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_2, ContextInfoRetrieverStubs.INTEGER_RETRIEVER));

        assertTrue(session.enableAllEvents());
        assertTrue(session.enableAppContext(RETRIEVER_NAME_1, CONTEXT_NAME));

        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        /* Test that only retriever-name-1 is present, name + value */
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ string = \"" + ContextInfoRetrieverStubs.STRING_VALUE + "\" }");
        testContextNotPresentInTrace(output, RETRIEVER_NAME_2, CONTEXT_NAME);

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_2));
    }

    /**
     * Test with two contexts enabled in the session but only one of them is
     * provided by the application. Both should be mentioned in the trace, but
     * only the provided one will have a value.
     */
    @Test
    public void testContextsOneAvailableTwoEnabled() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.STRING_RETRIEVER));

        assertTrue(session.enableAllEvents());
        assertTrue(session.enableAppContext(RETRIEVER_NAME_1, CONTEXT_NAME));
        assertTrue(session.enableAppContext(RETRIEVER_NAME_2, CONTEXT_NAME));

        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        /* Test that both contexts are present, but only retriever-1's has a value */
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ string = \"" + ContextInfoRetrieverStubs.STRING_VALUE + "\" }");
        testContextPresentInTrace(output, RETRIEVER_NAME_2, CONTEXT_NAME, "{ none = { } } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    // ------------------------------------------------------------------------
    // Context types tests
    // ------------------------------------------------------------------------

    /**
     * Utility method to enable all events, add the one context we are looking
     * for, take a trace, and return the trace output.
     */
    private List<String> enableContextAndTrace() {
        assertTrue(session.enableAllEvents());
        assertTrue(session.enableAppContext(RETRIEVER_NAME_1, CONTEXT_NAME));
        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        return output;
    }

    /**
     * Test a "null" context value.
     */
    @Test
    public void testContextValueNull() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.NULL_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME, "{ none = { } } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test an integer (int32) context value.
     */
    @Test
    public void testContextValueInteger() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.INTEGER_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ int32 = " + ContextInfoRetrieverStubs.INTEGER_VALUE + " } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a long (int64) context value.
     */
    @Test
    public void testContextValueLong() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.LONG_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ int64 = " + ContextInfoRetrieverStubs.LONG_VALUE + " } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a double context value.
     */
    @Test
    public void testContextValueDouble() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.DOUBLE_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ double = " + ContextInfoRetrieverStubs.DOUBLE_VALUE + " } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a character context value (should get converted to a string).
     */
    @Test
    public void testContextValueCharacter() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.CHARACTER_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ string = \"" + ContextInfoRetrieverStubs.CHARACTER_VALUE + "\" } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a float context value.
     */
    @Test
    public void testContextValueFloat() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.FLOAT_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ float = " + ContextInfoRetrieverStubs.FLOAT_VALUE + " } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a byte (int8) context value.
     */
    @Test
    public void testContextValueByte() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.BYTE_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ int8 = " + ContextInfoRetrieverStubs.BYTE_VALUE + " } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a short (int16) context value.
     */
    @Test
    public void testContextValueShort() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.SHORT_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ int16 = " + ContextInfoRetrieverStubs.SHORT_VALUE + " } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a "true" boolean context value (gets converted to a int8 of value 1).
     */
    @Test
    public void testContextValueBooleanTrue() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.BOOLEAN_TRUE_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME, "{ int8 = 1 } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a "false" boolean context value (gets converted to a int8 of value 0).
     */
    @Test
    public void testContextValueBooleanFalse() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.BOOLEAN_FALSE_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME, "{ int8 = 0 } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a string context value.
     */
    @Test
    public void testContextValueString() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.STRING_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ string = \"" + ContextInfoRetrieverStubs.STRING_VALUE + "\" } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test a Object context value (should be converted to a String via .toString()).
     */
    @Test
    public void testContextValueObject() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.OBJECT_RETRIEVER));

        List<String> output = enableContextAndTrace();
        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ string = \"" + ContextInfoRetrieverStubs.OBJECT_VALUE.toString() + "\" } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    // ------------------------------------------------------------------------
    // Tests related to filtering
    // ------------------------------------------------------------------------

    /**
     * Test with a filter expression using a context, but not having the actual
     * context enabled.
     *
     * The JNI should still send the context so UST can use it for filtering,
     * but it should not be present in the resulting trace.
     */
    @Test
    public void testContextFilterExpressionNotEnabled() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.STRING_RETRIEVER));

        assertTrue(session.enableEvent(EVENT_NAME, null, false,
                "$app." + RETRIEVER_NAME_1 + ':' + CONTEXT_NAME + "==\"" + ContextInfoRetrieverStubs.STRING_VALUE + '\"'));

        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        testContextNotPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME);

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test with a filter expression and an enabled context. The filter however
     * should *exclude* the events, so no events should be present in the
     * resulting trace.
     */
    @Test
    public void testContextFilterExpressionEnabledNotMatching() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.STRING_RETRIEVER));

        assertTrue(session.enableEvent(EVENT_NAME, null, false,
                "$app." + RETRIEVER_NAME_1 + ':' + CONTEXT_NAME + "!=\"" + ContextInfoRetrieverStubs.STRING_VALUE + '\"'));

        assertTrue(session.enableAppContext(RETRIEVER_NAME_1, CONTEXT_NAME));
        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertTrue(output.isEmpty());

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }

    /**
     * Test with a filter expression and an enabled context. The filter however
     * should match the events, so events with the context info should be
     * present in the resulting trace.
     */
    @Test
    public void testContextFilterExpressionEnabledMatching() {
        assertTrue(cim.registerContextInfoRetriever(RETRIEVER_NAME_1, ContextInfoRetrieverStubs.STRING_RETRIEVER));

        assertTrue(session.enableEvent(EVENT_NAME, null, false,
                "$app." + RETRIEVER_NAME_1 + ':' + CONTEXT_NAME + "==\"" + ContextInfoRetrieverStubs.STRING_VALUE + '\"'));

        assertTrue(session.enableAppContext(RETRIEVER_NAME_1, CONTEXT_NAME));
        assertTrue(session.start());
        sendEventsToLoggers();
        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        testContextPresentInTrace(output, RETRIEVER_NAME_1, CONTEXT_NAME,
                "{ string = \"" + ContextInfoRetrieverStubs.STRING_VALUE + "\" } }");

        assertTrue(cim.unregisterContextInfoRetriever(RETRIEVER_NAME_1));
    }
}
