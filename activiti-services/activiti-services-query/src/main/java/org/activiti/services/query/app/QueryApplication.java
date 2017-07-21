package org.activiti.services.query.app;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.activiti.services.query.app.dao.TaskRepository;
import org.activiti.services.query.app.dao.VariableRepository;
import org.activiti.services.query.app.model.Variable;
import org.activiti.services.query.events.ProcessCompletedEvent;
import org.activiti.services.query.events.ProcessEngineEvent;
import org.activiti.services.query.events.ProcessStartedEvent;
import org.activiti.services.query.events.TaskAssignedEvent;
import org.activiti.services.query.events.TaskCompletedEvent;
import org.activiti.services.query.events.TaskCreatedEvent;
import org.activiti.services.query.app.model.ProcessInstance;
import org.activiti.services.query.app.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@SpringBootApplication
@EnableBinding(QueryConsumerChannels.class)
public class QueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryApplication.class,
                              args);
    }

    @Autowired
    ProcessInstanceQueryRestResource processInstanceQueryRestResource;

    @Autowired
    TaskRepository taskQueryRestResource;

    @Autowired
    VariableRepository variableRepository;

    private int id=0;

    @StreamListener(QueryConsumerChannels.QUERY_CONSUMER)
    public synchronized void receive(ProcessEngineEvent event) {

        System.out.println("Event: " + event);
        System.out.println("Class: " + event.getClass().getCanonicalName());


        //@TODO: improve selection mechanism
        if (event instanceof ProcessStartedEvent) {
            ProcessStartedEvent startedEvent = (ProcessStartedEvent) event;
            System.out.println("Process Instance Id " + startedEvent.getProcessInstanceId());

            //TODO:temporary hack to test querying for nested attributes
            List<Variable> variables = new ArrayList<Variable>();
            Variable variable = new Variable(""+id++,"type","name","procInstId","taskId",new Date(),new Date(),"executionId");
            variableRepository.save(variable);
            variables.add(variable);

            processInstanceQueryRestResource.save(
                    new ProcessInstance(Long.parseLong(startedEvent.getProcessInstanceId()),
                                        startedEvent.getProcessDefinitionId(),
                                        "RUNNING",
                                        new Date(startedEvent.getTimestamp()),variables));
        } else if (event instanceof ProcessCompletedEvent) {
            ProcessCompletedEvent completedEvent = (ProcessCompletedEvent) event;
            Optional<ProcessInstance> processInstancebyId = processInstanceQueryRestResource.findById(completedEvent.getProcessInstanceId());
            ProcessInstance processInstance = processInstancebyId.get();
            processInstance.setStatus("COMPLETED");
            processInstance.setLastModified(new Date(completedEvent.getTimestamp()));
            processInstanceQueryRestResource.save(processInstance);
        } else if (event instanceof TaskCreatedEvent) {
            TaskCreatedEvent taskCreatedEvent = (TaskCreatedEvent) event;
            Task task = taskCreatedEvent.getTask();
            task.setStatus("CREATED");
            task.setLastModified(new Date(taskCreatedEvent.getTimestamp()));

            //TODO:temporary hack to test querying for nested attributes
            List<Variable> variables = new ArrayList<Variable>();
            Variable variable = new Variable(""+id++,"type","name","procInstId","taskId",new Date(),new Date(),"executionId");
            variableRepository.save(variable);
            variables.add(variable);
            task.setVariables(variables);

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
