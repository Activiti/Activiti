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
package org.activiti.spring.boot.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.tuple;

import java.util.HashMap;
import java.util.Map;

import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.RuntimeTestConfiguration;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TaskRuntimeStandaloneTaskTest {

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @Autowired
    private TaskRuntimeEventListeners taskRuntimeEventListeners;

    @AfterEach
    public void taskCleanUp(){
        taskCleanUpUtil.cleanUpWithAdmin();
    }

    @Test
    public void createStandaloneTaskForUser() {

        securityUtil.logInAs("user");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("cure Skipper")
                .withAssignee("user")
                .build());

        assertThat(RuntimeTestConfiguration.createdTasks).contains(standAloneTask.getId());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getAssignee()).isEqualTo("user");
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);
        assertThat(task.isStandalone()).isTrue();

        Task deletedTask = taskRuntime.delete(TaskPayloadBuilder
                .delete()
                .withTaskId(task.getId())
                .withReason("test clean up")
                .build());

        assertThat(deletedTask).isNotNull();
        assertThat(deletedTask.getStatus()).isEqualTo(Task.TaskStatus.CANCELLED);

        tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getContent()).hasSize(0);
    }

    @Test
    public void shouldEmmitEventForStandAloneTaskDeletion() {
        //given
        securityUtil.logInAs("user");

        Task firstTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("First task")
                                                         .withAssignee("user")
                                                         .build());
        Task secondTask = taskRuntime.create(TaskPayloadBuilder.create()
                                                         .withName("Second task")
                                                         .withAssignee("user")
                                                         .build());

        //when
        taskRuntime.delete(TaskPayloadBuilder
                                   .delete()
                                   .withTaskId(secondTask.getId())
                                   .build());

        //then
        assertThat(taskRuntimeEventListeners.getCancelledTasks())
                .extracting(Task::getId, Task::getName)
                .contains(tuple(secondTask.getId(), secondTask.getName()))
                .doesNotContain(tuple(firstTask.getId(), firstTask.getName()));
    }

    @Test
    public void createStandaloneTaskForGroup() {

        securityUtil.logInAs("garth");

        Task standAloneTask = taskRuntime.create(TaskPayloadBuilder.create()
                .withName("find Lucien Sanchez")
                .withCandidateGroup("doctor")
                .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                50));

        assertThat(tasks.getContent()).hasSize(1);
        Task task = tasks.getContent().get(0);

        assertThat(task.getId()).isEqualTo(standAloneTask.getId());
        assertThat(task.getAssignee()).isNull();
        assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.CREATED);
        assertThat(task.isStandalone()).isTrue();

        Task claimedTask = taskRuntime.claim(TaskPayloadBuilder.claim().withTaskId(task.getId()).build());

        assertThat(claimedTask.getAssignee()).isEqualTo("garth");
        assertThat(claimedTask.getStatus()).isEqualTo(Task.TaskStatus.ASSIGNED);

        Task deletedTask = taskRuntime.delete(TaskPayloadBuilder
                .delete()
                .withTaskId(task.getId())
                .withReason("test clean up")
                .build());

        assertThat(deletedTask).isNotNull();
        assertThat(deletedTask.getStatus()).isEqualTo(Task.TaskStatus.CANCELLED);

        tasks = taskRuntime.tasks(Pageable.of(0,
                50));
        assertThat(tasks.getContent()).hasSize(0);


    }

    @Test
    public void createStandaloneTaskFailWithEmptyName() {

        securityUtil.logInAs("user");

        //when
        Throwable throwable = catchThrowable(() -> taskRuntime.create(TaskPayloadBuilder.create()
                                                                      .withAssignee("user")
                                                                      .build()));

        //then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class);


        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));

        assertThat(tasks.getContent()).hasSize(0);
    }

    @Test
    public void should_throwExceptionOnTaskSave_when_charactersNotAllowedInVariableName() {

        securityUtil.logInAs("user");

        Task task = taskRuntime.create(TaskPayloadBuilder.create()
                                                 .withName("name")
                                                 .withAssignee("user")
                                                 .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);

        Map<String,Object> variables = new HashMap<>();
        variables.put("var_name1","good_value");
        variables.put("!wrong_name","!any_value>");
        Throwable throwable = catchThrowable(() -> taskRuntime.save(TaskPayloadBuilder.save()
                                                                    .withTaskId(task.getId())
                                                                    .withVariables(variables)
                                                                    .build()));

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throwExceptionOnTaskComplete_when_charactersNotAllowedInVariableName() {

        securityUtil.logInAs("user");

        Task task = taskRuntime.create(TaskPayloadBuilder.create()
                                                 .withName("name")
                                                 .withAssignee("user")
                                                 .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);

        Map<String,Object> variables = new HashMap<>();
        variables.put("var_name1","good_value");
        variables.put("!wrong_name","!any_value>");
        Throwable throwable = catchThrowable(() -> taskRuntime.complete(TaskPayloadBuilder.complete()
                                                                        .withTaskId(task.getId())
                                                                        .withVariables(variables)
                                                                        .build()));

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throwExceptionOnCreateVariable_when_charactersNotAllowedInVariableName() {

        securityUtil.logInAs("user");

        Task task = taskRuntime.create(TaskPayloadBuilder.create()
                                                 .withName("name")
                                                 .withAssignee("user")
                                                 .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);

        Throwable throwable = catchThrowable(() -> taskRuntime.createVariable(TaskPayloadBuilder.createVariable()
                                                                              .withTaskId(task.getId())
                                                                              .withVariable("!wrong_name", "value")
                                                                              .build()));

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throwExceptionOnUpdateVariable_when_charactersNotAllowedInVariableName() {

        securityUtil.logInAs("user");

        Task task = taskRuntime.create(TaskPayloadBuilder.create()
                                                 .withName("name")
                                                 .withAssignee("user")
                                                 .build());

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50));

        assertThat(tasks.getContent()).hasSize(1);

        Throwable throwable = catchThrowable(() -> taskRuntime.updateVariable(TaskPayloadBuilder.updateVariable()
                                                                              .withTaskId(task.getId())
                                                                              .withVariable("!wrong_name", "value")
                                                                              .build()));

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class);
    }
}
