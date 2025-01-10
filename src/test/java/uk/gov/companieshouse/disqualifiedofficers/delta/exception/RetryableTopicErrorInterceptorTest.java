package uk.gov.companieshouse.disqualifiedofficers.delta.exception;


import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper.createRecord;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetryableTopicErrorInterceptorTest  {

    private RetryableTopicErrorInterceptor interceptor;

    @BeforeEach
    public void setUp(){
        interceptor = new RetryableTopicErrorInterceptor();
    }

    @Test
    void when_correct_topic_is_sent_record_is_unchanged() {
        ProducerRecord<String, Object> currentRecord = createRecord("topic", "header");
        ProducerRecord<String, Object> newRecord = interceptor.onSend(currentRecord);

        assertThat(newRecord).isEqualTo(currentRecord);
    }

    @Test
    void when_error_is_nonretryable_topic_is_set_to_invalid() {
        ProducerRecord<String, Object> currentRecord = createRecord("topic-error", NonRetryableErrorException.class.getName());
        ProducerRecord<String, Object> newRecord = interceptor.onSend(currentRecord);

        assertThat(newRecord.topic()).isEqualTo("topic-invalid");
    }

    @Test
    void when_error_is_retryable_topic_is_unchanged() {
        ProducerRecord<String, Object> currentRecord = createRecord("topic-error", RetryableErrorException.class.getName());
        ProducerRecord<String, Object> newRecord = interceptor.onSend(currentRecord);

        assertThat(newRecord.topic()).isEqualTo("topic-error");
    }
}
