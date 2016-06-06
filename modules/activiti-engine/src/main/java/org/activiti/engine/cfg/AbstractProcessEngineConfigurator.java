package org.activiti.engine.cfg;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * @author jbarrez
 */
public abstract class AbstractProcessEngineConfigurator implements ProcessEngineConfigurator {
	
	public static int DEFAULT_CONFIGURATOR_PRIORITY = 10000;
	
	@Override
	public int getPriority() {
		return DEFAULT_CONFIGURATOR_PRIORITY;
	}
	
	public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    
  }
	
	public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
	  
	}

}
