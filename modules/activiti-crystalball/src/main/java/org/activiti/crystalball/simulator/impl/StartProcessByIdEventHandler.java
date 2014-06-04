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
