package org.activiti.starter.tests.cmdendpoint;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface MessageClientStream {

    String MY_CMD_PRODUCER = "myCmdProducer";
    String MY_CMD_RESULTS = "myCmdResults";

    @Output(MY_CMD_PRODUCER)
    MessageChannel myCmdProducer();

    @Input(MY_CMD_RESULTS)
    SubscribableChannel myCmdResults();
}
