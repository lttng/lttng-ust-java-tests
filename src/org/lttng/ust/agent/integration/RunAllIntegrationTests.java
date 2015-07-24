package org.lttng.ust.agent.integration;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    org.lttng.ust.agent.integration.jul.AllTests.class,
    org.lttng.ust.agent.integration.log4j.AllTests.class,
})
public class RunAllIntegrationTests {

}
