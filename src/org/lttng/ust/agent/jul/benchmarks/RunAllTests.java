package org.lttng.ust.agent.jul.benchmarks;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.lttng.ust.agent.jul.benchmarks.handler.NoHandlerBenchmark.class,
    org.lttng.ust.agent.jul.benchmarks.handler.DummyHandlerBenchmark.class,
    org.lttng.ust.agent.jul.benchmarks.handler.FileHandlerBenchmark.class,
    org.lttng.ust.agent.jul.benchmarks.handler.lttng.LttngJulHandlerTracingDisabledBenchmark.class,
    org.lttng.ust.agent.jul.benchmarks.handler.lttng.LttngJulHandlerTracingEnabledBenchmark.class,
    org.lttng.ust.agent.jul.benchmarks.handler.lttng.old.OldLttngJulHandlerTracingDisabledBenchmark.class,
    org.lttng.ust.agent.jul.benchmarks.handler.lttng.old.OldLttngJulHandlerTracingEnabledBenchmark.class
})
public class RunAllTests {
}
