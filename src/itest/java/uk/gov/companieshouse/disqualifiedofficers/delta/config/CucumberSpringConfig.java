package uk.gov.companieshouse.disqualifiedofficers.delta.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import io.cucumber.spring.CucumberContextConfiguration;
import uk.gov.companieshouse.disqualifiedofficers.delta.steps.MessageProcessedEventListener;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({KafkaTestContainerConfig.class, MessageProcessedEventListener.class})
@ActiveProfiles({"test"})
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:itest.properties"
})
public class CucumberSpringConfig {
}