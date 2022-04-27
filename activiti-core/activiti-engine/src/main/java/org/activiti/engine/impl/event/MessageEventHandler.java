/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.activiti.engine.impl.event;

import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


public class MessageEventHandler extends AbstractEventHandler {

    public final static String EVENT_HANDLER_TYPE = "message";

    private final EventSubscriptionPayloadMappingProvider messageEventVariableMappingProvider;

    public MessageEventHandler(EventSubscriptionPayloadMappingProvider messageEventVariableMappingProvider) {
        this.messageEventVariableMappingProvider = messageEventVariableMappingProvider;
    }

    public String getEventHandlerType() {
        return EVENT_HANDLER_TYPE;
    }

    @Override
    public void handleEvent(EventSubscriptionEntity eventSubscription, Object payload, CommandContext commandContext) {
        // As stated in the ActivitiEventType java-doc, the message-event is
        // thrown before the actual message has been sent
        if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
            ExecutionEntity execution = eventSubscription.getExecution();
            String messageName = eventSubscription.getEventName();
            String correlationKey = eventSubscription.getConfiguration();

            commandContext.getProcessEngineConfiguration()
                          .getEventDispatcher()
                          .dispatchEvent(
                                         ActivitiEventBuilder.createMessageReceivedEvent(execution,
                                                                                         messageName,
                                                                                         correlationKey,
                                                                                         payload));
        }

        Object variables = messageEventVariableMappingProvider.apply(payload,
                                                                     eventSubscription);

        super.handleEvent(eventSubscription, variables, commandContext);
    }

}
