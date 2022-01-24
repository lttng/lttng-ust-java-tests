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

package org.lttng.ust.agent.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lttng.tools.LttngToolsHelper;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.jul.LttngLogHandler;

/**
 * Utility methods for JUL tests
 */
public final class JulTestUtils {

    private JulTestUtils() {
    }

    /**
     * Setup method common to most JUL tests. To be called in a @BeforeClass.
     */
    public static void testClassSetup() {
        /* Make sure we can find the JNI library and lttng-tools */
        checkForJulLibrary();
        assertTrue(LttngUtils.checkForLttngTools(Domain.JUL), "lttng-tools is not working properly.");

        LttngToolsHelper.destroyAllSessions();
    }

    /**
     * Teardown method common to most JUL tests. To be called in a @AfterClass.
     */
    public static void testClassCleanup() {
        LttngToolsHelper.deleteAllTraces();
    }

    /**
     * Check the the JUL native library is available, effectively allowing LTTng
     * JUL handlers to be used.
     */
    private static void checkForJulLibrary() {
        try {
            LttngLogHandler testHandler = new LttngLogHandler();
            testHandler.close();
        } catch (SecurityException | IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Send 10 dummy events through the provided logger
     *
     * @param logger
     *            The logger to use to send events
     */
    public static void send10EventsTo(Logger logger) {
        String a = new String("a");
        Object[] params = { a, new String("b"), new Object() };

        // Levels are FINE, FINER, FINEST, INFO, SEVERE, WARNING
        logger.fine("A fine level message");
        logger.finer("A finer level message");
        logger.finest("A finest level message");
        logger.info("A info level message");
        logger.severe("A severe level message");
        logger.warning("A warning level message");
        logger.warning("Another warning level message");
        logger.log(Level.WARNING, "A warning message using Logger.log()");
        logger.log(Level.INFO, "A message with one parameter", a);
        logger.log(Level.INFO, "A message with parameters", params);
    }
}
