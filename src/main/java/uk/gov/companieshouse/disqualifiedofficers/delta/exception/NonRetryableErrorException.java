package uk.gov.companieshouse.disqualifiedofficers.delta.exception;

public class NonRetryableErrorException extends RuntimeException {
    public NonRetryableErrorException(String message) {
        super(message);
    }
}