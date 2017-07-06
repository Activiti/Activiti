package org.activiti.services.query.app;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface QueryConsumerChannels {

    String QUERY_CONSUMER = "queryConsumer";

    @Input(QUERY_CONSUMER)
    SubscribableChannel queryConsumer();
}
