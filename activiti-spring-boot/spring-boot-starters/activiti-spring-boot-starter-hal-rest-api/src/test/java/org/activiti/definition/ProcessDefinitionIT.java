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

import org.activiti.BaseRestIT;
import org.activiti.client.model.ProcessDefinition;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;

public class ProcessDefinitionIT extends BaseRestIT {

    private static final String PROCESS_DEFINITIONS_URL = "/api/repository/process-definitions/";

    @Test
    public void should_retrieve_list_of_processDefinition() throws Exception {
        //given


        //when
        ResponseEntity<PagedResources<ProcessDefinition>> entity = getProcessDefinitions();
        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getContent())
                .extracting(
                        ProcessDefinition::getName
                ).contains("Process with variables", "SimpleProcess");
    }

    private ResponseEntity<PagedResources<ProcessDefinition>> getProcessDefinitions() {
        ParameterizedTypeReference<PagedResources<ProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<ProcessDefinition>>() {
        };
        return getRestTemplate().exchange(PROCESS_DEFINITIONS_URL,
                                          HttpMethod.GET,
                                          null,
                                          responseType);
    }

    @Test
    public void should_return_process_definition_by_id() throws Exception {
        //given
        ParameterizedTypeReference<ProcessDefinition> responseType = new ParameterizedTypeReference<ProcessDefinition>() {
        };

        ResponseEntity<PagedResources<ProcessDefinition>> processDefinitionsEntity = getProcessDefinitions();
        assertThat(processDefinitionsEntity).isNotNull();
        assertThat(processDefinitionsEntity.getBody()).isNotNull();
        assertThat(processDefinitionsEntity.getBody().getContent()).isNotEmpty();
        ProcessDefinition aProcessDefinition = processDefinitionsEntity.getBody().getContent().iterator().next();

        //when
        ResponseEntity<ProcessDefinition> entity = getRestTemplate().exchange(PROCESS_DEFINITIONS_URL + aProcessDefinition.getId(),
                                                                                   HttpMethod.GET,
                                                                                   null,
                                                                                   responseType);

        //then
        assertThat(entity).isNotNull();
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getBody().getId()).isEqualTo(aProcessDefinition.getId());
    }
}