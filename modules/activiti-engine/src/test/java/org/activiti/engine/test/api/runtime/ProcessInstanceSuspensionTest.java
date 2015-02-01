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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class ProcessInstanceSuspensionTest extends PluggableActivitiTestCase {

  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testProcessInstanceActiveByDefault() {
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());      
    
  }
    
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testSuspendActivateProcessInstance() {    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());      
    
    //suspend
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertTrue(processInstance.isSuspended());      
    
    //activate
    runtimeService.activateProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testCannotActivateActiveProcessInstance() {    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());      
        
    try {
      //activate
      runtimeService.activateProcessInstanceById(processInstance.getId());
      fail("Expected activiti exception");
    }catch (ActivitiException e) {
     // expected
    }
   
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testCannotSuspendSuspendedProcessInstance() {    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertFalse(processInstance.isSuspended());      
    
    runtimeService.suspendProcessInstanceById(processInstance.getId());
        
    try {
      runtimeService.suspendProcessInstanceById(processInstance.getId());
      fail("Expected activiti exception");
    }catch (ActivitiException e) {
     // expected
    }
   
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/api/runtime/superProcessWithMultipleNestedSubProcess.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
          "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml"
          })
  public void testQueryForActiveAndSuspendedProcessInstances() {    
    runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");
    
    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(5, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    
    ProcessInstance piToSuspend = runtimeService.createProcessInstanceQuery()
            .processDefinitionKey("nestedSubProcessQueryTest")
            .singleResult();
    runtimeService.suspendProcessInstanceById(piToSuspend.getId());
    
    assertEquals(5, runtimeService.createProcessInstanceQuery().count());
    assertEquals(4, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());
    
    assertEquals(piToSuspend.getId(), runtimeService.createProcessInstanceQuery().suspended().singleResult().getId());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
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
      assertTrue(task.isSuspended());
    }
    
    // Activate process instance again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    for (Task task : tasks) {
      assertFalse(task.isSuspended());
    }
  }

  @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testTaskQueryAfterProcessInstanceSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    
    task = taskService.createTaskQuery().active().singleResult();
    assertNotNull(task);
    
    // Suspend
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(1, taskService.createTaskQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().active().count());
    
    // Activate
    runtimeService.activateProcessInstanceById(processInstance.getId());
    assertEquals(1, taskService.createTaskQuery().count());
    assertEquals(0, taskService.createTaskQuery().suspended().count());
    assertEquals(1, taskService.createTaskQuery().active().count());
    
    // Completing should end the process instance
    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment
  public void testChildExecutionsSuspendedAfterProcessInstanceSuspend() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testChildExecutionsSuspended");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    
    List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertTrue(execution.isSuspended());
    }
    
    // Activate again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    executions = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    for (Execution execution : executions) {
      assertFalse(execution.isSuspended());
    }
    
    // Finish process
    while (taskService.createTaskQuery().count() > 0) {
      for (Task task : taskService.createTaskQuery().list()) {
        taskService.complete(task.getId());
      }
    }
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testSubmitTaskFormAfterProcessInstanceSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    
    try {
      formService.submitTaskFormData(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId(), new HashMap<String, String>());
      fail();
    } catch(ActivitiException e) {
      // This is expected
    }
  }
  
  @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testProcessInstanceOperationsFailAfterSuspend() {
    
    // Suspend process instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    
    try {
      runtimeService.messageEventReceived("someMessage", processInstance.getId());
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.messageEventReceived("someMessage", processInstance.getId(), new HashMap<String, Object>());
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.removeVariable(processInstance.getId(), "someVariable");
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.removeVariableLocal(processInstance.getId(), "someVariable");
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.removeVariables(processInstance.getId(), Arrays.asList("one", "two", "three"));
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    
    try {
      runtimeService.removeVariablesLocal(processInstance.getId(), Arrays.asList("one", "two", "three"));
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.setVariable(processInstance.getId(), "someVariable", "someValue");
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.setVariableLocal(processInstance.getId(), "someVariable", "someValue");
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.setVariables(processInstance.getId(), new HashMap<String, Object>());
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.setVariablesLocal(processInstance.getId(), new HashMap<String, Object>());
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.signal(processInstance.getId());
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.signal(processInstance.getId(), new HashMap<String, Object>());
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.signalEventReceived("someSignal", processInstance.getId());
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
    
    try {
      runtimeService.signalEventReceived("someSignal", processInstance.getId(), new HashMap<String, Object>());
      fail();
    } catch (ActivitiException e) {
      // This is expected
      e.getMessage().contains("is suspended");
    }
  }
  
  @Deployment
  public void testSignalEventReceivedAfterProcessInstanceSuspended() {

    final String signal = "Some Signal";
    
    // Test if process instance can be completed using the signal 
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.signalEventReceived(signal);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    // Now test when suspending the process instance: the process instance shouldn't be continued
    processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    runtimeService.signalEventReceived(signal, new HashMap<String, Object>());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    // Activate and try again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = "org/activiti/engine/test/api/runtime/ProcessInstanceSuspensionTest.testSignalEventReceivedAfterProcessInstanceSuspended.bpmn20.xml")
  public void testSignalEventReceivedAfterMultipleProcessInstancesSuspended() {

    final String signal = "Some Signal";

    // Test if process instance can be completed using the signal
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.signalEventReceived(signal);
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // Now test when suspending the process instance: the process instance shouldn't be continued
    processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    processInstance = runtimeService.startProcessInstanceByKey("signalSuspendedProcessInstance");
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());

    runtimeService.signalEventReceived(signal, new HashMap<String, Object>());
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());

    // Activate and try again
    runtimeService.activateProcessInstanceById(processInstance.getId());
    runtimeService.signalEventReceived(signal);
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testTaskOperationsFailAfterProcessInstanceSuspend() {

    // Start a new process instance with one task
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    final Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    
    // Suspend the process instance
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    
    // Yeah, the following is pretty long and boring ... but I didn't have the patience
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
    assertEquals(1, managementService.createJobQuery().count());
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    assertEquals(1, managementService.createJobQuery().count());
    
    // The jobs should not be executed now
    processEngineConfiguration.getClock().setCurrentTime(new Date(now.getTime() + (60 * 60 * 1000))); // Timer is set to fire on 5 minutes
    Job job = managementService.createJobQuery().executable().singleResult();
    assertNull(job);
    
    assertEquals(1, managementService.createJobQuery().count());
    
    // Activation of the process instance should now allow for job execution
    runtimeService.activateProcessInstanceById(processInstance.getId());
    waitForJobExecutorToProcessAllJobs(1000L, 100L);
    assertEquals(0, managementService.createJobQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
  
}
