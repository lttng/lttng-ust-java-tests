package org.lttng.ust.agent.jul.benchmarks;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DummyHandlerBenchmark.class,
    FileHandlerBenchmark.class,
    LttngJulHandlerTracingDisabledBenchmark.class,
    LttngJulHandlerTracingEnabledBenchmark.class,
    NoHandlerBenchmark.class
})
public class RunAllTests {
}
