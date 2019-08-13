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

package org.activiti.runtime.api.impl;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.UpdateProcessPayload;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProcessRuntimeImplTest {

    private ProcessRuntimeImpl processRuntime;

    @Mock
    private ProcessSecurityPoliciesManager securityPoliciesManager;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private APIProcessInstanceConverter processInstanceConverter;

    @Before
    public void setUp() {
        initMocks(this);
        processRuntime = spy(new ProcessRuntimeImpl(repositoryService,
                null,
                runtimeService,
                securityPoliciesManager,
                processInstanceConverter,
                null,
                null,
                null));
        doReturn(true).when(securityPoliciesManager).canWrite("processDefinitionKey");

    }

    @Test
    public void updateShouldBeAbleToUpdateNameBusinessKey() {
        //given
        ProcessInstanceImpl process = new ProcessInstanceImpl();
        process.setId("processId");
        process.setProcessDefinitionKey("processDefinitionKey");

        doReturn(process).when(processRuntime).processInstance("processId");

        ProcessInstanceQuery processQuery = mock(ProcessInstanceQuery.class);
        doReturn(processQuery).when(processQuery).processInstanceId("processId");
        doReturn(processQuery).when(runtimeService).createProcessInstanceQuery();


        org.activiti.engine.runtime.ProcessInstance internalProcess = mock(org.activiti.engine.runtime.ProcessInstance.class);

        doReturn(internalProcess).when(processQuery).singleResult();

        UpdateProcessPayload updateProcessPayload = ProcessPayloadBuilder.update()
                .withProcessInstanceId("processId")
                .withBusinessKey("businessKey")
                .withName("name")
                .build();

        //when
        ProcessInstance updatedProcess = processRuntime.update(updateProcessPayload);

        //then
        verify(runtimeService).updateBusinessKey("processId", "businessKey");
        verifyNoMoreInteractions(internalProcess);
    }

}
