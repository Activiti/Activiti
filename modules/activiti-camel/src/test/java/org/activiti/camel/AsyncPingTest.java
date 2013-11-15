package org.activiti.camel;


/**
 * @author Saeid Mirzaei  
 */
import java.util.List;

/**
 * @author Saeid Mirzaei 
 */

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:camel-activiti-context.xml")
public class AsyncPingTest extends SpringActivitiTestCase {

  @Autowired
  RuntimeService runtimeService;

  @Deployment(resources = {"process/asyncPing.bpmn20.xml"})
  public void testRunProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncPingProcess");
    
    List<Execution> executionList = runtimeService.createExecutionQuery().list();
    assertEquals(1, executionList.size());

    waitForJobExecutorToProcessAllJobs(3000, 100);
    Thread.sleep(1500);
    
    executionList = runtimeService.createExecutionQuery().list();
    assertEquals(0, executionList.size());
   
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }

}
