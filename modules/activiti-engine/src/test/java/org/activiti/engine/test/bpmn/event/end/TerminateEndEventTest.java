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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.ExtensionAttribute;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.bpmn.behavior.TerminateEndEventActivityBehavior;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Nico Rehwaldt
 */
public class TerminateEndEventTest extends PluggableActivitiTestCase {

  public static int serviceTaskInvokedCount = 0;

  public static class CountDelegate implements JavaDelegate {

    public void execute(DelegateExecution execution) throws Exception {
      serviceTaskInvokedCount++;

      // leave only 3 out of n subprocesses
      execution.setVariableLocal("terminate", serviceTaskInvokedCount > 3);
    }
  }

  public static int serviceTaskInvokedCount2 = 0;

  public static class CountDelegate2 implements JavaDelegate {

    public void execute(DelegateExecution execution) throws Exception {
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
  public void testTerminateWithSubProcess() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the process and 
    long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
    assertEquals(4, executionEntities);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(pi.getId());
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateWithCallActivity.bpmn", 
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessNoTerminate.bpmn" 
  })
  public void testTerminateWithCallActivity() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
    assertEquals(4, executionEntities);
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(pi.getId());
  }

  @Deployment(resources = {
          "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInExclusiveGatewayWithCallActivity.bpmn",
          "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessNoTerminate.bpmn"
  })
  public void testTerminateInExclusiveGatewayWithCallActivity() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample-terminateAfterExclusiveGateway");

    long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
    assertEquals(4, executionEntities);

    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preTerminateEnd").singleResult();
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", 1);
    taskService.complete(task.getId(), variables);

    assertProcessEnded(pi.getId());
  }

  @Deployment
  public void testTerminateInExclusiveGatewayWithMultiInstanceSubProcess() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample-terminateAfterExclusiveGateway");

    long executionEntities = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).count();
    assertEquals(14, executionEntities);

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
    assertEquals(1, executionEntities);
    
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
    
    // 'preEndInnerTask' task in subprocess should have been terminated, only outerTask should exist
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
    assertEquals(1, executionEntities);
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(pi.getId());
  }
  
  @Deployment
  public void testTerminateInSubProcessConcurrentMultiInstance() throws Exception {
    serviceTaskInvokedCount = 0;
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    long executionEntities = runtimeService.createExecutionQuery().count();
    assertEquals(12, executionEntities);
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());
    
    long executionEntities2 = runtimeService.createExecutionQuery().count();
    assertEquals(10, executionEntities2);
    
    List<Task> tasks = taskService.createTaskQuery().list();
    for (Task t : tasks) {
      taskService.complete(t.getId());
    }
    
    assertProcessEnded(pi.getId());
  }

  @Deployment(resources = {"org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrentCallActivity.bpmn",
          "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateAfterUserTask.bpmn",
          "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testTerminateInCallActivityConcurrentCallActivity() throws Exception {
    serviceTaskInvokedCount = 0;

    // GIVEN - process instance starts and creates 2 subProcessInstances (with 2 user tasks - preTerminate and my task)
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventInCallActivityConcurrentCallActivity");
    assertThat(runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).list().size(), is(2));

    // WHEN - complete -> terminate end event
    Task preTerminate = taskService.createTaskQuery().taskName("preTerminate").singleResult();
    taskService.complete(preTerminate.getId());

    //THEN - super process is finished together with subprocesses
    assertProcessEnded(pi.getId());
  }
  
  @Deployment
  public void testTerminateInSubProcessMultiInstance() throws Exception {
    serviceTaskInvokedCount = 0;
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    long executionEntities = runtimeService.createExecutionQuery().count();
    assertEquals(1, executionEntities);
    
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
    
    // outer execution still available
    assertEquals(1, remainingExecutions);
    
    // three finished
    assertEquals(3, serviceTaskInvokedCount2);
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());
    
    // last task remaining
    assertProcessEnded(pi.getId());
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivity.bpmn", 
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessTerminate.bpmn" 
  })
  public void testTerminateInCallActivity() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the called process and continue the parent
    long executionEntities = runtimeService.createExecutionQuery().count();
    assertEquals(1, executionEntities);
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(pi.getId());
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityMulitInstance.bpmn", 
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessTerminate.bpmn" 
  })
  public void testTerminateInCallActivityMulitInstance() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the called process and continue the parent
    long executionEntities = runtimeService.createExecutionQuery().count();
    assertEquals(1, executionEntities);
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(pi.getId());
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrent.bpmn", 
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessConcurrentTerminate.bpmn"
  })
  public void testTerminateInCallActivityConcurrent() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the called process and continue the parent
    long executionEntities = runtimeService.createExecutionQuery().count();
    assertEquals(1, executionEntities);
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(pi.getId());
  }
  
  @Deployment(resources={
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.testTerminateInCallActivityConcurrentMulitInstance.bpmn", 
    "org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.subProcessConcurrentTerminate.bpmn" 
  })
  public void testTerminateInCallActivityConcurrentMulitInstance() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("terminateEndEventExample");

    // should terminate the called process and continue the parent
    long executionEntities = runtimeService.createExecutionQuery().count();
    assertEquals(1, executionEntities);
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).taskDefinitionKey("preNormalEnd").singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(pi.getId());
  }

  public void testParseTerminateEndEventDefinitionWithExtensions() {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/bpmn/event/end/TerminateEndEventTest.parseExtensionElements.bpmn20.xml").deploy();
    ProcessDefinition processDefinitionQuery = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
    ProcessDefinitionEntity processDefinition = this.processEngineConfiguration.getProcessDefinitionCache().get(processDefinitionQuery.getId());

    assertThat(processDefinition.getActivities().size(), is(2));
    ActivityImpl endEvent = processDefinition.getActivities().get(1);
    assertThat(endEvent.getId(), is("terminateEnd"));
    assertThat(endEvent.getActivityBehavior(), instanceOf(TerminateEndEventActivityBehavior.class));
    TerminateEndEventActivityBehavior terminateEndEventBehavior = (TerminateEndEventActivityBehavior) endEvent.getActivityBehavior();
    Map<String, List<ExtensionElement>> extensionElements = terminateEndEventBehavior.getEndEvent().getExtensionElements();
    assertThat(extensionElements.size(), is(1));
    List<ExtensionElement> strangeProperties = extensionElements.get("strangeProperty");
    assertThat(strangeProperties.size(), is(1));
    ExtensionElement strangeProperty = strangeProperties.get(0);
    assertThat(strangeProperty.getNamespace(), is("http://activiti.org/bpmn"));
    assertThat(strangeProperty.getElementText(), is("value"));
    assertThat(strangeProperty.getAttributes().size(), is(1));
    ExtensionAttribute id = strangeProperty.getAttributes().get("id").get(0);
    assertThat(id.getName(), is("id"));
    assertThat(id.getValue(), is("strangeId"));


    repositoryService.deleteDeployment(deployment.getId());
  }
}