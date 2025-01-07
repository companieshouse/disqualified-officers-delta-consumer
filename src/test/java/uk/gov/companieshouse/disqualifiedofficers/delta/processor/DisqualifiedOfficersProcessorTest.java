package uk.gov.companieshouse.disqualifiedofficers.delta.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.service.api.ApiClientService;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;
import uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper;
import java.io.IOException;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class DisqualifiedOfficersProcessorTest {

    private DisqualifiedOfficersDeltaProcessor deltaProcessor;
    private final TestHelper testHelper = new TestHelper();

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
    @DisplayName("Transforms a kafka message containing a ChsDelta payload into a DisqualificationDelta")
    void When_ValidChsDeltaMessage_Expect_ValidDisqualificationDeltaMapping() throws IOException {
        Message<ChsDelta> chsDelta = testHelper.createChsDeltaMessage(false);
        DisqualificationDelta expectedDelta = testHelper.createDisqualificationDelta();
        InternalNaturalDisqualificationApi apiObject = testHelper.createDisqualificationApi();
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        when(apiClientService.putNaturalDisqualification(any(),any(), eq(apiObject))).thenReturn(response);
        when(transformer.transformNaturalDisqualification(expectedDelta)).thenReturn(apiObject);
        when(objectMapper.readValue(chsDelta.getPayload().getData(), DisqualificationDelta.class)).thenReturn(expectedDelta);

        deltaProcessor.processDelta(chsDelta);

        verify(transformer).transformNaturalDisqualification(expectedDelta);
        verify(apiClientService).putNaturalDisqualification("context_id", "3002276133", apiObject);
    }

    @Test
    void When_InvalidChsDeltaMessage_Expect_NonRetryableError() throws JsonProcessingException {
        Message<ChsDelta> invalidDelta = testHelper.createInvalidChsDeltaMessage();
        when(objectMapper.readValue(invalidDelta.getPayload().getData(), DisqualificationDelta.class)).thenThrow(JsonProcessingException.class);
        assertThrows(NonRetryableErrorException.class, ()->deltaProcessor.processDelta(invalidDelta));
    }

    @Test
    void When_ApiReturns500_Expect_RetryableError() throws IOException {
        //TODO change the order of the stubbing to reflect actual flow.
        Message<ChsDelta> chsDelta = testHelper.createChsDeltaMessage(false);
        InternalNaturalDisqualificationApi apiObject = testHelper.createDisqualificationApi();
        when(apiClientService.putNaturalDisqualification(any(),any(), eq(apiObject))).thenThrow(new RetryableErrorException(""));
        when(objectMapper.readValue(chsDelta.getPayload().getData(), DisqualificationDelta.class)).thenReturn(testHelper.createDisqualificationDelta());
        when(transformer.transformNaturalDisqualification(any())).thenReturn(apiObject);
        assertThrows(RetryableErrorException.class, ()->deltaProcessor.processDelta(chsDelta));
    }

    @Test
    void WhenDisqualificationsTransformerFailsExpectNonRetryableError() throws IOException {
        //TODO this should test the catch block within one of the transformer test cases - need to test both failing catch blocks.
        Message<ChsDelta> delta = testHelper.createDeltaWithoutDisqualification();
        when(objectMapper.readValue(delta.getPayload().getData(), DisqualificationDelta.class)).thenThrow(NoSuchElementException.class);
        assertThrows(NonRetryableErrorException.class, ()->deltaProcessor.processDelta(delta));
    }

    //TODO should be a corporate officer positive test case as well. So make corporate ind 1 to get it to work.
    @Test
    void When_Valid_Delete_Message_Received_Delete_Endpoint_is_Called() throws IOException {
        Message<ChsDelta> chsDeleteDelta = testHelper.createChsDeltaMessage(true);
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        DisqualificationDeleteDelta expectedDelta = testHelper.createDisqualificationDeleteDelta();
        when(objectMapper.readValue(chsDeleteDelta.getPayload().getData(), DisqualificationDeleteDelta.class)).thenReturn(expectedDelta);

        when(apiClientService.deleteDisqualification(any(), any(), any(), any())).thenReturn(response);

        deltaProcessor.processDelete(chsDeleteDelta);

        verify(apiClientService).deleteDisqualification(any(), any(), any(), any());
    }

    @Test
    void When_InvalidDeleteMessage_Expect_NonRetryableError() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(anyString(), eq(DisqualificationDeleteDelta.class))).thenThrow(
                JsonProcessingException.class);
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();

        // when
        Executable executable = () -> deltaProcessor.processDelete(mockChsDeltaMessage);

        // then
        NonRetryableErrorException actual = assertThrows(NonRetryableErrorException.class, executable);
        assertEquals("Error deserialising disqualified-officers delete delta", actual.getMessage());
    }
}