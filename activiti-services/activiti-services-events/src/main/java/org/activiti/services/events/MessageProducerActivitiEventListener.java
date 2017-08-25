package org.activiti.services.events;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.events.converter.EventConverterContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageProducerActivitiEventListener implements ActivitiEventListener {

    private final EventConverterContext converterContext;

    private final MessageProducerCommandContextCloseListener messageListener;

    @Autowired
    public MessageProducerActivitiEventListener(EventConverterContext converterContext,
                                                MessageProducerCommandContextCloseListener messageListener) {
        this.converterContext = converterContext;
        this.messageListener = messageListener;
    }

    @Override
    public void onEvent(ActivitiEvent event) {
        CommandContext currentCommandContext = Context.getCommandContext();
        ProcessEngineEvent newEvent = converterContext.from(event);
        if (newEvent == null) {
            return;
        }

        List<ProcessEngineEvent> events = currentCommandContext.getGenericAttribute(MessageProducerCommandContextCloseListener.PROCESS_ENGINE_EVENTS);
        if (events != null) {
            events.add(newEvent);
        } else {
            events = new ArrayList<>();
            events.add(newEvent);
            currentCommandContext.addAttribute(MessageProducerCommandContextCloseListener.PROCESS_ENGINE_EVENTS,
                                               events);
        }

        if (!currentCommandContext.hasCloseListener(MessageProducerCommandContextCloseListener.class)) {
            currentCommandContext.addCloseListener(messageListener);
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
