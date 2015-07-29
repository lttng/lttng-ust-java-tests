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

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Utility methods to execute commands on the command line.
 *
 * @author Alexandre Montplaisir
 */
public final class ShellUtils {

    private ShellUtils() {}

    /**
     * Simple command to test that the environment / stdout are working
     * correctly.
     *
     * @param args
     *            Command-line arguments
     */
    public static void main(String[] args) {
        List<String> command = Arrays.asList("ls", "-l");
        executeCommand(command);
    }

    /**
     * Execute a shell command and retrieve its return value.
     *
     * @param command
     *            The command to execute, as a list of individual arguments (do
     *            not use spaces)
     * @return If the command returned successfully (ret code = 0)
     */
    public static boolean executeCommand(List<String> command) {
        try {
            /* "echo" the command to stdout */
            StringJoiner sj = new StringJoiner(" ", "$ ", "");
            command.stream().forEach(sj::add);
            System.out.println(sj.toString());

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(Redirect.INHERIT);

            Process p = builder.start();
            int ret = p.waitFor();

            System.out.println("(returned from command)");

            return (ret == 0);

        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Execute a shell command and retrieve its output.
     *
     * @param print
     *            Should the output also be printed to stdout as usual
     * @param command
     *            The command to execute, as a list of individual arguments (do
     *            not use spaces)
     * @return The output of the command, as one list element per line
     */
    public static List<String> getOutputFromCommand(boolean print, List<String> command) {
        try {
            /* "echo" the command to stdout */
            StringJoiner sj = new StringJoiner(" ", "$ ", "");
            command.stream().forEach(sj::add);
            System.out.println(sj.toString());

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
                lines.stream().forEach(System.out::println);
            } else {
                System.out.println("(output silenced)");
            }

            System.out.println("(returned from command)");
            return lines;

        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
