package uk.gov.companieshouse.disqualifiedofficers.delta.exception;

public class RetryableErrorException extends RuntimeException {
    public RetryableErrorException(String message) {
        super(message);
    }
}