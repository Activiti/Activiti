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
package org.activiti.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


public class ProcessInstanceSuspensionTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testProcessInstanceActiveByDefault() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.isSuspended()).isFalse();

  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testSuspendActivateProcessInstance() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.isSuspended()).isFalse();

    // suspend
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.isSuspended()).isTrue();

    // activate
    runtimeService.activateProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.isSuspended()).isFalse();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testCannotActivateActiveProcessInstance() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.isSuspended()).isFalse();

    try {
      // activate
      runtimeService.activateProcessInstanceById(processInstance.getId());
      fail("Expected activiti exception");
    } catch (ActivitiException e) {
      // expected
    }

  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testCannotSuspendSuspendedProcessInstance() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance.isSuspended()).isFalse();

    runtimeService.suspendProcessInstanceById(processInstance.getId());

    try {
      runtimeService.suspendProcessInstanceById(processInstance.getId());
      fail("Expected activiti exception");
    } catch (ActivitiException e) {
      // expected
    }

  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcessWithMultipleNestedSubProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
      "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryForActiveAndSuspendedProcessInstances() {
    runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(5);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(5);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(0);

    ProcessInstance piToSuspend = runtimeService.createProcessInstanceQuery().processDefinitionKey("nestedSubProcessQueryTest").singleResult();
    runtimeService.suspendProcessInstanceById(piToSuspend.getId());

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(5);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(4);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(1);

    assertThat(runtimeService.createProcessInstanceQuery().suspended().singleResult().getId()).isEqualTo(piToSuspend.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskSuspendedAfterProcessInstanceSuspension() {

    // Start Process Instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();

    // Suspense process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // Assert that the task is now also suspended
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertThat(task.isSuspended()).isTrue();
    }

    // Activate process instance again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertThat(task.isSuspended()).isFalse();
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testTaskQueryAfterProcessInstanceSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();

    task = taskService.createTaskQuery().active().singleResult();
    assertThat(task).isNotNull();

    // Suspend
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().suspended().count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().active().count()).isEqualTo(0);

    // Activate
    runtimeService.activateProcessInstanceById(processInstance.getId());
    assertThat(taskService.createTaskQuery().count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().suspended().count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().active().count()).isEqualTo(1);

    // Completing should end the process instance
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment
  public void testChildExecutionsSuspendedAfterProcessInstanceSuspend() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testChildExecutionsSuspended");
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertThat(execution.isSuspended()).isTrue();
    }

    // Activate again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertThat(execution.isSuspended()).isFalse();
    }

    // Finish process
    while (taskService.createTaskQuery().count() > 0) {
      for (Task task : taskService.createTaskQuery().list()) {
        taskService.complete(task.getId());
      }
    }
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testProcessInstanceOperationsFailAfterSuspend() {

    // Suspend process instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.messageEventReceived("someMessage", processInstance.getId()))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.messageEventReceived("someMessage", processInstance.getId(), new HashMap<>()))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.removeVariable(processInstance.getId(), "someVariable"))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.removeVariableLocal(processInstance.getId(), "someVariable"))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.removeVariables(processInstance.getId(), Arrays.asList("one", "two", "three")))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.removeVariablesLocal(processInstance.getId(), Arrays.asList("one", "two", "three")))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.setVariable(processInstance.getId(), "someVariable", "someValue"))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.setVariableLocal(processInstance.getId(), "someVariable", "someValue"))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.setVariables(processInstance.getId(), new HashMap<>()))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.setVariablesLocal(processInstance.getId(), new HashMap<>()))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.trigger(processInstance.getId()))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.trigger(processInstance.getId(), new HashMap<>()))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.signalEventReceived("someSignal", processInstance.getId()))
      .withMessageContaining("is suspended");

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.signalEventReceived("someSignal", processInstance.getId(), new HashMap<>()))
      .withMessageContaining("is suspended");
  }

  @Deployment
  public void testSignalEventReceivedAfterProcessInstanceSuspended() {

    final String signal = "Some Signal";

    // Test if process instance can be completed using the signal
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.signalEventReceived(signal);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    // Now test when suspending the process instance: the process instance
    // shouldn't be continued
    processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    runtimeService.signalEventReceived(signal, new HashMap<String, Object>());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    // Activate and try again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/runtime/ProcessInstanceSuspensionTest.testSignalEventReceivedAfterProcessInstanceSuspended.bpmn20.xml")
  public void testSignalEventReceivedAfterMultipleProcessInstancesSuspended() {

    final String signal = "Some Signal";

    // Test if process instance can be completed using the signal
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.signalEventReceived(signal);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

    // Now test when suspending the process instance: the process instance
    // shouldn't be continued
    processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(2);

    runtimeService.signalEventReceived(signal, new HashMap<String, Object>());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(2);

    // Activate and try again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testTaskOperationsFailAfterProcessInstanceSuspend() {

    // Start a new process instance with one task
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Suspend the process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());

    // Yeah, the following is pretty long and boring ... but I didn't have
    // the patience
    // to create separate tests for each of them.

    // Completing the task should fail
    try {
      taskService.complete(task.getId());
      fail("It is not allowed to complete a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Claiming the task should fail
    try {
      taskService.claim(task.getId(), "jos");
      fail("It is not allowed to claim a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Setting variable on the task should fail
    try {
      taskService.setVariable(task.getId(), "someVar", "someValue");
      fail("It is not allowed to set a variable on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Setting variable on the task should fail
    try {
      taskService.setVariableLocal(task.getId(), "someVar", "someValue");
      fail("It is not allowed to set a variable on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Setting variables on the task should fail
    try {
      HashMap<String, String> variables = new HashMap<String, String>();
      variables.put("varOne", "one");
      variables.put("varTwo", "two");
      taskService.setVariables(task.getId(), variables);
      fail("It is not allowed to set variables on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Setting variables on the task should fail
    try {
      HashMap<String, String> variables = new HashMap<String, String>();
      variables.put("varOne", "one");
      variables.put("varTwo", "two");
      taskService.setVariablesLocal(task.getId(), variables);
      fail("It is not allowed to set variables on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Removing variable on the task should fail
    try {
      taskService.removeVariable(task.getId(), "someVar");
      fail("It is not allowed to remove a variable on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Removing variable on the task should fail
    try {
      taskService.removeVariableLocal(task.getId(), "someVar");
      fail("It is not allowed to remove a variable on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Removing variables on the task should fail
    try {
      taskService.removeVariables(task.getId(), Arrays.asList("one", "two"));
      fail("It is not allowed to remove variables on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Removing variables on the task should fail
    try {
      taskService.removeVariablesLocal(task.getId(), Arrays.asList("one", "two"));
      fail("It is not allowed to remove variables on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Adding candidate groups on the task should fail
    try {
      taskService.addCandidateGroup(task.getId(), "blahGroup");
      fail("It is not allowed to add a candidate group on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Adding candidate users on the task should fail
    try {
      taskService.addCandidateUser(task.getId(), "blahUser");
      fail("It is not allowed to add a candidate user on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Adding candidate users on the task should fail
    try {
      taskService.addGroupIdentityLink(task.getId(), "blahGroup", IdentityLinkType.CANDIDATE);
      fail("It is not allowed to add a candidate user on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Adding an identity link on the task should fail
    try {
      taskService.addUserIdentityLink(task.getId(), "blahUser", IdentityLinkType.OWNER);
      fail("It is not allowed to add an identityLink on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Adding a comment on the task should fail
    try {
      taskService.addComment(task.getId(), processInstance.getId(), "test comment");
      fail("It is not allowed to add a comment on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Adding an attachment on the task should fail
    try {
      taskService.createAttachment("text", task.getId(), processInstance.getId(), "testName", "testDescription", "http://test.com");
      fail("It is not allowed to add an attachment on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Set an assignee on the task should fail
    try {
      taskService.setAssignee(task.getId(), "mispiggy");
      fail("It is not allowed to set an assignee on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Set an owner on the task should fail
    try {
      taskService.setOwner(task.getId(), "kermit");
      fail("It is not allowed to set an owner on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }

    // Set priority on the task should fail
    try {
      taskService.setPriority(task.getId(), 99);
      fail("It is not allowed to set a priority on a task of a suspended process instance");
    } catch (ActivitiException e) {
      // This is good
    }
  }

  @Deployment
  public void testJobNotExecutedAfterProcessInstanceSuspend() {

    Date now = new Date();
    processEngineConfiguration.getClock().setCurrentTime(now);

    // Suspending the process instance should also stop the execution of jobs for that process instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    assertThat(managementService.createSuspendedJobQuery().count()).isEqualTo(1);

    // The jobs should not be executed now
    processEngineConfiguration.getClock().setCurrentTime(new Date(now.getTime() + (60 * 60 * 1000))); // Timer is set to fire on 5 minutes
    Job job = managementService.createTimerJobQuery().executable().singleResult();
    assertThat(job).isNull();

    assertThat(managementService.createSuspendedJobQuery().count()).isEqualTo(1);

    // Activation of the process instance should now allow for job execution
    runtimeService.activateProcessInstanceById(processInstance.getId());
    waitForJobExecutorToProcessAllJobs(10000L, 100L);
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);
    assertThat(managementService.createSuspendedJobQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

}
