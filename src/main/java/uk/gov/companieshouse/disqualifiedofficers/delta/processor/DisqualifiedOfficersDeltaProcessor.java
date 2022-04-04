package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.producer.DisqualifiedOfficersDeltaProducer;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;
import uk.gov.companieshouse.logging.Logger;


@Component
public class DisqualifiedOfficersDeltaProcessor {

    private final DisqualifiedOfficersDeltaProducer deltaProducer;
    private final DisqualifiedOfficersApiTransformer transformer;
    private final Logger logger;

    /**
     * Constructor for the delta processor.
     * @param deltaProducer uses the chKafkaProducer to send a message
     * @param transformer transforms the data from delta to api object through mapstruct
     * @param logger logs out messages to the app logs
     */
    @Autowired
    public DisqualifiedOfficersDeltaProcessor(DisqualifiedOfficersDeltaProducer deltaProducer,
            DisqualifiedOfficersApiTransformer transformer, Logger logger) {
        this.deltaProducer = deltaProducer;
        this.transformer = transformer;
        this.logger = logger;
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

            DisqualificationOfficer disqualificationOfficer = disqualifiedOfficersDelta
                    .getDisqualifiedOfficer()
                    .get(0);

            if (Boolean.valueOf(disqualificationOfficer.getCorporateInd())) {
                InternalCorporateDisqualificationApi apiObject = transformer
                        .transformCorporateDisqualification(disqualifiedOfficersDelta);
                logger.info("InternalCorporateDisqualificationApi object" + apiObject);
                //invoke disqualified officers API with Corporate method
            } else {
                InternalNaturalDisqualificationApi apiObject = transformer
                        .transformNaturalDisqualification(disqualifiedOfficersDelta);
                logger.info("InternalNaturalDisqualificationApi object" + apiObject);
                //invoke disqualified officers API with Natural method
            }
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