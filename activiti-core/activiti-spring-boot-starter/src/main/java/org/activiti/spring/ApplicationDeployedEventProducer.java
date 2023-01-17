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

import java.util.List;
import java.util.stream.Collectors;
import org.activiti.api.process.model.Deployment;
import org.activiti.api.process.model.events.ApplicationDeployedEvent;
import org.activiti.api.process.model.events.ApplicationEvent.ApplicationEvents;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.event.impl.ApplicationDeployedEventImpl;
import org.activiti.api.runtime.event.impl.ApplicationDeployedEvents;
import org.activiti.api.runtime.model.impl.DeploymentImpl;
import org.activiti.engine.RepositoryService;
import org.activiti.runtime.api.model.impl.APIDeploymentConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

public class ApplicationDeployedEventProducer extends AbstractActivitiSmartLifeCycle {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDeployedEventProducer.class);

    private static final String APPLICATION_DEPLOYMENT_NAME= "SpringAutoDeployment";

    private RepositoryService repositoryService;
    private APIDeploymentConverter deploymentConverter;
    private List<ProcessRuntimeEventListener<ApplicationDeployedEvent>> listeners;
    private ApplicationEventPublisher eventPublisher;

    @Value("${activiti.deploy.after-rollback:false}")
    private boolean afterRollback;

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

        if (!applicationDeployedEvents.isEmpty()) {
            ApplicationDeployedEvent applicationDeployedEvent = applicationDeployedEvents.get(0);
            for (ProcessRuntimeEventListener<ApplicationDeployedEvent> listener : listeners) {
                listener.onEvent(applicationDeployedEvent);
            }

            eventPublisher.publishEvent(new ApplicationDeployedEvents(List.of(applicationDeployedEvent)));
        }
    }

    private List<ApplicationDeployedEvent> getApplicationDeployedEvents() {
        ApplicationEvents eventType = getEventType();
        return deploymentConverter.from(repositoryService
                        .createDeploymentQuery()
                        .deploymentName(APPLICATION_DEPLOYMENT_NAME)
                        .latestVersion()
                        .list())
            .stream()
            .map(this::withProjectVersion1Based)
            .map(deployment -> new ApplicationDeployedEventImpl(deployment, eventType))
            .collect(Collectors.toList());
    }

    private Deployment withProjectVersion1Based(Deployment deployment) {
        String projectReleaseVersion = deployment.getProjectReleaseVersion();
        if(StringUtils.isNumeric(projectReleaseVersion)) {
            //The project version in the database is 0 based while in the devops section is 1 based
            DeploymentImpl result = new DeploymentImpl();
            result.setVersion(deployment.getVersion());
            result.setId(deployment.getId());
            result.setName(deployment.getName());
            int projectReleaseVersionInt = Integer.valueOf(projectReleaseVersion) + 1;
            result.setProjectReleaseVersion(String.valueOf(projectReleaseVersionInt));
            return result;
        } else {
            return deployment;
        }
    }

    private ApplicationEvents getEventType() {
        ApplicationEvents eventType;
        if(afterRollback) {
            LOGGER.info("This pod has been marked as created after a rollback.");
            eventType = ApplicationEvents.APPLICATION_ROLLBACK;
        } else {
            eventType = ApplicationEvents.APPLICATION_DEPLOYED;
        }
        return eventType;
    }

    public void setAfterRollback(boolean afterRollback) {
        this.afterRollback = afterRollback;
    }

    @Override
    public void doStop() {
        //nothing
    }
}
