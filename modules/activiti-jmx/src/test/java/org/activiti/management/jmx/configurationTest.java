package org.activiti.management.jmx;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.junit.Test;



public class configurationTest {


  @Test
  public void firstConfig() throws InterruptedException {
    
    ProcessEngine processEngine =  ProcessEngines.getDefaultProcessEngine();
    processEngine.getRepositoryService().createDeployment().addClasspathResource("org/activiti/management/jmx/trivialProcess.bpmn").deploy();
    processEngine.getRepositoryService().createDeployment().addClasspathResource("org/activiti/management/jmx/trivialProcess.bpmn").deploy();
    Thread.sleep(100000000);
    
    
  }
}
