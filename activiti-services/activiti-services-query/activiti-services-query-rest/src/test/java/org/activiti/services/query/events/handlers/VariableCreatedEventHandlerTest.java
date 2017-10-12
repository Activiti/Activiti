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

package org.activiti.services.query.events.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.persistence.EntityManager;

import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.query.app.repository.VariableRepository;
import org.activiti.services.query.events.VariableCreatedEvent;
import org.activiti.services.query.model.ProcessInstance;
import org.activiti.services.query.model.Task;
import org.activiti.services.query.model.Variable;
import org.activiti.test.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class VariableCreatedEventHandlerTest {

    @InjectMocks
    private VariableCreatedEventHandler handler;

    @Mock
    private VariableRepository variableRepository;

    @Mock
    private EntityManager entityManager;
    
    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void handleShouldCreateAndStoreVariable() throws Exception {
        //given
    	String executionId = "10";
        long processInstanceId = 30L;
        String taskId = "50";
        String variableName = "var";
        String variableType = String.class.getName();
        ProcessInstance processInstance = mock(ProcessInstance.class);
        Task task = mock(Task.class);
        VariableCreatedEvent event = new VariableCreatedEvent(System.currentTimeMillis(),
                                                              "variableCreated",
                                                              executionId,
                                                              "20",
                                                              String.valueOf(processInstanceId),
                                                              variableName,
                                                              "content",
                                                              variableType,
                                                              taskId);

        when(entityManager.getReference(ArgumentMatchers.eq(ProcessInstance.class), any()))
    		.thenReturn(processInstance);

        when(entityManager.getReference(ArgumentMatchers.eq(Task.class), any()))
    		.thenReturn(task);

        //when
        handler.handle(event);

        //then
        ArgumentCaptor<Variable> captor = ArgumentCaptor.forClass(Variable.class);
        verify(variableRepository).save(captor.capture());
        
        Variable variable = captor.getValue();

        Assertions.assertThat(variable)
                .hasExecutionId(executionId)
                .hasProcessInstanceId(String.valueOf(processInstanceId))
                .hasName(variableName)
                .hasTaskId(taskId)
                .hasType(variableType)
                .hasTask(task)
                .hasProcessInstance(processInstance)
                ;
    }

    @Test
    public void getHandledEventClass() throws Exception {
        //when
        Class<? extends ProcessEngineEvent> handledEventClass = handler.getHandledEventClass();

        //then
        assertThat(handledEventClass).isEqualTo(VariableCreatedEvent.class);
    }
}