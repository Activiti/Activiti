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

package org.activiti.services.query.events.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.activiti.engine.ActivitiException;
import org.activiti.services.query.app.repository.VariableRepository;
import org.activiti.services.query.events.VariableCreatedEvent;
import org.activiti.services.query.model.QVariable;
import org.activiti.services.query.model.Variable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest(showSql=true)
@Sql(value="classpath:/jpa-test.sql")
@DirtiesContext
public class VariableCreatedEventHandlerIT {

    @Autowired
    private VariableRepository repository;

    @Autowired
    private VariableCreatedEventHandler handler;

    @SpringBootConfiguration
    @EnableJpaRepositories(basePackageClasses = VariableRepository.class)
    @EntityScan(basePackageClasses = Variable.class)
    @Import(VariableCreatedEventHandler.class)
    static class Configuation {
    }

    @Test
    public void contextLoads() {
        // Should pass
    }

    @Test
    public void handleShouldCreateAndStoreVariable() throws Exception {
        //given
    	String executionId = "10";
        long processInstanceId = 0L;
        String taskId = "1";
        String variableName = "var";
        String variableType = String.class.getName();
        VariableCreatedEvent event = new VariableCreatedEvent(System.currentTimeMillis(),
                                                              "variableCreated",
                                                              executionId,
                                                              "process_definition_id",
                                                              String.valueOf(processInstanceId),
                                                              variableName,
                                                              "content",
                                                              variableType,
                                                              taskId);

        //when
        handler.handle(event);

        //then
        Optional<Variable> result=repository.findOne(QVariable.variable.name.eq(variableName));
        
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getProcessInstance()).isNotNull();
        assertThat(result.get().getTask()).isNotNull();
    }	

    @Test
    public void handleShouldCreateAndStoreVariableWithOptionalTaskId() throws Exception {
        //given
    	String executionId = "10";
        long processInstanceId = 0L;
        String taskId = null;
        String variableName = "var1";
        String variableType = String.class.getName();
        VariableCreatedEvent event = new VariableCreatedEvent(System.currentTimeMillis(),
                                                              "variableCreated",
                                                              executionId,
                                                              "process_definition_id",
                                                              String.valueOf(processInstanceId),
                                                              variableName,
                                                              "content",
                                                              variableType,
                                                              taskId);

        //when
        handler.handle(event);

        //then
        Optional<Variable> result=repository.findOne(QVariable.variable.name.eq(variableName));
        
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getProcessInstance()).isNotNull();
        assertThat(result.get().getTask()).isNull();
    }	
    
    @Test(expected=ActivitiException.class)
    public void handleShouldFailCreateAndStoreVariableWithNonExistingProcessInstanceReference() throws Exception {
        //given
    	String executionId = "10";
        long processInstanceId = -1L; // does not exist
        String taskId = null;
        String variableName = "var";
        String variableType = String.class.getName();
        VariableCreatedEvent event = new VariableCreatedEvent(System.currentTimeMillis(),
                                                              "variableCreated",
                                                              executionId,
                                                              "process_definition_id",
                                                              String.valueOf(processInstanceId),
                                                              variableName,
                                                              "content",
                                                              variableType,
                                                              taskId);

        //when
        handler.handle(event);

        //then
        // should throw ActivitiException
    }
    
    @Test(expected=ActivitiException.class)
    public void handleShouldFailCreateAndStoreVariableWithNonExistingTaskReference() throws Exception {
        //given
    	String executionId = "10";
        long processInstanceId = -1L; // does not exist
        String taskId = "-1";
        String variableName = "var";
        String variableType = String.class.getName();
        VariableCreatedEvent event = new VariableCreatedEvent(System.currentTimeMillis(),
                                                              "variableCreated",
                                                              executionId,
                                                              "process_definition_id",
                                                              String.valueOf(processInstanceId),
                                                              variableName,
                                                              "content",
                                                              variableType,
                                                              taskId);

        //when
        handler.handle(event);

        //then
        // should throw ActivitiException
    }	    
}
