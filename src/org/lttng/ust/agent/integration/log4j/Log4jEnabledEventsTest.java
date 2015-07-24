package org.lttng.ust.agent.integration.log4j;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.lttng.ust.agent.integration.common.EnabledEventsTest;
import org.lttng.ust.agent.log4j.LttngLogAppender;
import org.lttng.ust.agent.utils.LttngSessionControl;
import org.lttng.ust.agent.utils.LttngSessionControl.Domain;

public class Log4jEnabledEventsTest extends EnabledEventsTest {

    private static final Domain DOMAIN = Domain.LOG4J;

    private Logger loggerA;
    private Logger loggerB;
    private Logger loggerC;
    private Logger loggerD;

    @BeforeClass
    public static void julClassSetup() {
        /* Skip tests if we can't find the JNI library or lttng-tools */
        try {
            LttngLogAppender testHandler = new LttngLogAppender();
            testHandler.close();
        } catch (SecurityException | IOException e) {
            assumeTrue(false);
        }

        boolean ret1 = LttngSessionControl.setupSession(null, DOMAIN);
        boolean ret2 = LttngSessionControl.stopSession(null);
        /* "lttng view" also tests that Babeltrace is installed and working */
        List<String> contents = LttngSessionControl.viewSession(null);
        boolean ret3 = LttngSessionControl.destroySession(null);
        assumeTrue(ret1 && ret2 && ret3);
        assumeTrue(contents.isEmpty());
    }

    @AfterClass
    public static void julClassCleanup() {
        LttngSessionControl.deleteAllTracee();
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

        handlerA = new LttngLogAppender();
        handlerB = new LttngLogAppender();
        handlerC = new LttngLogAppender();

        loggerA.addAppender((Appender) handlerA);
        loggerB.addAppender((Appender) handlerB);
        loggerC.addAppender((Appender) handlerC);
    }

    @After
    public void julTeardown() {
        loggerA.removeAppender((Appender) handlerA);
        loggerB.removeAppender((Appender) handlerB);
        loggerC.removeAppender((Appender) handlerC);

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
        send10Events(loggerA);
        send10Events(loggerB);
        send10Events(loggerC);
        send10Events(loggerD);
    }

    static void send10Events(Logger logger) {
        // Levels/priorities are DEBUG, ERROR, FATAL, INFO, TRACE, WARN
        logger.debug("Debug message. Lost among so many.");
        logger.debug("Debug message with a throwable", new IOException());
        logger.error("Error messsage. This might be bad.");
        logger.error("Error message with a throwable", new IOException());
        logger.fatal("A fatal message. You are already dead.");
        logger.info("A info message. Lol, who cares.");
        logger.trace("A trace message. No, no *that* trace");
        logger.warn("A warn message. Yellow underline.");
        logger.log(Level.DEBUG, "A debug message using .log()");
        logger.log(Level.ERROR, "A error message using .log()");
    }
}
