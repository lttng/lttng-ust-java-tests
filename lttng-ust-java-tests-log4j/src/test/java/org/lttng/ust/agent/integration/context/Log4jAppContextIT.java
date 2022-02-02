/*
 * Copyright (C) 2016, EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
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

package org.lttng.ust.agent.integration.context;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.log4j.LttngLogAppender;
import org.lttng.ust.agent.utils.Log4jTestUtils;

/**
 * Enabled app contexts test for the LTTng-UST JUL log handler.
 */
@Tag("agent:log4j")
@Tag("domain:log4j")
public class Log4jAppContextIT extends AppContextITBase {

    private static final Domain DOMAIN = Domain.LOG4J;

    private Logger logger;

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
    public void julSetup() throws SecurityException, IOException {
        logger = Logger.getLogger(EVENT_NAME);
        logger.setLevel(Level.ALL);

        logHandler = new LttngLogAppender();
        logger.addAppender((Appender) logHandler);
    }

    /**
     * Test teardown
     */
    @AfterEach
    public void julTeardown() {
        logger.removeAppender((Appender) logHandler);
        logger = null;
    }

    @Override
    protected Domain getDomain() {
        return DOMAIN;
    }

    @Override
    protected boolean closeHandlers()
    {
        return true;
    }

    @Override
    protected void sendEventsToLoggers() {
        Log4jTestUtils.send10Events(logger);
    }
}
