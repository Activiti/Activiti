package org.activiti.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.activiti.cdi.impl.ProcessEngineLookup;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * Implementation of the {@link ProcessEngineLookup} interface for providing a
 * seam configured process engine.
 * 
 * @author Daniel Meyer
 */
@Alternative
@ApplicationScoped
public class SeamConfiguredProcessEngine implements ProcessEngineLookup {

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected ProcessEngine processEngine;

  @Override
  public ProcessEngine getProcessEngine() {
    if (processEngine != null) {
      throw new IllegalStateException("Contract violation: " + getClass().getName()
              + " manages @ApplicationScoped ProcessEngines. ProcessEngine already built. " +
              		"Make sure not to call getProcessEngine() more than once.");
    }
    buildProcessEngine();
    return processEngine;
  }

  protected void buildProcessEngine() {
    processEngine = processEngineConfiguration.buildProcessEngine();
  }

  @Override
  public void ungetProcessEngine() {
    processEngine.close();
  }
    
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

}
