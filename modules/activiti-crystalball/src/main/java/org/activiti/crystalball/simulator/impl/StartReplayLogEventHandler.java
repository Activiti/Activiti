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


import java.util.HashMap;
import java.util.Map;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class schedules replay start simulation event and takes care about process start and
 * next event schedule
 *
 * @author martin.grofcik
 */
public class StartReplayLogEventHandler implements SimulationEventHandler {

  private static Logger log = LoggerFactory.getLogger(StartReplayLogEventHandler.class.getName());

  /** variable name where original process instance ID is stored - only for internal replay purposes */
  public static final String PROCESS_INSTANCE_ID = "_replay.processInstanceId";
  public static final String SIMULATION_RUN_ID = "_replay.simulationRunId";

  private final String processInstanceId;
  private final String processToStartIdKey;
  private final String businessKey;
  private final String variablesKey;

  public StartReplayLogEventHandler(String processInstanceId, String processToStartIdKey, String businessKey, String variablesKey) {
    this.processInstanceId = processInstanceId;
    this.processToStartIdKey = processToStartIdKey;
    this.businessKey = businessKey;
    this.variablesKey = variablesKey;
  }

  @Override
  public void init() {

  }

  @SuppressWarnings("unchecked")
  @Override
  public void handle(SimulationEvent event) {
    // start process now
    String processDefinitionId = (String) event.getProperty(processToStartIdKey);
    String eventBusinessKey = (String) event.getProperty(this.businessKey);
    Map<String, Object> variables = new HashMap<String, Object>();
    Map<String, Object> processVariables = (Map<String, Object>) event.getProperty(variablesKey);
    if (processVariables != null) {
      variables.putAll(processVariables);
    }
    variables.put(PROCESS_INSTANCE_ID, processInstanceId);
    variables.put(SIMULATION_RUN_ID, SimulationRunContext.getSimulationRunId());

    String startBusinessKey = null;
    if (eventBusinessKey != null) {
      startBusinessKey = eventBusinessKey;
    } else {
      startBusinessKey = this.businessKey;
    }
    log.debug("Starting new processDefId[{}] businessKey[{}] with variables[{}]", processDefinitionId, startBusinessKey, variables);
    SimulationRunContext.getRuntimeService().startProcessInstanceById(processDefinitionId, startBusinessKey, variables);
  }
}
