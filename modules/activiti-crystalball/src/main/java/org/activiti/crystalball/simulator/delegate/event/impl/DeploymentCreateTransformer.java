package org.activiti.crystalball.simulator.delegate.event.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * @author martin.grofcik
 */
public class DeploymentCreateTransformer extends Activiti2SimulationEventFunction {

  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  private final String resourcesKey;

  public DeploymentCreateTransformer(String simulationEventType, String resourcesKey) {
    super(simulationEventType);
    this.resourcesKey = resourcesKey;
  }

  @Override
  public SimulationEvent apply(ActivitiEvent event) {
    if (ActivitiEventType.ENTITY_CREATED.equals(event.getType()) &&
      (event instanceof ActivitiEntityEvent) &&
      ((ActivitiEntityEvent) event).getEntity() instanceof DeploymentEntity) {

      DeploymentEntity deploymentEntity = (DeploymentEntity) ((ActivitiEntityEvent) event).getEntity();

      Map<String, Object> simEventProperties = new HashMap<String, Object>();
      simEventProperties.put(resourcesKey, deploymentEntity.getResources());

      return new SimulationEvent.Builder(simulationEventType).
                  simulationTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime().getTime()).
                  properties(simEventProperties).
                  build();
    }
    return null;
  }
}
