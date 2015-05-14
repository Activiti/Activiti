package org.activiti.crystalball.simulator.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class drives simulation event handling by script.
 *
 * @author martin.grofcik
 */
public class ScriptEventHandler implements SimulationEventHandler {

  private static Logger log = LoggerFactory.getLogger(ScriptEventHandler.class);

  protected String scriptPropertyName;
  protected String language;


  public ScriptEventHandler(String scriptPropertyName, String language) {
    this.scriptPropertyName = scriptPropertyName;
    this.language = language;
  }

  @Override
  public void init() {

  }

  @Override
  public void handle(SimulationEvent event) {
    ScriptingEngines scriptingEngines = Context
      .getProcessEngineConfiguration()
      .getScriptingEngines();

    VariableScope execution = SimulationRunContext.getExecution();
    try {
      scriptingEngines.evaluate((String) event.getProperty(this.scriptPropertyName), language, execution, false);

    } catch (ActivitiException e) {
      log.warn("Exception while executing simulation event " + event + " scriptPropertyName :" +
        this.scriptPropertyName + "\n script: "+ event.getProperty(this.scriptPropertyName) +
        "\n exception is:" + e.getMessage());
      throw e;
    }
  }
}
