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

package org.lttng.ust.agent.integration.jul;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility methods for JUL tests
 */
final class JulTestUtils {

    JulTestUtils() {
    }

    static void send10EventsTo(Logger logger) {
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
