package org.activiti.crystalball.simulator.impl;

import org.activiti.crystalball.simulator.delegate.event.impl.AbstractRecordActivitiEventListener;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.runtime.Clock;

/**
 * This class is factory for recordable process engines
 */
public class RecordableProcessEngineFactory extends SimulationProcessEngineFactory {

  public RecordableProcessEngineFactory(ProcessEngineConfigurationImpl processEngineConfiguration, AbstractRecordActivitiEventListener listener) {
    super(processEngineConfiguration);
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  public ProcessEngineImpl getObject() {
    ProcessEngineImpl processEngine = super.getObject();

    return processEngine;
  }
}