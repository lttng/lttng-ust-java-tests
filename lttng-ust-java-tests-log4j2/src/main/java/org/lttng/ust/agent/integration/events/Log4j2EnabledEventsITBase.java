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

import java.io.IOException;

import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.utils.Log4j2TestContext;
import org.lttng.ust.agent.utils.Log4j2TestUtils;

/**
 * Enabled events test for the LTTng-UST Log4j 2.x log handler.
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class Log4j2EnabledEventsITBase extends EnabledEventsITBase {

    private static final String APPENDER_NAME_A = "LttngA";
    private static final String APPENDER_NAME_B = "LttngB";
    private static final String APPENDER_NAME_C = "LttngC";

    private Log4j2TestContext testContext;

    private Logger loggerA;
    private Logger loggerB;
    private Logger loggerC;
    private Logger loggerD;

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
     * Test setup
     *
     * @throws SecurityException
     * @throws IOException
     */
    @SuppressWarnings("resource")
    @BeforeEach
    public void log4j2Setup() throws SecurityException, IOException {

        testContext = new Log4j2TestContext("log4j2." + this.getClass().getSimpleName() + ".xml");

        testContext.beforeTest();

        loggerA = testContext.getLoggerContext().getLogger(EVENT_NAME_A);
        loggerB = testContext.getLoggerContext().getLogger(EVENT_NAME_B);
        loggerC = testContext.getLoggerContext().getLogger(EVENT_NAME_C);
        loggerD = testContext.getLoggerContext().getLogger(EVENT_NAME_D);

        handlerA = (ILttngHandler) loggerA.getAppenders().get(APPENDER_NAME_A);
        handlerB = (ILttngHandler) loggerB.getAppenders().get(APPENDER_NAME_B);
        handlerC = (ILttngHandler) loggerC.getAppenders().get(APPENDER_NAME_C);
    }

    /**
     * Test teardown
     */
    @AfterEach
    public void log4j2Teardown() {
        loggerA = null;
        loggerB = null;
        loggerC = null;
        loggerD = null;

        testContext.afterTest();
    }

    @Override
    protected boolean closeHandlers()
    {
        return false;
    }

    @Override
    protected void sendEventsToLoggers() {
        Log4j2TestUtils.send10Events(loggerA);
        Log4j2TestUtils.send10Events(loggerB);
        Log4j2TestUtils.send10Events(loggerC);
        Log4j2TestUtils.send10Events(loggerD);
    }

    @Override
    protected void sendLocalizedEvent(String rawString, Object[] params) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Test
    public void testLocalizedMessage() {
        /* Does not apply to log4j 1.2.x */
    }
}
