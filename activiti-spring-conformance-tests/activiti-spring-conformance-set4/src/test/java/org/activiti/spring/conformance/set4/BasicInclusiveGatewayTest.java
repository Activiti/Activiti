package org.activiti.spring.conformance.set4;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.tuple;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.event.VariableEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.Task.TaskStatus;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.conformance.util.RuntimeTestConfiguration;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BasicInclusiveGatewayTest {

    private final String processKey = "basicInclusiveGateway";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @Before
    public void cleanUp() {
        clearEvents();
    }

    @Test
    public void testProcessExecutionWithInclusiveGateway() {

        securityUtil.logInAs("user1");

        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .withVariable("input",1)
                .build());

        //then
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getStatus()).isEqualTo(ProcessInstance.ProcessInstanceStatus.RUNNING);
        assertThat(processInstance.getBusinessKey()).isEqualTo("my-business-key");
        assertThat(processInstance.getName()).isEqualTo("my-process-instance-name");

        // I should be able to get the process instance from the Runtime
        ProcessInstance processInstanceById = processRuntime.processInstance(processInstance.getId());
        assertThat(processInstanceById).isEqualTo(processInstance);

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getTotalItems()).isEqualTo(1);

        Task task = tasks.getContent().get(0);
        assertThat(task.getName()).isEqualTo("Start Process");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.ASSIGNED);
        assertThat(task.getAssignee()).isEqualTo("user1");

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        VariableEvent.VariableEvents.VARIABLE_CREATED,
                        TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                        TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED);

        clearEvents();

        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task.getId()).build());
        
        //then - two tasks should be available
        tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getTotalItems()).isEqualTo(2);
        
        Task task1 = tasks.getContent().get(0);
        Task task2 = tasks.getContent().get(1);
        
      
        assertThat(tasks.getContent())
        .extracting(Task::getStatus, Task::getName)
        .contains(
                tuple(  TaskStatus.ASSIGNED,
                        "Send e-mail"),
                tuple(  TaskStatus.ASSIGNED,
                        "Check account")
        );
        
        //check some events here, including start / complete events for inclusiveGateway 
        assertThat(RuntimeTestConfiguration.collectedEvents)
                .filteredOn(event -> event.getEventType().equals(BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED) ||
                                     event.getEventType().equals(BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED))
                .extracting("eventType",
                            "entity.activityType",
                            "entity.elementId")
                .contains(
                    tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                            "userTask",
                            "task0"),
                    tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                            "inclusiveGateway",
                            "inclusiveGateway"
                            ),
                    tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                            "inclusiveGateway",
                            "inclusiveGateway"),
                    tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                            "userTask",
                            "theTask1"
                            ),
                    tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                            "userTask",
                            "theTask2")
                    );

        clearEvents();
        
        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task1.getId()).build());
        
        //then - only second task should be available
        tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getTotalItems()).isEqualTo(1);
        
        assertThat(tasks.getContent())
        .extracting(Task::getStatus, Task::getName)
        .contains(
                tuple(  task2.getStatus(),
                        task2.getName())
        );
        
        //check some events here, including start event for inclusiveGatewayEnd 
        assertThat(RuntimeTestConfiguration.collectedEvents)
        .filteredOn(event -> event.getEventType().equals(BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED) ||
                             event.getEventType().equals(BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED))
        .extracting("eventType",
                    "entity.activityType",
                    "entity.elementId")
        .containsExactly(
            tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    "userTask",
                    "theTask1"),
            tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    "inclusiveGateway",
                    "inclusiveGatewayEnd"
                    ));

        clearEvents();
        
        
        //complete second task
        taskRuntime.complete(TaskPayloadBuilder.complete().withTaskId(task2.getId()).build());
        
        //No tasks should be available
        tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getTotalItems()).isEqualTo(0);
              
        //check some events here, including start event for inclusiveGatewayEnd 
        assertThat(RuntimeTestConfiguration.collectedEvents)
        .filteredOn(event -> event.getEventType().equals(BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED) ||
                             event.getEventType().equals(BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED))
        .extracting("eventType",
                    "entity.activityType",
                    "entity.elementId")
        .containsExactly(
            tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    "userTask",
                    "theTask2"),
            tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    "inclusiveGateway",
                    "inclusiveGatewayEnd"
                    ),
            tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    "inclusiveGateway",
                    "inclusiveGatewayEnd"
                    ),
            tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                    "endEvent",
                    "theEnd"
                    ),
            tuple(  BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                    "endEvent",
                    "theEnd"
                    ));
        
        clearEvents();
    }

    @After
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0, 50));
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }
        
        clearEvents();
    }
    
    public void clearEvents() {
        RuntimeTestConfiguration.collectedEvents.clear();
    }

}
