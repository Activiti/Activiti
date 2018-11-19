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

import java.util.Arrays;
import java.util.List;

import org.activiti.api.process.model.events.ProcessDeployedEvent;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProcessDeployedEventProducerTest {

    private ProcessDeployedEventProducer producer;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private APIProcessDefinitionConverter converter;

    @Mock
    private ProcessRuntimeEventListener<ProcessDeployedEvent> firstListener;

    @Mock
    private ProcessRuntimeEventListener<ProcessDeployedEvent> secondListener;

    @Before
    public void setUp() {
        initMocks(this);
        producer = new ProcessDeployedEventProducer(repositoryService,
                                                    converter,
                                                    Arrays.asList(firstListener,
                                                                  secondListener));
    }

    @Test
    public void shouldCallRegisteredListenersWhenWebApplicationTypeIsServlet() {
        //given
        ProcessDefinitionQuery definitionQuery = mock(ProcessDefinitionQuery.class);
        given(repositoryService.createProcessDefinitionQuery()).willReturn(definitionQuery);

        List<ProcessDefinition> internalProcessDefinitions = Arrays.asList(mock(ProcessDefinition.class),
                                                                           mock(ProcessDefinition.class));

        given(definitionQuery.list()).willReturn(internalProcessDefinitions);

        List<org.activiti.api.process.model.ProcessDefinition> apiProcessDefinitions = Arrays.asList(mock(org.activiti.api.process.model.ProcessDefinition.class),
                                                                                                     mock(org.activiti.api.process.model.ProcessDefinition.class));
        given(converter.from(internalProcessDefinitions)).willReturn(apiProcessDefinitions);

        //when
        producer.onApplicationEvent(buildApplicationReadyEvent(WebApplicationType.SERVLET));

        //then
        ArgumentCaptor<ProcessDeployedEvent> captor = ArgumentCaptor.forClass(ProcessDeployedEvent.class);
        verify(firstListener,
               times(2)).onEvent(captor.capture());
        verify(secondListener,
               times(2)).onEvent(captor.capture());

        List<ProcessDeployedEvent> allValues = captor.getAllValues();
        assertThat(allValues)
                .extracting(ProcessDeployedEvent::getEntity)
                .containsExactly(apiProcessDefinitions.get(0),//firstListener
                                 apiProcessDefinitions.get(1),//firstListener
                                 apiProcessDefinitions.get(0),//secondListener
                                 apiProcessDefinitions.get(1));//secondListener
    }

    private ApplicationReadyEvent buildApplicationReadyEvent(WebApplicationType applicationType) {
        SpringApplication springApplication = mock(SpringApplication.class);
        given(springApplication.getWebApplicationType()).willReturn(applicationType);

        ApplicationReadyEvent applicationReadyEvent = mock(ApplicationReadyEvent.class);
        given(applicationReadyEvent.getSpringApplication()).willReturn(springApplication);
        return applicationReadyEvent;
    }

    @Test
    public void shouldNotCallRegisteredListenerWhenApplicationTypeIsNone() {
        //when
        producer.onApplicationEvent(buildApplicationReadyEvent(WebApplicationType.NONE));

        //then
        verifyZeroInteractions(firstListener, secondListener);
    }
}