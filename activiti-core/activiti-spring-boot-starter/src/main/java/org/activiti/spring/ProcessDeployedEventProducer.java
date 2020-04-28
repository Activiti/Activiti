/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.spring;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.events.ProcessDeployedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.event.impl.ProcessDeployedEventImpl;
import org.activiti.api.runtime.event.impl.ProcessDeployedEvents;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StreamUtils;

public class ProcessDeployedEventProducer extends AbstractActivitiSmartLifeCycle {

    private RepositoryService repositoryService;
    private APIProcessDefinitionConverter converter;
    private List<ProcessRuntimeEventListener<ProcessDeployedEvent>> listeners;
    private ApplicationEventPublisher eventPublisher;

    public ProcessDeployedEventProducer(RepositoryService repositoryService,
                                        APIProcessDefinitionConverter converter,
                                        List<ProcessRuntimeEventListener<ProcessDeployedEvent>> listeners,
                                        ApplicationEventPublisher eventPublisher) {
        this.repositoryService = repositoryService;
        this.converter = converter;
        this.listeners = listeners;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void doStart() {
        List<ProcessDefinition> processDefinitions = converter.from(repositoryService.createProcessDefinitionQuery().list());
        List<ProcessDeployedEvent> processDeployedEvents = new ArrayList<>();
        for (ProcessDefinition processDefinition : processDefinitions) {
            try (InputStream inputStream = repositoryService.getProcessModel(processDefinition.getId())) {
                String xmlModel = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
                ProcessDeployedEventImpl processDeployedEvent = new ProcessDeployedEventImpl(processDefinition, xmlModel);
                processDeployedEvents.add(processDeployedEvent);
                for (ProcessRuntimeEventListener<ProcessDeployedEvent> listener : listeners) {
                    listener.onEvent(processDeployedEvent);
                }
            } catch (IOException e) {
                throw new ActivitiException("Error occurred while getting process model '" + processDefinition.getId() + "' : ", e);
            }
        }
        if (!processDeployedEvents.isEmpty()) {
            eventPublisher.publishEvent(new ProcessDeployedEvents(processDeployedEvents));
        }
    }

    @Override
    public void doStop() {
        // nothing

    }
}
