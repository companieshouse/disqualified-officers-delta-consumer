package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.producer.DisqualifiedOfficersDeltaProducer;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;


@Component
public class DisqualifiedOfficersDeltaProcessor {

    private final DisqualifiedOfficersDeltaProducer deltaProducer;
    private final DisqualifiedOfficersApiTransformer transformer;

    @Autowired
    public DisqualifiedOfficersDeltaProcessor(DisqualifiedOfficersDeltaProducer deltaProducer,
            DisqualifiedOfficersApiTransformer transformer) {
        this.deltaProducer = deltaProducer;
        this.transformer = transformer;
    }

    /**
     * Process CHS Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
        try {
            MessageHeaders headers = chsDelta.getHeaders();
            final String receivedTopic =
                    Objects.requireNonNull(headers.get(KafkaHeaders.RECEIVED_TOPIC)).toString();
            final ChsDelta payload = chsDelta.getPayload();

            ObjectMapper mapper = new ObjectMapper();
            DisqualificationDelta disqualifiedOfficersDelta = mapper.readValue(payload.getData(),
                    DisqualificationDelta.class);

            transformer.transform(disqualifiedOfficersDelta);
        } catch (RetryableErrorException ex) {
            retryDeltaMessage(chsDelta);
        } catch (Exception ex) {
            handleErrorMessage(chsDelta);
            // send to error topic
        }
    }

    public void retryDeltaMessage(Message<ChsDelta> chsDelta) {
    }

    private void handleErrorMessage(Message<ChsDelta> chsDelta) {
    }
}