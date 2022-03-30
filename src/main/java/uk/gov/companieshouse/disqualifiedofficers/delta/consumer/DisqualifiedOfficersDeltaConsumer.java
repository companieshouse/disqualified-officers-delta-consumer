package uk.gov.companieshouse.disqualifiedofficers.delta.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.processor.DisqualifiedOfficersDeltaProcessor;
import uk.gov.companieshouse.logging.Logger;


@Component
public class DisqualifiedOfficersDeltaConsumer {

    private final DisqualifiedOfficersDeltaProcessor deltaProcessor;
    private final Logger logger;

    @Autowired
    public DisqualifiedOfficersDeltaConsumer(DisqualifiedOfficersDeltaProcessor deltaProcessor,
                Logger logger) {
        this.deltaProcessor = deltaProcessor;
        this.logger = logger;
    }

    /**
     * Receives Main topic messages.
     */
    @KafkaListener(id = "${disqualified-officers.delta.main-id}",
            topics = "${disqualified-officers.delta.topic.main}",
            groupId = "${disqualified-officers.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(Message<ChsDelta> chsDeltaMessage) {
        logger.info("A new message read from MAIN topic with payload: "
                + chsDeltaMessage.getPayload());
        deltaProcessor.processDelta(chsDeltaMessage);
    }

    /**
     * Receives Retry topic messages.
     */
    @KafkaListener(id = "${disqualified-officers.delta.retry-id}",
            topics = "${disqualified-officers.delta.topic.retry}",
            groupId = "${disqualified-officers.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveRetryMessages(Message<ChsDelta> message) {
        logger.info(String.format("A new message read from RETRY topic with payload:%s "
                + "and headers:%s ", message.getPayload(), message.getHeaders()));
        deltaProcessor.processDelta(message);
    }

}