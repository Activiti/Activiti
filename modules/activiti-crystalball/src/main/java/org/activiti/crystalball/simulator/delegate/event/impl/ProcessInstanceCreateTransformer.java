package org.activiti.crystalball.simulator.delegate.event.impl;

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
    if (ActivitiEventType.ENTITY_CREATED.equals(event.getType()) &&
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
