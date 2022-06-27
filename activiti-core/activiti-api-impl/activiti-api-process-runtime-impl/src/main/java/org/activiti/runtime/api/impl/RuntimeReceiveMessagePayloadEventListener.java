/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.runtime.api.impl;

import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.runtime.api.message.ReceiveMessagePayloadEventListener;

import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of SignalPayloadEventListener that delegates
 * Spring SignalPayload event into embedded RuntimeService.
 *
 */
public class RuntimeReceiveMessagePayloadEventListener implements ReceiveMessagePayloadEventListener {

    private final RuntimeService runtimeService;

    private final ManagementService managementService;

    public RuntimeReceiveMessagePayloadEventListener(RuntimeService runtimeService,
                                                     ManagementService managementService) {
        this.runtimeService = runtimeService;
        this.managementService = managementService;
    }

    @Override
    public void receiveMessage(ReceiveMessagePayload messagePayload) {
        String messageName = messagePayload.getName();
        String correlationKey = messagePayload.getCorrelationKey();

        EventSubscriptionEntity subscription = managementService.executeCommand(new FindMessageEventSubscription(messageName,
                                                                                                                 correlationKey));
        if (subscription != null && Objects.equals(correlationKey, subscription.getConfiguration())) {
            Map<String, Object> variables = messagePayload.getVariables();
            String executionId = subscription.getExecutionId();

            runtimeService.messageEventReceived(messageName,
                                                executionId,
                                                variables);
        } else {
            throw new ActivitiObjectNotFoundException("Message subscription name '" + messageName + "' with correlation key '" + correlationKey + "' not found.");
        }
    }

    static class FindMessageEventSubscription implements Command<EventSubscriptionEntity> {

        private final String messageName;
        private final String correlationKey;

        public FindMessageEventSubscription(String messageName, String correlationKey) {
            this.messageName = messageName;
            this.correlationKey = correlationKey;
        }

        public EventSubscriptionEntity execute(CommandContext commandContext) {
            return new EventSubscriptionQueryImpl(commandContext).eventType("message")
                                                                 .eventName(messageName)
                                                                 .configuration(correlationKey)
                                                                 .singleResult();
        }
    }
}
