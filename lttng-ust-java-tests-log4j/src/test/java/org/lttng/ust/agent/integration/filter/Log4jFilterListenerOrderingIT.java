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

package org.lttng.ust.agent.integration.filter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.tools.LttngToolsHelper;
import org.lttng.ust.agent.log4j.LttngLogAppender;
import org.lttng.ust.agent.utils.Log4jTestUtils;
import org.lttng.ust.agent.utils.LttngUtils;

/**
 * Implementation of {@link FilterListenerOrderingITBase} for the log4j API.
 */
public class Log4jFilterListenerOrderingIT extends FilterListenerOrderingITBase {

    private Logger logger;
    private Appender appender;

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

    @Override
    protected Domain getDomain() {
        return Domain.LOG4J;
    }

    @Override
    protected void registerAgent() {
        logger = Logger.getLogger(EVENT_NAME_A);
        logger.setLevel(Level.ALL);

        try {
            appender = new LttngLogAppender();
        } catch (SecurityException | IOException e) {
            fail(e.getMessage());
        }
        logger.addAppender(appender);
    }

    @Override
    protected void deregisterAgent() {
        logger.removeAppender(appender);
        logger = null;

        appender.close();
        appender = null;
    }
}
