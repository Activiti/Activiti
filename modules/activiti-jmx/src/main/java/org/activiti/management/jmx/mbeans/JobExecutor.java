package org.activiti.management.jmx.mbeans;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.management.jmx.annotations.ManagedAttribute;
import org.activiti.management.jmx.annotations.ManagedOperation;
import org.activiti.management.jmx.annotations.ManagedResource;


@ManagedResource(description = "Process definition MBean")
public class JobExecutor {
  
 
  ProcessEngineConfiguration processEngineConfig;
  
  public JobExecutor(ProcessEngineConfiguration processEngineConfig) {
     this.processEngineConfig = processEngineConfig;
  }
  
  
  @ManagedAttribute(description = "check if the job executor is activated")
  public boolean isJobExecutorActivated() {
    return processEngineConfig.isJobExecutorActivate();
  }
  
  @ManagedOperation(description = "set job executor activate")
  public void setJobExecutorActivate(Boolean active) {
    processEngineConfig.setJobExecutorActivate(active);
    
  }
  


}
