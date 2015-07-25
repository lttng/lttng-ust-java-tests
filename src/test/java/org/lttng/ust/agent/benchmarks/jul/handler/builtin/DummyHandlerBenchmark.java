package org.lttng.ust.agent.benchmarks.jul.handler.builtin;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.junit.Before;
import org.lttng.ust.agent.benchmarks.jul.handler.JulHandlerBenchmarkBase;

public class DummyHandlerBenchmark extends JulHandlerBenchmarkBase {

	@Before
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
