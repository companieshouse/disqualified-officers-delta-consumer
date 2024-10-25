package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.DisqualificationDeleteDelta;
import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.mapper.MapperUtils;
import uk.gov.companieshouse.disqualifiedofficers.delta.service.api.ApiClientService;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;
import uk.gov.companieshouse.logging.Logger;

@Component
public class DisqualifiedOfficersDeltaProcessor {

    private final DisqualifiedOfficersApiTransformer transformer;
    private final ApiClientService apiClientService;
    private final Logger logger;

    /**
     * Constructor for the delta processor.
     * @param transformer transforms the data from delta to api object through mapstruct
     * @param logger logs out messages to the app logs
     * @param apiClientService handles PUT request to the disqualified data API
     */
    @Autowired
    public DisqualifiedOfficersDeltaProcessor(DisqualifiedOfficersApiTransformer transformer,
            Logger logger, ApiClientService apiClientService) {
        this.transformer = transformer;
        this.logger = logger;
        this.apiClientService = apiClientService;
    }

    /**
     * Process CHS Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String logContext = payload.getContextId();
        final Map<String, Object> logMap = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        DisqualificationDelta disqualifiedOfficersDelta;
        try {
            disqualifiedOfficersDelta = mapper.readValue(payload.getData(),
                    DisqualificationDelta.class);
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when extracting disqualified-officers delta", ex);
        }

        logger.info(String.format("DisqualificationDelta extracted for context ID [%s]"
                + " Kafka message: [%s]", logContext, disqualifiedOfficersDelta));

        DisqualificationOfficer disqualificationOfficer = disqualifiedOfficersDelta
                .getDisqualifiedOfficer()
                .getFirst();
        if (disqualificationOfficer.getCorporateInd() != null
                    && disqualificationOfficer.getCorporateInd().equals("1")) {
            InternalCorporateDisqualificationApi apiObject;
            try {
                apiObject = transformer.transformCorporateDisqualification(
                        disqualifiedOfficersDelta);
            } catch (Exception ex) {
                throw new RetryableErrorException(
                        "Error when transforming into Api object", ex);
            }

            logger.info(String.format("Message with context ID: [%s] successfully transformed into"
                    + " CorporateDisqualificationAPI object", logContext));

            //invoke disqualified officers API with Corporate method
            invokeDisqualificationsDataApi(logContext, disqualificationOfficer,
                    apiObject, logMap);
        } else {
            InternalNaturalDisqualificationApi apiObject;
            try {
                apiObject = transformer.transformNaturalDisqualification(
                        disqualifiedOfficersDelta);
            } catch (Exception ex) {
                throw new RetryableErrorException(
                        "Error when transforming into Api object", ex);
            }

            logger.info(String.format("Message with context ID: [%s] successfully" 
                    + " transformed into NaturalDisqualificationAPI object", logContext));

            //invoke disqualified officers API with Natural method
            invokeDisqualificationsDataApi(logContext, disqualificationOfficer,
                    apiObject, logMap);
        }
    }

    /**
     * Process CHS Delta delete message.
     */
    public void processDelete(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String logContext = payload.getContextId();
        final String officerId;

        ObjectMapper mapper = new ObjectMapper();
        DisqualificationDeleteDelta disqualifiedOfficersDelete;
        try {
            disqualifiedOfficersDelete = mapper.readValue(payload.getData(),
                    DisqualificationDeleteDelta.class);
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when extracting disqualified-officers delete delta", ex);
        }

        logger.info(String.format("DisqualificationDeleteDelta extracted for context ID" 
                + " [%s] Kafka message: [%s]", logContext, disqualifiedOfficersDelete));
        officerId = MapperUtils.encode(disqualifiedOfficersDelete.getOfficerId());
        logger.info(String.format("Performing a DELETE for officer id: [%s]", officerId));
        apiClientService.deleteDisqualification(logContext, officerId, disqualifiedOfficersDelete.getDeltaAt());
    }

    /**
     * Invoke Disqualifications Data API.
     */
    private void invokeDisqualificationsDataApi(final String logContext, 
                        DisqualificationOfficer disqualification,
                        InternalNaturalDisqualificationApi internalDisqualificationApi,
                        final Map<String, Object> logMap) {
        logger.infoContext(
                logContext,
                String.format("Process disqualification for officer with id [%s]",
                        disqualification.getOfficerId()),
                logMap);
        apiClientService.putDisqualification(logContext,
                internalDisqualificationApi.getInternalData().getOfficerId(),
                internalDisqualificationApi);
        logger.debugContext(logContext,
                "Response received from disqualified-officers-data-api", logMap);
    }

    private void invokeDisqualificationsDataApi(final String logContext,
                        DisqualificationOfficer disqualification,
                        InternalCorporateDisqualificationApi internalDisqualificationApi,
                        final Map<String, Object> logMap) {
        logger.infoContext(
                logContext,
                String.format("Process disqualification for officer with id [%s]",
                        disqualification.getOfficerId()),
                logMap);
        apiClientService.putDisqualification(logContext,
                internalDisqualificationApi.getInternalData().getOfficerId(),
                internalDisqualificationApi);
        logger.debugContext(logContext,
                "Response received from disqualified-officers-data-api", logMap);
    }
}