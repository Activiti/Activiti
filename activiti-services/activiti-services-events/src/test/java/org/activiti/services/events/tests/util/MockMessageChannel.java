package org.activiti.services.events.tests.util;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

public class MockMessageChannel implements MessageChannel {

    public static Message<?> messageResult;

    @Override
    public boolean send(Message<?> message) {
        return send(message, INDEFINITE_TIMEOUT);
    }

    @Override
    public boolean send(Message<?> message, long timeout) {
        messageResult = message;
        return false;
    }

}
