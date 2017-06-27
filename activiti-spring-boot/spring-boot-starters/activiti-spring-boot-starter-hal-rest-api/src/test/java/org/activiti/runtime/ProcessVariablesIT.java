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

import java.util.HashMap;
import java.util.Map;

import org.activiti.BaseRestIT;
import org.activiti.client.model.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessVariablesIT extends BaseRestIT {

    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        processInstanceRestTemplate = new ProcessInstanceRestTemplate(getRestTemplate());
    }

    @Test
    public void should_retrieve_process_variables() throws Exception {
        //given
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName",
                      "Pedro");
        variables.put("lastName",
                      "Silva");
        variables.put("age",
                      15);
        ResponseEntity<ProcessInstance> startResponse = processInstanceRestTemplate.startProcess("ProcessWithVariables",
                                                                                                        variables);

        //when
        ResponseEntity<Resource<Map<String, Object>>> variablesResponse = getRestTemplate().exchange(ProcessInstanceRestTemplate.PROCESS_INSTANCES_RELATIVE_URL + startResponse.getBody().getId() + "/variables",
                                                                                                    HttpMethod.GET,
                                                                                                    null,
                                                                                                    new ParameterizedTypeReference<Resource<Map<String, Object>>>() {
                                                                                  });

        //then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesResponse.getBody().getContent())
                .containsEntry("firstName", "Pedro")
                .containsEntry("lastName", "Silva")
                .containsEntry("age", 15);
    }
}
