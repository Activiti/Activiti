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


import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * @author martin.grofcik
 */
public class ProcessInstanceCreateTransformer extends Activiti2SimulationEventFunction {

  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  private final String processDefinitionIdKey;
  private final String businessKey;
  private final String variablesKey;

  public ProcessInstanceCreateTransformer(String simulationEventType, String processDefinitionIdKey, String businessKey, String variablesKey) {
    super(simulationEventType);
    this.processDefinitionIdKey = processDefinitionIdKey;
    this.businessKey = businessKey;
    this.variablesKey = variablesKey;
  }

  @Override
  public SimulationEvent apply(ActivitiEvent event) {
    if (ActivitiEventType.ENTITY_INITIALIZED.equals(event.getType()) &&
      (event instanceof ActivitiEntityEvent) &&
      ((ActivitiEntityEvent) event).getEntity() instanceof ProcessInstance &&
      ((ExecutionEntity) ((ActivitiEntityEvent) event).getEntity()).isProcessInstanceType()) {

      ProcessInstance processInstance = (ProcessInstance) ((ActivitiEntityEvent) event).getEntity();
      ExecutionEntity executionEntity = (ExecutionEntity) ((ActivitiEntityEvent) event).getEntity();

      Map<String, Object> simEventProperties = new HashMap<String, Object>();
      simEventProperties.put(processDefinitionIdKey, processInstance.getProcessDefinitionId());
      simEventProperties.put(businessKey, processInstance.getBusinessKey());
      simEventProperties.put(variablesKey, executionEntity.getVariables());
      simEventProperties.put(PROCESS_INSTANCE_ID, executionEntity.getProcessInstanceId());

      return new SimulationEvent.Builder(simulationEventType).
                  simulationTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime().getTime()).
                  properties(simEventProperties).
                  build();
    }
    return null;
  }
}
