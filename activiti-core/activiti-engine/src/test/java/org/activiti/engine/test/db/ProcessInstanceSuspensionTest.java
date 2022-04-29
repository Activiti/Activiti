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
package org.activiti.engine.test.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.TimerJobEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 *
 */
public class ProcessInstanceSuspensionTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/db/oneJobProcess.bpmn20.xml" })
  public void testJobsNotVisibleToAcquisitionIfInstanceSuspended() {

    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(pd.getKey());

    // now there is one job:
    Job job = managementService.createTimerJobQuery().singleResult();
    assertThat(job).isNotNull();

    makeSureJobDue(job);

    // the acquirejobs command sees the job:
    List<TimerJobEntity> acquiredJobs = executeAcquireJobsCommand();
    assertThat(acquiredJobs).hasSize(1);

    // suspend the process instance:
    runtimeService.suspendProcessInstanceById(pi.getId());

    // now, the acquirejobs command does not see the job:
    acquiredJobs = executeAcquireJobsCommand();
    assertThat(acquiredJobs).hasSize(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/db/oneJobProcess.bpmn20.xml" })
  public void testJobsNotVisibleToAcquisitionIfDefinitionSuspended() {

    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(pd.getKey());

    // now there is one job:
    Job job = managementService.createTimerJobQuery().singleResult();
    assertThat(job).isNotNull();

    makeSureJobDue(job);

    // the acquire jobs command sees the job:
    List<TimerJobEntity> acquiredJobs = executeAcquireJobsCommand();
    assertThat(acquiredJobs).hasSize(1);

    // suspend the process instance:
    repositoryService.suspendProcessDefinitionById(pd.getId(), true, null);

    // now, the acquire jobs command does not see the job:
    acquiredJobs = executeAcquireJobsCommand();
    assertThat(acquiredJobs).hasSize(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/db/oneJobProcess.bpmn20.xml" })
  public void testJobsVisibleToAcquisitionIfDefinitionSuspendedWithoutProcessInstances() {

    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(pd.getKey());

    // now there is one job:
    Job job = managementService.createTimerJobQuery().singleResult();
    assertThat(job).isNotNull();

    makeSureJobDue(job);

    // the acquire jobs command sees the job:
    List<TimerJobEntity> acquiredJobs = executeAcquireJobsCommand();
    assertThat(acquiredJobs).hasSize(1);

    // suspend the process instance:
    repositoryService.suspendProcessDefinitionById(pd.getId());

    // the acquire jobs command still sees the job, because the process instances are not suspended:
    acquiredJobs = executeAcquireJobsCommand();
    assertThat(acquiredJobs).hasSize(1);
  }

  @Deployment
  public void testSuspendedProcessTimerExecution() throws Exception {
    // Process with boundary timer-event that fires in 1 hour
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("suspendProcess");
    assertThat(procInst).isNotNull();
    assertThat(managementService.createTimerJobQuery().processInstanceId(procInst.getId()).count()).isEqualTo(1);

    // Roll time ahead to be sure timer is due to fire
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());

    // Check if timer is eligible to be executed, when process in not yet suspended
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    List<TimerJobEntity> jobs = commandExecutor.execute(new Command<List<TimerJobEntity>>() {

      @Override
      public List<TimerJobEntity> execute(CommandContext commandContext) {
        return processEngineConfiguration.getTimerJobEntityManager().findTimerJobsToExecute(new Page(0, 1));
      }

    });
    assertThat(jobs).hasSize(1);

    // Suspend process instance
    runtimeService.suspendProcessInstanceById(procInst.getId());

    // Check if the timer is NOT acquired, even though the duedate is reached
    jobs = commandExecutor.execute(new Command<List<TimerJobEntity>>() {

      @Override
      public List<TimerJobEntity> execute(CommandContext commandContext) {
        return processEngineConfiguration.getTimerJobEntityManager().findTimerJobsToExecute(new Page(0, 1));
      }
    });

    assertThat(jobs).hasSize(0);
  }

  protected void makeSureJobDue(final Job job) {
    processEngineConfiguration.getCommandExecutor().execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        Date currentTime = processEngineConfiguration.getClock().getCurrentTime();
        commandContext.getTimerJobEntityManager().findById(job.getId()).setDuedate(new Date(currentTime.getTime() - 10000));
        return null;
      }

    });
  }

  protected List<TimerJobEntity> executeAcquireJobsCommand() {
    return processEngineConfiguration.getCommandExecutor().execute(new Command<List<TimerJobEntity>>() {
      public List<TimerJobEntity> execute(CommandContext commandContext) {
        return commandContext.getTimerJobEntityManager().findTimerJobsToExecute(new Page(0, 1));
      }

    });
  }

}
