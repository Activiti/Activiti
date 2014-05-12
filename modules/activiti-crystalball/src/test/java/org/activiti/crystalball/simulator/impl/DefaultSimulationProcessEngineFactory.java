package org.activiti.crystalball.simulator.impl;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.runtime.Clock;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author martin.grofcik
 */
public class DefaultSimulationProcessEngineFactory implements FactoryBean<ProcessEngineImpl> {
  protected ProcessEngineImpl processEngine;

  public DefaultSimulationProcessEngineFactory(Clock clock) {
    this("", clock);
  }

  public DefaultSimulationProcessEngineFactory(String resourceToDeploy, Clock clock) {
    processEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
    if (!resourceToDeploy.isEmpty())
      processEngine.getRepositoryService().
        createDeployment().
        addClasspathResource(resourceToDeploy).
        deploy();

    final ProcessEngineConfigurationImpl processEngineConfiguration = processEngine.getProcessEngineConfiguration();
    processEngineConfiguration.setHistory("full");
    processEngineConfiguration.setClock(clock);
  }

  @Override
  public ProcessEngineImpl getObject() {
    return processEngine;
  }

  @Override
  public Class<?> getObjectType() {
    return ProcessEngineImpl.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
