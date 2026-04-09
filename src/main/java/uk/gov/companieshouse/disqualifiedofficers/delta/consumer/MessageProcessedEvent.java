package uk.gov.companieshouse.disqualifiedofficers.delta.consumer;

import org.springframework.context.ApplicationEvent;

public class MessageProcessedEvent extends ApplicationEvent {

    public MessageProcessedEvent(Object source) {
        super(source);
    }
}