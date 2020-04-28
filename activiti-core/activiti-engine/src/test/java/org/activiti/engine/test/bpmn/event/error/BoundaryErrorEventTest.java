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
package org.activiti.engine.test.bpmn.event.error;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.JvmUtil;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 */
public class BoundaryErrorEventTest extends PluggableActivitiTestCase {

  @Deployment
  public void testCatchErrorOnEmbeddedSubprocess() {
    runtimeService.startProcessInstanceByKey("boundaryErrorOnEmbeddedSubprocess");

    // After process start, usertask in subprocess should exist
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("subprocessTask");

    // After task completion, error end event is reached and caught
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("task after catching the error");
  }

  public void testThrowErrorWithEmptyErrorCode() {
    assertThatExceptionOfType(ActivitiException.class)
        .isThrownBy(() -> repositoryService
          .createDeployment()
          .addClasspathResource("org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testThrowErrorWithEmptyErrorCode.bpmn20.xml")
          .deploy())
        .withMessageContaining("activiti-error-missing-error-code");
  }

  public void testThrowErrorWithoutErrorCode() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> repositoryService
        .createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testThrowErrorWithoutErrorCode.bpmn20.xml")
        .deploy())
      .withMessageContaining("activiti-error-missing-error-code");
  }

  public void testCatchErrorOnEmbeddedSubprocessWithEmptyErrorCode() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> repositoryService
        .createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorOnEmbeddedSubprocessWithEmptyErrorCode.bpmn20.xml")
        .deploy())
      .withMessageContaining("activiti-error-missing-error-code");
  }

  public void testCatchErrorOnEmbeddedSubprocessWithoutErrorCode() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> repositoryService
        .createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorOnEmbeddedSubprocessWithoutErrorCode.bpmn20.xml")
        .deploy())
      .withMessageContaining("activiti-error-missing-error-code");
  }

  @Deployment
  public void testCatchErrorOfInnerSubprocessOnOuterSubprocess() {
    runtimeService.startProcessInstanceByKey("boundaryErrorTest");

    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getName()).isEqualTo("Inner subprocess task 1");
    assertThat(tasks.get(1).getName()).isEqualTo("Inner subprocess task 2");

    // Completing task 2, will cause the end error event to throw error with code 123
    taskService.complete(tasks.get(1).getId());
    taskService.createTaskQuery().list();
    Task taskAfterError = taskService.createTaskQuery().singleResult();
    assertThat(taskAfterError.getName()).isEqualTo("task outside subprocess");
  }

  @Deployment
  public void testCatchErrorInConcurrentEmbeddedSubprocesses() {
    assertErrorCaughtInConcurrentEmbeddedSubprocesses("boundaryEventTestConcurrentSubprocesses");
  }

  @Deployment
  public void testCatchErrorInConcurrentEmbeddedSubprocessesThrownByScriptTask() {
    assertErrorCaughtInConcurrentEmbeddedSubprocesses("catchErrorInConcurrentEmbeddedSubprocessesThrownByScriptTask");
  }

  private void assertErrorCaughtInConcurrentEmbeddedSubprocesses(String processDefinitionKey) {
    // Completing task A will lead to task D
    String procId = runtimeService.startProcessInstanceByKey(processDefinitionKey).getId();
    List<Task> tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getName()).isEqualTo("task A");
    assertThat(tasks.get(1).getName()).isEqualTo("task B");
    taskService.complete(tasks.get(0).getId());
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("task D");
    taskService.complete(task.getId());
    assertProcessEnded(procId);

    // Completing task B will lead to task C
    runtimeService.startProcessInstanceByKey(processDefinitionKey).getId();
    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getName()).isEqualTo("task A");
    assertThat(tasks.get(1).getName()).isEqualTo("task B");
    taskService.complete(tasks.get(1).getId());

    tasks = taskService.createTaskQuery().orderByTaskName().asc().list();
    assertThat(tasks).hasSize(2);
    assertThat(tasks.get(0).getName()).isEqualTo("task A");
    assertThat(tasks.get(1).getName()).isEqualTo("task C");
    taskService.complete(tasks.get(1).getId());
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("task A");

    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("task D");
  }

  @Deployment
  public void testDeeplyNestedErrorThrown() {

    // Input = 1 -> error1 will be thrown, which will destroy ALL BUT ONE
    // subprocess, which leads to an end event, which ultimately leads to
    // ending the process instance
    String procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Nested task");
    taskService.complete(task.getId(), singletonMap("input", 1));
    assertProcessEnded(procId);

    // Input == 2 -> error2 will be thrown, leading to a userTask outside
    // all subprocesses
    procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown").getId();
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Nested task");
    taskService.complete(task.getId(), singletonMap("input", 2));
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("task after catch");
    taskService.complete(task.getId());
    assertProcessEnded(procId);
  }

  @Deployment
  public void testDeeplyNestedErrorThrownOnlyAutomaticSteps() {
    // input == 1 -> error2 is thrown -> caught on subprocess2 -> end event
    // in subprocess -> proc inst end 1
    String procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown", singletonMap("input", 1)).getId();
    assertProcessEnded(procId);

    HistoricProcessInstance hip;
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      hip = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
      assertThat(hip.getEndActivityId()).isEqualTo("processEnd1");
    }
    // input == 2 -> error2 is thrown -> caught on subprocess1 -> proc inst
    // end 2
    procId = runtimeService.startProcessInstanceByKey("deeplyNestedErrorThrown", singletonMap("input", 1)).getId();
    assertProcessEnded(procId);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      hip = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
      assertThat(hip.getEndActivityId()).isEqualTo("processEnd1");
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorOnCallActivity-parent.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml" })
  public void testCatchErrorOnCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnCallActivity").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task in subprocess");

    // Completing the task will reach the end error event,
    // which is caught on the call activity boundary
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Escalated Task");

    // Completing the task will end the process instance
    taskService.complete(task.getId());
    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml" })
  public void testUncaughtError() {
    runtimeService.startProcessInstanceByKey("simpleSubProcess");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task in subprocess");

    // Completing the task will reach the end error event, which is never caught in the process
    assertThatExceptionOfType(BpmnError.class)
        .isThrownBy(() -> taskService.complete(task.getId()))
        .withMessageContaining("No catching boundary event found for error with errorCode 'myError', neither in same process nor in parent process");
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testUncaughtErrorOnCallActivity-parent.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml" })
  public void testUncaughtErrorOnCallActivity() {
    runtimeService.startProcessInstanceByKey("uncaughtErrorOnCallActivity");
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task in subprocess");

    try {
      // Completing the task will reach the end error event,
      // which is never caught in the process
      taskService.complete(task.getId());
    } catch (BpmnError e) {
      assertThat(e.getMessage()).contains("No catching boundary event found for error with errorCode 'myError', neither in same process nor in parent process");
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByCallActivityOnSubprocess.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml" })
  public void testCatchErrorThrownByCallActivityOnSubprocess() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnSubprocess").getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task in subprocess");

    // Completing the task will reach the end error event,
    // which is caught on the call activity boundary
    taskService.complete(task.getId());
    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Escalated Task");

    // Completing the task will end the process instance
    taskService.complete(task.getId());
    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByCallActivityOnCallActivity.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess2ndLevel.bpmn20.xml", "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.subprocess.bpmn20.xml" })
  public void testCatchErrorThrownByCallActivityOnCallActivity() throws InterruptedException {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnCallActivity2ndLevel").getId();

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Task in subprocess");

    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Escalated Task");

    // Completing the task will end the process instance
    taskService.complete(task.getId());
    assertProcessEnded(procId);
  }

  @Deployment
  public void testCatchErrorOnParallelMultiInstance() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnParallelMi").getId();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks).hasSize(5);

    // Complete two subprocesses, just to make it a bit more complex
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("throwError", false);
    taskService.complete(tasks.get(2).getId(), vars);
    taskService.complete(tasks.get(3).getId(), vars);

    // Reach the error event
    vars.put("throwError", true);
    taskService.complete(tasks.get(1).getId(), vars);

    assertThat(taskService.createTaskQuery().count()).isEqualTo(0);
    assertProcessEnded(procId);
  }

  @Deployment
  public void testCatchErrorOnSequentialMultiInstance() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnSequentialMi").getId();

    // complete one task
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("throwError", false);
    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId(), vars);

    // complete second task and throw error
    vars.put("throwError", true);
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId(), vars);

    assertProcessEnded(procId);
  }

  @Deployment
  public void testCatchErrorThrownByJavaDelegateOnServiceTask() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnServiceTask").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByJavaDelegateOnServiceTaskNotCancelActivity() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnServiceTaskNotCancelActiviti").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByJavaDelegateOnServiceTaskWithErrorCode() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnServiceTaskWithErrorCode").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByJavaDelegateOnEmbeddedSubProcess() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnEmbeddedSubProcess").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByJavaDelegateOnEmbeddedSubProcessInduction() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnEmbeddedSubProcessInduction").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-parent.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-child.bpmn20.xml" })
  public void testCatchErrorThrownByJavaDelegateOnCallActivity() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnCallActivity-parent").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-child.bpmn20.xml" })
  public void testUncaughtErrorThrownByJavaDelegateOnServiceTask() {
    try {
      runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnCallActivity-child");
    } catch (BpmnError e) {
      assertThat(e.getMessage()).contains("No catching boundary event found for error with errorCode '23', neither in same process nor in parent process");
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testUncaughtErrorThrownByJavaDelegateOnCallActivity-parent.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorThrownByJavaDelegateOnCallActivity-child.bpmn20.xml" })
  public void testUncaughtErrorThrownByJavaDelegateOnCallActivity() {
    try {
      runtimeService.startProcessInstanceByKey("uncaughtErrorThrownByJavaDelegateOnCallActivity-parent");
    } catch (BpmnError e) {
      assertThat(e.getMessage()).contains("No catching boundary event found for error with errorCode '23', neither in same process nor in parent process");
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorOnGroovyScriptTask.bpmn20.xml" })
  public void testCatchErrorOnGroovyScriptTask() {
    String procId = runtimeService.startProcessInstanceByKey("catchErrorOnScriptTask").getId();
    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testCatchErrorOnJavaScriptScriptTask.bpmn20.xml" })
  public void testCatchErrorOnJavaScriptScriptTask() {
    if (JvmUtil.isAtLeastJDK7()) {
      String procId = runtimeService.startProcessInstanceByKey("catchErrorOnScriptTask").getId();
      assertProcessEnded(procId);
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testUncaughtErrorOnScriptTaskWithEmptyErrorEventDefinition.bpmn20.xml" })
  public void testUncaughtErrorOnScriptTaskWithEmptyErrorEventDefinition() {
    String procId = runtimeService.startProcessInstanceByKey("uncaughtErrorOnScriptTaskWithEmptyErrorEventDefinition").getId();
    assertProcessEnded(procId);
  }

  @Deployment(resources = { "org/activiti/engine/test/bpmn/event/error/BoundaryErrorEventTest.testUncaughtErrorOnScriptTask.bpmn20.xml" })
  public void testUncaughtErrorOnScriptTask() {
    assertThatExceptionOfType(BpmnError.class)
      .as("The script throws error event with errorCode 'errorUncaught', but no catching boundary event was defined. An exception is expected which did not occur")
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey("uncaughtErrorOnScriptTask").getId())
      .withMessageContaining("No catching boundary event found for error with errorCode 'errorUncaught', neither in same process nor in parent process");
  }

  @Deployment
  public void testCatchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskSequential() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("executionsBeforeError", 2);
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskSequential", variables).getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskParallel() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("executionsBeforeError", 2);
    String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByJavaDelegateOnMultiInstanceServiceTaskParallel", variables).getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testErrorThrownByJavaDelegateNotCaughtByOtherEventType() {
    String procId = runtimeService.startProcessInstanceByKey("testErrorThrownByJavaDelegateNotCaughtByOtherEventType").getId();
    assertThatErrorHasBeenCaught(procId);
  }

  private void assertThatErrorHasBeenCaught(String procId) {
    // The service task will throw an error event,
    // which is caught on the service task boundary
    assertThat(taskService.createTaskQuery().count()).as("No tasks found in task list.").isEqualTo(1);
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName()).isEqualTo("Escalated Task");

    // Completing the task will end the process instance
    taskService.complete(task.getId());
    assertProcessEnded(procId);
  }

  @Deployment
  public void testConcurrentExecutionsInterruptedOnDestroyScope() {

    // this test makes sure that if the first concurrent execution destroys
    // the scope
    // (due to the interrupting boundary catch), the second concurrent
    // execution does not
    // move forward.

    // if the test fails, it produces a constraint violation in db.

    runtimeService.startProcessInstanceByKey("process");
  }

  @Deployment
  public void testCatchErrorThrownByExpressionOnServiceTask() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("bpmnErrorBean", new BpmnErrorBean());
    String procId = runtimeService.startProcessInstanceByKey("testCatchErrorThrownByExpressionOnServiceTask", variables).getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByDelegateExpressionOnServiceTask() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("bpmnErrorBean", new BpmnErrorBean());
    String procId = runtimeService.startProcessInstanceByKey("testCatchErrorThrownByDelegateExpressionOnServiceTask", variables).getId();
    assertThatErrorHasBeenCaught(procId);
  }

  @Deployment
  public void testCatchErrorThrownByJavaDelegateProvidedByDelegateExpressionOnServiceTask() {
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("bpmnErrorBean", new BpmnErrorBean());
    String procId = runtimeService.startProcessInstanceByKey("testCatchErrorThrownByJavaDelegateProvidedByDelegateExpressionOnServiceTask", variables).getId();
    assertThatErrorHasBeenCaught(procId);
  }

}
