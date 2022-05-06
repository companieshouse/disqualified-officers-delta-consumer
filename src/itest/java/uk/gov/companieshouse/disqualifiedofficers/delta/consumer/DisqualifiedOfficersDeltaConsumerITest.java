package uk.gov.companieshouse.disqualifiedofficers.delta.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.AbstractIntegrationTest;

public class DisqualifiedOfficersDeltaConsumerITest extends AbstractIntegrationTest {

    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${disqualified-officers.delta.topic}")
    private String mainTopic;

    @Test
    public void testSendingKafkaMessage() {
        ChsDelta chsDelta = new ChsDelta("{ \"key\": \"value\" }", 1, "some_id");
        kafkaTemplate.send(mainTopic, chsDelta);
    }

}