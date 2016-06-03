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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.JulTestUtils;

/**
 * Implementation of {@link LoggerHierachyListITBase} for JUL log handlers.
 *
 * @author Alexandre Montplaisir
 */
@RunWith(Parameterized.class)
public class JulLoggerHierarchyListIT extends LoggerHierachyListITBase {

    private Logger parentLogger;
    private Logger childLogger;

    private Handler parentHandler;
    private Handler childHandler;

    /**
     * Test constructor
     *
     * @param parentLoggerActive
     *            Parent logger has been instantiated
     * @param parentLoggerHasHandler
     *            Parent logger has a LTTng handler attached to it
     * @param childLoggerActive
     *            Child logger has been instantiated
     * @param childLoggerHasHandler
     *            Child logger has a LTTng handler attached to it
     */
    public JulLoggerHierarchyListIT(boolean parentLoggerActive,
            boolean parentLoggerHasHandler,
            boolean childLoggerActive,
            boolean childLoggerHasHandler) {
        /* Set by parameters defined in the base class */
        super(parentLoggerActive,
                parentLoggerHasHandler,
                childLoggerActive,
                childLoggerHasHandler);
    }

    // ------------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------------

    /**
     * Class setup
     */
    @BeforeClass
    public static void julClassSetup() {
        JulTestUtils.testClassSetup();
    }

    /**
     * Class cleanup
     */
    @AfterClass
    public static void julClassCleanup() {
        JulTestUtils.testClassCleanup();
    }

    /**
     * Test setup
     */
    @SuppressWarnings("static-method")
    @Before
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
    @After
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
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    @Override
    protected Domain getDomain() {
        return Domain.JUL;
    }

    @Override
    protected void activateLoggers() throws IOException {
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
