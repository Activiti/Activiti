package org.activiti.crystalball.simulator.delegate.event.impl;

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


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.activiti.crystalball.simulator.CrystalballException;
import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.event.logger.handler.Fields;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author martin.grofcik
 */
public class EventLogProcessInstanceCreateTransformer extends EventLog2SimulationEventFunction {

  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  private final String processDefinitionIdKey;
  private final String businessKey;
  private final String variablesKey;

  public EventLogProcessInstanceCreateTransformer(String simulationEventType, String processDefinitionIdKey, String businessKey, String variablesKey) {
    super(simulationEventType);
    this.processDefinitionIdKey = processDefinitionIdKey;
    this.businessKey = businessKey;
    this.variablesKey = variablesKey;
  }

  @SuppressWarnings({ "unchecked" })
  @Override
  public SimulationEvent apply(EventLogEntry event) {
    if ("PROCESSINSTANCE_START".equals(event.getType())) {

      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> data;
      try {
        data = objectMapper.readValue(event.getData(), new TypeReference<HashMap<String, Object>>() {});
      } catch (IOException e) {
        throw new CrystalballException("unable to parse JSON string.", e);
      }
      String processDefinitionId = (String) data.get(Fields.PROCESS_DEFINITION_ID);
      Map<String, Object> variableMap = (Map<String, Object>) data.get(Fields.VARIABLES);
      String businessKeyValue = (String) data.get(Fields.BUSINESS_KEY);
      String processInstanceId= (String) data.get(Fields.PROCESS_INSTANCE_ID);

      Map<String, Object> simEventProperties = new HashMap<String, Object>();
      simEventProperties.put(processDefinitionIdKey, processDefinitionId);
      simEventProperties.put(this.businessKey, businessKeyValue);
      simEventProperties.put(variablesKey, variableMap);
      simEventProperties.put(PROCESS_INSTANCE_ID, processInstanceId);

      return new SimulationEvent.Builder(simulationEventType)
          .priority((int) event.getLogNumber())
          .properties(simEventProperties)
          .build();
    }
    return null;
  }
}
