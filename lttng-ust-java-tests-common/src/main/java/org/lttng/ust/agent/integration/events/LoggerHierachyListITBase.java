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

package org.lttng.ust.agent.integration.events;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.lttng.tools.ILttngSession;

/**
 * Base class testing the "lttng list" command when using loggers organized as a
 * hierarchy.
 *
 * For example, if a "org.myapp" logger exists and has a LTTng handler attached,
 * a "org.myapp.mycomponent" logger should show up in "lttng list", even if
 * itself does not have a handler, because its parent's handler can catch the
 * log events as UST tracepoints.
 *
 * @author Alexandre Montplaisir
 */
public abstract class LoggerHierachyListITBase {

    protected static final String PARENT_LOGGER = "org.lttng";
    protected static final String CHILD_LOGGER = "org.lttng.mycomponent";

    protected final boolean parentLoggerActive;
    protected final boolean parentLoggerHasHandler;
    protected final boolean childLoggerActive;
    protected final boolean childLoggerHasHandler;

    private ILttngSession session;

    // ------------------------------------------------------------------------
    // Test parameter definition
    // ------------------------------------------------------------------------

    /**
     * Generator for the test parameters.
     *
     * Generates all possible combinations of the 4 constructor parameters,
     * except "parentActive" is necessarily true when "hasHandler" is true for a
     * given logger.
     *
     * @return The test parameters
     */
    @Parameters(name = "{index}: parentActive={0}, parentHasHandler={1}, childActive={2}, childHasHandler={3}")
    public static Iterable<Object[]> testCases() {
        /*
         * Kept the whole array for clarity, but some cases are commented out:
         * it is impossible to attach an handler if the logger itself is not
         * defined!
         */
        return Arrays.asList(new Object[][] {
                { Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE },
                { Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE },
//                { Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE },
                { Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE },

                { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE },
                { Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE },
//                { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE },
                { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE },

//                { Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE },
//                { Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE },
//                { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE },
//                { Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE },

                { Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE },
                { Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE },
//                { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE },
                { Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE },
        });
    }

    /**
     * Test constructor
     *
     * @param parentLoggerActive
     *            Parent logger has been instantiated
     * @param parentLoggerHasHandler
     *            Parent logger has a LTTng handler attached to it
     * @param childLoggerActive
     *            Child logger has been instantiated
     * @param childLoggerHasHandler
     *            Child logger has a LTTng handler attached to it
     */
    public LoggerHierachyListITBase(boolean parentLoggerActive,
            boolean parentLoggerHasHandler,
            boolean childLoggerActive,
            boolean childLoggerHasHandler) {
        this.parentLoggerActive = parentLoggerActive;
        this.parentLoggerHasHandler = parentLoggerHasHandler;
        this.childLoggerActive = childLoggerActive;
        this.childLoggerHasHandler = childLoggerHasHandler;
    }

    protected ILttngSession getSession() {
        return session;
    }

    // ------------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------------

    /**
     * Common test setup
     */
    @Before
    public void testSetup() {
        session = ILttngSession.createSession(null, getDomain());
    }

    /**
     * Common test teardown
     */
    @After
    public void testTeardown() {
        session.close();
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    protected abstract ILttngSession.Domain getDomain();

    protected abstract void activateLoggers() throws IOException;

    // ------------------------------------------------------------------------
    // Common tests
    // ------------------------------------------------------------------------

    /**
     * Test the output of the "lttng list" command.
     *
     * @throws IOException
     *             Fails the test
     */
    @Test
    public void testList() throws IOException {
        activateLoggers();

        List<String> enabledEvents = session.listEvents();
        List<String> expectedEvents = new ArrayList<>();

        /* Reminder: "hasHandler" implies "isActive" */

        if (parentLoggerHasHandler) {
            expectedEvents.add(PARENT_LOGGER);
        }
        if (childLoggerHasHandler ||
                (childLoggerActive && parentLoggerHasHandler)) {
            expectedEvents.add(CHILD_LOGGER);
        }

        Collections.sort(enabledEvents);
        Collections.sort(expectedEvents);
        assertEquals(expectedEvents, enabledEvents);
    }
}
