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
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.event.logger.handler.Fields;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author martin.grofcik
 */
public class EventLogUserTaskCompleteTransformer extends EventLog2SimulationEventFunction {

  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  public static final String TASK_DEFINITION_KEY = "taskDefinitionKey";
  public static final String TASK_VARIABLES = "taskVariables";
  public static final String VARIABLES_LOCAL_SCOPE = "variablesLocalScope";

  public EventLogUserTaskCompleteTransformer(String simulationEventType) {
    super(simulationEventType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public SimulationEvent apply(EventLogEntry event) {
    if (ActivitiEventType.TASK_COMPLETED.toString().equals(event.getType())) {

      ObjectMapper objectMapper = new ObjectMapper();
      Map<String, Object> data;
      try {
        data = objectMapper.readValue(event.getData(), new TypeReference<HashMap<String, Object>>() {});
      } catch (IOException e) {
        throw new CrystalballException("unable to parse JSON string.", e);
      }
      String taskIdValue = (String) data.get(Fields.ACTIVITY_ID);
      boolean localScope = false;
      Map<String, Object> variableMap = null;
      if (data.get(Fields.VARIABLES) != null) {
        variableMap = (Map<String, Object>) data.get(Fields.VARIABLES);
      } else {
        variableMap = (Map<String, Object>) data.get(Fields.LOCAL_VARIABLES);
        localScope = true;
      }
      String taskDefinitionKeyValue = (String) data.get(Fields.TASK_DEFINITION_KEY);
      
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put("taskId", taskIdValue);
      properties.put(TASK_DEFINITION_KEY, taskDefinitionKeyValue);
      properties.put(PROCESS_INSTANCE_ID, event.getProcessInstanceId());
      if (variableMap != null) {
        properties.put(TASK_VARIABLES, variableMap);
        properties.put(VARIABLES_LOCAL_SCOPE, localScope);
      }
      
      return new SimulationEvent.Builder(this.simulationEventType)
          .priority((int) event.getLogNumber())
          .properties(properties)
          .build();
    }
    return null;
  }

}
