package org.activiti.engine.test.db;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.asyncexecutor.AcquiredJobEntities;
import org.activiti.engine.impl.cmd.AcquireTimerJobsCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.GetUnlockedTimersByDuedateCmd;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
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

  @Deployment(resources={"org/activiti/engine/test/db/oneJobProcess.bpmn20.xml"})
  public void testJobsNotVisisbleToAcquisitionIfInstanceSuspended() {
    
    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(pd.getKey());
    
    // now there is one job:
    // now there is one job:
    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    
    makeSureJobDue(job);
    
    // the acquirejobs command sees the job:
    AcquiredJobEntities acquiredJobs = executeAcquireJobsCommand();
    assertEquals(1, acquiredJobs.size());
    
    // suspend the process instance:
    runtimeService.suspendProcessInstanceById(pi.getId());
    
    // now, the acquirejobs command does not see the job:
    acquiredJobs = executeAcquireJobsCommand();
    assertEquals(0, acquiredJobs.size());    
  }
  
  @Deployment(resources={"org/activiti/engine/test/db/oneJobProcess.bpmn20.xml"})
  public void testJobsNotVisisbleToAcquisitionIfDefinitionSuspended() {
    
    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();    
    runtimeService.startProcessInstanceByKey(pd.getKey());    
    // now there is one job:
    Job job = managementService.createJobQuery()
      .singleResult();
    assertNotNull(job);
    
    makeSureJobDue(job);
        
    // the acquirejobs command sees the job:
    AcquiredJobEntities acquiredJobs = executeAcquireJobsCommand();
    assertEquals(1, acquiredJobs.size());
    
    // suspend the process instance:
    repositoryService.suspendProcessDefinitionById(pd.getId());
    
    // now, the acquirejobs command does not see the job:
    acquiredJobs = executeAcquireJobsCommand();
    assertEquals(0, acquiredJobs.size());
  }
  
  @Deployment
  public void testSuspendedProcessTimerExecution() throws Exception {
    // Process with boundary timer-event that fires in 1 hour
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("suspendProcess");
    assertNotNull(procInst);
    assertEquals(1, managementService.createJobQuery().processInstanceId(procInst.getId()).count());
    
    // Roll time ahead to be sure timer is due to fire
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    processEngineConfiguration.getClock().setCurrentTime(tomorrow.getTime());
    
    // Check if timer is eligable to be executed, when process in not yet suspended
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    List<TimerEntity> jobs = commandExecutor.execute(new GetUnlockedTimersByDuedateCmd(processEngineConfiguration.getClock().getCurrentTime(), new Page(0, 1)));
    assertEquals(1, jobs.size());
    
    // Suspend process instancd
    runtimeService.suspendProcessInstanceById(procInst.getId());

    // Check if the timer is NOT aquired, even though the duedate is reached
    jobs = commandExecutor.execute(new GetUnlockedTimersByDuedateCmd(processEngineConfiguration.getClock().getCurrentTime(), new Page(0, 1)));
    assertEquals(0, jobs.size());
  }

  protected void makeSureJobDue(final Job job) {
    processEngineConfiguration.getCommandExecutor()
      .execute(new Command<Void>() {
        public Void execute(CommandContext commandContext) {
          Date currentTime = processEngineConfiguration.getClock().getCurrentTime();
          commandContext.getJobEntityManager()
            .findJobById(job.getId())
            .setDuedate(new Date(currentTime.getTime() - 10000));
          return null;
        }
        
      });
  }

  private AcquiredJobEntities executeAcquireJobsCommand() {
    return processEngineConfiguration.getCommandExecutor()
      .execute(new AcquireTimerJobsCmd("testLockOwner", 60000, 5));
  }
    
}
