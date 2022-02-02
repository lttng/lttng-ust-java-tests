/*
 * Copyright (C) 2022, EfficiOS Inc.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.utils.Log4j2TestContext;
import org.lttng.ust.agent.utils.Log4j2TestUtils;
import org.lttng.ust.agent.utils.TestPrintExtension;

/**
 * Test suite for the list events command for the log4j domain
 */
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(TestPrintExtension.class)
public abstract class Log4j2ListEventsITBase {

    protected static final String LOGGER_NAME_1 = "org.lttng.somecomponent";
    protected static final String LOGGER_NAME_2 = "org.lttng.mycomponent";
    protected static final String LOGGER_NAME_3 = "org.lttng.myothercomponent-àéç";

    @SuppressWarnings("unused")
    private Logger logger1;
    @SuppressWarnings("unused")
    private Logger logger2;
    @SuppressWarnings("unused")
    private Logger logger3;

    private ILttngSession session;
    private Log4j2TestContext testContext;

    protected abstract Domain getDomain();

    /**
     * Class setup
     */
    @BeforeAll
    public void log4j2ClassSetup() {
        Log4j2TestUtils.testClassSetup(getDomain());
    }

    /**
     * Class cleanup
     */
    @AfterAll
    public static void log4j2ClassCleanup() {
        Log4j2TestUtils.testClassCleanup();
    }

    /**
     * Create a new session before each test.
     *
     * @param testInfo
     *            current test information
     */
    @SuppressWarnings("resource")
    @BeforeEach
    public void testSetup(TestInfo testInfo) {
        session = ILttngSession.createSession("Log4j2ListEventsIT", getDomain());

        testContext = new Log4j2TestContext(
                "log4j2." + getDomain() + testInfo.getDisplayName().replaceAll("[()]", "") + ".xml");

        testContext.beforeTest();

        logger1 = testContext.getLoggerContext().getLogger(LOGGER_NAME_1);
        logger2 = testContext.getLoggerContext().getLogger(LOGGER_NAME_2);
        logger3 = testContext.getLoggerContext().getLogger(LOGGER_NAME_3);
    }

    /**
     * Close the current session after each test.
     */
    @AfterEach
    public void testTeardown() {
        session.close();
        testContext.afterTest();
    }

    /**
     * Test with many loggers existing, but none of them having a LTTng handler
     * attached.
     */
    @Test
    public void testManyLoggersNoneAttached() {

        /* Don't attach anything */
        List<String> actualEvents = session.listEvents();
        assertTrue(actualEvents.isEmpty());
    }

    /**
     * Test with many loggers existing, but only a subset of them has a LTTng
     * handler attached.
     */
    @Test
    public void testManyLoggersSomeAttached() {

        List<String> expectedEvents = Arrays.asList(LOGGER_NAME_1);
        List<String> actualEvents = session.listEvents();

        Collections.sort(expectedEvents);
        Collections.sort(actualEvents);

        assertEquals(expectedEvents, actualEvents);
    }

    /**
     * Test with many loggers existing, and all of them having a LTTng handler
     * attached.
     */
    @Test
    public void testManyLoggersAllAttached() {

        List<String> expectedEvents = Arrays.asList(LOGGER_NAME_1, LOGGER_NAME_2, LOGGER_NAME_3);
        List<String> actualEvents = session.listEvents();

        Collections.sort(expectedEvents);
        Collections.sort(actualEvents);

        assertEquals(expectedEvents, actualEvents);
    }
}
