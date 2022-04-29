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
package org.activiti.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.events.StartMessageDeployedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.event.impl.StartMessageDeployedEventImpl;
import org.activiti.api.runtime.event.impl.StartMessageDeployedEvents;
import org.activiti.api.runtime.model.impl.StartMessageDeploymentDefinitionImpl;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.activiti.runtime.api.event.impl.StartMessageSubscriptionConverter;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

public class StartMessageDeployedEventProducer extends AbstractActivitiSmartLifeCycle {

    private static Logger logger = LoggerFactory.getLogger(StartMessageDeployedEventProducer.class);

    private RepositoryService repositoryService;
    private ManagementService managementService;
    private APIProcessDefinitionConverter converter;
    private StartMessageSubscriptionConverter subscriptionConverter;
    private List<ProcessRuntimeEventListener<StartMessageDeployedEvent>> listeners;
    private ApplicationEventPublisher eventPublisher;

    public StartMessageDeployedEventProducer(RepositoryService repositoryService,
                                        ManagementService managementService,
                                        StartMessageSubscriptionConverter subscriptionConverter,
                                        APIProcessDefinitionConverter converter,
                                        List<ProcessRuntimeEventListener<StartMessageDeployedEvent>> listeners,
                                        ApplicationEventPublisher eventPublisher) {
        this.repositoryService = repositoryService;
        this.managementService = managementService;
        this.subscriptionConverter = subscriptionConverter;
        this.converter = converter;
        this.listeners = listeners;
        this.eventPublisher = eventPublisher;
    }

    public void doStart() {
        List<ProcessDefinition> processDefinitions = converter.from(repositoryService.createProcessDefinitionQuery().list());
        List<StartMessageDeployedEvent> messageDeployedEvents = new ArrayList<>();

        for (ProcessDefinition processDefinition : processDefinitions) {
            managementService.executeCommand(new FindStartMessageEventSubscriptions(processDefinition.getId()))
                             .stream()
                             .map(subscriptionConverter::convertToStartMessageSubscription)
                             .map(messageSubscription -> StartMessageDeploymentDefinitionImpl.builder()
                                                                                             .withMessageSubscription(messageSubscription)
                                                                                             .withProcessDefinition(processDefinition)
                                                                                             .build())
                             .map(startMessageDeploymentDefinition -> StartMessageDeployedEventImpl.builder()
                                                                                                   .withEntity(startMessageDeploymentDefinition)
                                                                                                   .build())
                             .forEach(messageDeployedEvents::add);
        }

        managementService.executeCommand(new DispatchStartMessageDeployedEvents(messageDeployedEvents));

        if (!messageDeployedEvents.isEmpty()) {
            eventPublisher.publishEvent(new StartMessageDeployedEvents(messageDeployedEvents));
        }
    }

    public void doStop() {
        // nothing
    }

    class DispatchStartMessageDeployedEvents implements Command<Void> {

        private final List<StartMessageDeployedEvent> messageDeployedEvents;

        public DispatchStartMessageDeployedEvents(List<StartMessageDeployedEvent> messageDeployedEvents) {
            this.messageDeployedEvents = messageDeployedEvents;
        }

        public Void execute(CommandContext commandContext) {
            for (ProcessRuntimeEventListener<StartMessageDeployedEvent> listener : listeners) {
                messageDeployedEvents.stream()
                                     .forEach(listener::onEvent);
            }

            return null;
        }
    }

    static class FindStartMessageEventSubscriptions implements Command<List<MessageEventSubscriptionEntity>> {

        private static final String MESSAGE = "message";
        private final String processDefinitionId;

        public FindStartMessageEventSubscriptions(String processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
        }

        public List<MessageEventSubscriptionEntity> execute(CommandContext commandContext) {
            return new EventSubscriptionQueryImpl(commandContext).eventType(MESSAGE)
                                                                 .configuration(processDefinitionId)
                                                                 .list()
                                                                 .stream()
                                                                 .map(MessageEventSubscriptionEntity.class::cast)
                                                                 .filter(it -> it.getProcessInstanceId() == null)
                                                                 .collect(Collectors.toList());
        }
    }

}
