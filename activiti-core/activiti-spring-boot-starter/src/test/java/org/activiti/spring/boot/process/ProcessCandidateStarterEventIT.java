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

import org.activiti.api.process.model.ProcessCandidateStarterGroup;
import org.activiti.api.process.model.ProcessCandidateStarterUser;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.boot.process.listener.ProcessCandidateStarterGroupAddedListener;
import org.activiti.spring.boot.process.listener.ProcessCandidateStarterGroupRemovedListener;
import org.activiti.spring.boot.process.listener.ProcessCandidateStarterUserAddedListener;
import org.activiti.spring.boot.process.listener.ProcessCandidateStarterUserRemovedListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = "activiti.candidateStarters.enabled=true")
public class ProcessCandidateStarterEventIT {

    @Autowired
    private ProcessCandidateStarterUserAddedListener candidateStarterUserListener;

    @Autowired
    private ProcessCandidateStarterGroupAddedListener candidateStarterGroupListener;

    @Autowired
    private ProcessCandidateStarterUserRemovedListener candidateStarterUserRemovedListener;

    @Autowired
    private ProcessCandidateStarterGroupRemovedListener candidateStarterGroupRemovedListener;

    @Autowired
    private RepositoryService repositoryService;


    @Test
    public void shouldTriggerProcessCandidateStarterAddedEvents() {
        String processDefinitionId = getProcessDefinitionId();
        assertCandidateStarters(processDefinitionId,
                                candidateStarterUserListener.getCandidateStarterUsers(),
                                candidateStarterGroupListener.getCandidateStarterGroups());
    }

    @Test
    public void shouldTriggerProcessCandidateStarterRemovedEvents() {
        String processDefinitionId = getProcessDefinitionId();

        repositoryService.deleteCandidateStarterUser(processDefinitionId, "user");
        repositoryService.deleteCandidateStarterGroup(processDefinitionId, "activitiTeam");

        assertCandidateStarters(processDefinitionId,
                                candidateStarterUserRemovedListener.getCandidateStarterUsers(),
                                candidateStarterGroupRemovedListener.getCandidateStarterGroups());
    }

    private String getProcessDefinitionId() {
        return repositoryService.createProcessDefinitionQuery()
                                .processDefinitionKey("SingleTaskProcessRestricted")
                                .latestVersion().singleResult().getId();
    }

    private void assertCandidateStarters(String processDefinitionId,
                                         List<ProcessCandidateStarterUser> candidateStarterUsers,
                                         List<ProcessCandidateStarterGroup> candidateStarterGroups) {
        assertThat(candidateStarterUsers)
            .extracting(ProcessCandidateStarterUser::getProcessDefinitionId, ProcessCandidateStarterUser::getUserId)
            .contains(tuple(processDefinitionId, "user"));

        assertThat(candidateStarterGroups)
            .extracting(ProcessCandidateStarterGroup::getProcessDefinitionId, ProcessCandidateStarterGroup::getGroupId)
            .contains(tuple(processDefinitionId, "activitiTeam"));
    }
}
