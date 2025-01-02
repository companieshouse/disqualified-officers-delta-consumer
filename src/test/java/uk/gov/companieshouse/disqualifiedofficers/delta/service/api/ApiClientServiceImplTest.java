package uk.gov.companieshouse.disqualifiedofficers.delta.service.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.handler.delta.disqualification.request.PrivateCorporateDisqualificationUpsert;
import uk.gov.companieshouse.api.handler.delta.disqualification.request.PrivateDisqualificationDelete;
import uk.gov.companieshouse.api.handler.delta.disqualification.request.PrivateNaturalDisqualificationUpsert;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficers.delta.processor.DisqualificationType;


@ExtendWith(MockitoExtension.class)
class ApiClientServiceImplTest {

    private static final String DELTA_AT = "20240925171003950844";

    @InjectMocks
    private ApiClientServiceImpl apiClientService;

    @Test
    void putNaturalDisqualification() {
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(),
                any(PrivateNaturalDisqualificationUpsert.class));

        ApiResponse<Void> response = apiClientServiceSpy.putNaturalDisqualification("context_id",
                "ZTgzYWQwODAzMGY1ZDNkNGZiOTAxOWQ1YzJkYzc5MWViMTE3ZjQxZA==",
                new InternalNaturalDisqualificationApi());
        verify(apiClientServiceSpy).executeOp(eq("putDisqualification"),
                any(PrivateNaturalDisqualificationUpsert.class));

        assertThat(response).isEqualTo(expectedResponse);

    }

    @Test
    void putCorporateDisqualification() {
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(),
                any(PrivateCorporateDisqualificationUpsert.class));

        ApiResponse<Void> response = apiClientServiceSpy.putCorporateDisqualification("context_id",
                "ZTgzYWQwODAzMGY1ZDNkNGZiOTAxOWQ1YzJkYzc5MWViMTE3ZjQxZA==",
                new InternalCorporateDisqualificationApi());
        verify(apiClientServiceSpy).executeOp(eq("putDisqualification"),
                any(PrivateCorporateDisqualificationUpsert.class));

        assertThat(response).isEqualTo(expectedResponse);

    }

    @Test
    void deleteDisqualification() {
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(),
                any(PrivateDisqualificationDelete.class));

        apiClientServiceSpy.deleteDisqualification(
                "context_id", "officer_id", DELTA_AT, DisqualificationType.NATURAL);

        verify(apiClientServiceSpy).executeOp(anyString(),
                any(PrivateDisqualificationDelete.class));
    }
}
