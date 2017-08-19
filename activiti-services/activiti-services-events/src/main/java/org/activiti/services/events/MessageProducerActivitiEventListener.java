package org.activiti.services.events;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.events.converter.EventConverterContext;
import org.springframework.beans.factory.annotation.Autowired;
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
        CommandContext currentCommandContext = Context.getCommandContext();
        ProcessEngineEvent newEvent = converterContext.from(event);
        if (newEvent == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<ProcessEngineEvent> events = (List<ProcessEngineEvent>) currentCommandContext
                                                                                          .getAttribute(MessageProducerCommandContextCloseListener.PROCESS_ENGINE_EVENTS);
        if (events != null) {
            events.add(newEvent);
        } else {
            events = new ArrayList<ProcessEngineEvent>();
            events.add(newEvent);
            currentCommandContext.addAttribute(MessageProducerCommandContextCloseListener.PROCESS_ENGINE_EVENTS,
                                               events);
        }

        List<CommandContextCloseListener> listeners = currentCommandContext.getCloseListeners();
        if (listeners.size() != 0) {
            for (CommandContextCloseListener listener : listeners) {
                if (listener instanceof MessageProducerCommandContextCloseListener) {
                    return;
                }
            }
        }
        MessageProducerCommandContextCloseListener messageListener = new MessageProducerCommandContextCloseListener(
                                                                                                                    producer);
        currentCommandContext.addCloseListener(messageListener);
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
