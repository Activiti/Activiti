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

package org.activiti.definition;

import org.activiti.services.core.model.ProcessDefinition;
import org.activiti.services.core.model.ProcessDefinitionMeta;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

import java.util.Iterator;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProcessDefinitionIT {

    @Autowired
    private TestRestTemplate restTemplate;

    public static final String PROCESS_DEFINITIONS_URL = "/process-definitions/";
    private static final String PROCESS_WITH_VARIABLES_2 = "ProcessWithVariables2";

    @Test
    public void shouldRetrieveListOfProcessDefinition() throws Exception {
        //given
        //processes are automatically deployed from src/test/resources/processes

        //when
        ResponseEntity<PagedResources<ProcessDefinition>> entity = getProcessDefinitions();

        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getContent()).extracting(ProcessDefinition::getName).contains("ProcessWithVariables", "ProcessWithVariables2", "SimpleProcess", "ProcessWithBoundarySignal");
    }

    private ResponseEntity<PagedResources<ProcessDefinition>> getProcessDefinitions() {
        ParameterizedTypeReference<PagedResources<ProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<ProcessDefinition>>() {
        };
        return restTemplate.exchange(PROCESS_DEFINITIONS_URL,
                                     HttpMethod.GET,
                                     null,
                                     responseType);
    }

    @Test
    public void shouldReturnProcessDefinitionById() throws Exception {
        //given
        ParameterizedTypeReference<ProcessDefinition> responseType = new ParameterizedTypeReference<ProcessDefinition>() {
        };

        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsEntity = getProcessDefinitions();
        assertThat(processDefinitionsEntity).isNotNull();
        assertThat(processDefinitionsEntity.getBody()).isNotNull();
        assertThat(processDefinitionsEntity.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = processDefinitionsEntity.getBody().getContent().iterator().next();

        //when
        ResponseEntity<ProcessDefinition> entity = restTemplate.exchange(PROCESS_DEFINITIONS_URL + aProcessDefinition.getId(),
                                                                         HttpMethod.GET,
                                                                         null,
                                                                         responseType);

        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getId()).isEqualTo(aProcessDefinition.getId());
    }
    
    @Test
    public void shouldReturnProcessDefinitionMetadata() throws Exception {
        //given
        ParameterizedTypeReference<ProcessDefinitionMeta> responseType = new ParameterizedTypeReference<ProcessDefinitionMeta>() {
        };

        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsEntity = getProcessDefinitions();
        assertThat(processDefinitionsEntity).isNotNull();
        assertThat(processDefinitionsEntity.getBody()).isNotNull();
        assertThat(processDefinitionsEntity.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = null;

        Iterator<ProcessDefinition> it = processDefinitionsEntity.getBody().getContent().iterator();
        do {
            aProcessDefinition = it.next();
        } while (!aProcessDefinition.getName().equals(PROCESS_WITH_VARIABLES_2));

        //when
        ResponseEntity<ProcessDefinitionMeta> entity = restTemplate.exchange(PROCESS_DEFINITIONS_URL + aProcessDefinition.getId() + "/meta", HttpMethod.GET, null, responseType);
        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getVariables()).hasSize(3);
        assertThat(entity.getBody().getUsers()).hasSize(4);
        assertThat(entity.getBody().getGroups()).hasSize(4);
        assertThat(entity.getBody().getUserTasks()).hasSize(2);
        assertThat(entity.getBody().getServiceTasks()).hasSize(2);
    }
}