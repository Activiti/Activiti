package org.activiti.crystalball.simulator.impl;

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
