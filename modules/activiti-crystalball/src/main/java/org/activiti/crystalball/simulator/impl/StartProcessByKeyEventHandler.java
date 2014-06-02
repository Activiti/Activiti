package org.activiti.crystalball.simulator.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Start new process event handler for playback purposes
 *
 * @author martin.grofcik
 */
public class StartProcessByKeyEventHandler implements SimulationEventHandler {

	private static Logger log = LoggerFactory.getLogger(StartProcessByKeyEventHandler.class);

	/** process to start key */
	protected String processToStartKey;
  protected String businessKey;
  protected String variablesKey;

  public StartProcessByKeyEventHandler(String processToStartKey, String businessKey, String variablesKey) {
    this.processToStartKey = processToStartKey;
    this.businessKey = businessKey;
    this.variablesKey = variablesKey;
  }


  @Override
	public void init() {
	}

	@Override
	public void handle(SimulationEvent event) {
		// start process now
    String processDefinitionKey = (String) event.getProperty(processToStartKey);
    String businessKey = (String) event.getProperty(this.businessKey);
    @SuppressWarnings("unchecked")
    Map<String, Object> variables = (Map<String, Object>) event.getProperty(variablesKey);

    log.debug("Starting new processDefKey[{}] businessKey[{}] with variables[{}]", processDefinitionKey, businessKey, variables);
		SimulationRunContext.getRuntimeService().startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
	}

}
