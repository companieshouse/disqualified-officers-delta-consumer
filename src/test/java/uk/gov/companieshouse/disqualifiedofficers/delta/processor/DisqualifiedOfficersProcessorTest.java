package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.api.delta.*;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.mapper.MapperUtils;
import uk.gov.companieshouse.disqualifiedofficers.delta.service.api.ApiClientService;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;
import uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisqualifiedOfficersProcessorTest {

    private DisqualifiedOfficersDeltaProcessor deltaProcessor;
    private final TestHelper testHelper = new TestHelper();

    @Mock
    private DisqualifiedOfficersApiTransformer transformer;
    @Mock
    private Logger logger;
    @Mock
    private ApiClientService apiClientService;


    @BeforeEach
    void setUp() {
        deltaProcessor = new DisqualifiedOfficersDeltaProcessor(
                transformer,
                logger,
                apiClientService
        );
    }

    @Test
    @DisplayName("Transforms a kafka message containing a ChsDelta payload into a DisqualificationDelta")
    void When_ValidChsDeltaMessage_Expect_ValidDisqualificationDeltaMapping() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(false);
        DisqualificationDelta expectedDelta = testHelper.createDisqualificationDelta();
        InternalNaturalDisqualificationApi apiObject = testHelper.createDisqualificationApi();
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        when(apiClientService.putDisqualification(any(),any(), eq(apiObject))).thenReturn(response);
        when(transformer.transformNaturalDisqualification(expectedDelta)).thenReturn(apiObject);

        deltaProcessor.processDelta(mockChsDeltaMessage);

        verify(transformer).transformNaturalDisqualification(expectedDelta);
        verify(apiClientService).putDisqualification("context_id", "3002276133", apiObject);
    }

    @Test
    void When_InvalidChsDeltaMessage_Expect_NonRetryableError() {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();
        assertThrows(NonRetryableErrorException.class, ()->deltaProcessor.processDelta(mockChsDeltaMessage));
    }

    @Test
    void When_ApiReturns500_Expect_RetryableError() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(false);
        InternalNaturalDisqualificationApi apiObject = testHelper.createDisqualificationApi();
        when(apiClientService.putDisqualification(any(),any(), eq(apiObject))).thenThrow(new RetryableErrorException(""));
        when(transformer.transformNaturalDisqualification(any())).thenReturn(apiObject);
        assertThrows(RetryableErrorException.class, ()->deltaProcessor.processDelta(mockChsDeltaMessage));
    }

    @Test
    void When_Transformer_Fails_Expect_RetryableError() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createBrokenChsDeltaMessage();
        when(transformer.transformNaturalDisqualification(any())).thenCallRealMethod();
        assertThrows(RetryableErrorException.class, ()->deltaProcessor.processDelta(mockChsDeltaMessage));
    }

    @Test
    void When_Valid_Delete_Message_Received_Delete_Endpoint_is_Called() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(true);
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        String encodedId = MapperUtils.encode("1234567890");

        when(apiClientService.deleteDisqualification(eq("context_id"), eq(encodedId), anyString())).thenReturn(response);

        deltaProcessor.processDelete(mockChsDeltaMessage);

        verify(apiClientService).deleteDisqualification(eq("context_id"), eq(encodedId), anyString());
    }

    @Test
    void When_InvalidDeleteMessage_Expect_NonRetryableError() {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();
        assertThrows(NonRetryableErrorException.class, ()->deltaProcessor.processDelete(mockChsDeltaMessage));
    }
}