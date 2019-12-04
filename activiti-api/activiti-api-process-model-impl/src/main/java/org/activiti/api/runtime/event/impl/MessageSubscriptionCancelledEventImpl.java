package org.activiti.api.runtime.event.impl;

import org.activiti.api.process.model.MessageSubscription;
import org.activiti.api.process.model.events.MessageSubscriptionCancelledEvent;
import org.activiti.api.process.model.events.MessageSubscriptionEvent;

public class MessageSubscriptionCancelledEventImpl extends RuntimeEventImpl<MessageSubscription, MessageSubscriptionEvent.MessageSubscriptionEvents> 
                                                   implements MessageSubscriptionCancelledEvent {

    public MessageSubscriptionCancelledEventImpl() {
    }

    public MessageSubscriptionCancelledEventImpl(MessageSubscription entity) {
        super(entity);
        
        setProcessInstanceId(entity.getProcessInstanceId());
        setProcessDefinitionId(entity.getProcessDefinitionId());
    }

    @Override
    public String toString() {
        return "MessageSubscriptionCancelledEventImpl {" + super.toString() + "}";
    }
}
