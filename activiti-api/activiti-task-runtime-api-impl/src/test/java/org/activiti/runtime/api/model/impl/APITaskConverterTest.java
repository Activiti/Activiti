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

package org.activiti.runtime.api.model.impl;

import java.util.Date;

import org.activiti.runtime.api.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.activiti.runtime.api.model.Task.TaskStatus.ASSIGNED;
import static org.activiti.runtime.api.model.Task.TaskStatus.CANCELLED;
import static org.activiti.runtime.api.model.Task.TaskStatus.CREATED;
import static org.activiti.runtime.api.model.Task.TaskStatus.SUSPENDED;
import static org.activiti.runtime.api.model.impl.MockTaskBuilder.taskBuilder;
import static org.activiti.runtime.api.model.impl.MockTaskBuilder.taskEntityBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

public class APITaskConverterTest {

    @InjectMocks
    private APITaskConverter taskConverter;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void convertFromTaskShouldSetAllFieldsInTheConvertedTask() {
        //WHEN
        Date now = new Date();
        Task convertedTask = taskConverter.from(
                taskBuilder()
                        .withId("testTaskId")
                        .withAssignee("testUser")
                        .withName("testTaskName")
                        .withDescription("testTaskDescription")
                        .withCreatedDate(now)
                        .withClaimedDate(now)
                        .withDueDate(now)
                        .withPriority(112)
                        .withProcessDefinitionId("testProcessDefinitionId")
                        .withProcessInstanceId("testProcessInstanceId")
                        .withParentTaskId("testParentTaskId")
                        .build()
        );

        //THEN
        assertThat(convertedTask)
                .isNotNull()
                .extracting(Task::getId,
                            Task::getAssignee,
                            Task::getName,
                            Task::getDescription,
                            Task::getCreatedDate,
                            Task::getClaimedDate,
                            Task::getDueDate,
                            Task::getPriority,
                            Task::getProcessDefinitionId,
                            Task::getProcessInstanceId,
                            Task::getParentTaskId,
                            Task::getStatus)
                .containsExactly("testTaskId",
                                 "testUser",
                                 "testTaskName",
                                 "testTaskDescription",
                                 now,
                                 now,
                                 now,
                                 112,
                                 "testProcessDefinitionId",
                                 "testProcessInstanceId",
                                 "testParentTaskId",
                                 ASSIGNED);
    }

    @Test
    public void calculateStatusForACancelledTaskShouldReturnCancelled() {
        assertThat(taskConverter.from(taskEntityBuilder().withCancelled(true).build()))
                .isNotNull()
                .extracting(Task::getStatus)
                .containsExactly(CANCELLED);
    }

    @Test
    public void calculateStatusForASuspendedTaskShouldReturnSuspended() {
        assertThat(taskConverter.from(taskEntityBuilder().withSuspended(true).build()))
                .isNotNull()
                .extracting(Task::getStatus)
                .containsExactly(SUSPENDED);
    }

    @Test
    public void calculateStatusForAnAssignedTaskShouldReturnAssigned() {
        assertThat(taskConverter.from(taskBuilder().withAssignee("testUser").build()))
                .isNotNull()
                .extracting(Task::getStatus)
                .containsExactly(ASSIGNED);
    }

    @Test
    public void calculateStatusCreatedAndNotAssignedTaskShouldReturnCreated() {
        assertThat(taskConverter.from(taskBuilder().build()))
                .isNotNull()
                .extracting(Task::getStatus)
                .containsExactly(CREATED);
    }
}