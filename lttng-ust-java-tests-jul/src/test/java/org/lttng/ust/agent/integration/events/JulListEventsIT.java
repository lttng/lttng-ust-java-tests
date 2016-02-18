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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.ust.agent.integration.events.ListEventsITBase;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.JulTestUtils;
import org.lttng.ust.agent.utils.LttngUtils;

/**
 * Test suite for the list events command for the JUL domain
 */
public class JulListEventsIT extends ListEventsITBase {

    private Logger[] loggers;
    private Handler[] handlers;

    /**
     * Test class setup
     */
    @BeforeClass
    public static void testClassSetup() {
        /* Make sure we can find the JNI library and lttng-tools */
        assertTrue(JulTestUtils.checkForJulLibrary());
        assertTrue(LttngUtils.checkForLttngTools(Domain.JUL));

        LttngToolsHelper.destroyAllSessions();
    }

    /**
     * Test setup
     *
     * @throws SecurityException
     * @throws IOException
     */
    @Before
    public void julSetup() throws SecurityException, IOException {
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
    @After
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
