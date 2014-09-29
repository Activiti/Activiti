package org.activiti.camel;


/**
 * @author Saeid Mirzaei  
 */
import java.util.List;

import org.activiti.engine.impl.test.JobTestHelper;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class AsyncPingTest {

   @Rule
   public ActivitiRule activitiRule = new ActivitiRule();
	  
   
   static CamelContext camelContext = new DefaultCamelContext();
   
   

   @BeforeClass
   public static void  setUp() throws Exception {
	 
	   
       camelContext.addRoutes(new RouteBuilder() {

       @Override
       public void configure() throws Exception {
    	   from("activiti:asyncPingProcess:serviceAsyncPing").to("activiti:asyncPingProcess:receiveAsyncPing");    	   
		  }
		});
  }
   
  @Test
  @Deployment(resources = {"process/asyncPing.bpmn20.xml"})
  public void testRunProcess() throws Exception {
    ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey("asyncPingProcess");
    
    List<Execution> executionList = activitiRule.getRuntimeService().createExecutionQuery().list();
    Assert.assertEquals(1, executionList.size());

    JobTestHelper.waitForJobExecutorToProcessAllJobs(activitiRule, 3000, 100);
    Thread.sleep(1500);
    
    executionList = activitiRule.getRuntimeService().createExecutionQuery().list();
    Assert.assertEquals(0, executionList.size());
   
    Assert.assertEquals(0, activitiRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }

}
