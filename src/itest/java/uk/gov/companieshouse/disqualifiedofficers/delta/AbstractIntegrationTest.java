package uk.gov.companieshouse.disqualifiedofficers.delta;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import uk.gov.companieshouse.disqualifiedofficers.delta.config.KafkaTestContainerConfig;
import uk.gov.companieshouse.disqualifiedofficers.delta.steps.MessageProcessedEventListener;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({KafkaTestContainerConfig.class, MessageProcessedEventListener.class})
@ActiveProfiles({"test"})
@TestPropertySource(locations = {
        "classpath:application.properties",
        "classpath:itest.properties"
})
public abstract class AbstractIntegrationTest {
    // ...existing code...

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        ConfluentKafkaContainer container = KafkaTestContainerConfig.getContainerInstance();
        if (container != null) {
            registry.add("spring.kafka.bootstrap-servers", container::getBootstrapServers);
        }
    }
}