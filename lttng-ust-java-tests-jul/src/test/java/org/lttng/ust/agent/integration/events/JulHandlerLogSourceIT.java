/*
 * Copyright (C) 2017, École Polytechnique de Montréal, Geneviève Bastien <gbastien@versatic.net>
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.lttng.tools.ILttngSession;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.JulTestUtils;

/**
 * Test the {@link LttngLogHandler} log output with source log enabled or not
 * 
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class JulHandlerLogSourceIT {

    private static final Domain DOMAIN = Domain.JUL;

    private static final String LOGGER_NAME = "org.lttng.test";

    private ILttngSession session;

    private Logger logger;
    
    private final Boolean logSource;
    private final String expectedString;
    
    // ------------------------------------------------------------------------
    // Test parameter definition
    // ------------------------------------------------------------------------

    /**
     * Get the test parameters, with / without source logging as well as the
     * string that should be present in the events as a result of the event
     * logging.
     *
     * @return The test parameters
     */
    @Parameters(name = "{index}: logSource={0}")
    public static Iterable<Object[]> testCases() {
        return Arrays.asList(new Object[][] {
                { Boolean.TRUE, "class_name = \"org.lttng.ust.agent.utils.JulTestUtils\", method_name = \"send10EventsTo\"" },
                { Boolean.FALSE, "class_name = \"\", method_name = \"\"" },
        });
    }

    /**
     * Test constructor
     *
     * @param logSource
     *            Should source information be logged with the event
     * @param expectedString
     *            A string that should be present in the event
     */
    public JulHandlerLogSourceIT(boolean logSource,
            String expectedString) {
        this.logSource = logSource;
        this.expectedString = expectedString;
    }

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
     * 
     * @throws IOException
     *             Exceptions thrown by handler
     * @throws SecurityException
     *             Exceptions thrown by handler
     */
    @Before
    public void setup() throws SecurityException, IOException {
        /* Clear the JUL logger configuration */
        LogManager.getLogManager().reset();
        System.gc();

        /* Initialize the logger */
        logger = Logger.getLogger(LOGGER_NAME);
        logger.setLevel(Level.ALL);
        
        /* Initialize the handler */
        LttngLogHandler handler = new LttngLogHandler();
        handler.setLogSource(logSource);
        logger.addHandler(handler);

        /* Create the lttng session */
        session = ILttngSession.createSession(null, DOMAIN);
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        session.close();
        Handler handler = logger.getHandlers()[0];
        logger.removeHandler(handler);
        handler.close();

        logger = null;
    }

    /**
     * Test tracing some events and see if the output contains the expected string
     */
    @Test
    public void testHandlerOutput() {
        assertTrue(session.enableAllEvents());
        assertTrue(session.start());

        JulTestUtils.send10EventsTo(logger);

        assertTrue(session.stop());

        List<String> output = session.view();
        assertNotNull(output);
        assertFalse(output.isEmpty());

        assertEquals(10, output.size());
        for (String str : output) {
            assertTrue("Validating event string : " + str, str.contains(expectedString));
        }
    }

}

