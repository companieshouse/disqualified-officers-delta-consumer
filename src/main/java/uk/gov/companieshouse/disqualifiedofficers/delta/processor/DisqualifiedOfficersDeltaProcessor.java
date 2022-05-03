package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.api.delta.DisqualificationOfficer;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.handler.ApiResponseHandler;
import uk.gov.companieshouse.disqualifiedofficers.delta.producer.DisqualifiedOfficersDeltaProducer;
import uk.gov.companieshouse.disqualifiedofficers.delta.service.api.ApiClientService;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;
import uk.gov.companieshouse.logging.Logger;


@Component
public class DisqualifiedOfficersDeltaProcessor {

    private final DisqualifiedOfficersDeltaProducer deltaProducer;
    private final DisqualifiedOfficersApiTransformer transformer;
    private final ApiClientService apiClientService;
    private final Logger logger;

    /**
     * Constructor for the delta processor.
     * @param deltaProducer uses the chKafkaProducer to send a message
     * @param transformer transforms the data from delta to api object through mapstruct
     * @param logger logs out messages to the app logs
     * @param apiClientService handles PUT request to the disqualified data API
     */
    @Autowired
    public DisqualifiedOfficersDeltaProcessor(DisqualifiedOfficersDeltaProducer deltaProducer,
            DisqualifiedOfficersApiTransformer transformer, Logger logger,
            ApiClientService apiClientService) {
        this.deltaProducer = deltaProducer;
        this.transformer = transformer;
        this.logger = logger;
        this.apiClientService = apiClientService;
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
            final String logContext = payload.getContextId();
            final Map<String, Object> logMap = new HashMap<>();

            ObjectMapper mapper = new ObjectMapper();
            DisqualificationDelta disqualifiedOfficersDelta = mapper.readValue(payload.getData(),
                    DisqualificationDelta.class);
            DisqualificationOfficer disqualificationOfficer = disqualifiedOfficersDelta
                    .getDisqualifiedOfficer()
                    .get(0);
            if (disqualificationOfficer.getCorporateInd() != null 
                        && disqualificationOfficer.getCorporateInd().equals("1")) {
                InternalCorporateDisqualificationApi apiObject = transformer
                        .transformCorporateDisqualification(disqualifiedOfficersDelta);
                //invoke disqualified officers API with Corporate method
                invokeDisqualificationsDataApi(logContext, disqualificationOfficer,
                        apiObject, logMap);
            } else {
                InternalNaturalDisqualificationApi apiObject = transformer
                        .transformNaturalDisqualification(disqualifiedOfficersDelta);
                //invoke disqualified officers API with Natural method
                invokeDisqualificationsDataApi(logContext, disqualificationOfficer, 
                        apiObject, logMap);
            }
        } catch (Exception ex) {
            throw new NonRetryableErrorException("Error when extracting disqualified-officers delta", ex);
        }
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
                null);
        final ApiResponse<Void> response =
                apiClientService.putDisqualification(logContext,
                        internalDisqualificationApi.getInternalData().getOfficerId(),
                        internalDisqualificationApi);
        ApiResponseHandler apiResponseHandler = new ApiResponseHandler();
        apiResponseHandler.handleResponse(HttpStatus.valueOf(response.getStatusCode()),
                logContext, logMap, logger);
    }

    private void invokeDisqualificationsDataApi(final String logContext,
                        DisqualificationOfficer disqualification,
                        InternalCorporateDisqualificationApi internalDisqualificationApi,
                        final Map<String, Object> logMap) {
        logger.infoContext(
                logContext,
                String.format("Process disqualification for officer with id [%s]",
                        disqualification.getOfficerId()),
                null);
        final ApiResponse<Void> response =
                apiClientService.putDisqualification(logContext,
                        internalDisqualificationApi.getInternalData().getOfficerId(),
                        internalDisqualificationApi);
        ApiResponseHandler apiResponseHandler = new ApiResponseHandler();
        apiResponseHandler.handleResponse(HttpStatus.valueOf(response.getStatusCode()),
                logContext, logMap, logger);
    }
}