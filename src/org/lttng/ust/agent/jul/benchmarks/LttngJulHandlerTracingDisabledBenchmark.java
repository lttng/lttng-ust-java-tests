package org.lttng.ust.agent.jul.benchmarks;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.lttng.ust.agent.jul.LTTngLogHandler;
import org.lttng.ust.agent.jul.LttngJulRootAgent;

public class LttngJulHandlerTracingDisabledBenchmark extends AbstractJulBenchmark {

    @Before
    public void testSetup() {
        handler = new LTTngLogHandler(true);

        assertFalse(LttngJulRootAgent.getInstance().listEnabledEvents().iterator().hasNext());
    }
}
