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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lttng.tools.ILttngSession;
import org.lttng.ust.agent.utils.TestPrintExtension;

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
@ExtendWith(TestPrintExtension.class)
public abstract class LoggerHierachyListITBase {

    protected static final String PARENT_LOGGER = "org.lttng";
    protected static final String CHILD_LOGGER = "org.lttng.mycomponent";

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
     * @param parentLoggerActive
     *            Parent logger has been instantiated
     * @param parentLoggerHasHandler
     *            Parent logger has a LTTng handler attached to it
     * @param childLoggerActive
     *            Child logger has been instantiated
     * @param childLoggerHasHandler
     *            Child logger has a LTTng handler attached to it
     *
     * @return The test parameters
     */
    protected static Stream<Arguments> provideArguments() {

        /*
         * Kept the whole array for clarity, but some cases are commented out:
         * it is impossible to attach an handler if the logger itself is not
         * defined!
         */
        return Stream.of(
                Arguments.of( Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE ),
                Arguments.of( Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE ),
//              Arguments.of( Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE ),
                Arguments.of( Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE ),

                Arguments.of( Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE ),
                Arguments.of( Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE ),
//              Arguments.of( Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE ),
                Arguments.of( Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE ),

//              Arguments.of( Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE ),
//              Arguments.of( Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE ),
//              Arguments.of( Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE ),
//              Arguments.of( Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE ),

                Arguments.of( Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE ),
                Arguments.of( Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE ),
//              Arguments.of( Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE ),
                Arguments.of( Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE )
        );
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
    @BeforeEach
    public void testSetup() {
        session = ILttngSession.createSession(null, getDomain());
    }

    /**
     * Common test teardown
     */
    @AfterEach
    public void testTeardown() {
        session.close();
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    protected abstract ILttngSession.Domain getDomain();

    protected abstract void activateLoggers(boolean parentLoggerActive,
            boolean parentLoggerHasHandler,
            boolean childLoggerActive,
            boolean childLoggerHasHandler) throws IOException;

    // ------------------------------------------------------------------------
    // Common tests
    // ------------------------------------------------------------------------

    /**
     * Test the output of the "lttng list" command.
     *
     * @param parentLoggerActive
     *            Parent logger has been instantiated
     * @param parentLoggerHasHandler
     *            Parent logger has a LTTng handler attached to it
     * @param childLoggerActive
     *            Child logger has been instantiated
     * @param childLoggerHasHandler
     *            Child logger has a LTTng handler attached to it
     *
     * @throws IOException
     *             Fails the test
     */
    @ParameterizedTest
    @MethodSource("provideArguments")
    public void testList(boolean parentLoggerActive,
            boolean parentLoggerHasHandler,
            boolean childLoggerActive,
            boolean childLoggerHasHandler) throws IOException {

        activateLoggers(parentLoggerActive,
                parentLoggerHasHandler,
                childLoggerActive,
                childLoggerHasHandler);

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
