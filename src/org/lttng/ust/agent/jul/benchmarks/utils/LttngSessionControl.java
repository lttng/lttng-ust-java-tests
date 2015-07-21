package org.lttng.ust.agent.jul.benchmarks.utils;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

public final class LttngSessionControl {

    private LttngSessionControl() {}

    public static boolean setupJulSessionNoEvents() {
        return executeCommands(new String[][] {
                { "lttng", "create" },
                /*
                 * We have to enable a channel for 'lttng start' to work.
                 * However, we cannot enable a channel directly, see
                 * https://bugs.lttng.org/issues/894 . Instead we will enable an
                 * event we know does not exist
                 */
                { "lttng", "enable-event", "-j", "non-event" },
                { "lttng", "start" }
        });
    }

    public static boolean setupJulSessionAllEvents() {
        return executeCommands(new String[][] {
                { "lttng", "create" },
                { "lttng", "enable-event", "-j", "-a" },
                { "lttng", "start" }
        });
    }

    public static boolean stopSession() {
        return executeCommand(new String[] { "lttng", "stop" });
    }

    public static boolean destroySession() {
        return executeCommand(new String[] { "lttng", "destroy" });
    }

    private static boolean executeCommands(String [][] commands) {
        for (String[] command : commands) {
            if (executeCommand(command) == false) {
                return false;
            }
        }
        return true;
    }

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
}
