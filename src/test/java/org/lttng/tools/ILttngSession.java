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

package org.lttng.tools;

import java.util.List;

/**
 * Java representation of a LTTng tracing session.
 *
 * @author Alexandre Montplaisir
 */
public interface ILttngSession extends AutoCloseable {

    /**
     * Tracing domains as they are defined by lttng-tools
     */
    enum Domain {
        /** The JUL (java.util.logging) domain */
        JUL("-j"), /** The log4j (org.apache.log4j) domain */
        LOG4J("-l");

        private final String flag;

        private Domain(String flag) {
            this.flag = flag;
        }

        /**
         * @return The corresponding command-line flag to pass to options like
         *         "lttng enable-event"
         */
        public String flag() {
            return flag;
        }
    }

    // ------------------------------------------------------------------------
    // Factory methods
    // ------------------------------------------------------------------------

    /**
     * Constructor to create a new LTTng tracing session, which will use the
     * command-line "lttng" utility.
     *
     * @param sessionName
     *            The name of the session to use. It can be null, in which case
     *            we will provide a unique random name.
     * @param domain
     *            The tracing domain of this session
     * @return The new session object
     */
    static ILttngSession newCommandLineSession(String sessionName, Domain domain) {
        return new LttngCommandLineSession(sessionName, domain);
    }

    // ------------------------------------------------------------------------
    // AutoCloseable
    // ------------------------------------------------------------------------

    /**
     * Should be used to destroy the LTTng session.
     */
    @Override
    void close();

    // ------------------------------------------------------------------------
    // Session management
    // ------------------------------------------------------------------------

    /**
     * Enable all events in the session (as with "enable-event -a").
     *
     * @return If the command executed successfully (return code = 0).
     */
    boolean enableAllEvents();

    /**
     * Enable individual event(s).
     *
     * @param enabledEvents
     *            The list of events to enable. Should not be null or empty
     * @return If the command executed successfully (return code = 0).
     */
    boolean enableEvents(String... enabledEvents);

    /**
     * Send a disable-event command. Used to disable events that were previously
     * enabled.
     *
     * @param disabledEvents
     *            The list of disabled events. Should not be null or empty
     * @return If the command executed successfully (return code = 0).
     */
    boolean disableEvents(String... disabledEvents);

    /**
     * Start tracing
     *
     * @return If the command executed successfully (return code = 0).
     */
    boolean start();

    /**
     * Stop the tracing session
     *
     * @return If the command executed successfully (return code = 0).
     */
    boolean stop();

    /**
     * Issue a "lttng view" command on the session, and returns its output. This
     * effectively returns the current content of the trace in text form.
     *
     * @return The output of Babeltrace on the session's current trace
     */
    List<String> view();
}
