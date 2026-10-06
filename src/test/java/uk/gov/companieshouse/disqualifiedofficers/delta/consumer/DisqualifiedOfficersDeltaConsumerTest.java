package uk.gov.companieshouse.disqualifiedofficers.delta.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.disqualifiedofficers.delta.processor.DisqualifiedOfficersDeltaProcessor;
import org.springframework.messaging.Message;

@ExtendWith(MockitoExtension.class)
class DisqualifiedOfficersDeltaConsumerTest {
    @Mock
    private DisqualifiedOfficersDeltaProcessor deltaProcessor;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private DisqualifiedOfficersDeltaConsumer consumer;

    @Mock
    private Message<ChsDelta> message;

    @Mock
    private ChsDelta chsDelta;

    @BeforeEach
    void setUp() {
        when(message.getPayload()).thenReturn(chsDelta);
    }

    @Test
    void whenMessageReceivedIsADelete_thenTheDeltaProcessorProcessesADelete() {
        when(chsDelta.getIsDelete()).thenReturn(true);

        consumer.receiveMainMessages(message, "test topic");

        verify(deltaProcessor).processDelete(message);
        verify(deltaProcessor, never()).processDelta(any());
    }

    @Test
    void whenMessageReceivedIsNotADelete_thenTheDeltaProcessorProcessesADeltA() {
        when(chsDelta.getIsDelete()).thenReturn(false);

        consumer.receiveMainMessages(message, "test topic");

        verify(deltaProcessor, never()).processDelete(message);
        verify(deltaProcessor).processDelta(any());
    }
}
