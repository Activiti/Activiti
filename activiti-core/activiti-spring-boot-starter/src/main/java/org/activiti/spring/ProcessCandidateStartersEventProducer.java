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
package org.activiti.spring;

import org.activiti.api.process.runtime.events.ProcessCandidateStarterGroupAddedEvent;
import org.activiti.api.process.runtime.events.ProcessCandidateStarterUserAddedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.event.impl.ProcessCandidateStarterGroupAddedEventImpl;
import org.activiti.api.runtime.event.impl.ProcessCandidateStarterGroupAddedEvents;
import org.activiti.api.runtime.event.impl.ProcessCandidateStarterUserAddedEventImpl;
import org.activiti.api.runtime.event.impl.ProcessCandidateStarterUserAddedEvents;
import org.activiti.api.runtime.model.impl.ProcessCandidateStarterGroupImpl;
import org.activiti.api.runtime.model.impl.ProcessCandidateStarterUserImpl;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.runtime.api.event.impl.ProcessCandidateStarterEventConverterHelper;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProcessCandidateStartersEventProducer extends AbstractActivitiSmartLifeCycle {

    private RepositoryService repositoryService;
    private List<ProcessRuntimeEventListener<ProcessCandidateStarterUserAddedEvent>> candidateStarterUserListeners;
    private List<ProcessRuntimeEventListener<ProcessCandidateStarterGroupAddedEvent>> candidateStarterGroupListeners;
    private ProcessCandidateStarterEventConverterHelper processCandidateStarterEventConverterHelper = new ProcessCandidateStarterEventConverterHelper();
    private ApplicationEventPublisher eventPublisher;

    public ProcessCandidateStartersEventProducer(RepositoryService repositoryService,
                                                 List<ProcessRuntimeEventListener<ProcessCandidateStarterUserAddedEvent>> candidateStarterUserListeners,
                                                 List<ProcessRuntimeEventListener<ProcessCandidateStarterGroupAddedEvent>> candidateStarterGroupListeners,
                                                 ApplicationEventPublisher eventPublisher) {
        this.repositoryService = repositoryService;
        this.candidateStarterUserListeners = Optional.ofNullable(candidateStarterUserListeners).orElseGet(() -> List.of());
        this.candidateStarterGroupListeners = Optional.ofNullable(candidateStarterGroupListeners).orElseGet(() -> List.of());
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void doStart() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().latestVersion().list();
        List<ProcessCandidateStarterUserAddedEvent> candidateStarterUserAddedEvents = new ArrayList<>();
        List<ProcessCandidateStarterGroupAddedEvent> candidateStarterGroupAddedEvents = new ArrayList<>();
        for (ProcessDefinition processDefinition : processDefinitions) {
            List<IdentityLink> identityLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
            for (IdentityLink identityLink: identityLinks){
                if(processCandidateStarterEventConverterHelper.isProcessCandidateStarterUserLink(identityLink)) {
                    ProcessCandidateStarterUserAddedEvent processCandidateStarterUserAddedEvent = createCandidateStarterUserEvent(identityLink);
                    candidateStarterUserAddedEvents.add(processCandidateStarterUserAddedEvent);
                    notifyCandidateStarterUserAddedListeners(processCandidateStarterUserAddedEvent);
                } else if(processCandidateStarterEventConverterHelper.isProcessCandidateStarterGroupLink(identityLink)) {
                    ProcessCandidateStarterGroupAddedEvent processCandidateStarterGroupAddedEvent = createCandidateStarterGroupEvent(identityLink);
                    candidateStarterGroupAddedEvents.add(processCandidateStarterGroupAddedEvent);
                    notifyCandidateStarterGroupAddedListeners(processCandidateStarterGroupAddedEvent);
                }
            }
        }

        publishCandidateStarterEvents(candidateStarterUserAddedEvents, candidateStarterGroupAddedEvents);
    }

    private void notifyCandidateStarterUserAddedListeners(ProcessCandidateStarterUserAddedEvent processCandidateStarterUserAddedEvent) {
        for (ProcessRuntimeEventListener<ProcessCandidateStarterUserAddedEvent> listener: candidateStarterUserListeners) {
            listener.onEvent(processCandidateStarterUserAddedEvent);
        }
    }

    private void notifyCandidateStarterGroupAddedListeners(ProcessCandidateStarterGroupAddedEvent processCandidateStarterGroupAddedEvent) {
        for (ProcessRuntimeEventListener<ProcessCandidateStarterGroupAddedEvent> listener: candidateStarterGroupListeners) {
            listener.onEvent(processCandidateStarterGroupAddedEvent);
        }
    }

    private ProcessCandidateStarterUserAddedEvent createCandidateStarterUserEvent(IdentityLink identityLink) {
        return new ProcessCandidateStarterUserAddedEventImpl(
            new ProcessCandidateStarterUserImpl(identityLink.getProcessDefinitionId(), identityLink.getUserId()));
    }

    private ProcessCandidateStarterGroupAddedEvent createCandidateStarterGroupEvent(IdentityLink identityLink) {
        return new ProcessCandidateStarterGroupAddedEventImpl(
            new ProcessCandidateStarterGroupImpl(identityLink.getProcessDefinitionId(), identityLink.getGroupId()));
    }

    private void publishCandidateStarterEvents(List<ProcessCandidateStarterUserAddedEvent> candidateStarterUserAddedEvents,
                                               List<ProcessCandidateStarterGroupAddedEvent> candidateStarterGroupAddedEvents) {

        if(!candidateStarterUserAddedEvents.isEmpty()) {
            eventPublisher.publishEvent(new ProcessCandidateStarterUserAddedEvents(candidateStarterUserAddedEvents));
        }

        if(!candidateStarterGroupAddedEvents.isEmpty()) {
            eventPublisher.publishEvent(new ProcessCandidateStarterGroupAddedEvents(candidateStarterGroupAddedEvents));
        }
    }

    @Override
    public void doStop() {
        // nothing
    }
}
