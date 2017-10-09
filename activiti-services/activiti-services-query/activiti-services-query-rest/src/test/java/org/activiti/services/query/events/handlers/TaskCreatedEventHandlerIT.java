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

import java.util.Date;
import java.util.Optional;

import org.activiti.engine.ActivitiException;
import org.activiti.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.events.TaskCreatedEvent;
import org.activiti.services.query.model.ProcessInstance;
import org.activiti.services.query.model.Task;
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

/**
 * TaskCreatedEventHandler JPA Repository Integration Tests
 * 
 */
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = true)
@Sql(value = "classpath:/jpa-test.sql")
@DirtiesContext
public class TaskCreatedEventHandlerIT {

    @Autowired
    private TaskRepository repository;

    @Autowired
    private TaskCreatedEventHandler handler;

    @SpringBootConfiguration
    @EnableJpaRepositories(basePackageClasses = ProcessInstanceRepository.class)
    @EntityScan(basePackageClasses = ProcessInstance.class)
    @Import(TaskCreatedEventHandler.class)
    static class Configuation {
    }

    @Test
    public void contextLoads() {
        // Should pass
    }

    @Test
    public void handleShouldStoreNewTaskInstance() throws Exception {
        String processInstanceId = "0";

        //given
        Task eventTask = new Task(
                                  "task_id",
                                  "assignee",
                                  "name",
                                  "description",
                                  new Date() /*createTime*/,
                                  new Date() /*dueDate*/,
                                  "priority",
                                  "category",
                                  "process_definition_id",
                                  processInstanceId,
                                  "CREATED",
                                  new Date() /*lastModified*/
        );
        TaskCreatedEvent taskCreated = new TaskCreatedEvent(System.currentTimeMillis(),
                                                            "taskCreated",
                                                            "10",
                                                            "process_definition_id",
                                                            processInstanceId,
                                                            eventTask);
        //when
        handler.handle(taskCreated);

        //then
        Optional<Task> result = repository.findById("task_id");

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getProcessInstance()).isNotNull();
    }

    @Test(expected = ActivitiException.class)
    public void handleShouldFailOnNewTaskInstanceWithNonExistingProcessInstanceReference() throws Exception {
        String processInstanceId = "-1";

        //given
        Task eventTask = new Task(
                                  "task_id",
                                  "assignee",
                                  "name",
                                  "description",
                                  new Date() /*createTime*/,
                                  new Date() /*dueDate*/,
                                  "priority",
                                  "category",
                                  "process_definition_id",
                                  processInstanceId,
                                  "CREATED",
                                  new Date() /*lastModified*/
        );

        TaskCreatedEvent taskCreated = new TaskCreatedEvent(System.currentTimeMillis(),
                                                            "taskCreated",
                                                            "10",
                                                            "process_definition_id",
                                                            processInstanceId,
                                                            eventTask);
        //when
        handler.handle(taskCreated);

        //then
        //should throw ActivitiException
    }

}
