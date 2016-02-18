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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.ust.agent.log4j.LttngLogAppender;
import org.lttng.ust.agent.utils.Log4jTestUtils;
import org.lttng.ust.agent.utils.LttngUtils;

/**
 * Enabled app contexts test for the LTTng-UST JUL log handler.
 */
public class Log4jAppContextIT extends AppContextITBase {

    private static final Domain DOMAIN = Domain.LOG4J;

    private Logger logger;

    /**
     * Class setup
     */
    @BeforeClass
    public static void julClassSetup() {
        /* Make sure we can find the JNI library and lttng-tools */
        assertTrue(Log4jTestUtils.checkForLog4jLibrary());
        assertTrue(LttngUtils.checkForLttngTools(Domain.LOG4J));

        LttngToolsHelper.destroyAllSessions();
    }

    /**
     * Class cleanup
     */
    @AfterClass
    public static void julClassCleanup() {
        LttngToolsHelper.deleteAllTraces();
    }

    /**
     * Test setup
     *
     * @throws SecurityException
     * @throws IOException
     */
    @Before
    public void julSetup() throws SecurityException, IOException {
        logger = Logger.getLogger(EVENT_NAME);
        logger.setLevel(Level.ALL);

        logHandler = new LttngLogAppender();
        logger.addAppender((Appender) logHandler);
    }

    /**
     * Test teardown
     */
    @After
    public void julTeardown() {
        logger.removeAppender((Appender) logHandler);
        logger = null;
    }

    @Override
    protected Domain getDomain() {
        return DOMAIN;
    }

    @Override
    protected void sendEventsToLoggers() {
        Log4jTestUtils.send10Events(logger);
    }
}
