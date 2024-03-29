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

package org.lttng.ust.agent.utils;

import java.util.Arrays;
import java.util.List;

import org.lttng.tools.ILttngSession;
import org.lttng.tools.ILttngSession.Domain;
import org.lttng.tools.utils.ShellUtils;

/**
 * Utility methods to test the presence of certain LTTng tools or libraries in
 * the runtime environment.
 */
public final class LttngUtils {

    private LttngUtils() {}

    /**
     * Check that lttng-tools and babeltrace are installed on the system and
     * working.
     *
     * @param domain
     *            The tracing domain to test for (we will try to setup a session
     *            with this domain)
     * @return True if the environment should allow tracing fine, false if there
     *         was an error
     */
    public static boolean checkForLttngTools(Domain domain) {
        System.out.println("Sanity check: start");
        try (ILttngSession session = ILttngSession.createSession(null, domain)) {
            boolean ret1 = session.enableAllEvents();
            boolean ret2 = session.start();
            boolean ret3 = session.stop();
            /*
             * "lttng view" also tests that Babeltrace is installed and working
             */
            List<String> contents = session.view();
            if (!contents.isEmpty()) {
                System.out.print(String.join("\n", contents));
	    }
            System.out.println("Sanity check: end");
            return (ret1 && ret2 && ret3 && contents.isEmpty());
        }
    }

    /**
     * Check if there is a user session daemon currently running on the system.
     * It needs to be of the same user as the application running this program.
     *
     * @return If there is a user session daemon currently running
     */
    public static boolean checkForUserSessiond() {
        String userName = System.getProperty("user.name");

        /* The user name is truncated to 7 characters in "ps" */
        String shortUserName = userName.substring(0, Math.min(userName.length(), 7));

        List<String> command = Arrays.asList("ps", "-e", "u");
        List<String> output = ShellUtils.getOutputFromCommand(false, command);
        return output.stream()
                .filter(s -> s.contains("lttng-sessiond"))
                .anyMatch(s -> s.startsWith(shortUserName));
    }

    /**
     * Check if there is a root user session daemon daemon currently running on
     * the system.
     *
     * @return If there is a root session daemon currently running
     */
    public static boolean checkForRootSessiond() {
        List<String> command = Arrays.asList("ps", "-e", "u");
        List<String> output = ShellUtils.getOutputFromCommand(false, command);
        return output.stream()
                .filter(s -> s.contains("lttng-sessiond"))
                .anyMatch(s -> s.startsWith("root"));
    }

}
