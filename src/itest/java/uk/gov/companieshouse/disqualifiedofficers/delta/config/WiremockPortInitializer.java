package uk.gov.companieshouse.disqualifiedofficers.delta.config;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class WiremockPortInitializer extends AbstractTestExecutionListener {
    @Override
    public void beforeTestClass(TestContext testContext) {
        WiremockTestConfig.setupWiremock();
        int wiremockPort = WiremockTestConfig.getPort();
        System.setProperty("WIREMOCK_PORT", String.valueOf(wiremockPort));
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        WiremockTestConfig.stopWiremock();
    }
}

