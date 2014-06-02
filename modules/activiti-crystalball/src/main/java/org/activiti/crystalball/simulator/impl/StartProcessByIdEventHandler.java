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
public class StartProcessByIdEventHandler implements SimulationEventHandler {

	private static Logger log = LoggerFactory.getLogger(StartProcessByIdEventHandler.class);
	
	/** process to start key */
	protected String processToStartIdKey;
  protected String businessKey;
  protected String variablesKey;

  public StartProcessByIdEventHandler(String processToStartIdKey, String businessKey, String variablesKey) {
    this.processToStartIdKey = processToStartIdKey;
    this.businessKey = businessKey;
    this.variablesKey = variablesKey;
  }


  @Override
	public void init() {
	}

	@Override
	public void handle(SimulationEvent event) {
		// start process now
    String processDefinitionId = (String) event.getProperty(processToStartIdKey);
    String businessKey = (String) event.getProperty(this.businessKey);
    @SuppressWarnings("unchecked")
    Map<String, Object> variables = (Map<String, Object>) event.getProperty(variablesKey);

    log.debug("Starting new processDefId[{}] businessKey[{}] with variables[{}]", processDefinitionId, businessKey, variables);
		SimulationRunContext.getRuntimeService().startProcessInstanceById(processDefinitionId, businessKey, variables);
	}

}
