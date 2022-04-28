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

package org.activiti.engine.test.bpmn.event.timer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.activiti.engine.impl.cmd.CancelJobsCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.runtime.TimerJobQuery;
import org.activiti.engine.test.Deployment;

public class StartTimerEventTest extends PluggableActivitiTestCase {

    @Deployment
    public void testDurationStartTimerEvent() throws Exception {

        // Set the clock fixed
        Date startTime = new Date();

        // After process start, there should be timer created
        TimerJobQuery jobQuery = managementService.createTimerJobQuery();
        assertThat(jobQuery.count()).isEqualTo(1);

        // After setting the clock to time '50 minutes and 5 seconds', the second timer should fire
        processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((50 * 60 * 1000) + 5000)));
        waitForJobExecutorToProcessAllJobs(5000L,
                                           200L);

        List<ProcessInstance> pi = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").list();
        assertThat(pi).hasSize(1);

        assertThat(jobQuery.count()).isEqualTo(0);
    }

    @Deployment
    public void testFixedDateStartTimerEvent() throws Exception {
        // After process start, there should be timer created
        TimerJobQuery jobQuery = managementService.createTimerJobQuery();
        assertThat(jobQuery.count()).isEqualTo(1);

        processEngineConfiguration.getClock().setCurrentTime(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("15/11/2036 11:12:30"));
        waitForJobExecutorToProcessAllJobs(5000L,
                                           200L);

        List<ProcessInstance> pi = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").list();
        assertThat(pi).hasSize(1);

        assertThat(jobQuery.count()).isEqualTo(0);
    }

    // FIXME: This test likes to run in an endless loop when invoking the
    // waitForJobExecutorOnCondition method
    @Deployment
    public void testCycleDateStartTimerEvent() throws Exception {
        processEngineConfiguration.getClock().setCurrentTime(new Date());

        // After process start, there should be timer created
        TimerJobQuery jobQuery = managementService.createTimerJobQuery();
        assertThat(jobQuery.count()).isEqualTo(1);

        final ProcessInstanceQuery piq = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample");

        moveByMinutes(5);
        waitForJobExecutorOnCondition(10000,
            new Callable<Boolean>() {
                                          public Boolean call() throws Exception {
                                              return 1 == piq.count();
                                          }
                                      });

        assertThat(jobQuery.count()).isEqualTo(1);

        moveByMinutes(5);
        waitForJobExecutorOnCondition(10000,
            new Callable<Boolean>() {
                                          public Boolean call() throws Exception {
                                              return 2 == piq.count();
                                          }
                                      });

        assertThat(jobQuery.count()).isEqualTo(1);
        // have to manually delete pending timer
        cleanDB();
    }

    private void moveByMinutes(int minutes) throws Exception {
        processEngineConfiguration.getClock().setCurrentTime(new Date(processEngineConfiguration.getClock().getCurrentTime().getTime() + ((minutes * 60 * 1000) + 5000)));
    }

    @Deployment
    public void testCycleWithLimitStartTimerEvent() throws Exception {
        processEngineConfiguration.getClock().setCurrentTime(new Date());

        // After process start, there should be timer created
        TimerJobQuery jobQuery = managementService.createTimerJobQuery();
        assertThat(jobQuery.count()).isEqualTo(1);

        moveByMinutes(6);
        String jobId = managementService.createTimerJobQuery().executable().singleResult().getId();
        managementService.moveTimerToExecutableJob(jobId);
        managementService.executeJob(jobId);
        assertThat(jobQuery.count()).isEqualTo(1);

        moveByMinutes(6);
        jobId = managementService.createTimerJobQuery().executable().singleResult().getId();
        managementService.moveTimerToExecutableJob(jobId);
        managementService.executeJob(jobId);
        assertThat(jobQuery.count()).isEqualTo(0);
    }

    @Deployment
    public void testExpressionStartTimerEvent() throws Exception {
        // ACT-1415: fixed start-date is an expression
        TimerJobQuery jobQuery = managementService.createTimerJobQuery();
        assertThat(jobQuery.count()).isEqualTo(1);

        processEngineConfiguration.getClock().setCurrentTime(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("15/11/2036 11:12:30"));
        waitForJobExecutorToProcessAllJobs(5000L,
                                           200L);

        List<ProcessInstance> pi = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").list();
        assertThat(pi).hasSize(1);

        assertThat(jobQuery.count()).isEqualTo(0);
    }

    @Deployment
    public void testVersionUpgradeShouldCancelJobs() throws Exception {
        processEngineConfiguration.getClock().setCurrentTime(new Date());

        // After process start, there should be timer created
        TimerJobQuery jobQuery = managementService.createTimerJobQuery();
        assertThat(jobQuery.count()).isEqualTo(1);

        // we deploy new process version, with some small change
        String process = new String(IoUtil.readInputStream(getClass().getResourceAsStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml"),
                                                           "")).replaceAll("beforeChange",
                                                                           "changed");
        String id = repositoryService.createDeployment().addInputStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml",
                                                                        new ByteArrayInputStream(process.getBytes())).deploy().getId();

        assertThat(jobQuery.count()).isEqualTo(1);

        moveByMinutes(5);
        waitForJobExecutorOnCondition(10000,
            new Callable<Boolean>() {
                                          public Boolean call() throws Exception {
                                              // we check that correct version was started
                                              ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("startTimerEventExample").singleResult();
                                              if (processInstance != null) {
                                                  String pi = processInstance.getId();
                                                  List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(pi).list();
                                                  Execution activityExecution = null;
                                                  for (Execution execution : executions) {
                                                      if (!execution.getProcessInstanceId().equals(execution.getId())) {
                                                          activityExecution = execution;
                                                          break;
                                                      }
                                                  }
                                                  if (activityExecution != null) {
                                                      return "changed".equals(activityExecution.getActivityId());
                                                  } else {
                                                      return false;
                                                  }
                                              } else {
                                                  return false;
                                              }
                                          }
                                      });
        assertThat(jobQuery.count()).isEqualTo(1);

        cleanDB();
        repositoryService.deleteDeployment(id, true);
    }

    @Deployment
    public void testTimerShouldNotBeRecreatedOnDeploymentCacheReboot() {

        // Just to be sure, I added this test. Sounds like something that could easily happen
        // when the order of deploy/parsing is altered.

        // After process start, there should be timer created
        TimerJobQuery jobQuery = managementService.createTimerJobQuery();
        assertThat(jobQuery.count()).isEqualTo(1);

        // Reset deployment cache
        processEngineConfiguration.getProcessDefinitionCache().clear();

        // Start one instance of the process definition, this will trigger a
        // cache reload
        runtimeService.startProcessInstanceByKey("startTimer");

        // No new jobs should have been created
        assertThat(jobQuery.count()).isEqualTo(1);
    }

    // Test for ACT-1533
    public void testTimerShouldNotBeRemovedWhenUndeployingOldVersion() throws Exception {
        // Deploy test process
        String processXml = new String(IoUtil.readInputStream(getClass().getResourceAsStream("StartTimerEventTest.testTimerShouldNotBeRemovedWhenUndeployingOldVersion.bpmn20.xml"),
                                                              ""));
        String firstDeploymentId = repositoryService.createDeployment().addInputStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml",
                                                                                       new ByteArrayInputStream(processXml.getBytes())).deploy().getId();

        // After process start, there should be timer created
        TimerJobQuery jobQuery = managementService.createTimerJobQuery();
        assertThat(jobQuery.count()).isEqualTo(1);

        //we deploy new process version, with some small change
        String processChanged = processXml.replaceAll("beforeChange",
                                                      "changed");
        String secondDeploymentId = repositoryService.createDeployment().addInputStream("StartTimerEventTest.testVersionUpgradeShouldCancelJobs.bpmn20.xml",
                                                                                        new ByteArrayInputStream(processChanged.getBytes())).deploy().getId();
        assertThat(jobQuery.count()).isEqualTo(1);

        // Remove the first deployment
        repositoryService.deleteDeployment(firstDeploymentId,
                                           true);

        // The removal of an old version should not affect timer deletion
        // ACT-1533: this was a bug, and the timer was deleted!
        assertThat(jobQuery.count()).isEqualTo(1);

        // Cleanup
        cleanDB();
        repositoryService.deleteDeployment(secondDeploymentId,
                                           true);
    }

    public void testOldJobsDeletedOnRedeploy() {

        for (int i = 0; i < 3; i++) {

            repositoryService.createDeployment()
                    .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testOldJobsDeletedOnRedeploy.bpmn20.xml")
                    .deploy();

            assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(i + 1);
            assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(i + 1);
            assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);
        }

        // Cleanup
        for (ProcessDefinition processDefinition : repositoryService.createProcessDefinitionQuery().processDefinitionKey("timer").orderByProcessDefinitionVersion().desc().list()) {
            repositoryService.deleteDeployment(processDefinition.getDeploymentId(),
                                               true);
        }

        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);
        assertThat(managementService.createJobQuery().count()).isEqualTo(0);
    }

    public void testTimersRecreatedOnDeploymentDelete() {

        // v1 has timer
        // v2 has no timer
        // v3 has no timer
        // v4 has no timer

        // Deploy v1
        repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v1.bpmn20.xml")
                .deploy().getId();

        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);

        // Deploy v2: no timer -> previous should be deleted
        String deployment2 = repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v2.bpmn20.xml")
                .deploy().getId();

        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(2);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);

        // Deploy v3: no timer
        String deployment3 = repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v3.bpmn20.xml")
                .deploy().getId();

        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(3);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(3);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);

        // Deploy v4: no timer
        String deployment4 = repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v4.bpmn20.xml")
                .deploy().getId();

        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(4);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(4);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);

        // Delete v4 -> V3 active. No timer active anymore (v3 doesn't have a timer)
        repositoryService.deleteDeployment(deployment4,
                                           true);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(3);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(3);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);

        // Delete v2 --> V3 still active, nothing changed there
        repositoryService.deleteDeployment(deployment2,
                                           true);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(2);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0); // v3 is still active

        // Delete v3 -> fallback to v1
        repositoryService.deleteDeployment(deployment3,
                                           true);
        assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().count()).isEqualTo(1);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);

        // Cleanup
        for (ProcessDefinition processDefinition : repositoryService.createProcessDefinitionQuery().processDefinitionKey("timer").orderByProcessDefinitionVersion().desc().list()) {
            repositoryService.deleteDeployment(processDefinition.getDeploymentId(),
                                               true);
        }

        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);
    }

    // Same test as above, but now with tenants
    public void testTimersRecreatedOnDeploymentDeleteWithTenantId() {

        // Deploy 4 versions without tenantId
        for (int i = 1; i <= 4; i++) {
            repositoryService.createDeployment()
                    .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v" + i + ".bpmn20.xml")
                    .deploy();
        }

        String testTenant = "Activiti-tenant";

        // Deploy v1
        String deployment1 = repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v1.bpmn20.xml")
                .tenantId(testTenant)
                .deploy().getId();

        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count()).isEqualTo(1);
        assertThat(managementService.createTimerJobQuery().jobTenantId(testTenant).count()).isEqualTo(1);

        // Deploy v2: no timer -> previous should be deleted
        String deployment2 = repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v2.bpmn20.xml")
                .tenantId(testTenant)
                .deploy().getId();

        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count()).isEqualTo(2);
        assertThat(managementService.createTimerJobQuery().jobTenantId(testTenant).count()).isEqualTo(0);

        // Deploy v3: no timer
        String deployment3 = repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v3.bpmn20.xml")
                .tenantId(testTenant)
                .deploy().getId();

        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count()).isEqualTo(3);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count()).isEqualTo(3);
        assertThat(managementService.createTimerJobQuery().jobTenantId(testTenant).count()).isEqualTo(0);

        // Deploy v4: no timer
        String deployment4 = repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testTimersRecreatedOnDeploymentDelete_v4.bpmn20.xml")
                .tenantId(testTenant)
                .deploy().getId();

        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count()).isEqualTo(4);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count()).isEqualTo(4);
        assertThat(managementService.createTimerJobQuery().jobTenantId(testTenant).count()).isEqualTo(1);

        // Delete v4 -> V3 active. No timer active anymore (v3 doesn't have a timer)
        repositoryService.deleteDeployment(deployment4,
                                           true);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count()).isEqualTo(3);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count()).isEqualTo(3);
        assertThat(managementService.createTimerJobQuery().jobTenantId(testTenant).count()).isEqualTo(0);

        // Delete v2 --> V3 still active, nothing changed there
        repositoryService.deleteDeployment(deployment2,
                                           true);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count()).isEqualTo(2);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count()).isEqualTo(2);
        assertThat(managementService.createTimerJobQuery().jobTenantId(testTenant).count()).isEqualTo(0);

        // Delete v3 -> fallback to v1
        repositoryService.deleteDeployment(deployment3, true);
        assertThat(repositoryService.createDeploymentQuery().deploymentTenantId(testTenant).count()).isEqualTo(1);
        assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionTenantId(testTenant).count()).isEqualTo(1);
        assertThat(managementService.createTimerJobQuery().jobTenantId(testTenant).count()).isEqualTo(1);

        // Cleanup
        for (ProcessDefinition processDefinition : repositoryService.createProcessDefinitionQuery().processDefinitionKey("timer").orderByProcessDefinitionVersion().desc().list()) {
            repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
        }

        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(0);
    }

    // Can't use @Deployment, we need to control the clock very strict to have a good test
    public void testMultipleStartEvents() {

        // Human time (GMT): Tue, 10 May 2016 18:50:01 GMT
        Date startTime = new Date(1462906201000L);
        processEngineConfiguration.getClock().setCurrentTime(startTime);

        String deploymentId = repositoryService.createDeployment()
                .addClasspathResource("org/activiti/engine/test/bpmn/event/timer/StartTimerEventTest.testMultipleStartEvents.bpmn20.xml")
                .deploy().getId();

        // After deployment, should have 4 jobs for the 4 timer events
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(4);
        assertThat(managementService.createTimerJobQuery().executable().count()).isEqualTo(0);

        // Path A: triggered at start + 10 seconds (18:50:11) (R2)
        // Path B: triggered at start + 5 seconds (18:50:06) (R3)
        // Path C: triggered at start + 15 seconds (18:50:16) (R1)
        // path D: triggerd at 18:50:20 (Cron)

        // Moving 7 seconds (18:50:08) should trigger one timer (the second start timer in the process diagram)
        Date newDate = new Date(startTime.getTime() + (7 * 1000));
        processEngineConfiguration.getClock().setCurrentTime(newDate);
        List<Job> executableTimers = managementService.createTimerJobQuery().executable().list();
        assertThat(executableTimers).hasSize(1);

        executeJobs(executableTimers);
        validateTaskCounts(0,
                           1,
                           0,
                           0);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(4);
        assertThat(managementService.createTimerJobQuery().executable().count()).isEqualTo(0);

        // New situation:
        // Path A : triggered at start + 10 seconds (18:50:11) (R2)
        // Path B: triggered at start + 2*5 seconds (18:50:11) (R2 - was R3) [CHANGED]
        // Path C: triggered at start + 15 seconds (18:50:16) (R1)
        // path D: triggerd at 18:50:20 (Cron)

        // Moving 4 seconds (18:50:12) should trigger both path A and B
        newDate = new Date(newDate.getTime() + (4 * 1000));
        processEngineConfiguration.getClock().setCurrentTime(newDate);

        executableTimers = managementService.createTimerJobQuery().executable().list();
        assertThat(executableTimers).hasSize(2);
        executeJobs(executableTimers);
        validateTaskCounts(1,
                           2,
                           0,
                           0);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(4);
        assertThat(managementService.createTimerJobQuery().executable().count()).isEqualTo(0);

        // New situation:
        // Path A : triggered at start + 2*10 seconds (18:50:21) (R1 - was R2) [CHANGED]
        // Path B: triggered at start + 3*5 seconds (18:50:16) (R1 - was R2) [CHANGED]
        // Path C: triggered at start + 15 seconds (18:50:16) (R1)
        // path D: triggerd at 18:50:20 (Cron)

        // Moving 6 seconds (18:50:18) should trigger B and C
        newDate = new Date(newDate.getTime() + (6 * 1000));
        processEngineConfiguration.getClock().setCurrentTime(newDate);

        executableTimers = managementService.createTimerJobQuery().executable().list();
        assertThat(executableTimers).hasSize(2);
        executeJobs(executableTimers);
        validateTaskCounts(1,
                           3,
                           1,
                           0);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(2);
        assertThat(managementService.createTimerJobQuery().executable().count()).isEqualTo(0);

        // New situation:
        // Path A : triggered at start + 2*10 seconds (18:50:21) (R1 - was R2) [CHANGED]
        // Path B: all repeats used up
        // Path C: all repeats used up
        // path D: triggerd at 18:50:20 (Cron)

        // Moving 10 seconds (18:50:28) should trigger A and D
        newDate = new Date(newDate.getTime() + (6 * 1000));
        processEngineConfiguration.getClock().setCurrentTime(newDate);

        executableTimers = managementService.createTimerJobQuery().executable().list();
        assertThat(executableTimers).hasSize(2);
        executeJobs(executableTimers);
        validateTaskCounts(2,
                           3,
                           1,
                           1);
        assertThat(managementService.createTimerJobQuery().count()).isEqualTo(1);
        assertThat(managementService.createTimerJobQuery().executable().count()).isEqualTo(0);

        // New situation:
        // Path A : all repeats used up
        // Path B: all repeats used up
        // Path C: all repeats used up
        // path D: triggerd at 18:50:40 (Cron)

        // Clean up
        repositoryService.deleteDeployment(deploymentId,
                                           true);
    }

    private void validateTaskCounts(long taskACount,
                                    long taskBCount,
                                    long taskCCount,
                                    long taskDCount) {
        assertThat(taskService.createTaskQuery().taskName("Task A").count()).as("task A counts are incorrect").isEqualTo(taskACount);
        assertThat(taskService.createTaskQuery().taskName("Task B").count()).as("task B counts are incorrect").isEqualTo(taskBCount);
        assertThat(taskService.createTaskQuery().taskName("Task C").count()).as("task C counts are incorrect").isEqualTo(taskCCount);
        assertThat(taskService.createTaskQuery().taskName("Task D").count()).as("task D counts are incorrect").isEqualTo(taskDCount);
    }

    private void executeJobs(List<Job> jobs) {
        for (Job job : jobs) {
            managementService.moveTimerToExecutableJob(job.getId());
            managementService.executeJob(job.getId());
        }
    }

    private void cleanDB() {
        String jobId = managementService.createTimerJobQuery().singleResult().getId();
        CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
        commandExecutor.execute(new CancelJobsCmd(jobId));
    }
}
