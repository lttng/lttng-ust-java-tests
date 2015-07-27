/*
 * Copyright (C) 2015, EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
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

package org.lttng.ust.agent.integration.log4j;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.lttng.ust.agent.integration.MultiSessionTestBase;
import org.lttng.ust.agent.log4j.LttngLogAppender;
import org.lttng.ust.agent.utils.LttngSession;
import org.lttng.ust.agent.utils.LttngSession.Domain;
import org.lttng.ust.agent.utils.MiscTestUtils;

public class Log4jMultiSessionTest extends MultiSessionTestBase {

    private static final Domain DOMAIN = Domain.LOG4J;

    private Logger loggerA;
    private Logger loggerB;
    private Logger loggerC;
    private Logger loggerD;

    @BeforeClass
    public static void log4jClassSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        assumeTrue(MiscTestUtils.checkForLog4jLibrary());
        assumeTrue(MiscTestUtils.checkForLttngTools(Domain.LOG4J));

        LttngSession.destroyAllSessions();
    }

    @AfterClass
    public static void log4jClassCleanup() {
        LttngSession.deleteAllTracee();
    }

    @Before
    public void log4jSetup() throws SecurityException, IOException {
        // TODO Wipe all existing LTTng sessions?

        loggerA = Logger.getLogger(EVENT_NAME_A);
        loggerB = Logger.getLogger(EVENT_NAME_B);
        loggerC = Logger.getLogger(EVENT_NAME_C);
        loggerD = Logger.getLogger(EVENT_NAME_D);

        loggerA.setLevel(Level.ALL);
        loggerB.setLevel(Level.ALL);
        loggerC.setLevel(Level.ALL);
        loggerD.setLevel(Level.ALL);

        handlerA = new LttngLogAppender();
        handlerB = new LttngLogAppender();
        handlerC = new LttngLogAppender();
        handlerD = new LttngLogAppender();

        loggerA.addAppender((Appender) handlerA);
        loggerB.addAppender((Appender) handlerB);
        loggerC.addAppender((Appender) handlerC);
        loggerD.addAppender((Appender) handlerD);
    }

    @After
    public void log4jTeardown() {
        loggerA.removeAppender((Appender) handlerA);
        loggerB.removeAppender((Appender) handlerB);
        loggerC.removeAppender((Appender) handlerC);
        loggerD.removeAppender((Appender) handlerD);

        loggerA = null;
        loggerB = null;
        loggerC = null;
        loggerD = null;
    }

    @Override
    protected Domain getDomain() {
        return DOMAIN;
    }

    @Override
    protected void sendEventsToLoggers() {
        Log4jTestUtils.send10Events(loggerA);
        Log4jTestUtils.send10Events(loggerB);
        Log4jTestUtils.send10Events(loggerC);
        Log4jTestUtils.send10Events(loggerD);
    }
}
