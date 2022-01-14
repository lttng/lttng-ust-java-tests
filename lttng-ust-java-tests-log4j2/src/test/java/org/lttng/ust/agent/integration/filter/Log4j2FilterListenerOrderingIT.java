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

package org.lttng.ust.agent.integration.filter;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.utils.Log4j2TestContext;
import org.lttng.ust.agent.utils.Log4j2TestUtils;

/**
 * Implementation of {@link FilterListenerOrderingITBase} for the log4j API.
 */
public class Log4j2FilterListenerOrderingIT extends FilterListenerOrderingITBase {

    private Log4j2TestContext testContext;

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

    @Override
    protected Domain getDomain() {
        return Domain.LOG4J;
    }

    @Override
    protected void registerAgent() {
        testContext = new Log4j2TestContext("log4j2.Log4j2FilterListenerOrderingIT.xml");
        testContext.beforeTest();
    }

    @Override
    protected void deregisterAgent() {
        testContext.afterTest();
    }
}
