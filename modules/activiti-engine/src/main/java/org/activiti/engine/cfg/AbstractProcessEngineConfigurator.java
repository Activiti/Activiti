package org.activiti.engine.cfg;

/**
 * @author jbarrez
 */
public abstract class AbstractProcessEngineConfigurator implements ProcessEngineConfigurator {
	
	public static int DEFAULT_CONFIGURATOR_PRIORITY = 10000;
	
	@Override
	public int getPriority() {
		return DEFAULT_CONFIGURATOR_PRIORITY;
	}

}
