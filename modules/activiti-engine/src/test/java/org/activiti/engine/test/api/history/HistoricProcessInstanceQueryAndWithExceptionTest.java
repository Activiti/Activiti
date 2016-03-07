package org.activiti.engine.test.api.history;

import java.util.List;

import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;

public class HistoricProcessInstanceQueryAndWithExceptionTest extends PluggableActivitiTestCase {

  private static final String PROCESS_DEFINITION_KEY_NO_EXCEPTION = "oneTaskProcess";
  private static final String PROCESS_DEFINITION_KEY_WITH_EXCEPTION_1 = "JobErrorCheck";
  private static final String PROCESS_DEFINITION_KEY_WITH_EXCEPTION_2 = "JobErrorDoubleCheck";

  private org.activiti.engine.repository.Deployment deployment;

  protected void setUp() throws Exception {
    super.setUp();
    deployment = repositoryService.createDeployment()
          .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
          .addClasspathResource("org/activiti/engine/test/api/runtime/JobErrorCheck.bpmn20.xml")
          .addClasspathResource("org/activiti/engine/test/api/runtime/JobErrorDoubleCheck.bpmn20.xml")
          .deploy();
  }
  
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deployment.getId(), true);
    super.tearDown();
  }
  
  public void testQueryWithException() throws InterruptedException{
    ProcessInstance processNoException = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_NO_EXCEPTION);
    
    HistoricProcessInstanceQuery queryNoException = historyService.createHistoricProcessInstanceQuery();
    assertEquals(1, queryNoException.count());
    assertEquals(1, queryNoException.list().size());
    assertEquals(processNoException.getId(), queryNoException.list().get(0).getId());

    HistoricProcessInstanceQuery queryWithException = historyService.createHistoricProcessInstanceQuery();
    assertEquals(0, queryWithException.withJobException().count());
    assertEquals(0, queryWithException.withJobException().list().size());
    
    ProcessInstance processWithException1 = startProcessInstanceWithFailingJob(PROCESS_DEFINITION_KEY_WITH_EXCEPTION_1);
    JobQuery jobQuery1 = managementService.createJobQuery().processInstanceId(processWithException1.getId());
    assertEquals(1, jobQuery1.withException().count());
    assertEquals(1, jobQuery1.withException().list().size());
    assertEquals(1, queryWithException.withJobException().count());
    assertEquals(1, queryWithException.withJobException().list().size());
    assertEquals(processWithException1.getId(), queryWithException.withJobException().list().get(0).getId());

    ProcessInstance processWithException2 = startProcessInstanceWithFailingJob(PROCESS_DEFINITION_KEY_WITH_EXCEPTION_2);
    JobQuery jobQuery2 = managementService.createJobQuery().processInstanceId(processWithException2.getId());
    assertEquals(2, jobQuery2.withException().count());
    assertEquals(2, jobQuery2.withException().list().size());

    assertEquals(2, queryWithException.withJobException().count());
    assertEquals(2, queryWithException.withJobException().list().size());
    assertEquals(processWithException1.getId(), queryWithException.withJobException().processDefinitionKey(PROCESS_DEFINITION_KEY_WITH_EXCEPTION_1).list().get(0).getId());
    assertEquals(processWithException2.getId(), queryWithException.withJobException().processDefinitionKey(PROCESS_DEFINITION_KEY_WITH_EXCEPTION_2).list().get(0).getId());
  }

  private ProcessInstance startProcessInstanceWithFailingJob(String processInstanceByKey) {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processInstanceByKey);
    
    List<Job> jobList = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .list();

    for(Job job : jobList){
        try {
          managementService.executeJob(job.getId());
          fail("RuntimeException");
        } catch(RuntimeException re) {
      }
    }
    return processInstance;
  }
}
