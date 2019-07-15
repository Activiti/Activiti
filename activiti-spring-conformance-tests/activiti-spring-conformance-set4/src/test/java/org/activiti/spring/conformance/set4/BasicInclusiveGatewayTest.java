package org.activiti.spring.conformance.set4;

import static org.activiti.test.matchers.BPMNStartEventMatchers.startEvent;
import static org.activiti.test.matchers.EndEventMatchers.endEvent;
import static org.activiti.test.matchers.InclusiveGatewayMatchers.inclusiveGateway;
import static org.activiti.test.matchers.ProcessInstanceMatchers.processInstance;
import static org.activiti.test.matchers.ProcessTaskMatchers.taskWithName;
import static org.activiti.test.matchers.SequenceFlowMatchers.sequenceFlow;
import static org.activiti.test.matchers.TaskMatchers.task;
import static org.activiti.test.matchers.TaskMatchers.withAssignee;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.tuple;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.conformance.util.RuntimeTestConfiguration;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.activiti.test.operations.ProcessOperations;
import org.activiti.test.operations.TaskOperations;
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
    
    @Autowired
    private ProcessOperations processOperations;
    
    @Autowired
    private TaskOperations taskOperations;

    @Before
    public void cleanUp() {
        clearEvents();
    }
    
    @Test
    public void testProcessExecutionWithInclusiveGateway() {
        //given
        securityUtil.logInAs("user1");
        
        //given
        ProcessInstance processInstance = processOperations.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .withVariable("input",1)
                .build())

        //then
                .expectFields(processInstance().status(ProcessInstance.ProcessInstanceStatus.RUNNING),
                              processInstance().name("my-process-instance-name"),
                              processInstance().businessKey("my-business-key"))
       
                .expect(processInstance().hasTask("Start Process",
                                                  Task.TaskStatus.ASSIGNED,
                                                  withAssignee("user1")))
                .expectEvents(processInstance().hasBeenStarted(),
                              startEvent("theStart").hasBeenStarted(),
                              startEvent("theStart").hasBeenCompleted(),
                              sequenceFlow("flow1").hasBeenTaken(),
                              taskWithName("Start Process").hasBeenCreated(),
                              taskWithName("Start Process").hasBeenAssigned()
                )
                .andReturn();
  
        // I should be able to get the process instance from the Runtime
        ProcessInstance processInstanceById = processRuntime.processInstance(processInstance.getId());
        assertThat(processInstanceById).isEqualTo(processInstance);
        
        // I should get a task for User1
        GetTasksPayload processInstanceTasksPayload = TaskPayloadBuilder
                                                      .tasks()
                                                      .withProcessInstanceId(processInstance.getId())
                                                      .build();
        
        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50),
                                             processInstanceTasksPayload);
        assertThat(tasks.getTotalItems()).isEqualTo(1);
        Task task = tasks.getContent().get(0);
        
        clearEvents();
        
        //given
        taskOperations.complete(TaskPayloadBuilder
                                .complete()
                                .withTaskId(task.getId())
                                .build())
        //then
                .expectEvents(task().hasBeenCompleted(),
                              sequenceFlow("flow2").hasBeenTaken(),
                              inclusiveGateway("inclusiveGateway").hasBeenStarted(), 
                              inclusiveGateway("inclusiveGateway").hasBeenCompleted(), 
                              sequenceFlow("flow3").hasBeenTaken(),
                              taskWithName("Send e-mail").hasBeenCreated(),
                              sequenceFlow("flow4").hasBeenTaken(),
                              taskWithName("Check account").hasBeenCreated())
                .expect(processInstance().hasTask("Send e-mail",
                                                  Task.TaskStatus.ASSIGNED),
                        processInstance().hasTask("Check account",
                                                  Task.TaskStatus.ASSIGNED)
                );
        
        //then - two tasks should be available
        tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getTotalItems()).isEqualTo(2);
        
        Task task1 = tasks.getContent().get(0);
        Task task2 = tasks.getContent().get(1);
        
        clearEvents();
   
        //given
        taskOperations.complete(TaskPayloadBuilder
                                .complete()
                                .withTaskId(task1.getId())
                                .build())
        //then
                .expectEvents(task().hasBeenCompleted(),
                              inclusiveGateway("inclusiveGatewayEnd").hasBeenStarted())
                .expect(processInstance().hasTask(task2.getName(),
                                                  task2.getStatus()));
        
        //then - only second task should be available
        tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getTotalItems()).isEqualTo(1);
        
        assertThat(tasks.getContent())
        .extracting(Task::getStatus, Task::getName)
        .contains(
                tuple(  task2.getStatus(),
                        task2.getName())
        );
        
        clearEvents();
        
        //complete second task
        taskOperations.complete(TaskPayloadBuilder
                                .complete()
                                .withTaskId(task2.getId())
                                .build())
        //then
                .expectEvents(task().hasBeenCompleted(),
                              inclusiveGateway("inclusiveGatewayEnd").hasBeenStarted(),
                              inclusiveGateway("inclusiveGatewayEnd").hasBeenCompleted(),
                              endEvent("theEnd").hasBeenStarted(),
                              endEvent("theEnd").hasBeenCompleted());
         
        //No tasks should be available
        tasks = taskRuntime.tasks(Pageable.of(0, 50));
        assertThat(tasks.getTotalItems()).isEqualTo(0);              
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
