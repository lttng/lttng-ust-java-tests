package org.lttng.ust.agent.integration.log4j;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    Log4jEnabledEventsTest.class,
    Log4jLegacyApiTest.class,
    Log4jMultiSessionTest.class
})
public class AllTests {

}
