package org.activiti.services.query.app;

import java.util.Date;
import java.util.Optional;

import org.activiti.services.query.app.events.ProcessCompletedEvent;
import org.activiti.services.query.app.events.ProcessEngineEvent;
import org.activiti.services.query.app.events.ProcessStartedEvent;
import org.activiti.services.query.app.events.TaskAssignedEvent;
import org.activiti.services.query.app.events.TaskCompletedEvent;
import org.activiti.services.query.app.events.TaskCreatedEvent;
import org.activiti.services.query.app.model.ProcessInstance;
import org.activiti.services.query.app.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@SpringBootApplication
@EnableBinding(QueryConsumerChannels.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,
                              args);
    }

    @Autowired
    ProcessInstanceQueryRestResource processInstanceQueryRestResource;

    @Autowired
    TaskQueryRestResource taskQueryRestResource;

    @StreamListener(QueryConsumerChannels.QUERY_CONSUMER)
    public synchronized void receive(ProcessEngineEvent event) {

        System.out.println("Event: " + event);
        System.out.println("Class: " + event.getClass().getCanonicalName());

        //@TODO: improve selection mechanism
        if (event instanceof ProcessStartedEvent) {
            ProcessStartedEvent startedEvent = (ProcessStartedEvent) event;
            System.out.println("Process Instance Id " + startedEvent.getProcessInstanceId());
            processInstanceQueryRestResource.save(
                    new ProcessInstance(Long.parseLong(startedEvent.getProcessInstanceId()),
                                        startedEvent.getProcessDefinitionId(),
                                        "RUNNING",
                                        new Date(startedEvent.getTimestamp())));
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
