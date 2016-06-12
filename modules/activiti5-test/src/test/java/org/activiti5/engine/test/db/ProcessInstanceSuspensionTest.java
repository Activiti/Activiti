package org.activiti5.engine.test.db;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti5.engine.impl.interceptor.Command;
import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;

/**
 * 
 * @author Daniel Meyer
 */
public class ProcessInstanceSuspensionTest extends PluggableActivitiTestCase {

  @Deployment(resources={"org/activiti5/engine/test/db/oneJobProcess.bpmn20.xml"})
  public void testJobsNotVisisbleToAcquisitionIfInstanceSuspended() {
    
    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(pd.getKey());
    
    // now there is one job:
    Job job = managementService.createTimerJobQuery().singleResult();
    assertNotNull(job);
    
    makeSureJobDue(job);
    
    // suspend the process instance:
    runtimeService.suspendProcessInstanceById(pi.getId());
    
    job = managementService.createTimerJobQuery().singleResult();
    assertNull(job);
    
    assertEquals(1, managementService.createSuspendedJobQuery().processInstanceId(pi.getId()).count());
  }
  
  @Deployment(resources={"org/activiti5/engine/test/db/oneJobProcess.bpmn20.xml"})
  public void testJobsNotVisisbleToAcquisitionIfDefinitionSuspended() {
    
    ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().singleResult();    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(pd.getKey());    
    // now there is one job:
    Job job = managementService.createTimerJobQuery().singleResult();
    assertNotNull(job);
    
    makeSureJobDue(job);
        
    // suspend the process instance:
    repositoryService.suspendProcessDefinitionById(pd.getId(), true, null);
    
    job = managementService.createTimerJobQuery().singleResult();
    assertNull(job);
    
    assertEquals(1, managementService.createSuspendedJobQuery().processInstanceId(pi.getId()).count());
  }
  
  @Deployment
  public void testSuspendedProcessTimerExecution() throws Exception {
    Clock clock = processEngineConfiguration.getClock();
    
    // Process with boundary timer-event that fires in 1 hour
    ProcessInstance procInst = runtimeService.startProcessInstanceByKey("suspendProcess");
    assertNotNull(procInst);
    assertEquals(1, managementService.createTimerJobQuery().processInstanceId(procInst.getId()).count());
    
    // Roll time ahead to be sure timer is due to fire
    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DAY_OF_YEAR, 1);
    clock.setCurrentCalendar(tomorrow);
    processEngineConfiguration.setClock(clock);
    
    // Check if timer is eligable to be executed, when process in not yet suspended
    List<Job> jobs = managementService.createTimerJobQuery().executable().processInstanceId(procInst.getId()).list();
    assertEquals(1, jobs.size());
    
    // Suspend process instance
    runtimeService.suspendProcessInstanceById(procInst.getId());

    // Check if the timer is NOT aquired, even though the duedate is reached
    jobs = managementService.createTimerJobQuery().executable().processInstanceId(procInst.getId()).list();
    assertEquals(0, jobs.size());
    
    processEngineConfiguration.resetClock();
  }

  protected void makeSureJobDue(final Job job) {
    CommandExecutor commandExecutor = (CommandExecutor) processEngineConfiguration.getActiviti5CompatibilityHandler().getRawCommandExecutor();
    commandExecutor.execute(new Command<Void>() {
      public Void execute(CommandContext commandContext) {
        Date currentTime = processEngineConfiguration.getClock().getCurrentTime();
        commandContext.getTimerJobEntityManager()
          .findJobById(job.getId())
          .setDuedate(new Date(currentTime.getTime() - 10000));
        return null;
      }
      
    });
  }
    
}
