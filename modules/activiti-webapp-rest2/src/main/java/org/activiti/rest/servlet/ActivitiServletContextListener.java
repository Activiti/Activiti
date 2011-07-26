package org.activiti.rest.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.util.LogUtil;

public class ActivitiServletContextListener implements ServletContextListener {
  
  //Initialise logging
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  protected static final Logger LOGGER = Logger.getLogger(ActivitiServletContextListener.class.getName());

  public void contextInitialized(ServletContextEvent event) {
    ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
    if (processEngine == null) {
      LOGGER.log(Level.SEVERE,"Could not start the Activiti Engine");
    }
  }

  public void contextDestroyed(ServletContextEvent event) {
    ProcessEngines.destroy();
  }

}
