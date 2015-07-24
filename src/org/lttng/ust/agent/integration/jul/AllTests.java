package org.lttng.ust.agent.integration.jul;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    JulEnabledEventsTest.class,
    JulLegacyApiTest.class
})
public class AllTests {

}
