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

import org.activiti.starter.tests.definition.ProcessDefinitionIT;
import org.activiti.starter.tests.keycloak.KeycloakEnabledBaseTestIT;
import org.activiti.starter.tests.keycloak.ProcessInstanceKeycloakRestTemplate;
import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.ProcessInstance;
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
import static org.activiti.starter.tests.keycloak.ProcessInstanceKeycloakRestTemplate.PROCESS_INSTANCES_RELATIVE_URL;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ProcessVariablesIT extends KeycloakEnabledBaseTestIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProcessInstanceKeycloakRestTemplate processInstanceRestTemplate;

    private Map<String, String> processDefinitionIds = new HashMap<>();

    private static final String SIMPLE_PROCESS_WITH_VARIABLES = "ProcessWithVariables";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitions = getProcessDefinitions();
        assertThat(processDefinitions.getStatusCode()).isEqualTo(HttpStatus.OK);
        for (ProcessDefinition pd : processDefinitions.getBody().getContent()) {
            processDefinitionIds.put(pd.getName(), pd.getId());
        }
    }

    @Test
    public void shouldRetrieveProcessVariables() throws Exception {
        //given
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName",
                      "Pedro");
        variables.put("lastName",
                      "Silva");
        variables.put("age",
                      15);
        ResponseEntity<ProcessInstance> startResponse = processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS_WITH_VARIABLES),
                                                                                                 variables,accessToken);

        //when
        ResponseEntity<Resource<Map<String, Object>>> variablesResponse = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + startResponse.getBody().getId() + "/variables",
                                                                                                HttpMethod.GET,
                                                                                                getRequestEntityWithHeaders(),
                                                                                                new ParameterizedTypeReference<Resource<Map<String, Object>>>() {
                                                                                                });

        //then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesResponse.getBody().getContent())
                .containsEntry("firstName",
                               "Pedro")
                .containsEntry("lastName",
                               "Silva")
                .containsEntry("age",
                               15);
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
