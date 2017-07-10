/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime;

import java.util.Collection;
import java.util.Collections;

import org.activiti.client.model.ClaimTaskInfo;
import org.activiti.client.model.CompleteTaskInfo;
import org.activiti.client.model.ProcessInstance;
import org.activiti.client.model.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.activiti.runtime.ProcessInstanceRestTemplate.PROCESS_INSTANCES_RELATIVE_URL;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TasksIT {

    private static final String TASKS_URL = "/api/tasks/";
    private static final ParameterizedTypeReference<Task> TASK_RESPONSE_TYPE = new ParameterizedTypeReference<Task>() {
    };
    public static final ParameterizedTypeReference<PagedResources<Task>> PAGED_TASKS_RESPONSE_TYPE = new ParameterizedTypeReference<PagedResources<Task>>() {
    };

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    @Test
    public void shouldGetAvailableTasks() throws Exception {
        //given
        processInstanceRestTemplate.startProcess("SimpleProcess");
        processInstanceRestTemplate.startProcess("SimpleProcess");

        //when
        ResponseEntity<PagedResources<Task>> responseEntity = executeRequestGetTasks();

        //then
        assertThat(responseEntity).isNotNull();
        Collection<Task> tasks = responseEntity.getBody().getContent();
        assertThat(tasks).extracting(Task::getName).contains("Perform action");
        assertThat(tasks.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldGetTasksRelatedToTheGivenProcessInstance() throws Exception {
        //given
        ResponseEntity<ProcessInstance> startProcessResponse = processInstanceRestTemplate.startProcess("SimpleProcess");

        //when
        ResponseEntity<PagedResources<Task>> tasksEntity = testRestTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + startProcessResponse.getBody().getId() + "/tasks",
                                                                                  HttpMethod.GET,
                                                                                  null,
                                                                                  PAGED_TASKS_RESPONSE_TYPE);

        //then
        assertThat(tasksEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(tasksEntity.getBody().getContent()).extracting(Task::getName).containsExactly("Perform action");
    }

    private ResponseEntity<PagedResources<Task>> executeRequestGetTasks() {
        return testRestTemplate.exchange(TASKS_URL,
                                         HttpMethod.GET,
                                         null,
                                         PAGED_TASKS_RESPONSE_TYPE);
    }

    @Test
    public void shouldGetTaskById() throws Exception {
        //given
        processInstanceRestTemplate.startProcess("SimpleProcess");
        Task task = executeRequestGetTasks().getBody().iterator().next();

        //when
        ResponseEntity<Task> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId(),
                                                                        HttpMethod.GET,
                                                                        null,
                                                                        TASK_RESPONSE_TYPE);

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(task);
    }

    @Test
    public void claimTaskShouldSetAssignee() throws Exception {
        //given
        processInstanceRestTemplate.startProcess("SimpleProcess");
        Task task = executeRequestGetTasks().getBody().iterator().next();
        ClaimTaskInfo claimTaskInfo = new ClaimTaskInfo();
        claimTaskInfo.setAssignee("peter");

        //when
        ResponseEntity<Task> responseEntity = executeRequestClaim(task,
                                                                  claimTaskInfo);

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody().getAssignee()).isEqualTo("peter");
    }

    private ResponseEntity<Task> executeRequestClaim(Task task,
                                                     ClaimTaskInfo claimTaskInfo) {
        return testRestTemplate.exchange(TASKS_URL + task.getId() + "/claim",
                                         HttpMethod.POST,
                                         new HttpEntity<>(claimTaskInfo),
                                         TASK_RESPONSE_TYPE);
    }

    @Test
    public void releaseTaskShouldSetAssigneeBackToNull() throws Exception {
        //given
        processInstanceRestTemplate.startProcess("SimpleProcess");
        Task task = executeRequestGetTasks().getBody().iterator().next();

        ClaimTaskInfo claimTaskInfo = new ClaimTaskInfo();
        claimTaskInfo.setAssignee("peter");
        executeRequestClaim(task,
                            claimTaskInfo);

        //when
        ResponseEntity<Task> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId() + "/release",
                                                                        HttpMethod.POST,
                                                                        null,
                                                                        TASK_RESPONSE_TYPE);

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getAssignee()).isNull();
    }

    @Test
    public void shouldCompleteATask() throws Exception {
        //given
        processInstanceRestTemplate.startProcess("SimpleProcess");
        Task task = executeRequestGetTasks().getBody().iterator().next();

        //when
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId() + "/complete",
                                                                        HttpMethod.POST,
                                                                        null,
                                                                        new ParameterizedTypeReference<Void>() {
                                                                        });

        //then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void shouldCompleteATaskPassingInputVariables() throws Exception {
        //given
        processInstanceRestTemplate.startProcess("SimpleProcess");
        Task task = executeRequestGetTasks().getBody().iterator().next();

        CompleteTaskInfo completeTaskInfo = new CompleteTaskInfo();
        completeTaskInfo.setInputVariables(Collections.singletonMap("myVar",
                                                                    "any"));

        //when
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId() + "/complete",
                                                                        HttpMethod.POST,
                                                                        new HttpEntity<>(completeTaskInfo),
                                                                        new ParameterizedTypeReference<Void>() {
                                                                        });

        //then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }
}
