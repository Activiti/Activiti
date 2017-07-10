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

import org.activiti.client.model.ProcessInstance;
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

import static org.activiti.runtime.ProcessInstanceRestTemplate.PROCESS_INSTANCES_RELATIVE_URL;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProcessInstanceIT {

    private static final String SIMPLE_PROCESS = "SimpleProcess";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    @Test
    public void shouldStartProcess() throws Exception {
        //when
        ResponseEntity<ProcessInstance> entity = processInstanceRestTemplate.startProcess(SIMPLE_PROCESS);

        //then
        assertThat(entity).isNotNull();
        ProcessInstance returnedProcInst = entity.getBody();
        assertThat(returnedProcInst).isNotNull();
        assertThat(returnedProcInst.getId()).isNotNull();
        assertThat(returnedProcInst.getProcessDefinitionId()).contains("SimpleProcess:");
    }

    @Test
    public void shouldRetrieveProcessInstanceById() throws Exception {
        //given
        ResponseEntity<ProcessInstance> startedProcessEntity = processInstanceRestTemplate.startProcess(SIMPLE_PROCESS);

        //when
        ResponseEntity<ProcessInstance> retrievedEntity = restTemplate.exchange(
                PROCESS_INSTANCES_RELATIVE_URL + startedProcessEntity.getBody().getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ProcessInstance>() {
                });

        //then
        assertThat(retrievedEntity.getBody()).isNotNull();
    }

    @Test
    public void shouldRetrieveListOfProcessInstances() throws Exception {
        //given
        processInstanceRestTemplate.startProcess(SIMPLE_PROCESS);
        processInstanceRestTemplate.startProcess(SIMPLE_PROCESS);
        processInstanceRestTemplate.startProcess(SIMPLE_PROCESS);

        //when
        ResponseEntity<PagedResources<ProcessInstance>> processInstancesPage = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + "?page=0&size=2",
                                                                                                          HttpMethod.GET,
                                                                                                          null,
                                                                                                          new ParameterizedTypeReference<PagedResources<ProcessInstance>>() {
                                                                                                          });

        //then
        assertThat(processInstancesPage).isNotNull();
        assertThat(processInstancesPage.getBody().getContent()).hasSize(2);
        assertThat(processInstancesPage.getBody().getMetadata().getTotalPages()).isGreaterThanOrEqualTo(2);
    }

}