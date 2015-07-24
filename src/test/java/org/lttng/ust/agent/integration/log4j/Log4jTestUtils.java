package org.lttng.ust.agent.integration.log4j;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

final class Log4jTestUtils {

    private Log4jTestUtils() {
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
