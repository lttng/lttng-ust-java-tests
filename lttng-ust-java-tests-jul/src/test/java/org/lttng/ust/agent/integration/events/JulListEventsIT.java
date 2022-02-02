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

package org.lttng.ust.agent.integration.events;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.lttng.tools.ILttngSession;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.JulTestUtils;

/**
 * Test suite for the list events command for the JUL domain
 */
@Tag("agent:jul")
@Tag("domain:jul")
public class JulListEventsIT extends ListEventsITBase {

    private Logger[] loggers;
    private Handler[] handlers;

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

    /**
     * Test setup
     *
     * @throws SecurityException
     * @throws IOException
     */
    @BeforeEach
    public void julSetup() throws SecurityException, IOException {
        /* Clear the JUL logger configuration */
        LogManager.getLogManager().reset();
        System.gc();

        loggers = new Logger[] {
                Logger.getLogger(LOGGER_NAME_1),
                Logger.getLogger(LOGGER_NAME_2),
                Logger.getLogger(LOGGER_NAME_3)
        };

        handlers = new Handler[] {
                new LttngLogHandler(),
                new LttngLogHandler()
        };
    }

    /**
     * Test teardown. Detach and close all log handlers.
     */
    @AfterEach
    public void julTeardown() {
        for (Logger logger : loggers) {
            for (Handler handler : handlers) {
                logger.removeHandler(handler);
            }
        }

        for (Handler handler : handlers) {
            handler.close();
        }
        handlers = null;
        loggers = null;
    }

    @Override
    protected ILttngSession.Domain getDomain() {
        return ILttngSession.Domain.JUL;
    }

    @Override
    protected void attachHandlerToLogger(int handlerIndex, int loggerIndex) {
        loggers[loggerIndex - 1].addHandler(handlers[handlerIndex - 1]);
    }

    @Override
    protected void detachHandlerFromLogger(int handlerIndex, int loggerIndex) {
        loggers[loggerIndex - 1].removeHandler(handlers[handlerIndex - 1]);
    }

}
