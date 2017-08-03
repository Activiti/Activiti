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

package org.activiti.starter.tests.runtime;

import org.activiti.starter.tests.keycloak.KeycloakEnabledBaseTestIT;
import org.activiti.starter.tests.keycloak.ProcessInstanceKeycloakRestTemplate;
import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.Task;
import org.activiti.services.core.model.commands.CompleteTaskCmd;
import org.junit.Before;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import static org.activiti.starter.tests.keycloak.ProcessInstanceKeycloakRestTemplate.PROCESS_INSTANCES_RELATIVE_URL;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TasksIT extends KeycloakEnabledBaseTestIT {

    private static final String TASKS_URL = "/v1/tasks/";
    private static final String SIMPLE_PROCESS = "SimpleProcess";
    private static final ParameterizedTypeReference<Task> TASK_RESPONSE_TYPE = new ParameterizedTypeReference<Task>() {
    };
    public static final ParameterizedTypeReference<PagedResources<Task>> PAGED_TASKS_RESPONSE_TYPE = new ParameterizedTypeReference<PagedResources<Task>>() {
    };
    public static final String PROCESS_DEFINITIONS_URL = "/v1/process-definitions/";


    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ProcessInstanceKeycloakRestTemplate processInstanceRestTemplate;

    private Map<String, String> processDefinitionIds = new HashMap<>();


    @Before
    public void setUp() throws Exception{
        keycloaktestuser = "hruser";
        //don't need to set password as same password as testuser
        accessToken = authenticateUser();

        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitions = getProcessDefinitions();
        assertThat(processDefinitions.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(processDefinitions.getBody().getContent()).isNotNull();
        for(ProcessDefinition pd : processDefinitions.getBody().getContent()){
            processDefinitionIds.put(pd.getName(), pd.getId());
        }
    }

    @Test
    public void shouldGetAvailableTasks() throws Exception {
        //we are hruser who is in hr group so we can see tasks

        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);

        //when
        ResponseEntity<PagedResources<Task>> responseEntity = executeRequestGetTasks();

        //then
        assertThat(responseEntity).isNotNull();
        Collection<Task> tasks = responseEntity.getBody().getContent();
        assertThat(tasks).extracting(Task::getName).contains("Perform action");
        assertThat(tasks.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldNotGetTasksWithoutPermission() throws Exception {
        keycloaktestuser = "testuser";
        //don't need to set password as same password as hruser
        accessToken = authenticateUser();

        //now authenticated as testuser who is not in hr group

        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);

        //when
        ResponseEntity<PagedResources<Task>> responseEntity = executeRequestGetTasks();

        //then
        assertThat(responseEntity).isNotNull();
        Collection<Task> tasks = responseEntity.getBody().getContent();
        assertThat(tasks.size()).isEqualTo(0);
    }

    private ResponseEntity<PagedResources<Task>> executeRequestGetTasks() {
        return testRestTemplate.exchange(TASKS_URL,
                                         HttpMethod.GET,
                                            getRequestEntityWithHeaders(),
                                         PAGED_TASKS_RESPONSE_TYPE);
    }


    @Test
    public void shouldGetTasksRelatedToTheGivenProcessInstance() throws Exception {
        //given
        ResponseEntity<ProcessInstance> startProcessResponse = processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);

        //when
        ResponseEntity<PagedResources<Task>> tasksEntity = testRestTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + startProcessResponse.getBody().getId() + "/tasks",
                HttpMethod.GET,
                getRequestEntityWithHeaders(),
                PAGED_TASKS_RESPONSE_TYPE);

        //then
        assertThat(tasksEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(tasksEntity.getBody().getContent()).extracting(Task::getName).containsExactly("Perform action");
    }


    @Test
    public void shouldGetTaskById() throws Exception {
        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);
        Task task = executeRequestGetTasks().getBody().iterator().next();

        //when
        ResponseEntity<Task> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId(),
                HttpMethod.GET,
                getRequestEntityWithHeaders(),
                TASK_RESPONSE_TYPE);

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isEqualToComparingFieldByField(task);
    }


    @Test
    public void claimTaskShouldSetAssignee() throws Exception {
        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);
        Task task = executeRequestGetTasks().getBody().iterator().next();

        //when
        ResponseEntity<Task> responseEntity = executeRequestClaim(task);


        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getAssignee()).isEqualTo(keycloaktestuser);
    }

    private ResponseEntity<Task> executeRequestClaim(Task task) {
        return testRestTemplate.exchange(TASKS_URL + task.getId() + "/claim",
                                         HttpMethod.POST,
                                            getRequestEntityWithHeaders(),
                                         TASK_RESPONSE_TYPE);
    }

    @Test
    public void releaseTaskShouldSetAssigneeBackToNull() throws Exception {
        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);
        Task task = executeRequestGetTasks().getBody().iterator().next();

        executeRequestClaim(task);

        //when
        ResponseEntity<Task> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId() + "/release",
                                                                        HttpMethod.POST,
                                                                        getRequestEntityWithHeaders(),
                                                                        TASK_RESPONSE_TYPE);

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getAssignee()).isNull();
    }


    @Test
    public void shouldCompleteATask() throws Exception {
        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);
        Task task = executeRequestGetTasks().getBody().iterator().next();

        //when
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId() + "/complete",
                HttpMethod.POST,
                getRequestEntityWithHeaders(),
                new ParameterizedTypeReference<Void>() {
                });

        //then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void shouldCompleteATaskPassingInputVariables() throws Exception {
        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),accessToken);
        Task task = executeRequestGetTasks().getBody().iterator().next();

        CompleteTaskCmd completeTaskCmd = new CompleteTaskCmd(task.getId(), Collections.singletonMap("myVar",
                "any"));

        //when
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId() + "/complete",
                HttpMethod.POST,
                new HttpEntity(completeTaskCmd,getHeaders(accessToken.getToken())),
                new ParameterizedTypeReference<Void>() {
                });

        //then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    private ResponseEntity<PagedResources<ProcessDefinition>> getProcessDefinitions() {
        ParameterizedTypeReference<PagedResources<ProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<ProcessDefinition>>() {
        };

        return testRestTemplate.exchange(PROCESS_DEFINITIONS_URL,
                                     HttpMethod.GET,
                                        getRequestEntityWithHeaders(),
                                     responseType);
    }
}
