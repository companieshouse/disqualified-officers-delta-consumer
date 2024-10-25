package uk.gov.companieshouse.disqualifiedofficers.delta.service.api;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;


/**
 * Service that sends REST requests via private SDK.
 */
@Primary
@Service
public class ApiClientServiceImpl extends BaseApiClientServiceImpl implements ApiClientService {

    @Value("${api.disqualified-officers-data-api-key}")
    private String chsApiKey;

    @Value("${api.api-url}")
    private String apiUrl;

    @Value("${api.internal-api-url}")
    private String internalApiUrl;

    /**
     * Construct an {@link ApiClientServiceImpl}.
     *
     * @param logger the CH logger
     */
    @Autowired
    public ApiClientServiceImpl(final Logger logger) {
        super(logger);
    }

    @Override
    public InternalApiClient getApiClient(String contextId) {
        InternalApiClient internalApiClient = new InternalApiClient(getHttpClient(contextId));
        internalApiClient.setBasePath(apiUrl);
        internalApiClient.setInternalBasePath(internalApiUrl);
        return internalApiClient;
    }

    private HttpClient getHttpClient(String contextId) {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(chsApiKey);
        httpClient.setRequestId(contextId);
        return httpClient;
    }

    @Override
    public ApiResponse<Void> putDisqualification(final String log, final String officerId,
                            InternalNaturalDisqualificationApi internalDisqualificationApi) {
        final String uri = String.format("/disqualified-officers/natural/%s/internal", officerId);

        Map<String, Object> logMap = createLogMap(officerId, "PUT", uri);
        logger.infoContext(log, String.format("PUT %s", uri), logMap);

        return executeOp(log, "putDisqualification", uri,
                getApiClient(log).privateDisqualificationResourceHandler()
                        .putDisqualification()
                        .upsert(uri, internalDisqualificationApi));
    }

    @Override
    public ApiResponse<Void> putDisqualification(final String log, final String officerId,
                            InternalCorporateDisqualificationApi internalDisqualificationApi) {
        final String uri = String.format("/disqualified-officers/corporate/%s/internal", officerId);

        Map<String, Object> logMap = createLogMap(officerId, "PUT", uri);
        logger.infoContext(log, String.format("PUT %s", uri), logMap);

        return executeOp(log, "putDisqualification", uri,
                getApiClient(log).privateDisqualificationResourceHandler()
                        .putDisqualification()
                        .upsert(uri, internalDisqualificationApi));
    }

    @Override
    public ApiResponse<Void> deleteDisqualification(
            final String log,
            final String officerId,
            final String deltaAt) {
        final String uri =
                String.format("/disqualified-officers/delete/%s/internal", officerId);

        Map<String,Object> logMap = createLogMap(officerId,"DELETE", uri);
        logger.infoContext(log, String.format("DELETE %s", uri), logMap);

        return executeOp(log, "deleteDisqualification", uri,
                getApiClient(log).privateDisqualificationResourceHandler()
                        .deleteDisqualification(uri, deltaAt));
    }

    private Map<String, Object> createLogMap(String officerId, String method, String path) {
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("officer_id", officerId);
        logMap.put("method", method);
        logMap.put("path", path);
        return logMap;
    }
}