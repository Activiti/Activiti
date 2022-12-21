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
import org.activiti.engine.RepositoryService;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessRuntimeCandidateStartersIT {

    private static final String NO_CANDIDATES_SET_PROCESS_DEFINITION_KEY = "SingleTaskProcess";
    private static final String RESTRICTED_PROCESS_DEFINITION_KEY = "SingleTaskProcessRestricted";

    @Autowired
    private RepositoryService repositoryService;

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
        loginAsCandidateStarterUser();

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
                                                                                                      50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()) // All processes except UnstartableProcess
            .hasSize((int) repositoryService.createProcessDefinitionQuery().count() -1);
    }

    @Test
    public void candidateStarterGroupMembers_should_getProcessDefinitions() {
        loginAsGroupMemberCandidateStarter();

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
            50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        assertThat(processDefinitionPage.getContent()) // All processes except UnstartableProcess
            .hasSize((int) repositoryService.createProcessDefinitionQuery().count() -1);
    }

    @Test
    public void nonCandidateStarters_shouldNot_getRestrictedProcessDefinitions() {
        loginAsANonCandidateStarter();

        Page<ProcessDefinition> processDefinitionPage = processRuntime.processDefinitions(Pageable.of(0,
            50));
        assertThat(processDefinitionPage.getContent()).isNotNull();
        // All processes except SingleTaskProcessRestricted and UnstartableProcess
        assertThat(processDefinitionPage.getContent())
            .hasSize((int) repositoryService.createProcessDefinitionQuery().count() - 2);
    }


    @Test
    public void candidateStarterUser_should_getProcessDefinition() {
        loginAsCandidateStarterUser();

        ProcessDefinition processDefinition = processRuntime.processDefinition(RESTRICTED_PROCESS_DEFINITION_KEY);
        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.getKey()).isEqualTo(RESTRICTED_PROCESS_DEFINITION_KEY);
    }

    @Test
    public void candidateStarterGroupMembers_should_getProcessDefinition() {
        loginAsGroupMemberCandidateStarter();

        ProcessDefinition processDefinition = processRuntime.processDefinition(RESTRICTED_PROCESS_DEFINITION_KEY);
        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.getKey()).isEqualTo(RESTRICTED_PROCESS_DEFINITION_KEY);
    }

    @Test
    public void nonCandidateStarter_should_getProcessDefinitionWithNoCandidatesSet() {
        loginAsCandidateStarterUser();

        ProcessDefinition processDefinition = processRuntime.processDefinition(NO_CANDIDATES_SET_PROCESS_DEFINITION_KEY);
        assertThat(processDefinition).isNotNull();
        assertThat(processDefinition.getKey()).isEqualTo(NO_CANDIDATES_SET_PROCESS_DEFINITION_KEY);
    }

    @Test
    public void nonCandidateStarters_shouldNot_getProcessDefinitionWithCandidatesSet() {
        loginAsANonCandidateStarter();

        Throwable throwable = catchThrowable(() -> processRuntime.processDefinition(RESTRICTED_PROCESS_DEFINITION_KEY));

        assertThat(throwable)
            .isInstanceOf(ActivitiObjectNotFoundException.class)
            .hasMessage("Unable to find process definition for the given id or key:'" + RESTRICTED_PROCESS_DEFINITION_KEY + "'");
    }

    @Test
    public void candidateStarterUser_can_startProcess() {
        loginAsCandidateStarterUser();

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
            .withProcessDefinitionKey(RESTRICTED_PROCESS_DEFINITION_KEY)
            .build());

        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getProcessDefinitionKey()).isEqualTo(RESTRICTED_PROCESS_DEFINITION_KEY);
    }

    private void loginAsCandidateStarterUser() {
        securityUtil.logInAs("user");
    }

    private void loginAsGroupMemberCandidateStarter() {
        securityUtil.logInAs("john");
    }

    private void loginAsANonCandidateStarter() {
        securityUtil.logInAs("garth");
    }

}
