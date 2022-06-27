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

package org.activiti.spring.test.taskassignment;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 */
@ContextConfiguration("classpath:org/activiti/spring/test/taskassignment/taskassignment-context.xml")
public class CustomTaskAssignmentTest extends SpringActivitiTestCase {

    private void cleanUp() {
        List<org.activiti.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.activiti.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    @Override
    public void tearDown() {
        cleanUp();
    }

    @Deployment
    public void testSetAssigneeThroughSpringService() {
        runtimeService.startProcessInstanceByKey("assigneeThroughSpringService",
                                                 singletonMap("emp", "fozzie"));
        assertThat(taskService.createTaskQuery().taskAssignee("Kermit The Frog").count()).isEqualTo(1);
    }

    @Deployment
    public void testSetCandidateUsersThroughSpringService() {
        runtimeService.startProcessInstanceByKey("candidateUsersThroughSpringService",
                                                 singletonMap("emp", "fozzie"));
        assertThat(taskService.createTaskQuery().taskCandidateUser("kermit").count()).isEqualTo(1);
        assertThat(taskService.createTaskQuery().taskCandidateUser("fozzie").count()).isEqualTo(1);
        assertThat(taskService.createTaskQuery().taskCandidateUser("gonzo").count()).isEqualTo(1);
        assertThat(taskService.createTaskQuery().taskCandidateUser("mispiggy").count()).isEqualTo(0);
    }

    @Deployment
    public void testSetCandidateGroupsThroughSpringService() {
        runtimeService.startProcessInstanceByKey("candidateUsersThroughSpringService",
                                                 singletonMap("emp", "fozzie"));
        assertThat(taskService.createTaskQuery().taskCandidateGroup("management").count()).isEqualTo(1);
        assertThat(taskService.createTaskQuery().taskCandidateGroup("directors").count()).isEqualTo(1);
        assertThat(taskService.createTaskQuery().taskCandidateGroup("accountancy").count()).isEqualTo(1);
        assertThat(taskService.createTaskQuery().taskCandidateGroup("sales").count()).isEqualTo(0);
    }
}
