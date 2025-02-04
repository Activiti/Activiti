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
package org.activiti.engine.impl.bpmn.helper.task;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.persistence.entity.AbstractEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.task.TaskInfo;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class ComparableTaskTest {

    @Test
    public void test1() {
    }
    @Test
    public void testCreatingComparableTaskFromTaskInfo() {
        // GIVEN: a TaskInfo
        TaskEntityImpl taskInfo = new TaskEntityImpl();
        taskInfo.setId("id");
        taskInfo.setName("name");
        taskInfo.setAssignee("assignee");
        taskInfo.setFormKey("formKey");
        taskInfo.setPriority(1);
        taskInfo.setCategory("category");
        taskInfo.setOwner("owner");
        taskInfo.setDescription("description");
        taskInfo.setDueDate(new Date(0));
        taskInfo.setTaskDefinitionKey("taskDefinitionKey");
        taskInfo.setParentTaskId("parentTaskId");

        // WHEN: creating a ComparableTask
        ComparableTask comparableTask = new ComparableTask(taskInfo);

        // THEN: the ComparableTask should have the same values as the TaskInfo
        assertThat(comparableTask.getName()).isEqualTo(taskInfo.getName()).isEqualTo("name");
        assertThat(comparableTask.getId()).isEqualTo(taskInfo.getId()).isEqualTo("id");
        assertThat(comparableTask.getDescription()).isEqualTo(taskInfo.getDescription()).isEqualTo("description");
        assertThat(comparableTask.getCategory()).isEqualTo(taskInfo.getCategory()).isEqualTo("category");
        assertThat(comparableTask.getTaskDefinitionKey()).isEqualTo(taskInfo.getTaskDefinitionKey()).isEqualTo("taskDefinitionKey");
        assertThat(comparableTask.getDueDate()).isEqualTo(taskInfo.getDueDate()).isEqualTo(new Date(0));
        assertThat(comparableTask.getPriority()).isEqualTo(taskInfo.getPriority()).isEqualTo(1);
        assertThat(comparableTask.getOwner()).isEqualTo(taskInfo.getOwner()).isEqualTo("owner");
        assertThat(comparableTask.getParentTaskId()).isEqualTo(taskInfo.getParentTaskId()).isEqualTo("parentTaskId");
    }
}
