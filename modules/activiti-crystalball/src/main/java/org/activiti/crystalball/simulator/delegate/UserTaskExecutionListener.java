package org.activiti.crystalball.simulator.delegate;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.crystalball.simulator.delegate.event.impl.UserTaskCompleteTransformer;
import org.activiti.crystalball.simulator.impl.StartReplayProcessEventHandler;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * in the case of task event create simulation event in the event calendar
 *
 * @author martin.grofcik
 */
public class UserTaskExecutionListener implements TaskListener {

  private final String typeToFind;
  protected final String typeToCreate;
  private final Collection<SimulationEvent> events;

  public UserTaskExecutionListener(String typeToFind, String typeToCreate, Collection<SimulationEvent> events) {
    this.typeToFind = typeToFind;
    this.typeToCreate = typeToCreate;
    this.events = events;
  }

	@Override
	public void notify(DelegateTask delegateTask) {
    SimulationEvent eventToSimulate = findUserTaskCompleteEvent(delegateTask);
    if (eventToSimulate != null) {
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put("taskId", delegateTask.getId());
      properties.put("variables", eventToSimulate.getProperty(UserTaskCompleteTransformer.TASK_VARIABLES));
      // we were able to resolve event to simulate automatically
      SimulationEvent e = new SimulationEvent.Builder(typeToCreate).
                          properties(properties).
                          build();
      SimulationRunContext.getEventCalendar().addEvent(e);
    }
	}

  private SimulationEvent findUserTaskCompleteEvent(DelegateTask delegateTask) {
    if (delegateTask.hasVariable(StartReplayProcessEventHandler.PROCESS_INSTANCE_ID)) {
      String toSimulateProcessInstanceId = (String) delegateTask.getVariable(StartReplayProcessEventHandler.PROCESS_INSTANCE_ID);
      String toSimulateTaskDefinitionKey = delegateTask.getTaskDefinitionKey();
      for (SimulationEvent e : events) {
        if (typeToFind.equals(e.getType())
          && toSimulateProcessInstanceId.equals(e.getProperty(UserTaskCompleteTransformer.PROCESS_INSTANCE_ID))
          && toSimulateTaskDefinitionKey.equals(e.getProperty(UserTaskCompleteTransformer.TASK_DEFINITION_KEY)))
          return e;
      }
    }
    return null;
  }
}
