package org.activiti.camel;


/**
 * @author Saeid Mirzaei  
 */
import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class AsyncPingTest extends SpringActivitiTestCase {
	  
   @Autowired
   protected CamelContext camelContext;
   
   @Autowired
   protected RuntimeService runtimeService;
   
   public void  setUp() throws Exception {
     camelContext.addRoutes(new RouteBuilder() {

       @Override
       public void configure() throws Exception {
    	   from("activiti:asyncPingProcess:serviceAsyncPing").to("seda:continueAsync");
    	   from("seda:continueAsync").to("activiti:asyncPingProcess:receiveAsyncPing");
		   }
     });
   }
   
   public void tearDown() throws Exception {
     List<Route> routes = camelContext.getRoutes();
     for (Route r: routes) {
       camelContext.stopRoute(r.getId());
       camelContext.removeRoute(r.getId());
     }
   }
   
  
  @Deployment(resources = {"process/asyncPing.bpmn20.xml"})
  public void testRunProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncPingProcess");
    
    List<Execution> executionList = runtimeService.createExecutionQuery().list();
    Assert.assertEquals(1, executionList.size());

    managementService.executeJob(managementService.createJobQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    Thread.sleep(1500);
    
    executionList = runtimeService.createExecutionQuery().list();
    Assert.assertEquals(0, executionList.size());
   
    Assert.assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }

}
