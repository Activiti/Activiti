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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.Task;
import org.activiti.starter.tests.definition.ProcessDefinitionIT;
import org.activiti.starter.tests.helper.ProcessInstanceRestTemplate;
import org.activiti.starter.tests.helper.TaskRestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TaskVariablesIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    @Autowired
    private TaskRestTemplate taskRestTemplate;

    private Map<String, String> processDefinitionIds = new HashMap<>();

    private static final String SIMPLE_PROCESS = "SimpleProcess";

    public static final String TASK_VARIABLES_URL = "/v1/taskId/";

    @Before
    public void setUp() throws Exception {
        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitions = getProcessDefinitions();
        assertThat(processDefinitions.getStatusCode()).isEqualTo(HttpStatus.OK);
        for (ProcessDefinition pd : processDefinitions.getBody().getContent()) {
            processDefinitionIds.put(pd.getName(), pd.getId());
        }
    }

    @Test
    public void shouldRetrieveTaskVariables() throws Exception {
        //given
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1",
                      "test1");
        ResponseEntity<ProcessInstance> startResponse = processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),
                                                                                                 variables);
        ResponseEntity<PagedResources<Task>> tasks = processInstanceRestTemplate.getTasks(startResponse);

        String taskId = tasks.getBody().getContent().iterator().next().getId();
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("var2",
                          "test2");
        taskRestTemplate.setVariablesLocal(taskId, taskVariables);

        //when
        ResponseEntity<Resource<Map<String, Object>>> variablesResponse = taskRestTemplate.getVariablesLocal(taskId);

        //then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesResponse.getBody().getContent())
                                                            .containsEntry("var2",
                                                                           "test2")
                                                            .doesNotContainKey("var1");

        // when
        variablesResponse = taskRestTemplate.getVariables(taskId);

        // then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesResponse.getBody().getContent())
                                                            .containsEntry("var2",
                                                                           "test2")
                                                            .containsEntry("var1",
                                                                           "test1");

        // give
        taskVariables = new HashMap<>();
        taskVariables.put("var2",
                          "test2-update");
        taskVariables.put("var3",
                          "test3");
        taskRestTemplate.setVariables(taskId, taskVariables);

        // when
        variablesResponse = taskRestTemplate.getVariables(taskId);

        // then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesResponse.getBody().getContent())
                                                            .containsEntry("var1",
                                                                           "test1")
                                                            .containsEntry("var2",
                                                                           "test2-update")
                                                            .containsEntry("var3",
                                                                           "test3");

        // when
        variablesResponse = taskRestTemplate.getVariablesLocal(taskId);

        // then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesResponse.getBody().getContent())
                                                            .containsEntry("var2",
                                                                           "test2-update")
                                                            .doesNotContainKey("var1")
                                                            .doesNotContainKey("var3");
    }

    private ResponseEntity<PagedResources<ProcessDefinition>> getProcessDefinitions() {
        ParameterizedTypeReference<PagedResources<ProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<ProcessDefinition>>() {
        };
        return restTemplate.exchange(ProcessDefinitionIT.PROCESS_DEFINITIONS_URL,
                                     HttpMethod.GET,
                                     null,
                                     responseType);
    }
}
