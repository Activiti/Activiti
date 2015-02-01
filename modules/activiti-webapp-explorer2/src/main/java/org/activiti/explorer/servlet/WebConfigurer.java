package org.activiti.explorer.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.activiti.explorer.conf.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
public class WebConfigurer implements ServletContextListener {
	
  private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

  public AnnotationConfigWebApplicationContext context;
  
  public void setContext(AnnotationConfigWebApplicationContext context) {
    this.context = context;
  }
  
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext servletContext = sce.getServletContext();

    log.debug("Configuring Spring root application context");
    
    AnnotationConfigWebApplicationContext rootContext = null;
    
    if (context == null) {
        rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(ApplicationConfiguration.class);
        rootContext.refresh();
    } else {
        rootContext = context;
    }

    servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, rootContext);

    initSpring(servletContext, rootContext);

    log.debug("Web application fully configured");
  }

  /**
   * Initializes Spring and Spring MVC.
   */
  private ServletRegistration.Dynamic initSpring(ServletContext servletContext, AnnotationConfigWebApplicationContext rootContext) {
    log.debug("Configuring Spring Web application context");
    AnnotationConfigWebApplicationContext dispatcherServletConfiguration = new AnnotationConfigWebApplicationContext();
    dispatcherServletConfiguration.setParent(rootContext);
    dispatcherServletConfiguration.register(DispatcherServletConfiguration.class);

    log.debug("Registering Spring MVC Servlet");
    ServletRegistration.Dynamic dispatcherServlet = servletContext.addServlet("dispatcher", new DispatcherServlet(dispatcherServletConfiguration));
    dispatcherServlet.addMapping("/service/*");
    dispatcherServlet.setLoadOnStartup(1);
    dispatcherServlet.setAsyncSupported(true);
    
    return dispatcherServlet;
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    log.info("Destroying Web application");
    WebApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
    AnnotationConfigWebApplicationContext gwac = (AnnotationConfigWebApplicationContext) ac;
    gwac.close();
    log.debug("Web application destroyed");
  }
}
