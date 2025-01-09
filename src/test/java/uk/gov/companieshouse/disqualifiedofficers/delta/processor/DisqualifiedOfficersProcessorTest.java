package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper.buildExpectedDisqualificationDelta;
import static uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper.createChsDeltaMessageCorporateOfficer;
import static uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper.createChsDeltaMessageNaturalOfficer;
import static uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper.createCorporateDisqualificationApi;
import static uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper.createDisqualificationDeleteDelta;
import static uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper.createInvalidChsDeltaMessage;
import static uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper.createNaturalDisqualificationApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.api.delta.DisqualificationDeleteDelta;
import uk.gov.companieshouse.api.delta.DisqualificationDelta;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.service.api.ApiClientService;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class DisqualifiedOfficersProcessorTest {

    private static final String CORPORATE_INDICATOR = "1";
    private static final String NATURAL_INDICATOR = " 0";

    private DisqualifiedOfficersDeltaProcessor deltaProcessor;

    @Mock
    private DisqualifiedOfficersApiTransformer transformer;
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        deltaProcessor = new DisqualifiedOfficersDeltaProcessor(
                transformer,
                apiClientService,
                objectMapper
        );
    }

    @Test
    @DisplayName("Positive processor test case with a NaturalDisqualificationDelta")
    void shouldProcessValidNaturalUpsertDeltaAndSendToApiSuccessfully() throws IOException {
        Message<ChsDelta> chsDelta = createChsDeltaMessageNaturalOfficer(false);
        DisqualificationDelta expectedDelta = buildExpectedDisqualificationDelta(NATURAL_INDICATOR);
        InternalNaturalDisqualificationApi apiObject = createNaturalDisqualificationApi();
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);

        when(objectMapper.readValue(chsDelta.getPayload().getData(), DisqualificationDelta.class)).thenReturn(expectedDelta);
        when(transformer.transformNaturalDisqualification(expectedDelta)).thenReturn(apiObject);
        when(apiClientService.putNaturalDisqualification(any(),any(), eq(apiObject))).thenReturn(response);

        deltaProcessor.processDelta(chsDelta);

        verify(transformer).transformNaturalDisqualification(expectedDelta);
        verify(apiClientService).putNaturalDisqualification("context_id", "3002276133", apiObject);
    }

    @Test
    @DisplayName("Positive processor test case with a CorporateDisqualificationDelta")
    void shouldProcessValidCorporateUpsertDeltaAndSendToApiSuccessfully() throws IOException {
        Message<ChsDelta> chsDelta = createChsDeltaMessageCorporateOfficer(false);
        DisqualificationDelta expectedDelta = buildExpectedDisqualificationDelta(CORPORATE_INDICATOR);
        InternalCorporateDisqualificationApi apiObject = createCorporateDisqualificationApi();
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);

        when(objectMapper.readValue(chsDelta.getPayload().getData(), DisqualificationDelta.class)).thenReturn(expectedDelta);
        when(transformer.transformCorporateDisqualification(expectedDelta)).thenReturn(apiObject);
        when(apiClientService.putCorporateDisqualification(any(),any(), eq(apiObject))).thenReturn(response);

        deltaProcessor.processDelta(chsDelta);

        verify(transformer).transformCorporateDisqualification(expectedDelta);
        verify(apiClientService).putCorporateDisqualification("context_id", "3002276133", apiObject);
    }

    @Test
    void shouldProcessValidDeltaAsNaturalIfCorporateIndIsNull() throws IOException {
        Message<ChsDelta> chsDelta = createChsDeltaMessageNaturalOfficer(false);
        DisqualificationDelta expectedDelta = buildExpectedDisqualificationDelta(null);
        InternalNaturalDisqualificationApi apiObject = createNaturalDisqualificationApi();
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);

        when(objectMapper.readValue(chsDelta.getPayload().getData(), DisqualificationDelta.class)).thenReturn(expectedDelta);
        when(transformer.transformNaturalDisqualification(expectedDelta)).thenReturn(apiObject);
        when(apiClientService.putNaturalDisqualification(any(),any(), eq(apiObject))).thenReturn(response);

        deltaProcessor.processDelta(chsDelta);

        verify(transformer).transformNaturalDisqualification(expectedDelta);
        verify(apiClientService).putNaturalDisqualification("context_id", "3002276133", apiObject);
    }

    @Test
    void shouldThrowRetryableExceptionWhenDisqualificationsApiReturns500() throws IOException {
        Message<ChsDelta> chsDelta = createChsDeltaMessageNaturalOfficer(false);
        InternalNaturalDisqualificationApi apiObject = createNaturalDisqualificationApi();

        when(objectMapper.readValue(chsDelta.getPayload().getData(), DisqualificationDelta.class)).thenReturn(buildExpectedDisqualificationDelta(NATURAL_INDICATOR));
        when(transformer.transformNaturalDisqualification(any())).thenReturn(apiObject);
        when(apiClientService.putNaturalDisqualification(any(),any(), eq(apiObject))).thenThrow(new RetryableErrorException(""));

        assertThrows(RetryableErrorException.class, ()->deltaProcessor.processDelta(chsDelta));
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenNaturalTransformerUnsuccessful() throws IOException {
        Message<ChsDelta> chsDelta = createChsDeltaMessageNaturalOfficer(false);

        when(objectMapper.readValue(chsDelta.getPayload().getData(), DisqualificationDelta.class)).thenReturn(buildExpectedDisqualificationDelta(NATURAL_INDICATOR));
        when(transformer.transformNaturalDisqualification(any())).thenThrow(NullPointerException.class);

        assertThrows(NonRetryableErrorException.class, ()->deltaProcessor.processDelta(chsDelta));
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenCorporateTransformerUnsuccessful() throws IOException {
        Message<ChsDelta> chsDelta = createChsDeltaMessageCorporateOfficer(false);

        when(objectMapper.readValue(chsDelta.getPayload().getData(), DisqualificationDelta.class)).thenReturn(buildExpectedDisqualificationDelta(CORPORATE_INDICATOR));
        when(transformer.transformCorporateDisqualification(any())).thenThrow(NullPointerException.class);

        assertThrows(NonRetryableErrorException.class, ()->deltaProcessor.processDelta(chsDelta));
    }

    @Test
    void shouldProcessValidDeleteDeltaAndSendToApiSuccessfully() throws IOException {
        Message<ChsDelta> chsDeleteDelta = createChsDeltaMessageNaturalOfficer(true);
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        DisqualificationDeleteDelta expectedDelta = createDisqualificationDeleteDelta();
        when(objectMapper.readValue(chsDeleteDelta.getPayload().getData(), DisqualificationDeleteDelta.class)).thenReturn(expectedDelta);

        when(apiClientService.deleteDisqualification(any(), any(), any(), any())).thenReturn(response);

        deltaProcessor.processDelete(chsDeleteDelta);

        verify(apiClientService).deleteDisqualification(any(), any(), any(), any());
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenUpsertDeserialisationFails() throws JsonProcessingException {
        Message<ChsDelta> invalidDelta = createInvalidChsDeltaMessage();
        when(objectMapper.readValue(invalidDelta.getPayload().getData(), DisqualificationDelta.class)).thenThrow(JsonProcessingException.class);
        assertThrows(NonRetryableErrorException.class, ()->deltaProcessor.processDelta(invalidDelta));
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenDeleteDeserialisationFails() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(DisqualificationDeleteDelta.class))).thenThrow(
                JsonProcessingException.class);
        Message<ChsDelta> mockChsDeltaMessage = createInvalidChsDeltaMessage();

        // when
        Executable executable = () -> deltaProcessor.processDelete(mockChsDeltaMessage);

        // then
        NonRetryableErrorException actual = assertThrows(NonRetryableErrorException.class, executable);
        assertEquals("Error deserialising disqualified-officers delete delta", actual.getMessage());
    }
}