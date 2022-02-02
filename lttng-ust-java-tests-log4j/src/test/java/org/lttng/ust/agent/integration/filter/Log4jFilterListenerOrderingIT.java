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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.log4j.LttngLogAppender;
import org.lttng.ust.agent.utils.Log4jTestUtils;

/**
 * Implementation of {@link FilterListenerOrderingITBase} for the log4j API.
 */
@Tag("agent:log4j")
@Tag("domain:log4j")
public class Log4jFilterListenerOrderingIT extends FilterListenerOrderingITBase {

    private Logger logger;
    private Appender appender;

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
