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

package org.lttng.ust.agent.integration.jul;

import static org.junit.Assume.assumeTrue;

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
import org.lttng.ust.agent.integration.EnabledEventsITBase;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngUtils;

/**
 * Enabled events test for the LTTng-UST JUL log handler.
 */
public class JulEnabledEventsIT extends EnabledEventsITBase {

    private static final Domain DOMAIN = Domain.JUL;

    private Logger loggerA;
    private Logger loggerB;
    private Logger loggerC;
    private Logger loggerD;

    /**
     * Class setup
     */
    @BeforeClass
    public static void julClassSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        assumeTrue(LttngUtils.checkForJulLibrary());
        assumeTrue(LttngUtils.checkForLttngTools(Domain.JUL));

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
        loggerA = Logger.getLogger(EVENT_NAME_A);
        loggerB = Logger.getLogger(EVENT_NAME_B);
        loggerC = Logger.getLogger(EVENT_NAME_C);
        loggerD = Logger.getLogger(EVENT_NAME_D);

        loggerA.setLevel(Level.ALL);
        loggerB.setLevel(Level.ALL);
        loggerC.setLevel(Level.ALL);
        loggerD.setLevel(Level.ALL);

        handlerA = new LttngLogHandler();
        handlerB = new LttngLogHandler();
        handlerC = new LttngLogHandler();

        loggerA.addHandler((Handler) handlerA);
        loggerB.addHandler((Handler) handlerB);
        loggerC.addHandler((Handler) handlerC);
    }

    /**
     * Test teardown
     */
    @After
    public void julTeardown() {
        loggerA.removeHandler((Handler) handlerA);
        loggerB.removeHandler((Handler) handlerB);
        loggerC.removeHandler((Handler) handlerC);

        loggerA = null;
        loggerB = null;
        loggerC = null;
        loggerD = null;
    }

    @Override
    protected Domain getDomain() {
        return DOMAIN;
    }

    @Override
    protected void sendEventsToLoggers() {
        JulTestUtils.send10EventsTo(loggerA);
        JulTestUtils.send10EventsTo(loggerB);
        JulTestUtils.send10EventsTo(loggerC);
        JulTestUtils.send10EventsTo(loggerD);
    }
}
