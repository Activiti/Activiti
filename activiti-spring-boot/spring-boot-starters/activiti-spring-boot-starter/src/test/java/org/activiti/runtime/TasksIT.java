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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.activiti.definition.ProcessDefinitionIT;
import org.activiti.engine.UserGroupLookupProxy;
import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.Task;
import org.activiti.services.core.model.commands.CompleteTaskCmd;
import org.activiti.services.core.tests.pageable.AuthenticationWrapper;
import org.activiti.services.core.tests.pageable.PageableTaskService;
import org.activiti.services.rest.controllers.TaskController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TasksIT {

    private static final String TASKS_URL = "/tasks/";
    private static final String SIMPLE_PROCESS = "SimpleProcess";
    private static final ParameterizedTypeReference<Task> TASK_RESPONSE_TYPE = new ParameterizedTypeReference<Task>() {
    };
    public static final ParameterizedTypeReference<PagedResources<Task>> PAGED_TASKS_RESPONSE_TYPE = new ParameterizedTypeReference<PagedResources<Task>>() {
    };

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    //mocking identity integration as that is to be supplied in implementation of starter (e.g. see sample)
    private UserGroupLookupProxy userGroupLookupProxy = Mockito.mock(UserGroupLookupProxy.class);
    private AuthenticationWrapper authenticationWrapper = Mockito.mock(AuthenticationWrapper.class);

    @Autowired
    private PageableTaskService pageableTaskService; //want to inject mocks for identity

    @Autowired
    private TaskController taskController;

    private Map<String, String> processDefinitionIds = new HashMap<>();

    @Before
    public void setUp() {
        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitions = getProcessDefinitions();
        assertThat(processDefinitions.getBody().getContent()).hasSize(3);
        for (ProcessDefinition pd : processDefinitions.getBody().getContent()) {
            processDefinitionIds.put(pd.getName(),
                                     pd.getId());
        }
        pageableTaskService.setUserGroupLookupProxy(userGroupLookupProxy);
        pageableTaskService.setAuthenticationWrapper(authenticationWrapper);
        taskController.setAuthenticationWrapper(authenticationWrapper);
    }

    @Test
    public void shouldGetAvailableTasks() throws Exception {
        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));

        //when
        ResponseEntity<PagedResources<Task>> responseEntity = executeRequestGetTasks();

        //then
        assertThat(responseEntity).isNotNull();
        Collection<Task> tasks = responseEntity.getBody().getContent();
        assertThat(tasks).extracting(Task::getName).contains("Perform action");
        assertThat(tasks.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void shouldGetAvailableTasksForUser() throws Exception {

        //user is not candidate for task by id but set as candidate by groups
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("testuser");
        when(userGroupLookupProxy.getGroupsForCandidateUser("testuser")).thenReturn(Arrays.asList("hr"));

        //should now get all of the tasks for SIMPLE_PROCESS
        shouldGetAvailableTasks();

        //verify that the mock was used
        verify(authenticationWrapper).getAuthenticatedUserId();
        verify(userGroupLookupProxy).getGroupsForCandidateUser("testuser");
    }

    @Test
    public void shouldNotGetTasksWithoutPermission() throws Exception {

        //now authenticated as testuser who is not in hr group as not setting any groups
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("testuser");
        when(userGroupLookupProxy.getGroupsForCandidateUser("testuser")).thenReturn(null);

        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));

        //when
        ResponseEntity<PagedResources<Task>> responseEntity = executeRequestGetTasks();

        //then
        assertThat(responseEntity).isNotNull();
        Collection<Task> tasks = responseEntity.getBody().getContent();
        assertThat(tasks.size()).isEqualTo(0);
    }

    @Test
    public void shouldGetTasksRelatedToTheGivenProcessInstance() throws Exception {
        //given
        ResponseEntity<ProcessInstance> startProcessResponse = processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));

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
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));
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
        when(authenticationWrapper.getAuthenticatedUserId()).thenReturn("testuser");
        when(userGroupLookupProxy.getGroupsForCandidateUser("testuser")).thenReturn(Arrays.asList("hr"));

        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));
        Task task = executeRequestGetTasks().getBody().iterator().next();

        //when
        ResponseEntity<Task> responseEntity = executeRequestClaim(task);

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getAssignee()).isEqualTo("testuser");
    }

    @Test
    public void claimTaskShouldFailWhenNoUserAvailable() throws Exception {
        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));
        Task task = executeRequestGetTasks().getBody().iterator().next();

        //when
        ResponseEntity<Task> responseEntity = executeRequestClaim(task);

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        // This is failing because the claim should use the Security/IDM layer
    }

    private ResponseEntity<Task> executeRequestClaim(Task task) {
        return testRestTemplate.exchange(TASKS_URL + task.getId() + "/claim",
                                         HttpMethod.POST,
                                         null,
                                         TASK_RESPONSE_TYPE);
    }

    @Test
    public void releaseTaskShouldSetAssigneeBackToNull() throws Exception {
        //given
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));
        Task task = executeRequestGetTasks().getBody().iterator().next();

        executeRequestClaim(task);

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
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));
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
        processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS));
        Task task = executeRequestGetTasks().getBody().iterator().next();

        CompleteTaskCmd completeTaskCmd = new CompleteTaskCmd(Collections.singletonMap("myVar",
                                                                                       "any"));

        //when
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(TASKS_URL + task.getId() + "/complete",
                                                                        HttpMethod.POST,
                                                                        new HttpEntity<>(completeTaskCmd),
                                                                        new ParameterizedTypeReference<Void>() {
                                                                        });

        //then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
    }

    private ResponseEntity<PagedResources<ProcessDefinition>> getProcessDefinitions() {
        ParameterizedTypeReference<PagedResources<ProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<ProcessDefinition>>() {
        };
        return testRestTemplate.exchange(ProcessDefinitionIT.PROCESS_DEFINITIONS_URL,
                                         HttpMethod.GET,
                                         null,
                                         responseType);
    }
}
