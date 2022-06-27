/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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

package org.activiti.engine.test.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class ProcessDefinitionSuspensionTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/db/processOne.bpmn20.xml" })
  public void testProcessDefinitionActiveByDefault() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.isSuspended()).isFalse();
  }

  @Deployment(resources = { "org/activiti/engine/test/db/processOne.bpmn20.xml" })
  public void testSuspendActivateProcessDefinitionById() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.isSuspended()).isFalse();

    // suspend
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.isSuspended()).isTrue();

    // activate
    repositoryService.activateProcessDefinitionById(processDefinition.getId());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.isSuspended()).isFalse();
  }

  @Deployment(resources = { "org/activiti/engine/test/db/processOne.bpmn20.xml" })
  public void testSuspendActivateProcessDefinitionByKey() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.isSuspended()).isFalse();

    // suspend
    repositoryService.suspendProcessDefinitionByKey(processDefinition.getKey());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.isSuspended()).isTrue();

    // activate
    repositoryService.activateProcessDefinitionByKey(processDefinition.getKey());
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.isSuspended()).isFalse();
  }

  @Deployment(resources = { "org/activiti/engine/test/db/processOne.bpmn20.xml" })
  public void testCannotActivateActiveProcessDefinition() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.isSuspended()).isFalse();

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> repositoryService.activateProcessDefinitionById(processDefinition.getId()));

  }

  @Deployment(resources = { "org/activiti/engine/test/db/processOne.bpmn20.xml" })
  public void testCannotSuspendActiveProcessDefinition() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.isSuspended()).isFalse();

    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> repositoryService.suspendProcessDefinitionById(processDefinition.getId()));
  }

  @Deployment(resources = { "org/activiti/engine/test/db/processOne.bpmn20.xml", "org/activiti/engine/test/db/processTwo.bpmn20.xml" })
  public void testQueryForActiveDefinitions() {

    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().list();
    assertThat(processDefinitionList).hasSize(2);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(2);

    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(2);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/db/processOne.bpmn20.xml", "org/activiti/engine/test/db/processTwo.bpmn20.xml" })
  public void testQueryForSuspendedDefinitions() {

    // default = all definitions
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().list();
    assertThat(processDefinitionList).hasSize(2);

    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(2);

    ProcessDefinition processDefinition = processDefinitionList.get(0);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(2);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/db/processOne.bpmn20.xml" })
  public void testStartProcessInstanceForSuspendedProcessDefinition() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    // By id
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceById(processDefinition.getId()))
      .withMessageContaining("Cannot start process instance");

    // By Key
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceByKey(processDefinition.getKey()))
      .withMessageContaining("Cannot start process instance");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testContinueProcessAfterProcessDefinitionSuspend() {

    // Start Process Instance
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(processDefinition.getKey());

    // Verify one task is created
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    // Suspend process definition
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    // Process should be able to continue
    taskService.complete(task.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testSuspendProcessInstancesDuringProcessDefinitionSuspend() {

    int nrOfProcessInstances = 9;

    // Fire up a few processes for the deployed process definition
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    for (int i = 0; i < nrOfProcessInstances; i++) {
      runtimeService.startProcessInstanceByKey(processDefinition.getKey());
    }
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(nrOfProcessInstances);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(nrOfProcessInstances);

    // Suspend process definitions and include process instances
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, null);

    // Verify all process instances are also suspended
    for (ProcessInstance processInstance : runtimeService.createProcessInstanceQuery().list()) {
      assertThat(processInstance.isSuspended()).isTrue();
    }

    // Verify all process instances can't be continued
    for (Task task : taskService.createTaskQuery().list()) {
      assertThatExceptionOfType(ActivitiException.class)
        .as("A suspended task shouldn't be able to be continued")
        .isThrownBy(() -> taskService.complete(task.getId()));
    }
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(nrOfProcessInstances);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(nrOfProcessInstances);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(0);

    // Activate the process definition again
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), true, null);

    // Verify that all process instances can be completed
    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(0);
  }

  @Deployment
  public void testJobIsExecutedOnProcessDefinitionSuspend() {

    Date now = new Date();
    processEngineConfiguration.getClock().setCurrentTime(now);

    // Suspending the process definition should not stop the execution of
    // jobs
    // Added this test because in previous implementations, this was the
    // case.
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);

    // The jobs should simply be executed
    processEngineConfiguration.getClock().setCurrentTime(new Date(now.getTime() + (60 * 60 * 1000))); // Timer is set to fire on 5 minutes
    waitForJobExecutorToProcessAllJobs(2000L, 100L);
    assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testDelayedSuspendProcessDefinition() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);

    // Suspend process definition in one week from now
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), false, new Date(oneWeekFromStartTime));

    // Verify we can just start process instances
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);

    // verify there is a job created
    assertThat(managementService.createTimerJobQuery().processDefinitionId(processDefinition.getId()).count()).isEqualTo(1);

    // Move clock 8 days further and let job executor run
    long eightDaysSinceStartTime = oneWeekFromStartTime + (24 * 60 * 60 * 1000);
    processEngineConfiguration.getClock().setCurrentTime(new Date(eightDaysSinceStartTime));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);

    // verify job is now removed
    assertThat(managementService.createJobQuery().processDefinitionId(processDefinition.getId()).count()).isEqualTo(0);
    assertThat(managementService.createTimerJobQuery().processDefinitionId(processDefinition.getId()).count()).isEqualTo(0);

    // Try to start process instance. It should fail now.
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceById(processDefinition.getId()))
      .withMessageContaining("suspended");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(1);

    // Activate again
    repositoryService.activateProcessDefinitionById(processDefinition.getId());
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(2);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testDelayedSuspendProcessDefinitionIncludingProcessInstances() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);

    // Start some process instances
    int nrOfProcessInstances = 30;
    for (int i = 0; i < nrOfProcessInstances; i++) {
      runtimeService.startProcessInstanceById(processDefinition.getId());
    }

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(nrOfProcessInstances);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(nrOfProcessInstances);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().suspended().count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().active().count()).isEqualTo(nrOfProcessInstances);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);

    // Suspend process definition in one week from now
    long oneWeekFromStartTime = startTime.getTime() + (7 * 24 * 60 * 60 * 1000);
    repositoryService.suspendProcessDefinitionById(processDefinition.getId(), true, new Date(oneWeekFromStartTime));

    // Verify we can start process instances
    runtimeService.startProcessInstanceById(processDefinition.getId());
    nrOfProcessInstances = nrOfProcessInstances + 1;
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(nrOfProcessInstances);

    // verify there is a job created
    assertThat(managementService.createTimerJobQuery().processDefinitionId(processDefinition.getId()).count()).isEqualTo(1);

    // Move clock 9 days further and let job executor run
    long eightDaysSinceStartTime = oneWeekFromStartTime + (2 * 24 * 60 * 60 * 1000);
    processEngineConfiguration.getClock().setCurrentTime(new Date(eightDaysSinceStartTime));
    waitForJobExecutorToProcessAllJobs(30000L, 50L);

    // verify job is now removed
    assertThat(managementService.createJobQuery().processDefinitionId(processDefinition.getId()).count()).isEqualTo(0);
    assertThat(managementService.createTimerJobQuery().processDefinitionId(processDefinition.getId()).count()).isEqualTo(0);


    // Try to start process instance. It should fail now.
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceById(processDefinition.getId()))
      .withMessageContaining("suspended");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(nrOfProcessInstances);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(nrOfProcessInstances);
    assertThat(taskService.createTaskQuery().suspended().count()).isEqualTo(nrOfProcessInstances);
    assertThat(taskService.createTaskQuery().active().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(1);

    // Activate again
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), true, null);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(nrOfProcessInstances);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(nrOfProcessInstances);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().suspended().count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().active().count()).isEqualTo(nrOfProcessInstances);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testDelayedActivateProcessDefinition() {

    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    repositoryService.suspendProcessDefinitionById(processDefinition.getId());

    // Try to start process instance. It should fail now.
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.startProcessInstanceById(processDefinition.getId()))
      .withMessageContaining("suspended");
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(1);

    // Activate in a day from now
    long oneDayFromStart = startTime.getTime() + (24 * 60 * 60 * 1000);
    repositoryService.activateProcessDefinitionById(processDefinition.getId(), false, new Date(oneDayFromStart));

    // Move clock two days and let job executor run
    long twoDaysFromStart = startTime.getTime() + (2 * 24 * 60 * 60 * 1000);
    processEngineConfiguration.getClock().setCurrentTime(new Date(twoDaysFromStart));
    waitForJobExecutorToProcessAllJobs(5000L, 50L);

    // Starting a process instance should now succeed
    runtimeService.startProcessInstanceById(processDefinition.getId());
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(1);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);
  }

  public void testSuspendMultipleProcessDefinitionsByKey() {

    // Deploy three processes
    int nrOfProcessDefinitions = 3;
    for (int i = 0; i < nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml").deploy();
    }
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);

    // Suspend all process definitions with same key
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess");
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(nrOfProcessDefinitions);

    // Activate again
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess");
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);

    // Start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // And suspend again, cascading to process instances
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, null);
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1);

    // Clean DB
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  public void testDelayedSuspendMultipleProcessDefinitionsByKey() {

    Date startTime = new Date();
    processEngineConfiguration.getClock().setCurrentTime(startTime);
    final long hourInMs = 60 * 60 * 1000;

    // Deploy five versions of the same process
    int nrOfProcessDefinitions = 5;
    for (int i = 0; i < nrOfProcessDefinitions; i++) {
      repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml").deploy();
    }
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);

    // Start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // Suspend all process definitions with same key in 2 hours from now
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, new Date(startTime.getTime() + (2 * hourInMs)));
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(1);

    // Verify a job is created for each process definition
    assertThat(managementService.createTimerJobQuery().count()).isEqualTo(nrOfProcessDefinitions);
    for (ProcessDefinition processDefinition : repositoryService.createProcessDefinitionQuery().list()) {
      assertThat(managementService.createTimerJobQuery().processDefinitionId(processDefinition.getId()).count()).isEqualTo(1);
    }

    // Move time 3 hours and run job executor
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + (3 * hourInMs)));
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(30000L, 100L);
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(1);

    // Activate again in 5 hours from now
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", true, new Date(startTime.getTime() + (5 * hourInMs)));
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(0);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(runtimeService.createProcessInstanceQuery().suspended().count()).isEqualTo(1);

    // Move time 6 hours and run job executor
    processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + (6 * hourInMs)));
    waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(60000L, 100L);
    assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().active().count()).isEqualTo(nrOfProcessDefinitions);
    assertThat(repositoryService.createProcessDefinitionQuery().suspended().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().active().count()).isEqualTo(1);

    // Clean DB
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

}
