package org.lttng.ust.agent.utils;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
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
     * Send a separate enable-event command.
     *
     * @param sessionName
     *            Name of the session in which to enable events. Use null for
     *            current session.
     * @param domain
     *            The tracing domain
     * @param enabledEvents
     *            The list of events to enable. Should not be null or empty
     * @return If the command executed successfully (return code = 0).
     */
    public static boolean enableEvents(String sessionName, Domain domain, String... enabledEvents) {
        if (enabledEvents == null || enabledEvents.length == 0) {
            throw new IllegalArgumentException();
        }
        List<String> command = new ArrayList<String>();
        command.add("lttng");
        command.add("enable-event");
        command.add(domain.flag());
        command.add(Arrays.stream(enabledEvents).collect(Collectors.joining(",")));
        if (sessionName != null) {
            command.add("-s");
            command.add(sessionName);
        }
        return executeCommand(command.toArray(new String[0]));
    }

    /**
     * Send a disable-event command. Used to disable events that were previously
     * enabled.
     *
     * @param sessionName
     *            Name of the session in which to disable events. Use null for
     *            current session.
     * @param domain
     *            The tracing domain
     * @param disabledEvents
     *            The list of disabled events. Should not be null or empty
     * @return If the command executed successfully (return code = 0).
     */
    public static boolean disableEvents(String sessionName, Domain domain, String... disabledEvents) {
        if (disabledEvents == null || disabledEvents.length == 0) {
            throw new IllegalArgumentException();
        }
        List<String> command = new ArrayList<String>();
        command.add("lttng");
        command.add("disable-event");
        command.add(domain.flag());
        command.add(Arrays.stream(disabledEvents).collect(Collectors.joining(",")));
        if (sessionName != null) {
            command.add("-s");
            command.add(sessionName);
        }
        return executeCommand(command.toArray(new String[0]));
    }

    /**
     * Stop the current tracing session
     *
     * @param sessionName
     *            The name of the session to stop. Use null for the current
     *            session.
     * @return If the command executed successfully (return code = 0).
     */
    public static boolean stopSession(String sessionName) {
        List<String> command = new ArrayList<String>();
        command.add("lttng");
        command.add("stop");
        if (sessionName != null) {
            command.add(sessionName);
        }
        return executeCommand(command.toArray(new String[0]));
    }

    /**
     * Issue a "lttng view" command on the provided session, and returns its
     * output. This effectively returns the current content of the trace in text
     * form.
     *
     * @param sessionName
     *            The name of the session to print. Use null for the current
     *            session.
     * @return The output of Babeltrace on the session's current trace
     */
    public static List<String> viewSession(String sessionName) {
        List<String> command = new ArrayList<String>();
        command.add("lttng");
        command.add("view");
        if (sessionName != null) {
            command.add(sessionName);
        }
        return getOutputFromCommand(command.toArray(new String[0]));
    }

    /**
     * Destroy the current tracing session
     *
     * @param sessionName
     *            The name of the session to destroy. Use null for the current
     *            session.
     * @return If the command executed successfully (return code = 0).
     */
    public static boolean destroySession(String sessionName) {
        List<String> command = new ArrayList<String>();
        command.add("lttng");
        command.add("destroy");
        if (sessionName != null) {
            command.add(sessionName);
        }
        return executeCommand(command.toArray(new String[0]));
    }

    /**
     * Try destroying the given tracing session, fail silently if there is no
     * session.
     *
     * @param sessionName
     *            The name of the session to destroy. Use null for the current
     *            session.
     */
    public static void tryDestroySession(String sessionName) {
        getOutputFromCommand(false, new String[] { "lttng", "destroy" });
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
        return getOutputFromCommand(true, command);
    }

    private static List<String> getOutputFromCommand(boolean print, String[] command) {
        try {
            Path tempFile = Files.createTempFile("test-output", null);

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(Redirect.to(tempFile.toFile()));

            Process p = builder.start();
            p.waitFor();

            List<String> lines = Files.readAllLines(tempFile);
            Files.delete(tempFile);

            if (print) {
                /* Also print the output to the console */
                lines.stream().forEach(s -> System.out.println(s));
            }

            return lines;

        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
