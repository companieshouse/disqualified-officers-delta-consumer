package uk.gov.companieshouse.disqualifiedofficers.delta.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.processor.DisqualifiedOfficersDeltaProcessor;

@Component
public class DisqualifiedOfficersDeltaConsumer {

    private final DisqualifiedOfficersDeltaProcessor deltaProcessor;
    public final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Default constructor.
     */
    @Autowired
    public DisqualifiedOfficersDeltaConsumer(DisqualifiedOfficersDeltaProcessor deltaProcessor, KafkaTemplate<String, Object> kafkaTemplate) {
        this.deltaProcessor = deltaProcessor;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Receives Main topic messages.
     */
    @RetryableTopic(attempts = "${disqualified-officers.delta.retry-attempts}",
            backoff = @Backoff(delayExpression = "${disqualified-officers.delta.backoff-delay}"),
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            dltTopicSuffix = "-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "false",
            exclude = NonRetryableErrorException.class)
    @KafkaListener(topics = "${disqualified-officers.delta.topic}",
            groupId = "${disqualified-officers.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(Message<ChsDelta> chsDeltaMessage,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        if (Boolean.TRUE.equals(chsDeltaMessage.getPayload().getIsDelete())) {
            deltaProcessor.processDelete(chsDeltaMessage);
        } else {
            deltaProcessor.processDelta(chsDeltaMessage);
        }
    }
}