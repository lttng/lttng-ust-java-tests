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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.JulTestUtils;
import org.lttng.ust.agent.utils.LttngUtils;

/**
 * Enabled app contexts test for the LTTng-UST JUL log handler.
 */
public class JulAppContextIT extends AppContextITBase {

    private static final Domain DOMAIN = Domain.JUL;

    private Logger logger;

    /**
     * Class setup
     */
    @BeforeClass
    public static void julClassSetup() {
        /* Make sure we can find the JNI library and lttng-tools */
        assertTrue(JulTestUtils.checkForJulLibrary());
        assertTrue(LttngUtils.checkForLttngTools(Domain.JUL));

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

        logHandler = new LttngLogHandler();
        logger.addHandler((Handler) logHandler);
    }

    /**
     * Test teardown
     */
    @After
    public void julTeardown() {
        logger.removeHandler((Handler) logHandler);
        logger = null;
    }

    @Override
    protected Domain getDomain() {
        return DOMAIN;
    }

    @Override
    protected void sendEventsToLoggers() {
        JulTestUtils.send10EventsTo(logger);
    }
}
