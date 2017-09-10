package org.activiti.services.audit.mongo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.audit.mongo.events.ActivityCancelledEventDocument;
import org.activiti.services.audit.mongo.events.ActivityCompletedEventDocument;
import org.activiti.services.audit.mongo.events.ActivityStartedEventDocument;
import org.activiti.services.audit.mongo.events.ProcessCancelledEventDocument;
import org.activiti.services.audit.mongo.events.ProcessCompletedEventDocument;
import org.activiti.services.audit.mongo.events.ProcessEngineEventDocument;
import org.activiti.services.audit.mongo.events.ProcessStartedEventDocument;
import org.activiti.services.audit.mongo.events.SequenceFlowTakenEventDocument;
import org.activiti.services.audit.mongo.events.TaskAssignedEventDocument;
import org.activiti.services.audit.mongo.events.TaskCompletedEventDocument;
import org.activiti.services.audit.mongo.events.TaskCreatedEventDocument;
import org.activiti.services.audit.mongo.events.VariableCreatedEventDocument;
import org.activiti.services.audit.mongo.events.VariableDeletedEventDocument;
import org.activiti.services.audit.mongo.events.VariableUpdatedEventDocument;
import org.activiti.services.audit.mongo.repository.EventsCustomRepository;
import org.activiti.services.audit.mongo.repository.EventsRepository;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.ProcessInstance.ProcessInstanceStatus;
import org.activiti.services.core.model.Task;
import org.activiti.services.events.ActivityCancelledEventImpl;
import org.activiti.services.events.ActivityCompletedEventImpl;
import org.activiti.services.events.ActivityStartedEventImpl;
import org.activiti.services.events.ProcessCancelledEventImpl;
import org.activiti.services.events.ProcessCompletedEventImpl;
import org.activiti.services.events.ProcessStartedEventImpl;
import org.activiti.services.events.SequenceFlowTakenEventImpl;
import org.activiti.services.events.TaskAssignedEventImpl;
import org.activiti.services.events.TaskCompletedEventImpl;
import org.activiti.services.events.TaskCreatedEventImpl;
import org.activiti.services.events.VariableCreatedEventImpl;
import org.activiti.services.events.VariableDeletedEventImpl;
import org.activiti.services.events.VariableUpdatedEventImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = EventsMongoRepositoryTest.EventsMongoRepositoryConfig.class)
public class EventsMongoRepositoryTest {

    @Autowired
    private EventsCustomRepository customRepositor;

    @Autowired
    private EventsRepository eventsRepository;

    @Before
    public void setUp() {
        eventsRepository.deleteAll();
    }

    @Test
    public void testAllEventType() throws IOException {
        List<ProcessEngineEventDocument> events = getAllEvents();
        customRepositor.insertAll(events.toArray(new ProcessEngineEventDocument[events.size()]));

        List<ProcessEngineEventDocument> retrievedEvents = eventsRepository.findAll();
        assertEquals(events.size(), retrievedEvents.size());

        assertThat(((ActivityCancelledEventDocument) events.get(0)),
                   is(samePropertyValuesAs((ActivityCancelledEventDocument) retrievedEvents.get(0))));

        assertThat(((ActivityStartedEventDocument) events.get(1)),
                     is(samePropertyValuesAs((ActivityStartedEventDocument) retrievedEvents.get(1))));

        assertThat(((ActivityCompletedEventDocument) events.get(2)),
                     is(samePropertyValuesAs((ActivityCompletedEventDocument) retrievedEvents.get(2))));

        assertThat(((ProcessCompletedEventDocument) events.get(3)),
                     is(samePropertyValuesAs((ProcessCompletedEventDocument) retrievedEvents.get(3))));

        assertThat(((ProcessCancelledEventDocument) events.get(4)),
                   is(samePropertyValuesAs((ProcessCancelledEventDocument) retrievedEvents.get(4))));

        assertThat(((ProcessStartedEventDocument) events.get(5)),
                   is(samePropertyValuesAs((ProcessStartedEventDocument) retrievedEvents.get(5))));

        assertThat(((SequenceFlowTakenEventDocument) events.get(6)),
                   is(samePropertyValuesAs((SequenceFlowTakenEventDocument) retrievedEvents.get(6))));

        assertThat(((TaskAssignedEventDocument) events.get(7)),
                   is(samePropertyValuesAs((TaskAssignedEventDocument) retrievedEvents.get(7))));

        assertThat(((TaskCompletedEventDocument) events.get(8)),
                   is(samePropertyValuesAs((TaskCompletedEventDocument) retrievedEvents.get(8))));

        assertThat(((TaskCreatedEventDocument) events.get(9)),
                   is(samePropertyValuesAs((TaskCreatedEventDocument) retrievedEvents.get(9))));

        assertThat(((VariableCreatedEventDocument) events.get(10)),
                   is(samePropertyValuesAs((VariableCreatedEventDocument) retrievedEvents.get(10))));

        assertThat(((VariableDeletedEventDocument) events.get(11)),
                   is(samePropertyValuesAs((VariableDeletedEventDocument) retrievedEvents.get(11))));

        assertThat(((VariableUpdatedEventDocument) events.get(12)),
                   is(samePropertyValuesAs((VariableUpdatedEventDocument) retrievedEvents.get(12))));
    }

    private List<ProcessEngineEventDocument> getAllEvents() throws IOException {
        String applicationName = "mock-app-name";
        String processDefinitionId = "test_process";
        String processInstanceId = "1";
        String executionId = "2";
        String activityId = "test_activity";
        String activityName = "Test activity";
        String processInstanceName = "Test process instance";
        String processInstanceDescription = "This is a test process instance.";
        String initiator = "test_initiator";
        String businessKey = "test_business_key";
        String processInstanceStatus = ProcessInstanceStatus.RUNNING.toString();
        String taskId = "test_task";
        String owner = "test_owner";
        String assignee = "test_assignee";
        String taskName = "Test task";
        String description = "This is a test task.";
        int priority = 50;
        String parentTaskId = "parent_test_task";
        String variableName = "var1";
        String variableValue = "abc";
        String variableType = "string";

        List<ProcessEngineEvent> events = new ArrayList<>();
        events.add(new ActivityCancelledEventImpl(applicationName,
                                                  executionId,
                                                  processDefinitionId,
                                                  processInstanceId,
                                                  activityId,
                                                  activityName,
                                                  "userTask",
                                                  "Activity cancel cause"));
        events.add(new ActivityStartedEventImpl(applicationName, executionId, processDefinitionId, processInstanceId, activityId, activityName, "userTask"));
        events.add(new ActivityCompletedEventImpl(applicationName, executionId, processDefinitionId, processInstanceId, activityId, activityName, "userTask"));
        ProcessInstance processInstance = new ProcessInstance(processInstanceId,
                                                              processInstanceName,
                                                              processInstanceDescription,
                                                              processDefinitionId,
                                                              initiator,
                                                              new Date(),
                                                              businessKey,
                                                              processInstanceStatus);
        events.add(new ProcessCompletedEventImpl(applicationName, executionId, processDefinitionId, processInstanceId, processInstance));
        events.add(new ProcessCancelledEventImpl(applicationName, executionId, processDefinitionId, processInstanceId, "Process cancel cause"));
        events.add(new ProcessStartedEventImpl(applicationName,
                                               executionId,
                                               processDefinitionId,
                                               processInstanceId,
                                               "nested_process",
                                               "3"));
        events.add(new SequenceFlowTakenEventImpl(applicationName,
                                                  executionId,
                                                  processDefinitionId,
                                                  processInstanceId,
                                                  "4",
                                                  "start_event",
                                                  "Start event",
                                                  "startEvent",
                                                  "user_task",
                                                  "User task",
                                                  "userTask"));
        Task task = new Task(taskId,
                             owner,
                             assignee,
                             taskName,
                             description,
                             new Date(),
                             new Date(),
                             new Date(),
                             priority,
                             processDefinitionId,
                             processInstanceId,
                             parentTaskId,
                             Task.TaskStatus.ASSIGNED.toString());
        events.add(new TaskAssignedEventImpl(applicationName,
                                             executionId,
                                             processDefinitionId,
                                             processInstanceId,
                                             task));
        events.add(new TaskCompletedEventImpl(applicationName,
                                              executionId,
                                              processDefinitionId,
                                              processInstanceId,
                                              task));
        events.add(new TaskCreatedEventImpl(applicationName,
                                            executionId,
                                            processDefinitionId,
                                            processInstanceId,
                                            task));
        events.add(new VariableCreatedEventImpl(applicationName,
                                                executionId,
                                                processDefinitionId,
                                                processInstanceId,
                                                variableName,
                                                variableValue,
                                                variableType,
                                                taskId));
        events.add(new VariableDeletedEventImpl(applicationName, executionId, processDefinitionId, processInstanceId, variableName, variableType, taskId));
        events.add(new VariableUpdatedEventImpl(applicationName,
                                                executionId,
                                                processDefinitionId,
                                                processInstanceId,
                                                variableName,
                                                variableValue,
                                                variableType,
                                                taskId));
        List<ProcessEngineEventDocument> documents = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        // convert to ProcessEngineEventDocument.class 
        for (ProcessEngineEvent event : events) {
            documents.add(mapper.readValue(mapper.writeValueAsString(event), ProcessEngineEventDocument.class));
        }
        // set id
        for (ProcessEngineEventDocument document : documents) {
            document.setId(UUID.randomUUID().toString());
        }
        return documents;
    }

    @Configuration
    @ComponentScan({"org.activiti.services.audit.mongo.repository", "org.activiti.services.audit.mongo.test.config"})
    public static class EventsMongoRepositoryConfig {
    }
}
