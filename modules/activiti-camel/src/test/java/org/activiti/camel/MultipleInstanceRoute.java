package org.activiti.camel;

import java.util.List;

import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration("classpath:generic-camel-activiti-context.xml")
public class MultipleInstanceRoute extends SpringActivitiTestCase {
	
  @Autowired
  protected CamelContext camelContext;

  public void  setUp() throws Exception {
    camelContext.addRoutes(new RouteBuilder() {

		  @Override
		  public void configure() throws Exception {
		    from("activiti:multiInstanceCamelProcess:servicetask1").to("log:logMessage");
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

	@Deployment(resources = {"process/multiInstanceCamel.bpmn20.xml"})
	public void testCamelBody() throws Exception {
		runtimeService.startProcessInstanceByKey("multiInstanceCamelProcess");
	}
}