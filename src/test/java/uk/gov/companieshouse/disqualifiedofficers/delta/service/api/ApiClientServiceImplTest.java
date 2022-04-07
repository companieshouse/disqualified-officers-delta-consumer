package uk.gov.companieshouse.disqualifiedofficers.delta.service.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.disqualification.request.PrivateNaturalDisqualificationUpsert;
import uk.gov.companieshouse.api.handler.delta.disqualification.request.PrivateCorporateDisqualificationUpsert;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ApiClientServiceImplTest {

    @Mock
    Logger logger;

    private ApiClientServiceImpl apiClientService;

    @BeforeEach
    void setup() {
        apiClientService = new ApiClientServiceImpl(logger);
        ReflectionTestUtils.setField(apiClientService, "chsApiKey", "apiKey");
        ReflectionTestUtils.setField(apiClientService, "apiUrl", "https://api.companieshouse.gov.uk");
    }

    @Test
    void putNaturalDisqualification() throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(), anyString(),
                anyString(),
                any(PrivateNaturalDisqualificationUpsert.class));

        ApiResponse<Void> response = apiClientServiceSpy.putDisqualification("context_id",
                "ZTgzYWQwODAzMGY1ZDNkNGZiOTAxOWQ1YzJkYzc5MWViMTE3ZjQxZA==",
                new InternalNaturalDisqualificationApi());
        verify(apiClientServiceSpy).executeOp(anyString(), eq("putDisqualification"),
                eq("/disqualified-officers/natural/" +
                        "ZTgzYWQwODAzMGY1ZDNkNGZiOTAxOWQ1YzJkYzc5MWViMTE3ZjQxZA==/internal"),
                any(PrivateNaturalDisqualificationUpsert.class));

        assertThat(response).isEqualTo(expectedResponse);

    }

    @Test
    void putCorporateDisqualification() throws ApiErrorResponseException, URIValidationException {
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(), anyString(),
                anyString(),
                any(PrivateCorporateDisqualificationUpsert.class));

        ApiResponse<Void> response = apiClientServiceSpy.putDisqualification("context_id",
                "ZTgzYWQwODAzMGY1ZDNkNGZiOTAxOWQ1YzJkYzc5MWViMTE3ZjQxZA==",
                new InternalCorporateDisqualificationApi());
        verify(apiClientServiceSpy).executeOp(anyString(), eq("putDisqualification"),
                eq("/disqualified-officers/corporate/" +
                        "ZTgzYWQwODAzMGY1ZDNkNGZiOTAxOWQ1YzJkYzc5MWViMTE3ZjQxZA==/internal"),
                any(PrivateCorporateDisqualificationUpsert.class));

        assertThat(response).isEqualTo(expectedResponse);

    }

}
