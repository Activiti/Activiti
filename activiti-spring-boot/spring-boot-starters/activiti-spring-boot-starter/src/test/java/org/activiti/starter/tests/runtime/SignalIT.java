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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.activiti.starter.tests.definition.ProcessDefinitionIT;
import org.activiti.starter.tests.keycloak.KeycloakEnabledBaseTestIT;
import org.activiti.starter.tests.keycloak.ProcessInstanceKeycloakRestTemplate;
import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.Task;
import org.activiti.services.core.model.commands.SignalProcessInstancesCmd;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.activiti.starter.tests.keycloak.ProcessInstanceKeycloakRestTemplate.PROCESS_INSTANCES_RELATIVE_URL;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SignalIT extends KeycloakEnabledBaseTestIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProcessInstanceKeycloakRestTemplate processInstanceRestTemplate;

    private static final String SIGNAL_PROCESS = "ProcessWithBoundarySignal";

    private Map<String, String> processDefinitionIds = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitions = getProcessDefinitions();
        assertThat(processDefinitions.getBody().getContent()).isNotNull();
        for (ProcessDefinition pd : processDefinitions.getBody().getContent()) {
            processDefinitionIds.put(pd.getName(), pd.getId());
        }
    }

    @Test
    public void processShouldTakeExceptionPathWhenSignalIsSent() throws Exception {
        //given
        ResponseEntity<ProcessInstance> startProcessEntity = processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIGNAL_PROCESS),accessToken);
        SignalProcessInstancesCmd signalProcessInstancesCmd = new SignalProcessInstancesCmd("go");

        //when
        ResponseEntity<Void> responseEntity = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + "/signal",
                                                                    HttpMethod.POST,
                                                                    new HttpEntity(signalProcessInstancesCmd,getHeaders(accessToken.getToken())),
                                                                    new ParameterizedTypeReference<Void>() {
                                                                    });

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<PagedResources<Task>> taskEntity = processInstanceRestTemplate.getTasks(startProcessEntity,accessToken);
        assertThat(taskEntity.getBody().getContent()).extracting(Task::getName).containsExactly("Boundary target");
    }

    @Test
    public void processShouldHaveVariablesSetWhenSignalCarriesVariables() throws Exception {
        //given
        ResponseEntity<ProcessInstance> startProcessEntity = processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIGNAL_PROCESS),accessToken);
        SignalProcessInstancesCmd signalProcessInstancesCmd = new SignalProcessInstancesCmd("go",
                                                                                            Collections.singletonMap("myVar",
                                                                                                                  "myContent"));

        //when
        ResponseEntity<Void> responseEntity = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + "/signal",
                                                                    HttpMethod.POST,
                                                                    new HttpEntity(signalProcessInstancesCmd,getHeaders(accessToken.getToken())),
                                                                    new ParameterizedTypeReference<Void>() {
                                                                    });

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<PagedResources<Task>> taskEntity = processInstanceRestTemplate.getTasks(startProcessEntity,accessToken);
        assertThat(taskEntity.getBody().getContent()).extracting(Task::getName).containsExactly("Boundary target");

        ResponseEntity<Resource<Map<String, Object>>> variablesEntity = processInstanceRestTemplate.getVariables(startProcessEntity,accessToken);
        assertThat(variablesEntity.getBody().getContent()).containsEntry("myVar",
                                                                         "myContent");
    }

    private ResponseEntity<PagedResources<ProcessDefinition>> getProcessDefinitions() {
        ParameterizedTypeReference<PagedResources<ProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<ProcessDefinition>>() {
        };
        return restTemplate.exchange(ProcessDefinitionIT.PROCESS_DEFINITIONS_URL,
                                     HttpMethod.GET,
                                     getRequestEntityWithHeaders(),
                                     responseType);
    }
}
