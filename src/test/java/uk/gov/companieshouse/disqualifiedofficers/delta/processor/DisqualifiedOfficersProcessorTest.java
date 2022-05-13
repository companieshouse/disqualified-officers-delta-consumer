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
import uk.gov.companieshouse.disqualifiedofficers.delta.service.api.ApiClientService;
import uk.gov.companieshouse.disqualifiedofficers.delta.transformer.DisqualifiedOfficersApiTransformer;
import uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DisqualifiedOfficersProcessorTest {

    private DisqualifiedOfficersDeltaProcessor deltaProcessor;
    private TestHelper testHelper = new TestHelper();

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
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage();
        DisqualificationDelta expectedDelta = testHelper.createDisqualificationDelta();
        InternalNaturalDisqualificationApi apiObject = testHelper.createDisqualificationApi();
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        when(apiClientService.putDisqualification(any(),any(), eq(apiObject))).thenReturn(response);
        when(transformer.transformNaturalDisqualification(expectedDelta)).thenReturn(apiObject);

        deltaProcessor.processDelta(mockChsDeltaMessage);

        verify(transformer).transformNaturalDisqualification(expectedDelta);
        verify(apiClientService).putDisqualification("context_id", "3002276133", apiObject);
    }
}