package org.activiti.engine.impl.webservice.experimental;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.ws.Endpoint;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

/**
 * Easy Servlet to provide JAX-B WebServices via CXF (see http://cxf.apache.org/docs/servlet-transport.html).
 * 
 * To play with it, add the following Servlet to your activiti-rest web.xml:
 * 
  <servlet>   
    <servlet-name>WsCxfServlet</servlet-name>
    <servlet-class>org.activiti.engine.impl.webservice.experimental.WebServiceProviderServlet</servlet-class>
  </servlet>
  <servlet-mapping>
    <servlet-name>WsCxfServlet</servlet-name>
    <url-pattern>/ws/*</url-pattern> 
  </servlet-mapping>
 * 
 * After adding the activiti-cxf.jar to your activiti-rest WEB-INF/lib directory you can access the WebService
 * via: http://localhost:8080/activiti-rest/ws/RuntimeService?wsdl
 * 
 * Currently data types are a problem (the Maps, the {@link ProcessInstance} interfaces, ...
 * 
 * So this was really just a first test to get an impression
 * 
 * @author ruecker
 */
public class WebServiceProviderServlet extends CXFNonSpringServlet {
  
  private static final long serialVersionUID = 1L;

  @Override
  public void loadBus(ServletConfig servletConfig) throws ServletException {
      super.loadBus(servletConfig);
      
      // TODO: Hacky first version to get default Process Engine
      ProcessEngine processEngine = ProcessEngineConfiguration
        .createProcessEngineConfigurationFromResourceDefault()
        .buildProcessEngine();
      
      Bus bus = getBus();
      BusFactory.setDefaultBus(bus); 
      Endpoint.publish("/RuntimeService", new RuntimeWebServiceImpl(processEngine));
      
      // You can als use the simple frontend API to do this
      ServerFactoryBean factory = new ServerFactoryBean();
      factory.setBus(bus);
      factory.setServiceClass(RuntimeWebServiceImpl.class);
      factory.setAddress("/RuntimeService");
      factory.create();              
  }

}
