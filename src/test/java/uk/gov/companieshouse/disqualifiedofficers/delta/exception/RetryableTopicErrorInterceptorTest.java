package uk.gov.companieshouse.disqualifiedofficers.delta.exception;


import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.disqualifiedofficers.delta.utils.TestHelper;

import static org.assertj.core.api.Assertions.assertThat;

public class RetryableTopicErrorInterceptorTest  {

    private RetryableTopicErrorInterceptor interceptor;

    @BeforeEach
    public void setUp(){
        interceptor = new RetryableTopicErrorInterceptor();
    }

    @Test
    void when_correct_topic_is_sent_record_is_unchanged() {
        TestHelper testHelper = new TestHelper();
        ProducerRecord<String, Object> record = testHelper.createRecord("topic", "header");
        ProducerRecord<String, Object> newRecord = interceptor.onSend(record);

        assertThat(newRecord).isEqualTo(record);
    }

    @Test
    void when_error_is_nonretryable_topic_is_set_to_invalid() {
        TestHelper testHelper = new TestHelper();
        ProducerRecord<String, Object> record = testHelper.createRecord("topic-error", NonRetryableErrorException.class.getName());
        ProducerRecord<String, Object> newRecord = interceptor.onSend(record);

        assertThat(newRecord.topic()).isEqualTo("topic-invalid");
    }
}
