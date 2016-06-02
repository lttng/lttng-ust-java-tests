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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.LTTngAgent;
import org.lttng.ust.agent.utils.JulTestUtils;

/**
 * Implementation of {@link LoggerHierachyListITBase} for the JUL part of the
 * legacy LTTngAgent API.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("deprecation")
@RunWith(Parameterized.class)
public class JulLegacyApiLoggerHierarchyListIT extends LoggerHierachyListITBase {

    private LTTngAgent agent;
    private Logger parentLogger;
    private Logger childLogger;

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
    public JulLegacyApiLoggerHierarchyListIT(boolean parentLoggerActive,
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
        LogManager.getLogManager().reset();
        System.gc();
    }

    /**
     * Test cleanup
     */
    @After
    public void cleanup() {
        agent.dispose();

        if (parentLogger != null) {
            parentLogger = null;
        }

        if (childLogger != null) {
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
    protected void activateLoggers() throws IOException {
        agent = LTTngAgent.getLTTngAgent();

        /*
         * There is no notion of "hasHandler" for the legacy API: there is only
         * one log handler attached to the root logger, so there are no handlers
         * to specific events/loggers.
         */

        if (parentLoggerActive) {
            parentLogger = Logger.getLogger(PARENT_LOGGER);
        }

        if (childLoggerActive) {
            childLogger = Logger.getLogger(CHILD_LOGGER);
        }
    }

    // ------------------------------------------------------------------------
    // Overridden tests
    // ------------------------------------------------------------------------

    /**
     * Due to how the legacy agent works, there is no notion of "hasHandler". If
     * the logger exists, it will be visible in "lttng list" because the single
     * log handler is attached to the root logger.
     */
    @Override
    @Test
    public void testList() throws IOException {
        activateLoggers();

        List<String> enabledEvents = getSession().listEvents();
        List<String> expectedEvents = new ArrayList<>();

        if (parentLoggerActive) {
            expectedEvents.add(PARENT_LOGGER);
        }
        if (childLoggerActive) {
            expectedEvents.add(CHILD_LOGGER);
        }

        Collections.sort(enabledEvents);
        Collections.sort(expectedEvents);
        assertEquals(expectedEvents, enabledEvents);
    }

}
