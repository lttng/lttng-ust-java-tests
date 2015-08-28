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

package org.lttng.ust.agent.integration.filter;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.log4j.LttngLogAppender;
import org.lttng.ust.agent.utils.ILogLevelStrings;
import org.lttng.ust.agent.utils.LttngUtils;

/**
 * Filter notifications tests using the log4j logging API.
 *
 * @author Alexandre Montplaisir
 */
public class Log4jFilterListenerIT extends FilterListenerITBase {

    /**
     * Class setup
     */
    @BeforeClass
    public static void log4jClassSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        assumeTrue(LttngUtils.checkForLog4jLibrary());
        assumeTrue(LttngUtils.checkForLttngTools(ILttngSession.Domain.LOG4J));
        LttngToolsHelper.destroyAllSessions();
    }

    @Override
    protected ILttngSession.Domain getSessionDomain() {
        return ILttngSession.Domain.LOG4J;
    }

    @Override
    protected ILttngHandler getLogHandler() throws SecurityException, IOException {
        return new LttngLogAppender();
    }

    @Override
    protected ILogLevelStrings getLogLevelStrings() {
        return ILogLevelStrings.LOG4J_LOGLEVEL_STRINGS;
    }

}
