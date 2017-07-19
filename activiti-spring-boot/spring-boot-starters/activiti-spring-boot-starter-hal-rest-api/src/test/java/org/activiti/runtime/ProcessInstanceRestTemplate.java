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

import java.util.Map;

import org.activiti.client.model.commands.StartProcessInstanceCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.*;

@Component
public class ProcessInstanceRestTemplate {

    public static final String PROCESS_INSTANCES_RELATIVE_URL = "/process-instances/";

    @Autowired
    private TestRestTemplate testRestTemplate;

    public ResponseEntity<ProcessInstance> startProcess(String processDefinitionId,
                                                        Map<String, Object> variables) {
        StartProcessInstanceCmd cmd = new StartProcessInstanceCmd(processDefinitionId, variables);
        HttpEntity<StartProcessInstanceCmd> requestEntity = new HttpEntity<>(cmd);

        ResponseEntity<ProcessInstance> responseEntity = testRestTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL,
                                                                             HttpMethod.POST,
                                                                             requestEntity,
                                                                             new ParameterizedTypeReference<ProcessInstance>() {
                                                                             });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getId()).isNotNull();
        return responseEntity;
    }

    public ResponseEntity<ProcessInstance> startProcess(String processDefinitionId) {
        return startProcess(processDefinitionId,
                            null);
    }

    public ResponseEntity<ProcessInstance> getProcessInstance(ResponseEntity<ProcessInstance> processInstanceEntity) {

        ResponseEntity<ProcessInstance> responseEntity = testRestTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + processInstanceEntity.getBody().getId(),
                                                                                        HttpMethod.GET,
                                                                                        null,
                                                                                        new ParameterizedTypeReference<ProcessInstance>() {
                                                                                        });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        return responseEntity;
    }

    public ResponseEntity<PagedResources<Task>> getTasks(ResponseEntity<ProcessInstance> processInstanceEntity) {
        ResponseEntity<PagedResources<Task>> responseEntity = testRestTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + processInstanceEntity.getBody().getId() + "/tasks",
                                                                                  HttpMethod.GET,
                                                                                  null,
                                                                                  new ParameterizedTypeReference<PagedResources<Task>>() {
                                                                                  });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        return responseEntity;
    }

    public ResponseEntity<Resource<Map<String, Object>>> getVariables(ResponseEntity<ProcessInstance> processInstanceEntity) {
        ResponseEntity<Resource<Map<String, Object>>> responseEntity = testRestTemplate.exchange(ProcessInstanceRestTemplate.PROCESS_INSTANCES_RELATIVE_URL + processInstanceEntity.getBody().getId() + "/variables",
                                                                                           HttpMethod.GET,
                                                                                           null,
                                                                                           new ParameterizedTypeReference<Resource<Map<String, Object>>>() {
                                                                                           });
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        return responseEntity;
    }

}
