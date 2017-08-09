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

package org.activiti.services.query.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.activiti.services.query.app.model.ProcessInstance;
import org.activiti.services.query.app.model.Variable;
import org.activiti.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.services.query.app.repository.VariableRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:test-application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProcessIntancesIT {

    private static final String PROC_URL = "/v1/query/process-instances";
    private static final ParameterizedTypeReference<PagedResources<ProcessInstance>> PAGED_TASKS_RESPONSE_TYPE = new ParameterizedTypeReference<PagedResources<ProcessInstance>>() {
    };

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ProcessInstanceRepository processInstanceRepository;
    @Autowired
    private VariableRepository variableRepository;

    @Before
    public void setUp() throws Exception{
        ProcessInstance processInstance = new ProcessInstance(1L,
                "processDefinitionId",
                "RUNNING",
                null);

        Variable variable = new Variable(""+1,"type","name","procInstId","taskId",null,null,"executionId");
        variableRepository.save(variable);
        List<Variable> variables = new ArrayList<>();
        variables.add(variable);
        processInstance.setVariables(variables);
        processInstanceRepository.save(processInstance);
    }

    @Test
    public void shouldGetAvailableProcInsts() throws Exception {


        Iterator<ProcessInstance> tasksFromRep = processInstanceRepository.findAll().iterator();
        assertThat(tasksFromRep.hasNext()); //there should be records

        //when
        ResponseEntity<PagedResources<ProcessInstance>> responseEntity = executeRequestGetProcInsts();

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Collection<ProcessInstance> tasks = responseEntity.getBody().getContent();
        assertThat(tasks.size()).isGreaterThanOrEqualTo(1);
    }

    private ResponseEntity<PagedResources<ProcessInstance>> executeRequestGetProcInsts() {
        return testRestTemplate.exchange(PROC_URL,
                                         HttpMethod.GET,
                                            null,
                                         PAGED_TASKS_RESPONSE_TYPE);
    }


}
