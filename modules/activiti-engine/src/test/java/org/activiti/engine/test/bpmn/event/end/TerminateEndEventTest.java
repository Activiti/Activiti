/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.bpmn.event.end;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Tijs Rademakers
 */
public class TerminateEndEventTest extends PluggableActivitiTestCase {

	public static int serviceTaskInvokedCount = 0;

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
		assertEquals(3, executionEntities);

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateTask").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}
	
	@Deployment
	public void testSimpleProcessTerminateWithAuthenticatedUser() throws Exception {
	  try
	  {
	    Authentication.setAuthenticatedUserId("user1");
  	  ProcessInstance pi = runtimeService.startProcessInstanceByKey("simpleProcessTerminateWithAuthenticatedUser");
  
  	  long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
  	  assertEquals(2, executionEntities);
  
  	  Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("ID_6").singleResult();
  	  taskService.complete(task.getId());

      assertProcessEnded(pi.getId());
	  }
	  finally
	  {
	    Authentication.setAuthenticatedUserId(null);
	  }
	}

	@Deployment
	public void testTerminateWithSubProcess() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		// should terminate the process and
		long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
		assertEquals(4, executionEntities);

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}

	@Deployment(resources = { "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateWithCallActivity.bpmn",
	        "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessNoTerminate.bpmn" })
	public void testTerminateWithCallActivity() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
		assertEquals(3, executionEntities); // THe main process has 3 executions
		
		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}

	@Deployment(resources = { "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInExclusiveGatewayWithCallActivity.bpmn",
	        "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessNoTerminate.bpmn" })
	public void testTerminateInExclusiveGatewayWithCallActivity() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample-terminateAfterExclusiveGateway");

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("input", 1);
		taskService.complete(task.getId(), variables);

		assertProcessEnded(pi.getId());
	}

	@Deployment
	public void testTerminateInExclusiveGatewayWithMultiInstanceSubProcess() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample-terminateAfterExclusiveGateway");

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("input", 1);
		taskService.complete(task.getId(), variables);

		assertProcessEnded(pi.getId());
	}

	@Deployment
	public void testTerminateInSubProcess() throws Exception {
		serviceTaskInvokedCount = 0;

		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		// should terminate the subprocess and continue the parent
		long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
		assertEquals(2, executionEntities);

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}

	@Deployment
	public void testTerminateInSubProcessWithBoundary() throws Exception {
		serviceTaskInvokedCount = 0;

		Date startTime = new Date();

		// Test terminating process

		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventWithBoundary");

		assertEquals(3, taskService.createTaskQuery().processInstanceId(pi.getId()).count());

		// Set clock time to '1 hour and 5 seconds' ahead to fire timer
		processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));
		waitForJobExecutorToProcessAllJobs(5000L, 25L);

		// timer has fired
		assertEquals(0L, managementService.createJobQuery().count());

		assertProcessEnded(pi.getId());

		// Test terminating subprocess

		pi = runtimeService.startProcessInstanceByKey("terminateEndEventWithBoundary");

		assertEquals(3, taskService.createTaskQuery().processInstanceId(pi.getId()).count());

		// a job for boundary event timer should exist
		assertEquals(1L, managementService.createJobQuery().count());

		// Complete sub process task that leads to a terminate end event
		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTermInnerTask").singleResult();
		taskService.complete(task.getId());

		// 'preEndInnerTask' task in subprocess should have been terminated,
		// only outerTask should exist
		assertEquals(1, taskService.createTaskQuery().processInstanceId(pi.getId()).count());

		// job for boundary event timer should have been removed
		assertEquals(0L, managementService.createJobQuery().count());

		// complete outerTask
		task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("outerTask").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}

	@Deployment
	public void testTerminateInSubProcessConcurrent() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		long executionEntities = runtimeService.createExecutionQuery().count();
		assertEquals(2, executionEntities);

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}

	@Deployment
	public void testTerminateInSubProcessConcurrentMultiInstance() throws Exception {
		serviceTaskInvokedCount = 0;

		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
		taskService.complete(task.getId());

		List<Execution> waitExecutions = runtimeService.createExecutionQuery().activityId("subWait").list();
		assertEquals(5, waitExecutions.size());
		for (Execution execution : waitExecutions) {
      runtimeService.trigger(execution.getId());
    }
		
		List<Task> tasks = taskService.createTaskQuery().list();
		assertEquals(3, tasks.size()); // The service task delegate should have set the 'terminate' variable 3 times out of the 5 subprocess runs
		for (Task t : tasks) {
			taskService.complete(t.getId());
		}

		assertProcessEnded(pi.getId());
	}

	@Deployment(resources = { "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrentCallActivity.bpmn",
	        "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateAfterUserTask.bpmn", "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
	public void testTerminateInCallActivityConcurrentCallActivity() throws Exception {
		serviceTaskInvokedCount = 0;

		// GIVEN - process instance starts and creates 2 subProcessInstances
		// (with 2 user tasks - preTerminate and my task)
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventInCallActivityConcurrentCallActivity");
		assertThat(runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).list().size(), is(2));

		// WHEN - complete -> terminate end event
		Task preTerminate = taskService.createTaskQuery().taskName("preTerminate").singleResult();
		taskService.complete(preTerminate.getId());
		
		Task myTask = taskService.createTaskQuery().taskDefinitionKey("theTask").singleResult();
		taskService.complete(myTask.getId());

		// THEN - super process is finished together with subprocesses
		assertProcessEnded(pi.getId());
	}

	@Deployment
	public void testTerminateInSubProcessMultiInstance() throws Exception {
		serviceTaskInvokedCount = 0;

		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}

	@Deployment
	public void testTerminateInSubProcessSequentialConcurrentMultiInstance() throws Exception {
		serviceTaskInvokedCount = 0;
		serviceTaskInvokedCount2 = 0;

		// Starting multi instance with 5 instances; terminating 2, finishing 3
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		long remainingExecutions = runtimeService.createExecutionQuery().count();

		// three finished
		assertEquals(3, serviceTaskInvokedCount2);

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
		taskService.complete(task.getId());

		// last task remaining
		assertProcessEnded(pi.getId());
	}

	@Deployment(resources = { "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivity.bpmn",
	        "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessTerminate.bpmn" })
	public void testTerminateInCallActivity() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		// should terminate the called process and continue the parent
		long executionEntities = runtimeService.createExecutionQuery().count();
		assertEquals(2, executionEntities);

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}

	@Deployment(resources = { "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityMulitInstance.bpmn",
	        "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessTerminate.bpmn" })
	public void testTerminateInCallActivityMulitInstance() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}

	@Deployment(resources = { "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrent.bpmn",
	        "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessConcurrentTerminate.bpmn" })
	public void testTerminateInCallActivityConcurrent() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		List<ProcessInstance> childProcesses = runtimeService.createProcessInstanceQuery().processDefinitionKey("terminateEndEventSubprocessExample").list();
    assertEquals(1, childProcesses.size());
    
    Execution waitExecution = runtimeService.createExecutionQuery().activityId("subProcessWait").processInstanceId(childProcesses.get(0).getId()).singleResult();
    runtimeService.trigger(waitExecution.getId());

    List<HistoricProcessInstance> historicChildProcesses = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("terminateEndEventSubprocessExample").list();
    assertEquals(1, historicChildProcesses.size());
    assertNotNull(historicChildProcesses.get(0).getEndTime());
		
		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
		taskService.complete(task.getId());

		assertProcessEnded(pi.getId());
	}

	@Deployment(resources = { "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrentMulitInstance.bpmn",
	        "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessConcurrentTerminate.bpmn" })
	public void testTerminateInCallActivityConcurrentMultiInstance() throws Exception {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

		List<ProcessInstance> childProcesses = runtimeService.createProcessInstanceQuery().processDefinitionKey("terminateEndEventSubprocessExample").list();
		assertEquals(5, childProcesses.size());
		
		for (ProcessInstance childProcess : childProcesses) {
		  Execution waitExecution = runtimeService.createExecutionQuery().activityId("subProcessWait").processInstanceId(childProcess.getId()).singleResult();
	    runtimeService.trigger(waitExecution.getId());
    }
		
		List<HistoricProcessInstance> historicChildProcesses = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("terminateEndEventSubprocessExample").list();
    assertEquals(5, historicChildProcesses.size());
    for (HistoricProcessInstance historicChildProcess : historicChildProcesses) {
      assertNotNull(historicChildProcess.getEndTime());
    }
		
		// should terminate the called process and continue the parent
		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
		taskService.complete(task.getId());
		
		assertProcessEnded(pi.getId());
	}
}