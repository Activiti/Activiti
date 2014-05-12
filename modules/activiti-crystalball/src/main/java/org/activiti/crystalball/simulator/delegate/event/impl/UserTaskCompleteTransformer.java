package org.activiti.crystalball.simulator.delegate.event.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.task.Task;

import java.util.HashMap;
import java.util.Map;

/**
 * @author martin.grofcik
 */
public class UserTaskCompleteTransformer extends Activiti2SimulationEventFunction {

  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  public static final String TASK_DEFINITION_KEY = "taskDefinitionKey";
  public static final String TASK_VARIABLES = "taskVariables";

  public UserTaskCompleteTransformer(String simulationEventType) {
    super(simulationEventType);
  }

  @Override
  public SimulationEvent apply(ActivitiEvent event) {
    if (ActivitiEventType.TASK_COMPLETED.equals(event.getType())) {
      Task task = (Task) ((ActivitiEntityEvent) event).getEntity();

      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put("taskId", task.getId());
      properties.put(TASK_DEFINITION_KEY, task.getTaskDefinitionKey());
      properties.put(PROCESS_INSTANCE_ID, task.getProcessInstanceId());
      properties.put(TASK_VARIABLES, task.getProcessVariables());
      return
          new SimulationEvent.Builder(this.simulationEventType).
            simulationTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime().getTime()).
            properties(properties).
            build();
    }
    return null;
  }
}
