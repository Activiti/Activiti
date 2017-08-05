package org.activiti.services.events;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.services.events.converter.EventConverterContext;
import org.activiti.services.core.model.events.ProcessEngineEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class MessageProducerActivitiEventListener implements ActivitiEventListener {

    private final ProcessEngineChannels producer;

    private final EventConverterContext converterContext;

    @Autowired
    public MessageProducerActivitiEventListener(ProcessEngineChannels producer,
                                                EventConverterContext converterContext) {
        this.producer = producer;
        this.converterContext = converterContext;
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        ProcessEngineEvent newEvent = converterContext.from(event);
        if (newEvent != null) {
            producer.auditProducer().send(MessageBuilder.withPayload(newEvent).build());
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
