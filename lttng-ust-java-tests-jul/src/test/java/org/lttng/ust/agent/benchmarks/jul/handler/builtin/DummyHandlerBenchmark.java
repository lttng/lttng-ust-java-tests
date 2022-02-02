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

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;

/**
 * Test suite of using a "dummy" handler, which means a handler that does
 * exactly nothing.
 */
@Tag("agent:jul")
@Tag("domain:jul")
@Tag("benchmark")
public class DummyHandlerBenchmark extends JulHandlerBenchmarkBase {

	/**
	 * Test setup
	 */
	@BeforeEach
	public void testSetup() {
		handler = new DummyHandler();
	}

	private static class DummyHandler extends Handler {

		public DummyHandler() {
			super();
		}

		@Override
		public void close() throws SecurityException {}

		@Override
		public void flush() {}

		@Override
		public void publish(LogRecord record) {}

	}
}
