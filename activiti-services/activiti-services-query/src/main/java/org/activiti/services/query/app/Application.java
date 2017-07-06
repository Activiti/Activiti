package org.activiti.services.query.app;

import java.util.Date;

import org.activiti.services.query.app.events.ProcessEngineEvent;
import org.activiti.services.query.app.events.ProcessStartedEvent;
import org.activiti.services.query.app.model.ProcessInstance;
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
        }
//        else if (event instanceof TaskCreatedEvent) {
//            TaskCreatedEvent taskCreatedEvent = (TaskCreatedEvent) event;
//            taskQueryRestResource.save(new Task());
//        }
    }
}
