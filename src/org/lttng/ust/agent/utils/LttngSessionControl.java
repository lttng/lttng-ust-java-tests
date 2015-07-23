package org.lttng.ust.agent.utils;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class LttngSessionControl {

    private LttngSessionControl() {}

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

    // ------------------------------------------------------------------------
    // Public utility methods
    // ------------------------------------------------------------------------

    /**
     * Setup a LTTng session by enabling certain events (or none).
     *
     * @param sessionName
     *            The name of the session to create. May be null to use the
     *            default one from lttng-tools.
     * @param domain
     *            The tracing domain
     * @param enabledEvents
     *            The list of events to enable. May be null or empty, to not
     *            enable any events.
     * @return If the command executed successfully (return code = 0).
     */
    public static boolean setupSession(String sessionName, Domain domain, String... enabledEvents) {
        String[] createCommand = (sessionName == null ?
                new String[] { "lttng", "create" } :
                new String[] { "lttng", "create", sessionName}
                );

        String eventsToEnable = (enabledEvents == null || enabledEvents.length == 0 ?
                /*
                 * We have to enable a channel for 'lttng start' to work.
                 * However, we cannot enable a channel directly, see
                 * https://bugs.lttng.org/issues/894 . Instead we will enable an
                 * event we know does not exist
                 */
                "non-event" :
                Arrays.stream(enabledEvents).collect(Collectors.joining(","))
                );

        return executeCommands(new String[][] {
            createCommand,
            { "lttng", "enable-event", domain.flag(), eventsToEnable},
            { "lttng", "start" }
        });
    }

    /**
     * Setup a LTTng session with all events enabled (lttng enable-event -a).
     *
     * @param sessionName
     *            The name of the session to create. May be null to use the
     *            default one from lttng-tools.
     * @param domain
     *            The tracing domain
     * @return If the command executed successfully (return code = 0).
     */
    public static boolean setupSessionAllEvents(String sessionName, Domain domain) {
        String[] createCommand = (sessionName == null ?
                new String[] { "lttng", "create" } :
                new String[] { "lttng", "create", sessionName}
                );

        return executeCommands(new String[][] {
                createCommand,
                { "lttng", "enable-event", domain.flag(), "-a" },
                { "lttng", "start" }
        });
    }

    /**
     * Stop the current tracing session
     *
     * @return If the command executed successfully (return code = 0).
     */
    public static boolean stopSession() {
        return executeCommand(new String[] { "lttng", "stop" });
    }

    public static List<String> viewSession() {
        return getOutputFromCommand(new String[] { "lttng", "view" });
    }

    /**
     * Destroy the current tracing session
     *
     * @return If the command executed successfully (return code = 0).
     */
    public static boolean destroySession() {
        return executeCommand(new String[] { "lttng", "destroy" });
    }

    // ------------------------------------------------------------------------
    // Private helper methods
    // ------------------------------------------------------------------------

    private static boolean executeCommands(String [][] commands) {
        for (String[] command : commands) {
            if (executeCommand(command) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Just to test the environment / stdout are working correctly
     */
    public static void main(String[] args) {
        executeCommand(new String[] {"ls", "-l"});
    }

    private static boolean executeCommand(String[] command) {
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

    private static List<String> getOutputFromCommand(String[] command) {
        try {
            Path tempFile = Files.createTempFile("test-output", null);

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(Redirect.to(tempFile.toFile()));

            Process p = builder.start();
            p.waitFor();

            List<String> lines = Files.readAllLines(tempFile);
            Files.delete(tempFile);

            /* Also print the output to the console */
            lines.stream().forEach(s -> System.out.println(s));

            return lines;

        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
