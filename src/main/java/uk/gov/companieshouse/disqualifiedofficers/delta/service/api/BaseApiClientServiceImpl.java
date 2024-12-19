package uk.gov.companieshouse.disqualifiedofficers.delta.service.api;

import static uk.gov.companieshouse.disqualifiedofficers.delta.DisqualifiedOfficersDeltaConsumerApplication.NAMESPACE;

import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.Executor;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import java.util.Arrays;

public abstract class BaseApiClientServiceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    /**
     * General execution of an SDK endpoint.
     *
     * @param <T>           type of api response
     * @param operationName name of operation
     * @param executor      executor to use
     * @return the response object
     */
    public <T> ApiResponse<T> executeOp(final String operationName,
                                        final Executor<ApiResponse<T>> executor) {
        try {

            return executor.execute();

        } catch (URIValidationException ex) {
            String msg = "404 NOT_FOUND response received from disqualified-officers-data-api";
            LOGGER.error(msg, ex, DataMapHolder.getLogMap());

            throw new RetryableErrorException(msg, ex);
        } catch (ApiErrorResponseException ex) {
            DataMapHolder.get().status(String.valueOf(ex.getStatusCode()));

            int status = ex.getStatusCode();
            if (status == HttpStatus.BAD_REQUEST.value()
                    || status == HttpStatus.CONFLICT.value()) {
                // 400 BAD REQUEST or 409 CONFLICT status cannot be retried
                String msg = String.format(
                        "Non-retryable response %s received from disqualified-officers-data-api",
                        status);
                LOGGER.error(msg, ex, DataMapHolder.getLogMap());
                throw new NonRetryableErrorException(msg, ex);
                //TODO the below is not required as we will never get 404 back from data api in delete case, will in upsert case.
            } else if (status == HttpStatus.NOT_FOUND.value()
                    && operationName.equals("deleteDisqualification")) {
                String msg = String.format(
                        "Retryable response %s received from disqualified-officers-data-api",
                        status);
                throw new RetryableErrorException(msg);
            }

            // any other client or server status is retryable
            String msg = String.format(
                    "Non-Successful response %s received from disqualified-officers-data-api",
                    status);
            LOGGER.info(msg + ", retry. "
                    + Arrays.toString(ex.getStackTrace()), DataMapHolder.getLogMap());
            throw new RetryableErrorException(msg, ex);
        }
    }
}
