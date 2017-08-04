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

import java.util.Date;
import java.util.Optional;

import org.activiti.services.query.app.repository.TaskRepository;
import org.activiti.services.query.app.repository.ProcessInstanceRepository;
import org.activiti.services.query.events.ProcessCompletedEvent;
import org.activiti.services.query.events.ProcessEngineEvent;
import org.activiti.services.query.events.ProcessStartedEvent;
import org.activiti.services.query.events.TaskAssignedEvent;
import org.activiti.services.query.events.TaskCompletedEvent;
import org.activiti.services.query.events.TaskCreatedEvent;
import org.activiti.services.query.app.model.ProcessInstance;
import org.activiti.services.query.app.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBinding(QueryConsumerChannels.class)
@ComponentScan(basePackages = {
        "org.activiti",
        "org.activiti.services.identity.keycloak"
})
public class QueryApplication {

    private static Logger logger = LoggerFactory.getLogger(QueryApplication.class);

    private final ProcessInstanceRepository processInstanceQueryRestResource;

    private final TaskRepository taskQueryRestResource;

    @Autowired
    public QueryApplication(ProcessInstanceRepository processInstanceQueryRestResource,
                            TaskRepository taskQueryRestResource) {
        this.processInstanceQueryRestResource = processInstanceQueryRestResource;
        this.taskQueryRestResource = taskQueryRestResource;
    }

    public static void main(String[] args) {
        SpringApplication.run(QueryApplication.class,
                              args);
    }



    @StreamListener(QueryConsumerChannels.QUERY_CONSUMER)
    public synchronized void receive(ProcessEngineEvent event) {

        logger.info("Event: " + event);
        logger.info("Class: " + event.getClass().getCanonicalName());


        //@TODO: improve selection mechanism
        if (event instanceof ProcessStartedEvent) {
            ProcessStartedEvent startedEvent = (ProcessStartedEvent) event;
            logger.info("Process Instance Id " + startedEvent.getProcessInstanceId());

            processInstanceQueryRestResource.save(
                    new ProcessInstance(Long.parseLong(startedEvent.getProcessInstanceId()),
                                        startedEvent.getProcessDefinitionId(),
                                        "RUNNING",
                                        new Date(startedEvent.getTimestamp()),null));
        } else if (event instanceof ProcessCompletedEvent) {
            ProcessCompletedEvent completedEvent = (ProcessCompletedEvent) event;
            Optional<ProcessInstance> processInstancebyId = processInstanceQueryRestResource.findById(Long.parseLong(completedEvent.getProcessInstanceId()));
            ProcessInstance processInstance = processInstancebyId.get();
            processInstance.setStatus("COMPLETED");
            processInstance.setLastModified(new Date(completedEvent.getTimestamp()));
            processInstanceQueryRestResource.save(processInstance);
        } else if (event instanceof TaskCreatedEvent) {
            TaskCreatedEvent taskCreatedEvent = (TaskCreatedEvent) event;
            Task task = taskCreatedEvent.getTask();
            logger.info("Task id "+task.getId());
            task.setStatus("CREATED");
            task.setLastModified(new Date(taskCreatedEvent.getTimestamp()));


            taskQueryRestResource.save(task);
        } else if (event instanceof TaskAssignedEvent) {
            TaskAssignedEvent taskAssignedEvent = (TaskAssignedEvent) event;
            Optional<Task> taskById = taskQueryRestResource.findById(taskAssignedEvent.getTask().getId());
            Task task = taskById.get();
            task.setAssignee(taskAssignedEvent.getTask().getAssignee());
            task.setStatus("ASSIGNED");
            task.setLastModified(new Date(taskAssignedEvent.getTimestamp()));
            taskQueryRestResource.save(task);
        } else if (event instanceof TaskCompletedEvent) {
            TaskCompletedEvent taskCompletedEvent = (TaskCompletedEvent) event;
            Optional<Task> taskById = taskQueryRestResource.findById(taskCompletedEvent.getTask().getId());
            Task task = taskById.get();
            task.setStatus("COMPLETED");
            task.setLastModified(new Date(taskCompletedEvent.getTimestamp()));
            taskQueryRestResource.save(task);
        }
    }
}