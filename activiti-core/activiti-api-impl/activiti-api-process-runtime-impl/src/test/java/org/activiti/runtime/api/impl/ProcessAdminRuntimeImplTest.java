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
package org.activiti.runtime.api.impl;

import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.GetProcessDefinitionsPayload;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProcessAdminRuntimeImplTest {

    private ProcessAdminRuntimeImpl processAdminRuntime;

    @Mock
    private ProcessSecurityPoliciesManager securityPoliciesManager;

    @Mock
    private CommandExecutor commandExecutor;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private APIProcessInstanceConverter processInstanceConverter;

    @Mock
    private ProcessVariablesPayloadValidator processVariableValidator;

    @Mock
    private APIProcessDefinitionConverter processDefinitionConverter;


    private RepositoryServiceImpl repositoryService;

    @BeforeEach
    void setUp() {
        repositoryService = spy(new RepositoryServiceImpl());
        repositoryService.setCommandExecutor(commandExecutor);

        processAdminRuntime = spy(new ProcessAdminRuntimeImpl(repositoryService,
            processDefinitionConverter,
            runtimeService,
            processInstanceConverter,
            null,
            null,
            processVariableValidator));

    }

    @Test
    void should_applyPaginationParams_whenSearchingProcessDefinitions() {

        ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(0, 2)).willReturn(Collections.emptyList());

        processAdminRuntime.processDefinitions(Pageable.of(0, 2));

        verify(processDefinitionQuery).listPage(0, 2);
    }

    @Test
    void should_setCategoryNotEquals_when_excludedCategoryIsSet() {
        String processCategory = "#triggerableByForm";

        ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class, Answers.RETURNS_SELF);
        given(repositoryService.createProcessDefinitionQuery()).willReturn(processDefinitionQuery);
        given(processDefinitionQuery.listPage(anyInt(), anyInt())).willReturn(Collections.emptyList());
        given(processDefinitionQuery.count()).willReturn(0L);

        Pageable pageable = Pageable.of(0, 10);
        GetProcessDefinitionsPayload payload = ProcessPayloadBuilder.processDefinitions()
            .withProcessCategoryToExclude(processCategory)
            .build();

        processAdminRuntime.processDefinitions(pageable, payload);

        verify(processDefinitionQuery).processDefinitionCategoryNotEquals(processCategory);
    }

}
