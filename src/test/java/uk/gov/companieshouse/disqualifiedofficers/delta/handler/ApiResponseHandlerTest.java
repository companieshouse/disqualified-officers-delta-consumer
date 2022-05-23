package uk.gov.companieshouse.disqualifiedofficers.delta.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.disqualifiedofficers.delta.exception.RetryableErrorException;
import uk.gov.companieshouse.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.verify;
import static org.junit.Assert.assertThrows;


@ExtendWith(MockitoExtension.class)
class ApiResponseHandlerTest {

    private ApiResponseHandler apiResponseHandler = new ApiResponseHandler();

    @Mock
    private Logger logger;

    @Test
    void handle200Response() throws NonRetryableErrorException, RetryableErrorException {
        HttpStatus httpStatus = HttpStatus.OK;
        Map<String, Object> logMap = new HashMap<>();

        apiResponseHandler.handleResponse(
                httpStatus,"status", logMap, logger );
        verify(logger).debugContext("status", "Response received from disqualified-officers-data-api", logMap);
    }

    @Test
    void handleBadResponse() throws NonRetryableErrorException, RetryableErrorException {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        Map<String, Object> logMap = new HashMap<>();
        assertThrows(NonRetryableErrorException.class, () -> apiResponseHandler.handleResponse(
                httpStatus, "status", logMap, logger));
        verify(logger).errorContext("status",
                "400 BAD_REQUEST response received from disqualified-officers-data-api", null, logMap);
    }

    @Test
    void handle404Response() throws NonRetryableErrorException, RetryableErrorException {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        Map<String, Object> logMap = new HashMap<>();
        assertThrows(RetryableErrorException.class, () -> apiResponseHandler.handleResponse(
                httpStatus, "status", logMap, logger));
        verify(logger).errorContext("status",
                "Non-Successful response received from disqualified-officers-data-api, retry", null, logMap);
    }

    @Test
    void handle500Response() throws NonRetryableErrorException, RetryableErrorException {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> logMap = new HashMap<>();
        assertThrows(RetryableErrorException.class, () -> apiResponseHandler.handleResponse(
                httpStatus, "status", logMap, logger));
        verify(logger).errorContext("status",
                "Non-Successful response received from disqualified-officers-data-api, retry", null, logMap);
    }
}
