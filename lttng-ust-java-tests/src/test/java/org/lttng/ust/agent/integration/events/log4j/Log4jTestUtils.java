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

package org.lttng.ust.agent.integration.events.log4j;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Utility methods for log4j tests
 */
final class Log4jTestUtils {

    private Log4jTestUtils() {
    }

    static void send10Events(Logger logger) {
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
