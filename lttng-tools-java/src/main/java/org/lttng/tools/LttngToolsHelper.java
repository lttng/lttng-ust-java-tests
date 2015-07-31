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

package org.lttng.tools;

import static org.lttng.tools.utils.ShellUtils.executeCommand;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

/**
 * Helper class to issue LTTng commands that do not affect a single session, and
 * as such, do not fit into the scope of one {@link ILttngSession}.
 *
 * @author Alexandre Montplaisir
 */
public final class LttngToolsHelper {

    private LttngToolsHelper() {}

    /**
     * Utility method to destroy all existing sessions. Useful when first
     * setting up a test to make sure no existing session interferes.
     *
     * @return If the command completed successfully
     */
    public static boolean destroyAllSessions() {
        return executeCommand(Arrays.asList("lttng", "destroy", "-a"));
    }

    /**
     * Outside of the scope of lttng-tools, but this utility method can be used
     * to delete all traces currently under ~/lttng-traces/. This can be used by
     * tests to cleanup a trace they have created.
     *
     * @return True if the command completes successfully, false if there was an
     *         error.
     */
    public static boolean deleteAllTraces() {
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
}
