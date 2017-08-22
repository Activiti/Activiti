package org.activiti.services.events;

import java.util.List;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class MessageProducerCommandContextCloseListener implements CommandContextCloseListener {

    public static final String PROCESS_ENGINE_EVENTS = "processEngineEvents";

    private final ProcessEngineChannels producer;

    @Autowired
    public MessageProducerCommandContextCloseListener(ProcessEngineChannels producer) {
        this.producer = producer;
    }

    @Override
    public void closed(CommandContext commandContext) {
        CommandContext currentCommandContext = Context.getCommandContext();
        List<ProcessEngineEvent> events = currentCommandContext
                                                               .getGenericAttribute(PROCESS_ENGINE_EVENTS);
        if (events != null && events.size() != 0) {
            producer.auditProducer().send(MessageBuilder.withPayload(events.toArray(new ProcessEngineEvent[events
                                                                                                                 .size()]))
                                                        .build());
        }
    }

    @Override
    public void closing(CommandContext commandContext) {
        // No need to implement this method in this class
    }

    @Override
    public void afterSessionsFlush(CommandContext commandContext) {
        // No need to implement this method in this class
    }

    @Override
    public void closeFailure(CommandContext commandContext) {
        // No need to implement this method in this class
    }
}
