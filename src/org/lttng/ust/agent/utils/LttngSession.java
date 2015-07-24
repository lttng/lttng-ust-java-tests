package org.lttng.ust.agent.utils;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LttngSession implements AutoCloseable {

    public enum Domain {
        JUL("-j"),
        LOG4J("-l");

        private final String flag;

        private Domain(String flag) {
            this.flag = flag;
        }

        public String flag() {
            return flag;
        }
    }

    private final String sessionName;
    private final Domain domain;

    private volatile boolean channelCreated = false;

    public LttngSession(String sessionName, Domain domain) {
        if (sessionName != null) {
            this.sessionName = sessionName;
        } else {
            this.sessionName = UUID.randomUUID().toString();
        }
        this.domain = domain;

        /* Create the session in LTTng */
        executeCommand(Arrays.asList("lttng", "create", this.sessionName));
    }

    @Override
    public void close() {
        /* Destroy the session */
        executeCommand(Arrays.asList("lttng", "destroy", sessionName));
     // FIXME also delete the trace we generated ?
    }

    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------

    /**
     * Enable all events in the given session (enable-event -a)
     *
     * @return If the command executed successfully (return code = 0).
     */
    public boolean enableAllEvents() {
        channelCreated = true;
        return executeCommand(Arrays.asList(
                "lttng", "enable-event", domain.flag(), "-a", "-s", sessionName));
    }

    /**
     * Enable individual event(s).
     *
     * @param enabledEvents
     *            The list of events to enable. Should not be null or empty
     * @return If the command executed successfully (return code = 0).
     */
    public boolean enableEvents(String... enabledEvents) {
        if (enabledEvents == null || enabledEvents.length == 0) {
            throw new IllegalArgumentException();
        }
        channelCreated = true;
        return executeCommand(Arrays.asList(
                "lttng", "enable-event", domain.flag(),
                Arrays.stream(enabledEvents).collect(Collectors.joining(",")),
                "-s", sessionName));
    }

    /**
     * Send a disable-event command. Used to disable events that were previously
     * enabled.
     *
     * @param disabledEvents
     *            The list of disabled events. Should not be null or empty
     * @return If the command executed successfully (return code = 0).
     */
    public boolean disableEvents(String... disabledEvents) {
        if (disabledEvents == null || disabledEvents.length == 0) {
            throw new IllegalArgumentException();
        }
        return executeCommand(Arrays.asList(
                "lttng", "disable-event", domain.flag(),
                Arrays.stream(disabledEvents).collect(Collectors.joining(",")),
                "-s", sessionName));
    }

    public boolean start() {
        /*
         * We have to enable a channel for 'lttng start' to work. However, we
         * cannot enable a channel directly, see
         * https://bugs.lttng.org/issues/894 . Instead we will enable an event
         * we know does not exist
         */
        if (!channelCreated) {
            enableEvents("non-event");
        }
        return executeCommand(Arrays.asList("lttng", "start", sessionName));
    }

    /**
     * Stop the tracing session
     *
     * @return If the command executed successfully (return code = 0).
     */
    public boolean stop() {
        return executeCommand(Arrays.asList("lttng", "stop", sessionName));
    }

    /**
     * Issue a "lttng view" command on the session, and returns its output. This
     * effectively returns the current content of the trace in text form.
     *
     * @return The output of Babeltrace on the session's current trace
     */
    public List<String> view() {
        return TestUtils.getOutputFromCommand(Arrays.asList("lttng", "view", sessionName));
    }

    /**
     * Utility method to destroy all existing sessions. Useful when first
     * setting up a test to make sure no existing session interferes.
     */
    public static void destroyAllSessions() {
        executeCommand(Arrays.asList("lttng", "destroy", "-a"));
    }

    /**
     * Outside of the scope of lttng-tools, but this utility method can be used
     * to delete all traces currently under ~/lttng-traces/. This can be used by
     * tests to cleanup a trace they have created.
     *
     * @return True if the command completes successfully, false if there was an
     *         error.
     */
    public static boolean deleteAllTracee() {
        String tracesDir = new String(System.getProperty("user.home") + "/lttng-traces/");
        return deleteDirectory(Paths.get(tracesDir));
    }

    // ------------------------------------------------------------------------
    // Private helper methods
    // ------------------------------------------------------------------------

    private static boolean deleteDirectory(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            /* At least we tried... */
            return false;
        }
        return true;
    }

    /**
     * Just to test the environment / stdout are working correctly
     */
    public static void main(String[] args) {
        List<String> command = Arrays.asList("ls", "-l");
        executeCommand(command);
    }

    private static boolean executeCommand(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(Redirect.INHERIT);

            Process p = builder.start();
            int ret = p.waitFor();
            return (ret == 0);

        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
