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

import org.activiti.client.model.ExtendedProcessInstance;
import org.activiti.client.model.ProcessInstance;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class ProcessInstanceRestTemplate {

    protected static final String PROCESS_INSTANCES_RELATIVE_URL = "/api/runtime/process-instances/";
    private final TestRestTemplate testRestTemplate;

    public ProcessInstanceRestTemplate(TestRestTemplate testRestTemplate) {
        this.testRestTemplate = testRestTemplate;
    }

    public ResponseEntity<ProcessInstance> startProcess(String processDefinitionKey,
                                                        Map<String, Object> variables) {
        ExtendedProcessInstance processInstance = new ExtendedProcessInstance();
        processInstance.setProcessDefinitionKey(processDefinitionKey);
        processInstance.setVariables(variables);
        HttpEntity<ExtendedProcessInstance> requestEntity = new HttpEntity<>(processInstance);

        return testRestTemplate.exchange(PROCESS_INSTANCES_RELATIVE_URL,
                                         HttpMethod.POST,
                                         requestEntity,
                                         new ParameterizedTypeReference<ProcessInstance>() {
                                          });
    }

    public ResponseEntity<ProcessInstance> startProcess(String processDefinitionKey) {
        return startProcess(processDefinitionKey,
                            null);
    }

}
