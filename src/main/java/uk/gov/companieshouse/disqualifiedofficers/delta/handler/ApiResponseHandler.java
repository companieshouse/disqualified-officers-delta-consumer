package uk.gov.companieshouse.disqualifiedofficers.delta.handler;

import java.util.Map;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.logging.Logger;

public class ApiResponseHandler {

    /**
     * Handle responses by logging result and throwing any exceptions.
     */
    public void handleResponse(
            final HttpStatus httpStatus,
            final String logContext,
            final Map<String, Object> logMap,
            Logger logger)
            throws NonRetryableErrorException, RetryableErrorException {
        logMap.put("status", httpStatus.toString());
        if (HttpStatus.BAD_REQUEST == httpStatus) {
            // 400 BAD REQUEST status cannot be retried
            String msg = "400 BAD_REQUEST response received from disqualified-officers-data-api";
            logger.errorContext(logContext, msg, null, logMap);
            throw new NonRetryableErrorException(msg);
        } else if (!httpStatus.is2xxSuccessful()) {
            // any other client or server status can be retried
            String msg = "Non-Successful 200 response received from disqualified-officers-data-api";
            logger.errorContext(logContext, msg + ", retry", null, logMap);
            throw new RetryableErrorException(msg);
        } else {
            logger.debugContext(logContext,
                    "Response received from disqualified-officers-data-api", logMap);
        }
    }
}
