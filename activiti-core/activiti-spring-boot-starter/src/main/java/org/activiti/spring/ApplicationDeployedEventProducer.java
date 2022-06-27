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
import org.activiti.api.process.model.Deployment;
import org.activiti.api.process.model.events.ApplicationDeployedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.event.impl.ApplicationDeployedEventImpl;
import org.activiti.api.runtime.event.impl.ApplicationDeployedEvents;
import org.activiti.engine.RepositoryService;
import org.activiti.runtime.api.model.impl.APIDeploymentConverter;
import org.springframework.context.ApplicationEventPublisher;

public class ApplicationDeployedEventProducer extends AbstractActivitiSmartLifeCycle {

    private RepositoryService repositoryService;
    private APIDeploymentConverter deploymentConverter;
    private List<ProcessRuntimeEventListener<ApplicationDeployedEvent>> listeners;
    private ApplicationEventPublisher eventPublisher;
    private static final String APPLICATION_DEPLOYMENT_NAME= "SpringAutoDeployment";

    public ApplicationDeployedEventProducer(RepositoryService repositoryService,
            APIDeploymentConverter deploymentConverter,
            List<ProcessRuntimeEventListener<ApplicationDeployedEvent>> listeners,
            ApplicationEventPublisher eventPublisher) {
        this.repositoryService = repositoryService;
        this.deploymentConverter = deploymentConverter;
        this.listeners = listeners;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void doStart() {
        List<ApplicationDeployedEvent> applicationDeployedEvents = getApplicationDeployedEvents();
        for (ProcessRuntimeEventListener<ApplicationDeployedEvent> listener : listeners) {
            applicationDeployedEvents.forEach(listener::onEvent);
        }
        if (!applicationDeployedEvents.isEmpty()) {
            eventPublisher.publishEvent(new ApplicationDeployedEvents(applicationDeployedEvents));
        }
    }

    private List<ApplicationDeployedEvent> getApplicationDeployedEvents() {
        List<Deployment> deployments = deploymentConverter.from(repositoryService
                        .createDeploymentQuery()
                        .deploymentName(APPLICATION_DEPLOYMENT_NAME)
                        .list());

        List<ApplicationDeployedEvent> applicationDeployedEvents = new ArrayList<>();
        for (Deployment deployment : deployments) {
            ApplicationDeployedEventImpl applicationDeployedEvent = new ApplicationDeployedEventImpl(deployment);
            applicationDeployedEvents.add(applicationDeployedEvent);
        }
        return applicationDeployedEvents;
    }

    @Override
    public void doStop() {
        //nothing
    }
}
