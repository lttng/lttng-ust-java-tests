package org.lttng.ust.agent.jul.benchmarks;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.lttng.ust.agent.jul.LTTngLogHandler;
import org.lttng.ust.agent.jul.LttngJulRootAgent;

public class LttngJulHandlerTracingEnabledBenchmark extends AbstractJulBenchmark {

    @Before
    public void testSetup() {
        handler = new LTTngLogHandler(true);

        LttngJulRootAgent agent = LttngJulRootAgent.getInstance();
        agent.enableEvent("*");
        assertTrue(agent.listEnabledEvents().iterator().hasNext());
    }
}
