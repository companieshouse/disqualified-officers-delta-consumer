package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import static uk.gov.companieshouse.disqualifiedofficers.delta.DisqualifiedOfficersDeltaConsumerApplication.NAMESPACE;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.companieshouse.disqualifiedofficers.delta.logging.DataMapHolder;
import uk.gov.companieshouse.disqualifiedofficers.delta.mapper.MapperUtils;
import uk.gov.companieshouse.disqualifiedofficers.delta.service.api.ApiClientService;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class DisqualifiedOfficersDeltaProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final DisqualifiedOfficersApiTransformer transformer;
    private final ApiClientService apiClientService;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for the delta processor.
     *
     * @param transformer      transforms the data from delta to api object through mapstruct
     * @param apiClientService handles PUT request to the disqualified data API
     */
    @Autowired
    public DisqualifiedOfficersDeltaProcessor(DisqualifiedOfficersApiTransformer transformer,
                      ApiClientService apiClientService, ObjectMapper objectMapper) {
        this.transformer = transformer;
        this.apiClientService = apiClientService;
        this.objectMapper = objectMapper;
    }

    /**
     * Process CHS Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();

        DisqualificationDelta disqualifiedOfficersDelta = new DisqualificationDelta();
        try {
            disqualifiedOfficersDelta = objectMapper.readValue(payload.getData(),
                    DisqualificationDelta.class);
        } catch (Exception ex) {
            LOGGER.error("Unable to deserialise delta: [%s]".formatted(disqualifiedOfficersDelta),
                    ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(
                    "Error deserialising disqualified-officers delta", ex);
        }

        DisqualificationOfficer disqualificationOfficer = disqualifiedOfficersDelta
                .getDisqualifiedOfficer()
                .getFirst();
        DataMapHolder.get().officerId(disqualificationOfficer.getOfficerId());
        if (disqualificationOfficer.getCorporateInd() != null
                && disqualificationOfficer.getCorporateInd().equals("1")) {
            InternalCorporateDisqualificationApi apiObject;
            try {
                apiObject = transformer.transformCorporateDisqualification(
                        disqualifiedOfficersDelta);
            } catch (Exception ex) {
                LOGGER.error("Unable to transform delta into Corporate disqualification: [%s]"
                        .formatted(disqualifiedOfficersDelta), ex, DataMapHolder.getLogMap());
                throw new NonRetryableErrorException(
                        "Error transforming delta to Corporate disqualification request", ex);
            }
            invokeDataApiForCorporateDisqualification(contextId, apiObject);
        } else {
            InternalNaturalDisqualificationApi apiObject;
            try {
                apiObject = transformer.transformNaturalDisqualification(
                        disqualifiedOfficersDelta);
            } catch (Exception ex) {
                LOGGER.error("Unable to transform delta into Natural disqualification: [%s]"
                        .formatted(disqualifiedOfficersDelta), ex, DataMapHolder.getLogMap());
                throw new NonRetryableErrorException(
                        "Error transforming delta to Natural disqualification request", ex);
            }
            invokeDataApiForNaturalDisqualification(contextId, apiObject);
        }
    }

    /**
     * Process CHS Delta delete message.
     */
    public void processDelete(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();

        DisqualificationDeleteDelta disqualifiedOfficersDelete = new DisqualificationDeleteDelta();
        try {
            disqualifiedOfficersDelete = objectMapper.readValue(payload.getData(),
                    DisqualificationDeleteDelta.class);
        } catch (Exception ex) {
            LOGGER.error("Unable to deserialise delta: [%s]".formatted(disqualifiedOfficersDelete),
                    ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(
                    "Error deserialising disqualified-officers delete delta", ex);
        }

        final String officerId = MapperUtils.encode(disqualifiedOfficersDelete.getOfficerId());
        DataMapHolder.get().officerId(officerId);
        apiClientService.deleteDisqualification(
                contextId,
                officerId,
                disqualifiedOfficersDelete.getDeltaAt(),
                DisqualificationType.getTypeFromCorporateInd(disqualifiedOfficersDelete.getCorporateInd()));
    }

    /**
     * Invoke Disqualifications Data API.
     */
    private void invokeDataApiForNaturalDisqualification(final String contextId,
            InternalNaturalDisqualificationApi internalDisqualificationApi) {
        apiClientService.putNaturalDisqualification(contextId,
                internalDisqualificationApi.getInternalData().getOfficerId(),
                internalDisqualificationApi);
    }

    private void invokeDataApiForCorporateDisqualification(final String contextId,
            InternalCorporateDisqualificationApi internalDisqualificationApi) {
        apiClientService.putCorporateDisqualification(contextId,
                internalDisqualificationApi.getInternalData().getOfficerId(),
                internalDisqualificationApi);
    }
}