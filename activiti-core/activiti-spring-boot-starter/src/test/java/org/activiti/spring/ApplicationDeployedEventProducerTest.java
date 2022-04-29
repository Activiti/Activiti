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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.activiti.api.process.model.events.ApplicationDeployedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.api.runtime.event.impl.ApplicationDeployedEvents;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.runtime.api.model.impl.APIDeploymentConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class ApplicationDeployedEventProducerTest {

    private ApplicationDeployedEventProducer producer;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private APIDeploymentConverter converter;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ProcessRuntimeEventListener<ApplicationDeployedEvent> firstListener;

    @Mock
    private ProcessRuntimeEventListener<ApplicationDeployedEvent> secondListener;

    private static final String APPLICATION_DEPLOYMENT_NAME= "SpringAutoDeployment";

    @BeforeEach
    public void setUp() {
        producer = new ApplicationDeployedEventProducer(repositoryService,
                converter,
                asList(firstListener, secondListener),
                eventPublisher);
    }

    @Test
    public void shouldPublishEventsWhenApplicationIsDeployed() {
        DeploymentQuery deploymentQuery = mock(DeploymentQuery.class);
        given(repositoryService.createDeploymentQuery()).willReturn(deploymentQuery);

        List<Deployment> internalDeployment = asList(mock(Deployment.class),
                mock(Deployment.class));

        given(deploymentQuery.deploymentName(APPLICATION_DEPLOYMENT_NAME)).willReturn(deploymentQuery);
        given(deploymentQuery.list()).willReturn(internalDeployment);

        List<org.activiti.api.process.model.Deployment> apiDeployments= asList(
                mock(org.activiti.api.process.model.Deployment.class));
        given(converter.from(internalDeployment)).willReturn(apiDeployments);

        producer.start();

        ArgumentCaptor<ApplicationDeployedEvent> captor = ArgumentCaptor.forClass(ApplicationDeployedEvent.class);
        verify(firstListener).onEvent(captor.capture());
        verify(secondListener).onEvent(captor.capture());

        List<ApplicationDeployedEvent> allValues = captor.getAllValues();
        assertThat(allValues)
                .extracting(ApplicationDeployedEvent::getEntity)
                .hasSize(2)
                .containsOnly(apiDeployments.get(0));

        ArgumentCaptor<ApplicationDeployedEvents> captorPublisher = ArgumentCaptor.forClass(ApplicationDeployedEvents.class);
        verify(eventPublisher).publishEvent(captorPublisher.capture());
    }

}
