package org.activiti.engine.test.db;

import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.impl.jobexecutor.AcquiredJobs;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
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
    assertNotNull(managementService.createJobQuery()
      .singleResult());
    
    // the acquirejobs command sees the job:
    AcquiredJobs acquiredJobs = executeAcquireJobsCommand();
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
    assertNotNull(managementService.createJobQuery()
      .singleResult());
    
    // the acquirejobs command sees the job:
    AcquiredJobs acquiredJobs = executeAcquireJobsCommand();
    assertEquals(1, acquiredJobs.size());
    
    // suspend the process instance:
    repositoryService.suspendProcessDefinitionById(pd.getId());
    
    // now, the acquirejobs command does not see the job:
    acquiredJobs = executeAcquireJobsCommand();
    assertEquals(0, acquiredJobs.size());
  }

  private AcquiredJobs executeAcquireJobsCommand() {
    return processEngineConfiguration.getCommandExecutorTxRequired()
      .execute(new AcquireJobsCmd(processEngineConfiguration.getJobExecutor()));
  }
    
}
