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
package org.activiti.spring.boot.process;

import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:application-with-candidate-starters-enabled.properties")
public class ProcessRuntimeCandidateStartersIT {

    private static final String RESTRICTED_PROCESS_DEFINITION_KEY = "SingleTaskProcessRestricted";
    private static final Pageable PAGEABLE = Pageable.of(0,
        50);

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @AfterEach
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void candidateStarterUser_should_getProcessDefinitions() {
        securityUtil.logInAs("user");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                                                                                                      50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()).hasSize(1);
        assertThat(processDefinitionPage.getContent().get(0).getKey()).isEqualTo(RESTRICTED_PROCESS_DEFINITION_KEY);
    }

    @Test
    public void candidateStarterGroupMembers_should_getProcessDefinitions() {
        securityUtil.logInAs("john");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
            50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()).hasSize(1);
        assertThat(processDefinitionPage.getContent().get(0).getKey()).isEqualTo(RESTRICTED_PROCESS_DEFINITION_KEY);
    }

    @Test
    public void nonCandidateStarters_shouldNot_getProcessDefinitions() {
        securityUtil.logInAs("garth");

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
            50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()).hasSize(0);
    }


    @Test
    public void candidateStarterUser_should_getProcessDefinition() {
        securityUtil.logInAs("user");

        ProcessDefinition processDefinition = processRuntime.processDefinition(RESTRICTED_PROCESS_DEFINITION_KEY);
        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.getKey()).isEqualTo(RESTRICTED_PROCESS_DEFINITION_KEY);
    }

    @Test
    public void candidateStarterGroupMembers_should_getProcessDefinition() {
        securityUtil.logInAs("john");

        ProcessDefinition processDefinition = processRuntime.processDefinition(RESTRICTED_PROCESS_DEFINITION_KEY);
        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.getKey()).isEqualTo(RESTRICTED_PROCESS_DEFINITION_KEY);
    }

    @Test
    public void nonCandidateStarters_shouldNot_getProcessDefinition() {
        securityUtil.logInAs("garth");

        Throwable throwable = catchThrowable(() -> processRuntime.processDefinition(RESTRICTED_PROCESS_DEFINITION_KEY));

        assertThat(throwable)
            .isInstanceOf(ActivitiObjectNotFoundException.class)
            .hasMessage("Unable to find process definition for the given id:'" + RESTRICTED_PROCESS_DEFINITION_KEY + "'");
    }

    @Test
    public void candidateStarterUser_can_startProcess() {
        securityUtil.logInAs("user");

        Page<ProcessInstance> processInstancePage = processRuntime.processInstances(PAGEABLE);

        assertThat(processInstancePage).isNotNull();
        assertThat(processInstancePage.getContent()).isEmpty();

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
            .withProcessDefinitionKey(RESTRICTED_PROCESS_DEFINITION_KEY)
            .build());

        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getProcessDefinitionKey()).isEqualTo(RESTRICTED_PROCESS_DEFINITION_KEY);
    }

}
