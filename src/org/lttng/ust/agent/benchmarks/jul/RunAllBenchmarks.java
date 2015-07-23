package org.lttng.ust.agent.benchmarks.jul;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.lttng.ust.agent.benchmarks.jul.handler.NoHandlerBenchmark.class,
    org.lttng.ust.agent.benchmarks.jul.handler.DummyHandlerBenchmark.class,
//    org.lttng.ust.agent.jul.benchmarks.handler.FileHandlerBenchmark.class,
    org.lttng.ust.agent.benchmarks.jul.handler.lttng.LttngJulHandlerTracingDisabledBenchmark.class,
    org.lttng.ust.agent.benchmarks.jul.handler.lttng.LttngJulHandlerTracingEnabledBenchmark.class,
    org.lttng.ust.agent.benchmarks.jul.handler.lttng.old.OldLttngJulHandlerTracingDisabledBenchmark.class,
    org.lttng.ust.agent.benchmarks.jul.handler.lttng.old.OldLttngJulHandlerTracingEnabledBenchmark.class
})
public class RunAllBenchmarks {
}
