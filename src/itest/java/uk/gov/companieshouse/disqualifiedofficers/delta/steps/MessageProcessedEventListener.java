package uk.gov.companieshouse.disqualifiedofficers.delta.steps;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.disqualifiedofficers.delta.consumer.MessageProcessedEvent;

import java.util.concurrent.CountDownLatch;

@Component
public class MessageProcessedEventListener {

    private CountDownLatch latch = new CountDownLatch(1);

    @EventListener
    public void onMessageProcessed(MessageProcessedEvent event) {
        latch.countDown();
    }

    public void reset() {
        latch = new CountDownLatch(1);
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}