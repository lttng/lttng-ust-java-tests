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

package org.lttng.ust.agent.integration.context;

import java.io.IOException;

import org.apache.logging.log4j.core.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.ILttngHandler;
import org.lttng.ust.agent.utils.Log4j2TestContext;
import org.lttng.ust.agent.utils.Log4j2TestUtils;

/**
 * Enabled app contexts test for the LTTng-UST Log4j 2.x log handler.
 */
public class Log4j2AppContextIT extends AppContextITBase {

    private static final Domain DOMAIN = Domain.LOG4J;

    private Log4j2TestContext testContext;
    private Logger logger;

    /**
     * Class setup
     */
    @BeforeClass
    public static void log4j2ClassSetup() {
        Log4j2TestUtils.testClassSetup();
    }

    /**
     * Class cleanup
     */
    @AfterClass
    public static void log4j2ClassCleanup() {
        Log4j2TestUtils.testClassCleanup();
    }

    /**
     * Test setup
     *
     * @throws SecurityException
     * @throws IOException
     */
    @Before
    public void log4j2Setup() throws SecurityException, IOException {
        testContext = new Log4j2TestContext("log4j2.Log4j2AppContextIT.xml");

        testContext.beforeTest();

        logger = testContext.getLoggerContext().getLogger(EVENT_NAME);

        logHandler = (ILttngHandler) logger.getAppenders().get("Lttng");
    }

    /**
     * Test teardown
     */
    @After
    public void log4j2Teardown() {
        testContext.afterTest();
        logger = null;
    }

    @Override
    protected Domain getDomain() {
        return DOMAIN;
    }

    @Override
    protected boolean closeHandlers()
    {
        return false;
    }

    @Override
    protected void sendEventsToLoggers() {
        Log4j2TestUtils.send10Events(logger);
    }
}
