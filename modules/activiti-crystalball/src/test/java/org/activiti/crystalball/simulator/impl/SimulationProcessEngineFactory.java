package org.activiti.crystalball.simulator.impl;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.runtime.Clock;
import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author martin.grofcik
 */
public class SimulationProcessEngineFactory implements FactoryBean<ProcessEngineImpl> {
  protected final ProcessEngineConfiguration processEngineConfiguration;
  protected final AtomicLong uniqueLongId;

  public SimulationProcessEngineFactory(ProcessEngineConfiguration processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
    this.uniqueLongId = new AtomicLong(0);
  }

  @Override
  public ProcessEngineImpl getObject() {
    this.processEngineConfiguration.setProcessEngineName("simRunProcessEngine-" + uniqueLongId.getAndIncrement());

    return (ProcessEngineImpl) this.processEngineConfiguration.buildProcessEngine();
  }

  @Override
  public Class<?> getObjectType() {
    return ProcessEngineImpl.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
}
