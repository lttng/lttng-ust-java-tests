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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.log4j.LttngLogAppender;

/**
 * Utility methods for log4j tests
 */
public final class Log4jTestUtils {

    private Log4jTestUtils() {
    }

    /**
     * Setup method common to most log4j tests. To be called in a @BeforeClass.
     */
    public static void testClassSetup() {
        /* Make sure we can find the JNI library and lttng-tools */
        checkForLog4jLibrary();
        assertTrue("lttng-tools is not working properly.", LttngUtils.checkForLttngTools(Domain.LOG4J));

        LttngToolsHelper.destroyAllSessions();
    }

    /**
     * Teardown method common to most log4j tests. To be called in a @AfterClass.
     */
    public static void testClassCleanup() {
        LttngToolsHelper.deleteAllTraces();
    }

    /**
     * Check the the Log4j native library is available, effectively allowing
     * LTTng Log4j appenders to be used.
     */
    private static void checkForLog4jLibrary() {
        try {
            LttngLogAppender testAppender = new LttngLogAppender();
            testAppender.close();
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
    public static void send10Events(Logger logger) {
        // Levels/priorities are DEBUG, ERROR, FATAL, INFO, TRACE, WARN
        logger.debug("Debug message. Lost among so many.");
        logger.debug("Debug message with a throwable", new IOException());
        logger.error("Error messsage. This might be bad.");
        logger.error("Error message with a throwable", new IOException());
        logger.fatal("A fatal message. You are already dead.");
        logger.info("A info message. Lol, who cares.");
        logger.trace("A trace message. No, no *that* trace");
        logger.warn("A warn message. Yellow underline.");
        logger.log(Level.DEBUG, "A debug message using .log()");
        logger.log(Level.ERROR, "A error message using .log()");
    }
}
