/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.services.connectors.behavior;

import java.util.UUID;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.delegate.TriggerableActivityBehavior;
import org.activiti.services.connectors.channel.ProcessEngineIntegrationChannels;
import org.activiti.services.connectors.model.AsyncContext;
import org.activiti.services.connectors.model.IntegrationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class MQServiceTaskBehavior extends AbstractBpmnActivityBehavior implements TriggerableActivityBehavior {

    private final ProcessEngineIntegrationChannels channels;

    @Autowired
    public MQServiceTaskBehavior(ProcessEngineIntegrationChannels channels) {
        this.channels = channels;
    }

    @Override
    public void execute(DelegateExecution execution) {
        AsyncContext context = new AsyncContext(execution.getProcessInstanceId(),
                                                     execution.getCurrentActivityId(),
                                                     execution.getId());
        IntegrationEvent event = new IntegrationEvent(UUID.randomUUID().toString(),
                                                      context,
                                                      execution.getVariables());
        channels.integrationEventsProducer().send(MessageBuilder.withPayload(event).build());
    }

    @Override
    public void trigger(DelegateExecution execution,
                        String signalEvent,
                        Object signalData) {
        leave(execution);
    }
}
