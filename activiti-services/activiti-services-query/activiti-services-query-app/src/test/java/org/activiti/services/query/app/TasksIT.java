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

package org.activiti.services.query.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.activiti.services.query.app.model.Task;
import org.activiti.services.query.app.model.Variable;
import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.app.repository.VariableRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:test-application.properties")
public class TasksIT {

    private static final String TASKS_URL = "/v1/tasks?nameLike=na&priority=priority&variables.name=name";
    private static final ParameterizedTypeReference<PagedResources<Task>> PAGED_TASKS_RESPONSE_TYPE = new ParameterizedTypeReference<PagedResources<Task>>() {
    };

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private VariableRepository variableRepository;

    @Before
    public void setUp() throws Exception {
        Task task = new Task("1",
                             "hruser",
                             "name",
                             "description",
                             null,
                             null,
                             "priority",
                             "category",
                             "processDefinitionId",
                             "processInstanceId",
                             "RUNNING",
                             null,
                             null);
        Variable variable = new Variable("type",
                                         "name",
                                         "procInstId",
                                         "taskId",
                                         null,
                                         null,
                                         "executionId",
                                         "content");
        variableRepository.save(variable);
        List<Variable> variables = new ArrayList<>();
        variables.add(variable);
        task.setVariables(variables);
        taskRepository.save(task);
    }

    @After
    public void tearDown() throws Exception {
        taskRepository.deleteAll();
        variableRepository.deleteAll();
    }

    @Test
    public void shouldGetAvailableTasks() throws Exception {

        Iterator<Task> tasksFromRep = taskRepository.findAll().iterator();
        assertThat(tasksFromRep.hasNext()); //there should be tasks

        //when
        ResponseEntity<PagedResources<Task>> responseEntity = executeRequestGetTasks();

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Collection<Task> tasks = responseEntity.getBody().getContent();
        assertThat(tasks).extracting(Task::getName).contains("name");
        assertThat(tasks.size()).isGreaterThanOrEqualTo(1);
    }

    private ResponseEntity<PagedResources<Task>> executeRequestGetTasks() {
        return testRestTemplate.exchange(TASKS_URL,
                                         HttpMethod.GET,
                                         null,
                                         PAGED_TASKS_RESPONSE_TYPE);
    }
}
