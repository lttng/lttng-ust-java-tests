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

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.lttng.tools.ILttngSession;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.ILogLevelStrings;
import org.lttng.ust.agent.utils.JulTestUtils;

/**
 * Filter notifications tests using the JUL logging API.
 *
 * @author Alexandre Montplaisir
 */
public class JulFilterListenerIT extends FilterListenerITBase {

    /**
     * Class setup
     */
    @BeforeAll
    public static void julClassSetup() {
        JulTestUtils.testClassSetup();
    }

    /**
     * Class cleanup
     */
    @AfterAll
    public static void julClassCleanup() {
        JulTestUtils.testClassCleanup();
    }

    @Override
    protected ILttngSession.Domain getSessionDomain() {
        return ILttngSession.Domain.JUL;
    }

    @Override
    protected ILttngHandler getLogHandler() throws SecurityException, IOException {
        return new LttngLogHandler();
    }

    @Override
    protected ILogLevelStrings getLogLevelStrings() {
        return ILogLevelStrings.JUL_LOGLEVEL_STRINGS;
    }

}
