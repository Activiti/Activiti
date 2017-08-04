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

import java.util.Collection;
import java.util.Iterator;

import org.activiti.services.query.app.model.Variable;
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
public class VariablesIT {

    private static final String TASKS_URL = "/v1/query/variables?name=name";
    private static final ParameterizedTypeReference<Variable> VAR_RESPONSE_TYPE = new ParameterizedTypeReference<Variable>() {
    };
    private static final ParameterizedTypeReference<PagedResources<Variable>> PAGED_TASKS_RESPONSE_TYPE = new ParameterizedTypeReference<PagedResources<Variable>>() {
    };

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private VariableRepository variableRepository;

    @Before
    public void setUp() throws Exception{
        Variable variable = new Variable(""+1,"type","name","procInstId","taskId",null,null,"executionId");
        variableRepository.save(variable);
    }

    @Test
    public void shouldGetAvailableVariables() throws Exception {


        Iterator<Variable> variableIterator = variableRepository.findAll().iterator();
        assertThat(variableIterator.hasNext()); //there should be records

        //when
        ResponseEntity<PagedResources<Variable>> responseEntity = executeRequestGetVariables();

        //then
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Collection<Variable> variables = responseEntity.getBody().getContent();
        assertThat(variables).extracting(Variable::getName).contains("name");
        assertThat(variables.size()).isGreaterThanOrEqualTo(1);
    }

    private ResponseEntity<PagedResources<Variable>> executeRequestGetVariables() {
        return testRestTemplate.exchange(TASKS_URL,
                                         HttpMethod.GET,
                                            null,
                                         PAGED_TASKS_RESPONSE_TYPE);
    }


}
