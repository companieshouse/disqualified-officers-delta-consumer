package uk.gov.companieshouse.disqualifiedofficers.delta.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
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
    @RetryableTopic(attempts = "${disqualified-officers.delta.retry-attempts}",
            backoff = @Backoff(delayExpression = "${disqualified-officers.delta.backoff-delay}"),
            fixedDelayTopicStrategy = FixedDelayStrategy.SINGLE_TOPIC,
            retryTopicSuffix = "-${charges.delta.group-id}-retry",
            dltTopicSuffix = "-${charges.delta.group-id}-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "false",
            exclude = NonRetryableErrorException.class)
    @KafkaListener(id = "${disqualified-officers.delta.main-id}",
            topics = "${disqualified-officers.delta.topic.main}",
            groupId = "${disqualified-officers.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(Message<ChsDelta> chsDeltaMessage,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        logger.info("A new message read from MAIN topic with payload: "
                + chsDeltaMessage.getPayload());
        try {
            deltaProcessor.processDelta(chsDeltaMessage);
        } catch(Exception exception) {
            logger.error(String.format("Exception occurred while processing the topic: %s "
                    + "with message: %s", topic, chsDeltaMessage), exception);
            throw exception;
        }
    }
}