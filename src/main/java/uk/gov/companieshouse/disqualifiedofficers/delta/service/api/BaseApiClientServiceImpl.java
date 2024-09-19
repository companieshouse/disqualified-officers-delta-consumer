package uk.gov.companieshouse.disqualifiedofficers.delta.service.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.Executor;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.logging.Logger;

public abstract class BaseApiClientServiceImpl {
    protected Logger logger;

    protected BaseApiClientServiceImpl(final Logger logger) {
        this.logger = logger;
    }

    /**
     * General execution of an SDK endpoint.
     *
     * @param <T>           type of api response
     * @param logContext    context ID for logging
     * @param operationName name of operation
     * @param uri           uri of sdk being called
     * @param executor      executor to use
     * @return the response object
     */
    public <T> ApiResponse<T> executeOp(final String logContext,
                                        final String operationName,
                                        final String uri,
                                        final Executor<ApiResponse<T>> executor) {

        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("operation_name", operationName);
        logMap.put("path", uri);

        try {

            return executor.execute();

        } catch (URIValidationException ex) {
            String msg = "404 NOT_FOUND response received from disqualified-officers-data-api";
            logger.errorContext(logContext, msg, ex, logMap);

            throw new RetryableErrorException(msg, ex);
        } catch (ApiErrorResponseException ex) {
            logMap.put("status", ex.getStatusCode());

            int status = ex.getStatusCode();

            if (status == HttpStatus.BAD_REQUEST.value()
                    || status == HttpStatus.CONFLICT.value()) {
                // 400 BAD REQUEST status cannot be retried
                String msg = String.format(
                        "Non-retryable response %s received from disqualified-officers-data-api",
                        status);
                logger.errorContext(logContext, msg, ex, logMap);
                throw new NonRetryableErrorException(msg, ex);
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
            logger.infoContext(logContext, msg + ", retry. "
                            + Arrays.toString(ex.getStackTrace()), logMap);
            throw new RetryableErrorException(msg, ex);
        }
    }
}
