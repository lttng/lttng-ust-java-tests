package org.lttng.ust.agent.integration.jul;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    JulEnabledEventsTest.class,
    JulLegacyApiTest.class,
    JulMultiSessionTest.class
})
public class AllTests {

}
