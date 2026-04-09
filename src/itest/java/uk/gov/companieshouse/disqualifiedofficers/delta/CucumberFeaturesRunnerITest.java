package uk.gov.companieshouse.disqualifiedofficers.delta;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, json:target/cucumber-report.json")
// REMOVED @CucumberContextConfiguration — now on CucumberSpringConfig
public class CucumberFeaturesRunnerITest {
    // No longer extends AbstractIntegrationTest — the runner is not a Spring context
}