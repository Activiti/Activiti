package org.activiti.services.history.app;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * Created by msalatino on 23/06/2017.
 */
public interface HistoryConsumerChannels {

    String HISTORY_CONSUMER = "historyConsumer";

    @Input(HISTORY_CONSUMER)
    SubscribableChannel historyConsumer();
}
