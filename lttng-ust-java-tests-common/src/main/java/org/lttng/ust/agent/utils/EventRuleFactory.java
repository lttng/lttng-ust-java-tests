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

import java.util.StringJoiner;

import org.lttng.ust.agent.session.EventRule;
import org.lttng.ust.agent.session.LogLevelSelector;

/**
 * Factory class for {@link EventRule} objects, offering convenience methods
 * that mimic the results of passing the given arguments on the command-line.
 *
 * @author Alexandre Montplaisir
 */
public final class EventRuleFactory {

    /** Name of the "all" (-a) event */
    public static final String EVENT_NAME_ALL = "*";

    /** Log level set by default when it is not specified */
    public static final LogLevelSelector LOG_LEVEL_UNSPECIFIED = new LogLevelSelector(Integer.MIN_VALUE, 0);

    private EventRuleFactory() {}

    /**
     * Construct an event by only passing the event name on the command-line.
     *
     * @param eventName
     *            The event name
     * @return The corresponding event rule
     */
    public static EventRule createRule(String eventName) {
        return new EventRule(eventName, LOG_LEVEL_UNSPECIFIED, filterStringFromEventName(eventName));
    }

    /**
     * Construct and event rule by specifying the event name and log level.
     *
     * @param eventName
     *            The event name
     * @param logLevelSelector
     *            The log level
     * @return The corresponding event rule
     */
    public static EventRule createRule(String eventName, LogLevelSelector logLevelSelector) {
        StringJoiner sj = new StringJoiner(") && (", "(", ")");
        String filterStr = sj.add(filterStringFromEventName(eventName))
                .add(filterStringFromLogLevel(logLevelSelector))
                .toString();
        return new EventRule(eventName, logLevelSelector, filterStr);
    }

    /**
     * Construct and event rule by specifying the event name, log level, and a
     * filter string.
     *
     * @param eventName
     *            The event name
     * @param logLevelSelector
     *            The log level
     * @param extraFilter
     *            The filter string passed on the command-line
     * @return The corresponding event rule
     */
    public static EventRule createRule(String eventName, LogLevelSelector logLevelSelector, String extraFilter) {
        StringJoiner sj1 = new StringJoiner(") && (", "(", ")");
        sj1.add(extraFilter);
        sj1.add(filterStringFromEventName(eventName));
        String firstPart = sj1.toString();

        if (logLevelSelector.equals(LOG_LEVEL_UNSPECIFIED)) {
            return new EventRule(eventName, logLevelSelector, firstPart);
        }

        /*
         * If there is both a filter and a log level, the filter + event name is
         * "resolved" first.
         */
        StringJoiner sj2 = new StringJoiner(") && (", "(", ")");
        sj2.add(firstPart);
        sj2.add(filterStringFromLogLevel(logLevelSelector));
        return new EventRule(eventName, logLevelSelector, sj2.toString());
    }

    /**
     * Construct an event rule corresponding to enabling all (-a) events.
     *
     * @return The corresponding event rule
     */
    public static EventRule createRuleAllEvents() {
        return new EventRule(EVENT_NAME_ALL, LOG_LEVEL_UNSPECIFIED, "");
    }

    private static String filterStringFromEventName(String eventName) {
        return "logger_name == \"" + eventName + "\"";
    }

    private static String filterStringFromLogLevel(LogLevelSelector logLevelSelector) {
        StringBuilder sb = new StringBuilder();
        sb.append("int_loglevel ");

        switch (logLevelSelector.getLogLevelType()) {
        case LTTNG_EVENT_LOGLEVEL_RANGE:
            sb.append(">=");
            break;
        case LTTNG_EVENT_LOGLEVEL_SINGLE:
            sb.append("==");
            break;
        case LTTNG_EVENT_LOGLEVEL_ALL:
        default:
            throw new IllegalArgumentException();
        }

        sb.append(" " + logLevelSelector.getLogLevel());
        return sb.toString();
    }

}
