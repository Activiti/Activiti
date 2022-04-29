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
package org.activiti.spring.boot;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.conf.ProcessRuntimeConfiguration;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.core.common.spring.security.policies.ProcessSecurityPoliciesManager;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.runtime.api.impl.ProcessAdminRuntimeImpl;
import org.activiti.runtime.api.impl.ProcessRuntimeImpl;
import org.activiti.runtime.api.impl.ProcessVariablesPayloadValidator;
import org.activiti.runtime.api.model.impl.APIDeploymentConverter;
import org.activiti.runtime.api.model.impl.APIProcessDefinitionConverter;
import org.activiti.runtime.api.model.impl.APIProcessInstanceConverter;
import org.activiti.runtime.api.model.impl.APIVariableInstanceConverter;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:application-history.properties")
public class HistoryConfigurationTest {

    private static final String CATEGORIZE_PROCESS = "categorizeProcess";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private APIProcessDefinitionConverter processDefinitionConverter;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessSecurityPoliciesManager securityPoliciesManager;

    @Autowired
    private APIProcessInstanceConverter processInstanceConverter;

    @Autowired
    private APIVariableInstanceConverter variableInstanceConverter;

    @Autowired
    private ProcessRuntimeConfiguration configuration;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    ProcessVariablesPayloadValidator processVariablesValidator;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private APIDeploymentConverter deploymentConverter;

    @Autowired
    private SecurityManager securityManager;

    @AfterEach
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @BeforeEach
    public void init() {
        ApplicationEventPublisher eventPublisher = spy(applicationEventPublisher);

        spy(new ProcessRuntimeImpl(repositoryService,
                     processDefinitionConverter,
                     runtimeService,
                     taskService,
                     securityPoliciesManager,
                     processInstanceConverter,
                     variableInstanceConverter,
                     deploymentConverter,
                     configuration,
                     eventPublisher,
                     processVariablesValidator,
                     securityManager));

        spy(new ProcessAdminRuntimeImpl(repositoryService,
                     processDefinitionConverter,
                     runtimeService,
                     processInstanceConverter,
                     variableInstanceConverter,
                     eventPublisher,
                     processVariablesValidator));

        //Reset test variables
        RuntimeTestConfiguration.processImageConnectorExecuted = false;
        RuntimeTestConfiguration.tagImageConnectorExecuted = false;
        RuntimeTestConfiguration.discardImageConnectorExecuted = false;

    }

    @Test
    public void shouldRecordHistory() {
        securityUtil.logInAs("user");

        //when
        ProcessInstance categorizeProcess = processRuntime.start(ProcessPayloadBuilder.start()
                .withProcessDefinitionKey(CATEGORIZE_PROCESS)
                .withVariable("expectedKey",
                        true)
                .build());

        assertThat(RuntimeTestConfiguration.completedProcesses).contains(categorizeProcess.getId());
        assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(categorizeProcess.getId()).count()).isEqualTo(1);
    }
}
