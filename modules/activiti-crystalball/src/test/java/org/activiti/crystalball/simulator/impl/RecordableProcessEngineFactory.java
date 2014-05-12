package org.activiti.crystalball.simulator.impl;

import org.activiti.crystalball.simulator.delegate.event.impl.AbstractRecordActivitiEventListener;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.runtime.Clock;

/**
 * This class is factory for recordable process engines
 */
public class RecordableProcessEngineFactory extends DefaultSimulationProcessEngineFactory {

  private AbstractRecordActivitiEventListener listener;

  public RecordableProcessEngineFactory(Clock clock, AbstractRecordActivitiEventListener listener) {
    this("", clock, listener);
  }

  public RecordableProcessEngineFactory(String resourceToDeploy, Clock clock, AbstractRecordActivitiEventListener listener) {
    super(resourceToDeploy, clock);
    this.listener = listener;
    final ProcessEngineConfigurationImpl processEngineConfiguration = this.processEngine.getProcessEngineConfiguration();
    processEngineConfiguration.setDatabaseSchemaUpdate("create-drop");
  }

  @Override
  public ProcessEngineImpl getObject() {
    ProcessEngineImpl processEngine = super.getObject();

    //add eventListener
    final ProcessEngineConfigurationImpl processEngineConfiguration = processEngine.getProcessEngineConfiguration();
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);

    return processEngine;
  }
}