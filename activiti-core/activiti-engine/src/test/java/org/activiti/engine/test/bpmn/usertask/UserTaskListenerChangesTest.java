/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
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

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class UserTaskListenerChangesTest extends PluggableActivitiTestCase {

    @Deployment
    public void testUserTaskDetailChangedByTaskListener() {
        // GIVEN: a process where an userTask, when created, has dueDate set to 1970/Jan/01
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
        assertThat(processInstance).isNotNull();

        // WHEN: the userTask is created
        // THEN: the taskListener adds a day on dueDate, making it 1970/Jan/02
        final Task task = taskService.createTaskQuery().singleResult();
        assertThat(task.getDueDate()).hasDayOfMonth(2).hasMonth(1).hasYear(1970);
    }
}
