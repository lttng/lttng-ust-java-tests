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
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.JulTestUtils;

/**
 * Implementation of {@link LoggerHierachyListITBase} for JUL log handlers.
 *
 * @author Alexandre Montplaisir
 */
@Tag("agent:jul")
@Tag("domain:jul")
public class JulLoggerHierarchyListIT extends LoggerHierachyListITBase {

    private Logger parentLogger;
    private Logger childLogger;

    private Handler parentHandler;
    private Handler childHandler;

    // ------------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------------

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
     */
    @SuppressWarnings("static-method")
    @BeforeEach
    public void setup() {
        /*
         * Kind of hackish, but it's the only way to ensure that loggers are
         * really removed in-between tests, since LogManager does not provide a
         * way to forcibly remove a logger, and it doesn't seem like it will any
         * time soon, see http://bugs.java.com/view_bug.do?bug_id=4811930
         */
        LogManager.getLogManager().reset();
        System.gc();
    }

    /**
     * Test cleanup
     */
    @AfterEach
    public void cleanup() {
        if (parentLogger != null) {
            if (parentHandler != null) {
                parentLogger.removeHandler(parentHandler);
                parentHandler.close();
                parentHandler = null;
            }
            parentLogger = null;
        }

        if (childLogger != null) {
            if (childHandler != null) {
                childLogger.removeHandler(childHandler);
                childHandler.close();
                childHandler = null;
            }
            childLogger = null;
        }

        LogManager.getLogManager().reset();
        System.gc();
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    @Override
    protected Domain getDomain() {
        return Domain.JUL;
    }

    @Override
    protected void activateLoggers(boolean parentLoggerActive,
            boolean parentLoggerHasHandler,
            boolean childLoggerActive,
            boolean childLoggerHasHandler) throws IOException {
        if (parentLoggerActive) {
            parentLogger = Logger.getLogger(PARENT_LOGGER);
            if (parentLoggerHasHandler) {
                parentHandler = new LttngLogHandler();
                parentLogger.addHandler(parentHandler);
            }
        }

        if (childLoggerActive) {
            childLogger = Logger.getLogger(CHILD_LOGGER);
            if (childLoggerHasHandler) {
                childHandler = new LttngLogHandler();
                childLogger.addHandler(childHandler);
            }
        }
    }

}
