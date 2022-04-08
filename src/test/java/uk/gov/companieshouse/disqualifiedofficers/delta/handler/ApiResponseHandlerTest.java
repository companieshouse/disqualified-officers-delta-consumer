package uk.gov.companieshouse.disqualifiedofficers.delta.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
    @Mock
    ResponseStatusException ex;

    @Test
    void handle200Response() throws NonRetryableErrorException, RetryableErrorException {
        HttpStatus httpStatus = HttpStatus.OK;
        Map<String, Object> logMap = new HashMap<>();

        apiResponseHandler.handleResponse(
                ex, httpStatus,"status", "testy test test", logMap, logger );
        verify(logger).debugContext("status", "testy test test", logMap);
    }

    @Test
    void handleBadResponse() throws NonRetryableErrorException, RetryableErrorException {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        Map<String, Object> logMap = new HashMap<>();
        assertThrows(NonRetryableErrorException.class, () -> apiResponseHandler.handleResponse(
                ex, httpStatus, "status", "testy test test", logMap, logger));
        verify(logger).errorContext("status", "testy test test", null, logMap);
    }

    @Test
    void handle500Response() throws NonRetryableErrorException, RetryableErrorException {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> logMap = new HashMap<>();
        assertThrows(RetryableErrorException.class, () -> apiResponseHandler.handleResponse(
                ex, httpStatus, "status", "testy test test", logMap, logger));
        verify(logger).errorContext("status", "testy test test, retry", null, logMap);
    }
}
