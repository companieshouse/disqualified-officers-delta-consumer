package uk.gov.companieshouse.disqualifiedofficers.delta.service.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficers.delta.logging.DataMapHolder;
import uk.gov.companieshouse.disqualifiedofficers.delta.processor.DisqualificationType;


/**
 * Service that sends REST requests via private SDK.
 */
@Primary
@Service
public class ApiClientServiceImpl extends BaseApiClientServiceImpl implements ApiClientService {

    private final String chsApiKey;
    private final String apiUrl;
    private final String internalApiUrl;

    public ApiClientServiceImpl(
            @Value("${api.disqualified-officers-data-api-key}")String chsApiKey,
            @Value("${api.api-url}")String apiUrl,
            @Value("${api.internal-api-url}")String internalApiUrl) {
        this.chsApiKey = chsApiKey;
        this.apiUrl = apiUrl;
        this.internalApiUrl = internalApiUrl;
    }

    @Override
    public InternalApiClient getApiClient(final String contextId) {
        InternalApiClient internalApiClient = new InternalApiClient(getHttpClient(contextId));
        internalApiClient.setBasePath(apiUrl);
        internalApiClient.setInternalBasePath(internalApiUrl);
        return internalApiClient;
    }

    private HttpClient getHttpClient(final String contextId) {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(chsApiKey);
        httpClient.setRequestId(contextId);
        return httpClient;
    }

    @Override
    public ApiResponse<Void> putNaturalDisqualification(final String contextId, final String officerId,
                                                        InternalNaturalDisqualificationApi internalDisqualificationApi) {
        final String uri = String.format("/disqualified-officers/natural/%s/internal", officerId);
        DataMapHolder.get().resourceUri(uri);
        return executeOp(getApiClient(contextId).privateDisqualificationResourceHandler()
                        .putDisqualification()
                        .upsert(uri, internalDisqualificationApi));
    }

    @Override
    public ApiResponse<Void> putCorporateDisqualification(final String contextId, final String officerId,
            InternalCorporateDisqualificationApi internalDisqualificationApi) {
        final String uri = String.format("/disqualified-officers/corporate/%s/internal", officerId);
        DataMapHolder.get().resourceUri(uri);
        return executeOp(getApiClient(contextId).privateDisqualificationResourceHandler()
                        .putDisqualification()
                        .upsert(uri, internalDisqualificationApi));
    }

    @Override
    public ApiResponse<Void> deleteDisqualification(final String contextId, final String officerId,
            final String deltaAt, DisqualificationType type) {
        final String uri = "/disqualified-officers/%s/%s/internal".formatted(type.getTypeAsString(), officerId);
        DataMapHolder.get().resourceUri(uri);
        DataMapHolder.get().officerType(type.getTypeAsString());
        return executeOp(
                getApiClient(contextId).privateDisqualificationResourceHandler()
                        .deleteDisqualification(uri, deltaAt));
    }
}