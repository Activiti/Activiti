package org.activiti.engine.test.db;

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
 * @author Daniel Meyer
 */
public class ProcessInstanceSuspensionTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/engine/test/db/oneJobProcess.bpmn20.xml" })
  public void testJobsNotVisibleToAcquisitionIfInstanceSuspended() {

    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(pd.getKey());

    // now there is one job:
    Job job = managementService.createTimerJobQuery().singleResult();
    assertNotNull(job);

    makeSureJobDue(job);

    // the acquirejobs command sees the job:
    List<TimerJobEntity> acquiredJobs = executeAcquireJobsCommand();
    assertEquals(1, acquiredJobs.size());

    // suspend the process instance:
    runtimeService.suspendProcessInstanceById(pi.getId());

    // now, the acquirejobs command does not see the job:
    acquiredJobs = executeAcquireJobsCommand();
    assertEquals(0, acquiredJobs.size());
  }

  @Deployment(resources = { "org/activiti/engine/test/db/oneJobProcess.bpmn20.xml" })
  public void testJobsNotVisibleToAcquisitionIfDefinitionSuspended() {

    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(pd.getKey());
    
    // now there is one job:
    Job job = managementService.createTimerJobQuery().singleResult();
    assertNotNull(job);

    makeSureJobDue(job);

    // the acquire jobs command sees the job:
    List<TimerJobEntity> acquiredJobs = executeAcquireJobsCommand();
    assertEquals(1, acquiredJobs.size());

    // suspend the process instance:
    repositoryService.suspendProcessDefinitionById(pd.getId(), true, null);

    // now, the acquire jobs command does not see the job:
    acquiredJobs = executeAcquireJobsCommand();
    assertEquals(0, acquiredJobs.size());
  }
  
  @Deployment(resources = { "org/activiti/engine/test/db/oneJobProcess.bpmn20.xml" })
  public void testJobsVisibleToAcquisitionIfDefinitionSuspendedWithoutProcessInstances() {

    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceByKey(pd.getKey());
    
    // now there is one job:
    Job job = managementService.createTimerJobQuery().singleResult();
    assertNotNull(job);

    makeSureJobDue(job);

    // the acquire jobs command sees the job:
    List<TimerJobEntity> acquiredJobs = executeAcquireJobsCommand();
    assertEquals(1, acquiredJobs.size());

    // suspend the process instance:
    repositoryService.suspendProcessDefinitionById(pd.getId());

    // the acquire jobs command still sees the job, because the process instances are not suspended:
    acquiredJobs = executeAcquireJobsCommand();
    assertEquals(1, acquiredJobs.size());
  }

  @Deployment
  public void testSuspendedProcessTimerExecution() throws Exception {
    // Process with boundary timer-event that fires in 1 hour
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("suspendProcess");
    assertNotNull(procInst);
    assertEquals(1, managementService.createTimerJobQuery().processInstanceId(procInst.getId()).count());

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
    assertEquals(1, jobs.size());

    // Suspend process instance
    runtimeService.suspendProcessInstanceById(procInst.getId());

    // Check if the timer is NOT acquired, even though the duedate is reached
    jobs = commandExecutor.execute(new Command<List<TimerJobEntity>>() {

      @Override
      public List<TimerJobEntity> execute(CommandContext commandContext) {
        return processEngineConfiguration.getTimerJobEntityManager().findTimerJobsToExecute(new Page(0, 1));
      }
    });
    
    assertEquals(0, jobs.size());
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
