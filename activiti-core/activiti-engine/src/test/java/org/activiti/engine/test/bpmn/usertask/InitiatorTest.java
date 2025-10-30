/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
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
package org.activiti.engine.test.bpmn.usertask;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**

 */
public class InitiatorTest extends PluggableActivitiTestCase {

    @Deployment
    public void testInitiator() {
        withAuthenticatedUserId("bono", () -> {
            runtimeService.startProcessInstanceByKey("InitiatorProcess");
        });

        assertThat(taskService.createTaskQuery().taskAssignee("bono").count()).isEqualTo(1);
    }

    // See ACT-1372
    @Deployment
    public void testInitiatorWithWhiteSpaceInExpression() {
        withAuthenticatedUserId("bono", () -> {
            runtimeService.startProcessInstanceByKey("InitiatorProcess");
        });

        assertThat(taskService.createTaskQuery().taskAssignee("bono").count()).isEqualTo(1);
    }

    @Deployment(
        resources = {
            "org/activiti/engine/test/bpmn/usertask/InitiatorTest.testInitiatorWithinCallActivitySubProcess.bpmn20.xml",
            "org/activiti/engine/test/bpmn/usertask/InitiatorTest.testInitiator.bpmn20.xml",
        }
    )
    public void testInitiatorWithinCallActivitySubProcess() {
        withAuthenticatedUserId("bono", () -> {
            runtimeService.startProcessInstanceByKey("CallActivityWithInitiatorSubprocess");
        });

        assertThat(managementService.createJobQuery().count()).isEqualTo(1);

        waitForJobExecutorToProcessAllJobs(5000L, 100L);

        assertThat(taskService.createTaskQuery().taskAssignee("bono").count()).isEqualTo(1);
    }

    private void withAuthenticatedUserId(String userId, Runnable runnable) {
        try {
            Authentication.setAuthenticatedUserId(userId);
            runnable.run();
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }
}
