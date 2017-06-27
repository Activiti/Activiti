package org.activiti.services.audit.producer.app;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 */
@Component
public class MessageProducerActivitiEventListener implements ActivitiEventListener {

    @Autowired
    private AuditProducerChannels producer;

    @Override
    public void onEvent(ActivitiEvent event) {
        producer.auditProducer().send(MessageBuilder.withPayload(event.toString()).build());
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
