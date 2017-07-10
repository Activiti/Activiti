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

package org.activiti.runtime.events;

import java.util.Collections;
import java.util.Map;

import org.activiti.client.model.ProcessInstance;
import org.activiti.client.model.SignalInfo;
import org.activiti.client.model.Task;
import org.activiti.runtime.ProcessInstanceRestTemplate;
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
import org.springframework.test.context.junit4.SpringRunner;

import static org.activiti.runtime.ProcessInstanceRestTemplate.PROCESS_INSTANCES_RELATIVE_URL;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignalIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProcessInstanceRestTemplate  processInstanceRestTemplate;

    @Test
    public void processShouldTakeExceptionPathWhenSignalIsSent() throws Exception {
        //given
        ResponseEntity<ProcessInstance> startProcessEntity = processInstanceRestTemplate.startProcess("ProcessWithBoundarySignal");
        SignalInfo signalInfo = new SignalInfo();
        signalInfo.setName("go");

        //when
        ResponseEntity<Void> responseEntity = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + "/send-signal",
                                                              HttpMethod.POST,
                                                              new HttpEntity<>(signalInfo),
                                                              new ParameterizedTypeReference<Void>() {
                                                              });

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseEntity<PagedResources<Task>> taskEntity = processInstanceRestTemplate.getTasks(startProcessEntity);
        assertThat(taskEntity.getBody().getContent()).extracting(Task::getName).containsExactly("Boundary target");
    }

    @Test
    public void processShouldHaveVariablesSetWhenSignalCarriesVariables() throws Exception {
        //given
        ResponseEntity<ProcessInstance> startProcessEntity = processInstanceRestTemplate.startProcess("ProcessWithBoundarySignal");
        SignalInfo signalInfo = new SignalInfo();
        signalInfo.setName("go");
        signalInfo.setInputVariables(Collections.singletonMap("myVar", "myContent"));

        //when
        ResponseEntity<Void> responseEntity = restTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL + "/send-signal",
                                                              HttpMethod.POST,
                                                              new HttpEntity<>(signalInfo),
                                                              new ParameterizedTypeReference<Void>() {
                                                              });

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<PagedResources<Task>> taskEntity = processInstanceRestTemplate.getTasks(startProcessEntity);
        assertThat(taskEntity.getBody().getContent()).extracting(Task::getName).containsExactly("Boundary target");

        ResponseEntity<Resource<Map<String, Object>>> variablesEntity = processInstanceRestTemplate.getVariables(startProcessEntity);
        assertThat(variablesEntity.getBody().getContent()).containsEntry("myVar", "myContent");
    }


}
