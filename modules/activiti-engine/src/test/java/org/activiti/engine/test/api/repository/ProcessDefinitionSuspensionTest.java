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
package org.activiti.engine.test.api.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class ProcessDefinitionSuspensionTest extends PluggableActivitiTestCase {

  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testProcessDefinitionActiveByDefault() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertFalse(processDefinition.isSuspended());      
  }
    
  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testSuspendActivateProcessDefinitionById() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    // suspend
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());    
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertTrue(processDefinition.isSuspended());      
    
    // activate
    repositoryService.activateProcessDefinitionById(processDefinition.getId());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());
  }

  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testSuspendActivateProcessDefinitionByKey() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    //suspend
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey());    
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertTrue(processDefinition.isSuspended());      
    
    //activate
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());
  }
  
  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testCannotActivateActiveProcessDefinition() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    try {
      repositoryService.activateProcessDefinitionById(processDefinition.getId());
      fail("Exception exprected");
    }catch (ActivitiException e) {
      // expected
    }
    
  }
  
  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testCannotSuspendActiveProcessDefinition() {    
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();    
    assertFalse(processDefinition.isSuspended());   
    
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    try {
      repositoryService.suspendProcessDefinitionById(processDefinition.getId());
      fail("Exception exprected");
    }catch (ActivitiException e) {
      // expected
    }
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/db/processOne.bpmn20.xml",
          "org/activiti/engine/test/db/processTwo.bpmn20.xml"
          })
  public void testQueryForActiveDefinitions() {    
    
    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(2, processDefinitionList.size());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());
    
    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
  }
  
  @Deployment(resources={
          "org/activiti/engine/test/db/processOne.bpmn20.xml",
          "org/activiti/engine/test/db/processTwo.bpmn20.xml"
          })
  public void testQueryForSuspendedDefinitions() {    
    
    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
      .list();    
    assertEquals(2, processDefinitionList.size());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());
    
    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testStartProcessInstanceForSuspendedProcessDefinition() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    // By id
    try {
      runtimeService.startProcessInstanceById(processDefinition.getId());
      fail("Exception is expected but not thrown");
    } catch(ActivitiException e) {
      assertTextPresentIgnoreCase("cannot start process instance", e.getMessage());
    }
    
    // By Key
    try {
      runtimeService.startProcessInstanceByKey(processDefinition.getKey());
      fail("Exception is expected but not thrown");
    } catch(ActivitiException e) {
      assertTextPresentIgnoreCase("cannot start process instance", e.getMessage());
    }
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testContinueProcessAfterProcessDefinitionSuspend() {
    
    // Start Process Instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    
    // Verify one task is created
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    // Suspend process definition
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    // Process should be able to continue
    taskService.complete(task.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testSuspendProcessInstancesDuringProcessDefinitionSuspend() {
    
    int nrOfProcessInstances = 9;
    
    // Fire up a few processes for the deployed process definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    for (int i=0; i<nrOfProcessInstances; i++) {
      runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    }
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().active().count());

    // Suspend process definitions and include process instances
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);
    
    // Verify all process instances are also suspended
    for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().list()) {
      assertTrue(processInstance.isSuspended());
    }
    
    // Verify all process instances can't be continued
    for (Task task : taskService.createTaskQuery().list()) {
      try {
        taskService.complete(task.getId());
        fail("A suspended task shouldn't be able to be continued");
      } catch(ActivitiException e) {
        // This is good
      }
    }
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().active().count());
    
    // Activate the process definition again
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), true, null);
    
    // Verify that all process instances can be completed
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().active().count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testSubmitStartFormAfterProcessDefinitionSuspend() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    try {
      formService.submitStartFormData(processDefinition.getId(), new HashMap<String, String>());
      fail();
    } catch (ActivitiException e) {
      // This is expected
    }
    
    try {
      formService.submitStartFormData(processDefinition.getId(), "someKey", new HashMap<String, String>());
      fail();
    } catch (ActivitiException e) {
      e.printStackTrace();
      // This is expected
    }
    
  }
  
  @Deployment
  public void testJobIsExecutedOnProcessDefinitionSuspend() {
    
    Date now = new Date();
    processEngineConfiguration.getClock().setCurrentTime(now);
    
    // Suspending the process definition should not stop the execution of jobs
    // Added this test because in previous implementations, this was the case.
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    assertEquals(1, managementService.createJobQuery().count());
    
    // The jobs should simply be executed
    processEngineConfiguration.getClock().setCurrentTime(new Date(now.getTime() + (60 * 60 * 1000))); // Timer is set to fire on 5 minutes
    waitForJobExecutorToProcessAllJobs(2000L, 100L);
    assertEquals(0, managementService.createJobQuery().count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testDelayedSuspendProcessDefinition() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);
    
    // Suspend process definition in one week from now
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000); 
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), false, new Date(oneWeekFromStartTime));

    // Verify we can just start process instances
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // verify there is a job created
    assertEquals(1, managementService.createJobQuery().processDefinitionId(processDefinition.getId()).count());
    
    // Move clock 8 days further and let job executor run
    long eightDaysSinceStartTime = oneWeekFromStartTime + (24 * 60 * 60 * 1000);
    processEngineConfiguration.getClock().setCurrentTime(new Date(eightDaysSinceStartTime));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);
    
    // verify job is now removed
    assertEquals(0, managementService.createJobQuery().processDefinitionId(processDefinition.getId()).count());
    
    // Try to start process instance. It should fail now.
    try {
      runtimeService.startProcessInstanceById(processDefinition.getId());
      fail();
    } catch (ActivitiException e) {
      assertTextPresentIgnoreCase("suspended", e.getMessage());
    }
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // Activate again
    repositoryService.activateProcessDefinitionById(processDefinition.getId());
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(2, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testDelayedSuspendProcessDefinitionIncludingProcessInstances() {
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);
    
    // Start some process instances
    int nrOfProcessInstances = 30;
    for (int i=0; i<nrOfProcessInstances; i++) {
      runtimeService.startProcessInstanceById(processDefinition.getId());
    }
    
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().suspended().count());
    assertEquals(nrOfProcessInstances, taskService.createTaskQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // Suspend process definition in one week from now
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000); 
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, new Date(oneWeekFromStartTime));

    // Verify we can start process instances
    runtimeService.startProcessInstanceById(processDefinition.getId());
    nrOfProcessInstances = nrOfProcessInstances + 1;
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    
    // Move clock 9 days further and let job executor run
    long eightDaysSinceStartTime = oneWeekFromStartTime + (2 * 24 * 60 * 60 * 1000);
    processEngineConfiguration.getClock().setCurrentTime(new Date(eightDaysSinceStartTime));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);
    
    // Try to start process instance. It should fail now.
    try {
      runtimeService.startProcessInstanceById(processDefinition.getId());
      fail();
    } catch (ActivitiException e) {
      assertTextPresentIgnoreCase("suspended", e.getMessage());
    }
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(nrOfProcessInstances, taskService.createTaskQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // Activate again
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), true, null);
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().count());
    assertEquals(nrOfProcessInstances, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, taskService.createTaskQuery().suspended().count());
    assertEquals(nrOfProcessInstances, taskService.createTaskQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testDelayedActivateProcessDefinition() {
    
    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    
    // Try to start process instance. It should fail now.
    try {
      runtimeService.startProcessInstanceById(processDefinition.getId());
      fail();
    } catch (ActivitiException e) {
      assertTextPresentIgnoreCase("suspended", e.getMessage());
    }
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // Activate in a day from now
    long oneDayFromStart = startTime.getTime() + (24 * 60 * 60 * 1000);
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), false, new Date(oneDayFromStart));
    
    // Move clock two days and let job executor run
    long twoDaysFromStart = startTime.getTime() + (2 * 24 * 60 * 60 * 1000);
    processEngineConfiguration.getClock().setCurrentTime(new Date(twoDaysFromStart));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);
    
    // Starting a process instance should now succeed
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
  }
  
  public void testSuspendMultipleProcessDefinitionsByKey () {

    // Deploy three processes
    int nrOfProcessDefinitions = 3;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml").deploy();
    }
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // Suspend all process definitions with same key
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess");
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // Activate again
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess");
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // Start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    // And suspend again, cascading to process instances
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, null);
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().active().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());
    
    // Clean DB
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }
  
  public void testDelayedSuspendMultipleProcessDefinitionsByKey () {
    
    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;
    
    // Deploy five versions of the same process
    int nrOfProcessDefinitions = 5;
    for (int i=0; i<nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml").deploy();
    }
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    
    // Start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    // Suspend all process definitions with same key in 2 hourse from now
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, new Date(startTime.getTime() + (2 * hourInMs)));
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().active().count());
    
    // Verify a job is created for each process definition
    assertEquals(nrOfProcessDefinitions, managementService.createJobQuery().count());
    for (ProcessDefinition processDefinition : repositoryService.createProcessDefinitionQuery().list()) {
      assertEquals(1, managementService.createJobQuery().processDefinitionId(processDefinition.getId()).count());
    }
    
    // Move time 3 hours and run job executor
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + (3 * hourInMs)));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());
    
    // Activate again in 5 hourse from now
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", true, new Date(startTime.getTime() + (5 * hourInMs)));
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().suspended().count());
    
    // Move time 6 hours and run job executor
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + (6 * hourInMs)));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(nrOfProcessDefinitions, repositoryService.createProcessDefinitionQuery().active().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().active().count());
    
    // Clean DB
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }
  
  
}
