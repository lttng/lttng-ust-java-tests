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

/**
 * Interface to match the log level values used by LTTng to their representation
 * (name and integer value) used by the logging APIs.
 *
 * @author Alexandre Montplaisir
 */
public interface ILogLevelStrings {

    /**
     * @return The string representation of the "warning" level
     */
    String warningName();

    /**
     * @return The integer representation of the "warning" level
     */
    int warningInt();

    /**
     * @return The string representation of the "info" level
     */
    String infoName();

    /**
     * @return The integer representation of the "info" level
     */
    int infoInt();

    /**
     * Values for JUL
     */
    ILogLevelStrings JUL_LOGLEVEL_STRINGS = new ILogLevelStrings() {

        @Override
        public String warningName() {
            return "warning";
        }

        @Override
        public int warningInt() {
            return 900;
        }

        @Override
        public String infoName() {
            return "info";
        }

        @Override
        public int infoInt() {
            return 800;
        }
    };

    /**
     * Values for log4j 1.x
     */
    ILogLevelStrings LOG4J_LOGLEVEL_STRINGS = new ILogLevelStrings() {

        @Override
        public String warningName() {
            return "warn";
        }

        @Override
        public int warningInt() {
            return 30000;
        }

        @Override
        public String infoName() {
            return "info";
        }

        @Override
        public int infoInt() {
            return 20000;
        }
    };

    /**
     * Values for log4j 2.x
     */
    ILogLevelStrings LOG4J2_LOGLEVEL_STRINGS = new ILogLevelStrings() {

        @Override
        public String warningName() {
            return "warn";
        }

        @Override
        public int warningInt() {
            return 300;
        }

        @Override
        public String infoName() {
            return "info";
        }

        @Override
        public int infoInt() {
            return 400;
        }
    };
}
