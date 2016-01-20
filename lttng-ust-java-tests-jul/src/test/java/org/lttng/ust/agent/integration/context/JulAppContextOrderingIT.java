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

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.BeforeClass;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.JulTestUtils;
import org.lttng.ust.agent.utils.LttngUtils;

/**
 * Implementation of {@link AppContextOrderingITBase} for the JUL API.
 */
public class JulAppContextOrderingIT extends AppContextOrderingITBase {

    private Logger logger;

    /**
     * Class setup
     */
    @BeforeClass
    public static void julClassSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        assumeTrue(JulTestUtils.checkForJulLibrary());
        assumeTrue(LttngUtils.checkForLttngTools(Domain.JUL));

        LttngToolsHelper.destroyAllSessions();
    }

    /**
     * Test teardown
     */
    @After
    public void julTeardown() {
        logger.removeHandler((Handler) logHandler);
        logger = null;

        logHandler.close();
        logHandler = null;
    }

    @Override
    protected Domain getDomain() {
        return Domain.JUL;
    }

    @Override
    protected void registerAgent() {
        logger = Logger.getLogger(EVENT_NAME);
        logger.setLevel(Level.ALL);

        try {
            logHandler = new LttngLogHandler();
        } catch (SecurityException | IOException e) {
            fail();
        }
        logger.addHandler((Handler) logHandler);
    }

    @Override
    protected void sendEventsToLoggers() {
        JulTestUtils.send10EventsTo(logger);
    }
}
