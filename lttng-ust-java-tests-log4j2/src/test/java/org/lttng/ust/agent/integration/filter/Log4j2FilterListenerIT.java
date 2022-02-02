/*
 * Copyright (C) 2022, EfficiOS Inc.
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

package org.lttng.ust.agent.integration.filter;

import org.junit.jupiter.api.Tag;
import org.lttng.tools.ILttngSession;
import org.lttng.ust.agent.utils.ILogLevelStrings;


/**
 * Filter notifications tests using the log4j logging API.
 */
@Tag("agent:log4j2")
@Tag("domain:log4j2")
public class Log4j2FilterListenerIT extends Log4j2FilterListenerITBase {

    @Override
    protected ILttngSession.Domain getSessionDomain() {
        return ILttngSession.Domain.LOG4J2;
    }

    @Override
    protected ILogLevelStrings getLogLevelStrings() {
        return ILogLevelStrings.LOG4J2_LOGLEVEL_STRINGS;
    }
}
