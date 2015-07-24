package org.lttng.ust.agent.integration.jul;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.lttng.ust.agent.integration.common.EnabledEventsTest;
import org.lttng.ust.agent.jul.LttngLogHandler;
import org.lttng.ust.agent.utils.LttngSession;
import org.lttng.ust.agent.utils.LttngSession.Domain;
import org.lttng.ust.agent.utils.TestUtils;

public class JulEnabledEventsTest extends EnabledEventsTest {

    private static final Domain DOMAIN = Domain.JUL;

    private Logger loggerA;
    private Logger loggerB;
    private Logger loggerC;
    private Logger loggerD;

    @BeforeClass
    public static void julClassSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        assumeTrue(TestUtils.checkForJulLibrary());
        assumeTrue(TestUtils.checkForLttngTools(Domain.JUL));
    }

    @AfterClass
    public static void julClassCleanup() {
        LttngSession.deleteAllTracee();
    }

    @Before
    public void julSetup() throws SecurityException, IOException {
        // TODO Wipe all existing LTTng sessions?

        loggerA = Logger.getLogger(EVENT_NAME_A);
        loggerB = Logger.getLogger(EVENT_NAME_B);
        loggerC = Logger.getLogger(EVENT_NAME_C);
        loggerD = Logger.getLogger(EVENT_NAME_D);

        loggerA.setLevel(Level.ALL);
        loggerB.setLevel(Level.ALL);
        loggerC.setLevel(Level.ALL);
        loggerD.setLevel(Level.ALL);

        handlerA = new LttngLogHandler();
        handlerB = new LttngLogHandler();
        handlerC = new LttngLogHandler();

        loggerA.addHandler((Handler) handlerA);
        loggerB.addHandler((Handler) handlerB);
        loggerC.addHandler((Handler) handlerC);
    }

    @After
    public void julTeardown() {
        loggerA.removeHandler((Handler) handlerA);
        loggerB.removeHandler((Handler) handlerB);
        loggerC.removeHandler((Handler) handlerC);

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
        JulTestUtils.send10EventsTo(loggerA);
        JulTestUtils.send10EventsTo(loggerB);
        JulTestUtils.send10EventsTo(loggerC);
        JulTestUtils.send10EventsTo(loggerD);
    }
}
