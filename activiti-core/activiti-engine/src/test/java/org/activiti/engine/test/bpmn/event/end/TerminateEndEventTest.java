/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.bpmn.event.end;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**


 */
public class TerminateEndEventTest extends PluggableActivitiTestCase {

  public static int serviceTaskInvokedCount = 0;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    serviceTaskInvokedCount = 0;
    serviceTaskInvokedCount2 = 0;
  }

  public static class CountDelegate implements JavaDelegate {

    public void execute(DelegateExecution execution) {
      serviceTaskInvokedCount++;

      // leave only 3 out of n subprocesses
      execution.setVariableLocal("terminate", serviceTaskInvokedCount > 3);
    }
  }

  public static int serviceTaskInvokedCount2 = 0;

  public static class CountDelegate2 implements JavaDelegate {

    public void execute(DelegateExecution execution) {
      serviceTaskInvokedCount2++;
    }
  }

  @Deployment
  public void testProcessTerminate() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
    assertThat(executionEntities).isEqualTo(3);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateTask").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);

    assertHistoricProcessInstanceDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT);
    assertHistoricTasksDeleteReason(pi, null, "check before termination");
    assertHistoricTasksDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "check before end");
    assertHistoricActivitiesDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "preNormalTerminateTask");
    assertHistoricActivitiesDeleteReason(pi, null, "preTerminateTask");
  }

  @Deployment
  public void testProcessTerminateAll() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateTask").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);

    assertHistoricProcessInstanceDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT);
    assertHistoricTasksDeleteReason(pi, null, "check before termination");
    assertHistoricTasksDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "check before end");
    assertHistoricActivitiesDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "preNormalTerminateTask");
    assertHistoricActivitiesDeleteReason(pi, null, "preTerminateTask");
  }

  @Deployment
  public void testTerminateWithSubProcess() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the process and
    long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
    assertThat(executionEntities).isEqualTo(4);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);

    assertHistoricProcessInstanceDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT);
    assertHistoricTasksDeleteReason(pi, null, "check before termination");
    assertHistoricTasksDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "check before end");
    assertHistoricActivitiesDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "preNormalEnd");
    assertHistoricActivitiesDeleteReason(pi, null, "preTerminateEnd");
  }

  @Deployment
  public void testTerminateWithSubProcess2() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // Completing the task -> terminal end event -> subprocess ends
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);

    assertHistoricProcessInstanceDeleteReason(pi, null);
    assertHistoricTasksDeleteReason(pi, null, "check before termination", "check before end");
    assertHistoricActivitiesDeleteReason(pi, null, "preNormalEnd", "preTerminateEnd");
    assertHistoricActivitiesDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "SubProcess_1");
  }

  @Deployment
  public void testTerminateWithSubProcessTerminateAll() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // Completing the task -> terminal end event -> all ends (termninate all)
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);

    assertHistoricProcessInstanceDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT);
    assertHistoricTasksDeleteReason(pi, null, "check before end");
    assertHistoricTasksDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "check before termination");
    assertHistoricActivitiesDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "preTerminateEnd");
    assertHistoricActivitiesDeleteReason(pi, null, "preNormalEnd");
  }

  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateWithCallActivity.bpmn",
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessNoTerminate.bpmn"
  })
  public void testTerminateWithCallActivity() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertThat(subProcessInstance).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);

    assertHistoricProcessInstanceDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT);
    assertHistoricTasksDeleteReason(pi, null, "check before termination");
    assertHistoricTasksDeleteReason(subProcessInstance, DeleteReason.TERMINATE_END_EVENT, "Perform Sample");
    assertHistoricActivitiesDeleteReason(pi, null, "preTerminateEnd");
    assertHistoricActivitiesDeleteReason(subProcessInstance, DeleteReason.TERMINATE_END_EVENT, "task");
  }

	@Deployment(resources = {
	    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateWithCallActivityTerminateAll.bpmn20.xml",
	    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessNoTerminate.bpmn" })
	public void testTerminateWithCallActivityTerminateAll() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
	  assertThat(subProcessInstance).isNotNull();

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId())
		    .taskDefinitionKey("preTerminateEnd").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
		assertHistoricProcessInstanceDetails(pi);

		assertHistoricProcessInstanceDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT);
	  assertHistoricTasksDeleteReason(pi, null, "check before termination");
	  assertHistoricTasksDeleteReason(subProcessInstance, DeleteReason.TERMINATE_END_EVENT, "Perform Sample");
	  assertHistoricActivitiesDeleteReason(pi, null, "preTerminateEnd");
	  assertHistoricActivitiesDeleteReason(subProcessInstance, DeleteReason.TERMINATE_END_EVENT, "task");
	}


  @Deployment(resources = {
          "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInExclusiveGatewayWithCallActivity.bpmn",
          "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessNoTerminate.bpmn"
  })
  public void testTerminateInExclusiveGatewayWithCallActivity() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample-terminateAfterExclusiveGateway");

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertThat(subProcessInstance).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", 1);
    taskService.complete(task.getId(), variables);

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInExclusiveGatewayWithMultiInstanceSubProcess() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample-terminateAfterExclusiveGateway");

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", 1);
    taskService.complete(task.getId(), variables);

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);

    assertHistoricProcessInstanceDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT);
    assertHistoricTasksDeleteReason(pi, null, "check before termination");
    assertHistoricTasksDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "User Task");
    assertHistoricActivitiesDeleteReason(pi, null, "preTerminateEnd");
    assertHistoricActivitiesDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "task");
  }

  @Deployment
  public void testTerminateInExclusiveGatewayWithMultiInstanceSubProcessTerminateAll() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample-terminateAfterExclusiveGateway");

    // Completing the task once should only destroy ONE multi instance
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("task").list();
    assertThat(tasks).hasSize(5);

    for (int i=0; i<5; i++) {
    	taskService.complete(tasks.get(i).getId());
    	assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count() > 0).isTrue();
    }

    // Other task will now finish the process instance
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", 1);
    taskService.complete(task.getId(), variables);

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count() == 0).isTrue();
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcess() throws Exception {
  	ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

  	// should terminate the subprocess and continue the parent
  	long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
  	assertThat(executionEntities > 0).isTrue();

  	Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
  	taskService.complete(task.getId());

  	assertProcessEnded(pi.getId());
  	assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessTerminateAll() throws Exception {
  	ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");
  	assertProcessEnded(pi.getId());
  	assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessWithBoundary() throws Exception {
    Date startTime = new Date();

    // Test terminating process via boundary timer

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventWithBoundary");

    assertThat(taskService.createTaskQuery().processInstanceId(pi.getId()).count()).isEqualTo(3);

    // Set clock time to '1 hour and 5 seconds' ahead to fire timer
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
    waitForJobExecutorToProcessAllJobs(5000L);

    // timer has fired
    assertThat(managementService.createJobQuery().count()).isEqualTo(0L);

    assertProcessEnded(pi.getId());

    assertHistoricProcessInstanceDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT);
    assertHistoricTasksDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "check before normal end");
    assertHistoricActivitiesDeleteReason(pi, DeleteReason.TERMINATE_END_EVENT, "outerTask");

    // Test terminating subprocess

    pi = runtimeService.startProcessInstanceByKey("terminateEndEventWithBoundary");

    assertThat(taskService.createTaskQuery().processInstanceId(pi.getId()).count()).isEqualTo(3);

    // a job for boundary event timer should exist
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1L);

    // Complete sub process task that leads to a terminate end event
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTermInnerTask").singleResult();
    taskService.complete(task.getId());

    // 'preEndInnerTask' task in subprocess should have been terminated, only outerTask should exist
    assertThat(taskService.createTaskQuery().processInstanceId(pi.getId()).count()).isEqualTo(1);

    // job for boundary event timer should have been removed
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0L);

    // complete outerTask
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("outerTask").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessWithBoundaryTerminateAll() throws Exception {
    // Test terminating subprocess

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventWithBoundary");

    assertThat(taskService.createTaskQuery().processInstanceId(pi.getId()).count()).isEqualTo(3);

    // Complete sub process task that leads to a terminate end event
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTermInnerTask").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessConcurrent() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    long executionEntities = runtimeService.createExecutionQuery().count();
    assertThat(executionEntities > 0).isTrue();

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessConcurrentTerminateAll() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");
    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessConcurrentTerminateAll2() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(2);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskName("User Task").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessConcurrentMultiInstance() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertThat(tasks).hasSize(4); // 3 user tasks in MI  +1 (preNormalEnd) = 4 (2 were killed because it went directly to the terminate end event)

    long executionEntitiesCount = runtimeService.createExecutionQuery().count();
    assertThat(executionEntitiesCount).isEqualTo(9);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    executionEntitiesCount = runtimeService.createExecutionQuery().count();
    assertThat(executionEntitiesCount).isEqualTo(8);

    tasks = taskService.createTaskQuery().list();
    for (Task t : tasks) {
      taskService.complete(t.getId());
    }

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessConcurrentMultiInstance2() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).taskName("User Task").list();
    assertThat(tasks).hasSize(3);

    for (Task t : tasks) {
    	taskService.complete(t.getId());
    }

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessConcurrentMultiInstanceTerminateAll() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");
    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment(resources = {"org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrentCallActivity.bpmn",
          "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateAfterUserTask.bpmn",
          "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testTerminateInCallActivityConcurrentCallActivity() throws Exception {
    // GIVEN - process instance starts and creates 2 subProcessInstances (with 2 user tasks - preTerminate and my task)
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventInCallActivityConcurrentCallActivity");
    assertThat(runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).list()).hasSize(2);

    // WHEN - complete -> terminate end event
    Task preTerminate = taskService.createTaskQuery().taskName("preTerminate").singleResult();
    taskService.complete(preTerminate.getId());

    //THEN - super process is not finished together
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count()).isEqualTo(1);
  }

  @Deployment
  public void testTerminateInSubProcessMultiInstance() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    long executionEntities = runtimeService.createExecutionQuery().count();
    assertThat(executionEntities > 0).isTrue();

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessSequentialConcurrentMultiInstance() throws Exception {

    // Starting multi instance with 5 instances; terminating 2, finishing 3
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    long remainingExecutions = runtimeService.createExecutionQuery().count();
    assertThat(remainingExecutions > 0).isTrue();

    // three finished
    assertThat(serviceTaskInvokedCount2).isEqualTo(3);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    // last task remaining
    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testTerminateInSubProcessSequentialConcurrentMultiInstanceTerminateAll() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");
    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivity.bpmn",
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessTerminate.bpmn"
  })
  public void testTerminateInCallActivity() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the called process and continue the parent
    long executionEntities = runtimeService.createExecutionQuery().count();
    assertThat(executionEntities > 0).isTrue();

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityMulitInstance.bpmn",
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessTerminate.bpmn"
  })
  public void testTerminateInCallActivityMultiInstance() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the called process and continue the parent
    long executionEntities = runtimeService.createExecutionQuery().count();
    assertThat(executionEntities > 0).isTrue();

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

	@Deployment(resources = {
	    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityMulitInstance.bpmn",
	    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessTerminateTerminateAll.bpmn20.xml" })
	public void testTerminateInCallActivityMultiInstanceTerminateAll() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");
		assertProcessEnded(pi.getId());
		assertHistoricProcessInstanceDetails(pi);
	}

  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrent.bpmn",
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessConcurrentTerminate.bpmn"
  })
  public void testTerminateInCallActivityConcurrent() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the called process and continue the parent
    long executionEntities = runtimeService.createExecutionQuery().count();
    assertThat(executionEntities > 0).isTrue();

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

  @Deployment
  public void testMiCallActivityParallel() {
  	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testMiCallActivity");

  	List<Task> aTasks = taskService.createTaskQuery().taskName("A").list();
  	assertThat(aTasks).hasSize(5);

  	List<Task> bTasks = taskService.createTaskQuery().taskName("B").list();
  	assertThat(bTasks).hasSize(5);

  	// Completing B should terminate one instance (it goes to a terminate end event)
  	int bTasksCompleted = 0;
  	for (Task bTask : bTasks) {

  		taskService.complete(bTask.getId());
  		bTasksCompleted++;

  		aTasks = taskService.createTaskQuery().taskName("A").list();
    	assertThat(aTasks).hasSize(5-bTasksCompleted);
  	}

  	Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
  	assertThat(task.getName()).isEqualTo("After call activity");

  	taskService.complete(task.getId());
  	assertProcessEnded(processInstance.getId());
  	assertHistoricProcessInstanceDetails(processInstance);
  }

  @Deployment
  public void testMiCallActivitySequential() {
  	ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testMiCallActivity");

  	List<Task> aTasks = taskService.createTaskQuery().taskName("A").list();
  	assertThat(aTasks).hasSize(1);

  	List<Task> bTasks = taskService.createTaskQuery().taskName("B").list();
  	assertThat(bTasks).hasSize(1);

  	// Completing B should terminate one instance (it goes to a terminate end event)
  	for (int i=0; i<9; i++) {

  		Task bTask = taskService.createTaskQuery().taskName("B").singleResult();

  		taskService.complete(bTask.getId());

  		if (i != 8) {
  			aTasks = taskService.createTaskQuery().taskName("A").list();
  			assertThat(aTasks).as("Expected task for i=" + i).hasSize(1);

  			bTasks = taskService.createTaskQuery().taskName("B").list();
            assertThat(bTasks).as("Expected task for i=" + i).hasSize(1);
  		}
  	}

  	Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
  	assertThat(task.getName()).isEqualTo("After call activity");

  	taskService.complete(task.getId());
  	assertProcessEnded(processInstance.getId());
  	assertHistoricProcessInstanceDetails(processInstance);

  }

  @Deployment(resources={
      "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrent.bpmn",
      "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessConcurrentTerminateTerminateAll.bpmn20.xml"
    })
    public void testTerminateInCallActivityConcurrentTerminateAll() throws Exception {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");
      assertProcessEnded(pi.getId());
      assertHistoricProcessInstanceDetails(pi);
    }

  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrentMulitInstance.bpmn",
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessConcurrentTerminate.bpmn"
  })
  public void testTerminateInCallActivityConcurrentMulitInstance() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the called process and continue the parent
    long executionEntities = runtimeService.createExecutionQuery().count();
    assertThat(executionEntities > 0).isTrue();

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
    assertHistoricProcessInstanceDetails(pi);
  }

	@Deployment(resources = {
	    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrentMulitInstance.bpmn",
	    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessConcurrentTerminateTerminateAll.bpmn20.xml" })
	public void testTerminateInCallActivityConcurrentMulitInstanceTerminateALl() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");
		assertProcessEnded(pi.getId());
		assertHistoricProcessInstanceDetails(pi);
	}

	@Deployment
	public void testTerminateNestedSubprocesses() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestTerminateNestedSubprocesses");

		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
		assertThat(tasks.get(0).getName()).isEqualTo("A");
		assertThat(tasks.get(1).getName()).isEqualTo("B");
		assertThat(tasks.get(2).getName()).isEqualTo("D");
		assertThat(tasks.get(3).getName()).isEqualTo("E");
		assertThat(tasks.get(4).getName()).isEqualTo("F");

		// Completing E should finish the lower subprocess and make 'H' active
		taskService.complete(tasks.get(3).getId());
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("H").singleResult();
		assertThat(task).isNotNull();

		// Completing A should make C active
		taskService.complete(tasks.get(0).getId());
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("C").singleResult();
		assertThat(task).isNotNull();

		// Completing C should make I active
		taskService.complete(task.getId());
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("I").singleResult();
		assertThat(task).isNotNull();

		// Completing I and B should make G active
		taskService.complete(task.getId());
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("G").singleResult();
		assertThat(task).isNull();
		taskService.complete(tasks.get(1).getId());
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("G").singleResult();
		assertThat(task).isNotNull();
	}

	@Deployment
	public void testTerminateNestedSubprocessesTerminateAll1() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestTerminateNestedSubprocesses");
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("E").singleResult();

		// Completing E leads to a terminate end event with termninate all set to true
		taskService.complete(task.getId());
		assertProcessEnded(processInstance.getId());
		assertHistoricProcessInstanceDetails(processInstance);
	}

	@Deployment
	public void testTerminateNestedSubprocessesTerminateAll2() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestTerminateNestedSubprocesses");
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("A").singleResult();

		// Completing A and C leads to a terminate end event with termninate all set to true
		taskService.complete(task.getId());
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("C").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(processInstance.getId());
	}

	@Deployment
	public void testTerminateNestedMiSubprocesses() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestTerminateNestedMiSubprocesses");

		taskService.complete(taskService.createTaskQuery().taskName("A").singleResult().getId());

		// Should have 7 tasks C active
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("C").list();
		assertThat(tasks).hasSize(7);

		// Completing these should lead to task I being active
		for (Task task : tasks) {
			taskService.complete(task.getId());
		}

		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("I").singleResult();
		assertThat(task).isNotNull();

		// Should have 3 instances of E active
		tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("E").list();
		assertThat(tasks).hasSize(3);

		// Completing these should make H active
		for (Task t : tasks) {
			taskService.complete(t.getId());
		}
		task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("H").singleResult();
		assertThat(task).isNotNull();
	}

	@Deployment
	public void testTerminateNestedMiSubprocessesSequential() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestTerminateNestedMiSubprocesses");

		taskService.complete(taskService.createTaskQuery().taskName("A").singleResult().getId());

		// Should have 7 tasks C active after each other
		for (int i=0; i<7; i++) {
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("C").singleResult();
			assertThat(task).as("Task was null for i = " + i).isNotNull();
			taskService.complete(task.getId());
		}

		// I should be active now
		assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("I").singleResult()).isNotNull();

		// Should have 3 instances of E active after each other
		for (int i=0; i<3; i++) {
			assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("D").count()).isEqualTo(1);
			assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("F").count()).isEqualTo(1);

			// Completing F should not finish the subprocess
			taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("F").singleResult().getId());

			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("E").singleResult();
			taskService.complete(task.getId());
		}

		assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("H").singleResult()).isNotNull();
	}

	@Deployment
	public void testTerminateNestedMiSubprocessesTerminateAll1() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestTerminateNestedMiSubprocesses");
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("E").list().get(0);
		taskService.complete(task.getId());
		assertProcessEnded(processInstance.getId());
		assertHistoricProcessInstanceDetails(processInstance);
	}

	@Deployment
	public void testTerminateNestedMiSubprocessesTerminateAll2() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestTerminateNestedMiSubprocesses");
		taskService.complete(taskService.createTaskQuery().taskName("A").singleResult().getId());
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("C").list().get(0);
		taskService.complete(task.getId());
		assertProcessEnded(processInstance.getId());
		assertHistoricProcessInstanceDetails(processInstance);
	}

	@Deployment
	public void testTerminateNestedMiSubprocessesTerminateAll3() { // Same as 1, but sequential Multi-Instance
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestTerminateNestedMiSubprocesses");
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("E").list().get(0);
		taskService.complete(task.getId());
		assertProcessEnded(processInstance.getId());
		assertHistoricProcessInstanceDetails(processInstance);
	}

	@Deployment
	public void testTerminateNestedMiSubprocessesTerminateAll4() { // Same as 2, but sequential Multi-Instance
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestTerminateNestedMiSubprocesses");
		taskService.complete(taskService.createTaskQuery().taskName("A").singleResult().getId());
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("C").list().get(0);
		taskService.complete(task.getId());
		assertProcessEnded(processInstance.getId());
		assertHistoricProcessInstanceDetails(processInstance);
	}

	@Deployment
	public void testNestedCallActivities() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestNestedCallActivities");

		// Verify the tasks
		List<Task> tasks = assertTaskNames(processInstance,
				asList("B", "B", "B", "B", "Before A", "Before A", "Before A", "Before A", "Before B", "Before C"));

		// Completing 'before c'
		taskService.complete(tasks.get(9).getId());
		tasks = assertTaskNames(processInstance,
				asList("After C", "B", "B", "B", "B", "Before A", "Before A", "Before A", "Before A", "Before B"));

		// Completing 'before A' of one instance
		Task task = taskService.createTaskQuery().taskName("task_subprocess_1").singleResult();
		assertThat(task).isNull();
		taskService.complete(tasks.get(5).getId());

		// Multi instance call activity is sequential, so expecting 5 more times the same task
		for (int i=0; i<6; i++) {
			task = taskService.createTaskQuery().taskName("subprocess1_task").singleResult();
			assertThat(task).as("Task is null for index " + i).isNotNull();
			taskService.complete(task.getId());
		}

		tasks = assertTaskNames(processInstance,
				asList("After A", "After C", "B", "B", "B", "B", "Before A", "Before A", "Before A", "Before B"));

	}

	@Deployment
	public void testNestedCallActivitiesTerminateAll() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestNestedCallActivities");

		// Verify the tasks
		List<Task> tasks = assertTaskNames(processInstance,
				asList("B", "B", "B", "B", "Before A", "Before A", "Before A", "Before A", "Before B", "Before C"));

		// Completing 'Before B' should lead to process instance termination
		taskService.complete(tasks.get(8).getId());
		assertProcessEnded(processInstance.getId());
		assertHistoricProcessInstanceDetails(processInstance);

		// Completing 'Before C' too
		processInstance = runtimeService.startProcessInstanceByKey("TestNestedCallActivities");
		tasks = assertTaskNames(processInstance,
				asList("B", "B", "B", "B", "Before A", "Before A", "Before A", "Before A", "Before B", "Before C"));
		taskService.complete(tasks.get(9).getId());
		assertProcessEnded(processInstance.getId());
		assertHistoricProcessInstanceDetails(processInstance);

		// Now the tricky one. 'Before A' leads to 'callActivity A', which calls subprocess02 which terminates
		processInstance = runtimeService.startProcessInstanceByKey("TestNestedCallActivities");
		tasks = assertTaskNames(processInstance,
				asList("B", "B", "B", "B", "Before A", "Before A", "Before A", "Before A", "Before B", "Before C"));
		taskService.complete(tasks.get(5).getId());
		Task task = taskService.createTaskQuery().taskName("subprocess1_task").singleResult();
		assertThat(task).isNotNull();
		taskService.complete(task.getId());
		assertProcessEnded(processInstance.getId());
		assertHistoricProcessInstanceDetails(processInstance);

	}

	private List<Task> assertTaskNames(ProcessInstance processInstance, List<String> taskNames) {
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
		for (int i=0; i<taskNames.size(); i++) {
			assertThat(tasks.get(i).getName()).as("Task name at index " + i + " does not match").isEqualTo(taskNames.get(i));
		}
		return tasks;
	}

  public void testParseTerminateEndEventDefinitionWithExtensions() {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.parseExtensionElements.bpmn20.xml").deploy();
    ProcessDefinition processDefinitionQuery = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
    BpmnModel bpmnModel = this.processEngineConfiguration.getProcessDefinitionCache()
        .get(processDefinitionQuery.getId()).getBpmnModel();

    Map<String, List<ExtensionElement>> extensionElements = bpmnModel.getProcesses().get(0)
        .findFlowElementsOfType(EndEvent.class).get(0).getExtensionElements();
    assertThat(extensionElements).hasSize(1);
    List<ExtensionElement> strangeProperties = extensionElements.get("strangeProperty");
    assertThat(strangeProperties).hasSize(1);
    ExtensionElement strangeProperty = strangeProperties.get(0);
    assertThat(strangeProperty.getNamespace()).isEqualTo("http://activiti.org/bpmn");
    assertThat(strangeProperty.getElementText()).isEqualTo("value");
    assertThat(strangeProperty.getAttributes()).hasSize(1);
    ExtensionAttribute id = strangeProperty.getAttributes().get("id").get(0);
    assertThat(id.getName()).isEqualTo("id");
    assertThat(id.getValue()).isEqualTo("strangeId");


    repositoryService.deleteDeployment(deployment.getId());
  }

  // Unit test for ACT-4101 : NPE when there are multiple routes to terminateEndEvent, and both are reached
  @Deployment
  public void testThreeExecutionsArrivingInTerminateEndEvent() {
     Map<String, Object> variableMap = new HashMap<String, Object>();
     variableMap.put("passed_QC", false);
     variableMap.put("has_bad_pixel_pattern", true);
     ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("skybox_image_pull_request", variableMap);
     String processInstanceId = processInstance.getId();
     assertThat(processInstance).isNotNull();
     while(processInstance != null) {
       List<Execution> executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
       String activityId = "";
       for (Execution execution : executionList) {
         activityId = execution.getActivityId();
         if (activityId == null
             || activityId.equalsIgnoreCase("quality_control_passed_gateway")
             || activityId.equalsIgnoreCase("parallelgateway1")
             || activityId.equalsIgnoreCase("catch_bad_pixel_signal")
             || activityId.equalsIgnoreCase("throw_bad_pixel_signal")
             || activityId.equalsIgnoreCase("has_bad_pixel_pattern")
             || activityId.equalsIgnoreCase("")) {
               continue;
             }
         runtimeService.trigger(execution.getId());
       }
       processInstance =
           runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
     }

     assertProcessEnded(processInstanceId);
     assertHistoricProcessInstanceDetails(processInstanceId);
  }

  protected void assertHistoricProcessInstanceDetails(ProcessInstance pi) {
    assertHistoricProcessInstanceDetails(pi.getId());
  }

  protected void assertHistoricProcessInstanceDetails(String processInstanceId) {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstanceId).singleResult();

      assertThat(historicProcessInstance.getEndTime()).isNotNull();
      assertThat(historicProcessInstance.getDurationInMillis()).isNotNull();
      assertThat(historicProcessInstance.getEndActivityId()).isNotNull();
    }
  }

  protected void assertHistoricProcessInstanceDeleteReason(ProcessInstance processInstance, String expectedDeleteReason) {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstance.getId()).singleResult();
      if (expectedDeleteReason == null) {
        assertThat(historicProcessInstance.getDeleteReason()).isNull();
      } else {
        assertThat(historicProcessInstance.getDeleteReason().startsWith(expectedDeleteReason)).isTrue();
      }
    }
  }

  protected void assertHistoricTasksDeleteReason(ProcessInstance processInstance, String expectedDeleteReason, String ... taskNames) {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      for (String taskName : taskNames) {
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(processInstance.getId()).taskName(taskName).list();
        assertThat(historicTaskInstances.size() > 0).isTrue();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
          assertThat(historicTaskInstance.getEndTime()).isNotNull();
          if (expectedDeleteReason == null) {
            assertThat(historicTaskInstance.getDeleteReason()).isNull();
          } else {
            assertThat(historicTaskInstance.getDeleteReason().startsWith(expectedDeleteReason)).isTrue();
          }
        }
      }
    }
  }

  protected void assertHistoricActivitiesDeleteReason(ProcessInstance processInstance, String expectedDeleteReason, String ... activityIds) {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      for (String activityId : activityIds) {
        List<HistoricActivityInstance> historicActiviyInstances = historyService.createHistoricActivityInstanceQuery()
            .activityId(activityId).processInstanceId(processInstance.getId()).list();
        assertThat(historicActiviyInstances.size() > 0).isTrue();
        for (HistoricActivityInstance historicActiviyInstance : historicActiviyInstances) {
          assertThat(historicActiviyInstance.getEndTime()).isNotNull();
          if (expectedDeleteReason == null) {
            assertThat(historicActiviyInstance.getDeleteReason()).isNull();
          } else {
            assertThat(historicActiviyInstance.getDeleteReason().startsWith(expectedDeleteReason)).isTrue();
          }
        }
      }
    }
  }

}
