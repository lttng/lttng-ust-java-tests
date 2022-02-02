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

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.log4j2.LttngLogAppender;

import org.lttng.ust.agent.utils.Log4j2TestUtils;

/**
 * Filter notifications tests using the log4j logging API.
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class Log4j2FilterListenerITBase extends FilterListenerITBase {

    /**
     * Class setup
     */
    @BeforeAll
    public void log4j2ClassSetup() {
        Log4j2TestUtils.testClassSetup(getSessionDomain());
    }

    /**
     * Class cleanup
     */
    @AfterAll
    public static void log4j2ClassCleanup() {
        Log4j2TestUtils.testClassCleanup();
    }

    @Override
    protected ILttngHandler getLogHandler() throws SecurityException, IOException {
        return LttngLogAppender.createAppender(this.getClass().getSimpleName(), getSessionDomain().toString(), null, null);
    }
}
