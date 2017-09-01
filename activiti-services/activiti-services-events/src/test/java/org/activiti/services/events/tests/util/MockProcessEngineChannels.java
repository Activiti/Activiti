package org.activiti.services.events.tests.util;

import org.activiti.services.events.ProcessEngineChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

@Component
public class MockProcessEngineChannels implements ProcessEngineChannels {

    @Override
    public SubscribableChannel commandConsumer() {
        return null;
    }

    @Override
    public MessageChannel commandResults() {
        return null;
    }

    @Override
    public MessageChannel auditProducer() {
        return new MockMessageChannel();
    }

}
