package uk.gov.companieshouse.disqualifiedofficers.delta.service.api;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.model.ApiResponse;

/**
 * The {@code ApiClientService} interface provides an abstraction that can be
 * used when testing {@code ApiClientManager} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface ApiClientService {

    InternalApiClient getApiClient(String contextId);

    /**
     * Submit disqualification.
     */
    ApiResponse<Void> putDisqualification(
            final String log,
            final String officerId,
            final InternalNaturalDisqualificationApi internalDisqualificationApi);

    ApiResponse<Void> putDisqualification(
            final String log,
            final String officerId,
            final InternalCorporateDisqualificationApi internalDisqualificationApi);

    /**
     * Delete disqualification.
     */
    ApiResponse<Void> deleteDisqualification(final String log, final String officerId, final String deltaAt);
}