package uk.gov.companieshouse.disqualifiedofficers.delta.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Import(KafkaTestContainerConfig.class)
@ActiveProfiles({"test"})
public class CucumberSpringConfig {
    // Dedicated Cucumber-Spring wiring class.
    // @CucumberContextConfiguration must NOT be on the runner itself.
}