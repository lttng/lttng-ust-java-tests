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

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.lttng.tools.ILttngSession;
import org.lttng.ust.agent.log4j.LttngLogAppender;
import org.lttng.ust.agent.utils.Log4jTestUtils;

/**
 * Test suite for the list events command for the log4j domain
 */
@Tag("agent:log4j")
@Tag("domain:log4j")
public class Log4jListEventsIT extends ListEventsITBase {

    private Logger[] loggers;
    private Appender[] appenders;

    /**
     * Class setup
     */
    @BeforeAll
    public static void log4jClassSetup() {
        Log4jTestUtils.testClassSetup();
    }

    /**
     * Class cleanup
     */
    @AfterAll
    public static void log4jClassCleanup() {
        Log4jTestUtils.testClassCleanup();
    }

    /**
     * Test setup
     *
     * @throws SecurityException
     * @throws IOException
     */
    @BeforeEach
    public void log4jSetup() throws SecurityException, IOException {
        /* Try clearing the log4j logger configuration */
        LogManager.resetConfiguration();
        System.gc();

        loggers = new Logger[] {
                Logger.getLogger(LOGGER_NAME_1),
                Logger.getLogger(LOGGER_NAME_2),
                Logger.getLogger(LOGGER_NAME_3)
        };

        appenders = new Appender[] {
                new LttngLogAppender(),
                new LttngLogAppender()
        };
    }

    /**
     * Test teardown. Detach and close all log handlers.
     */
    @AfterEach
    public void log4jTeardown() {
        for (Logger logger : loggers) {
            for (Appender appender : appenders) {
                logger.removeAppender(appender);
            }
        }

        for (Appender appender : appenders) {
            appender.close();
        }
        appenders = null;
        loggers = null;
    }

    @Override
    protected ILttngSession.Domain getDomain() {
        return ILttngSession.Domain.LOG4J;
    }

    @Override
    protected void attachHandlerToLogger(int handlerIndex, int loggerIndex) {
        loggers[loggerIndex - 1].addAppender(appenders[handlerIndex - 1]);
    }

    @Override
    protected void detachHandlerFromLogger(int handlerIndex, int loggerIndex) {
        loggers[loggerIndex - 1].removeAppender(appenders[handlerIndex - 1]);
    }

}
