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

package org.lttng.ust.agent.benchmarks.jul.handler.builtin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;

/**
 * Test class using a {@link FileHandler}, which a {@link SimpleFormatter}.
 */
@Tag("agent:jul")
@Tag("domain:jul")
@Tag("benchmark")
public class FileHandlerBenchmark extends JulHandlerBenchmarkBase {

    private Path outputFile;

	/**
	 * Test setup
	 *
	 * @throws SecurityException
	 *             If there is problem setting up the handler
	 * @throws IOException
	 *             If there is problem setting up the handler
	 */
	@BeforeEach
	public void testSetup() throws SecurityException, IOException {
		outputFile = Files.createTempFile(this.getClass().getSimpleName(), null);

		handler = new FileHandler(outputFile.toString(), false);
		handler.setFormatter(new SimpleFormatter());
	}

	/**
	 * Test cleanup
	 *
	 * @throws IOException
	 *             If we could not delete the test file
	 */
	@AfterEach
	public void testTeardown() throws IOException {
		Files.deleteIfExists(outputFile);
	}
}
