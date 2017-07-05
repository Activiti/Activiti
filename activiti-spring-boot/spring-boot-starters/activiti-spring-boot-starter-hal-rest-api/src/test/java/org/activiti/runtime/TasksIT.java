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

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TasksIT {

    private static final String TASKS_URL = "/api/tasks/";
    private static final ParameterizedTypeReference<Task> TASK_RESPONSE_TYPE = new ParameterizedTypeReference<Task>() {
    };

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    @Test
    public void should_get_available_tasks() throws Exception {
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

    private ResponseEntity<PagedResources<Task>> executeRequestGetTasks() {
        return testRestTemplate.exchange(TASKS_URL,
                                         HttpMethod.GET,
                                         null,
                                         new ParameterizedTypeReference<PagedResources<Task>>() {
                                         });
    }

    @Test
    public void should_get_task_by_id() throws Exception {
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
    public void claimTask_should_set_assignee() throws Exception {
        //given
        processInstanceRestTemplate.startProcess("SimpleProcess");
        Task task = executeRequestGetTasks().getBody().iterator().next();
        ClaimTaskInfo claimTaskInfo = new ClaimTaskInfo();
        claimTaskInfo.setAssignee("peter");

        //when
        ResponseEntity<Task> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId() + "/claim",
                                                                        HttpMethod.POST,
                                                                        new HttpEntity<>(claimTaskInfo),
                                                                        TASK_RESPONSE_TYPE);

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody().getAssignee()).isEqualTo("peter");
    }

    @Test
    public void should_complete_a_task() throws Exception {
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
    public void should_complete_a_task_passing_input_variables() throws Exception {
        //given
        processInstanceRestTemplate.startProcess("SimpleProcess");
        Task task = executeRequestGetTasks().getBody().iterator().next();

        CompleteTaskInfo completeTaskInfo = new CompleteTaskInfo();
        completeTaskInfo.setInputVariables(Collections.singletonMap("myVar", "any"));

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
