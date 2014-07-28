package org.activiti.crystalball.simulator.impl.replay;

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


import java.util.Map;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.crystalball.simulator.delegate.event.impl.EventLogUserTaskCompleteTransformer;
import org.activiti.crystalball.simulator.impl.StartReplayLogEventHandler;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * complete user task handler for replay purposes
 *
 * @author martin.grofcik
 */
public class ReplayUserTaskCompleteEventHandler implements SimulationEventHandler {

	private static Logger log = LoggerFactory.getLogger(ReplayUserTaskCompleteEventHandler.class);

	@Override
	public void handle(SimulationEvent event) {
    String taskDefinitionKey = (String) event.getProperty(EventLogUserTaskCompleteTransformer.TASK_DEFINITION_KEY);
    String processInstanceId = (String) event.getProperty(EventLogUserTaskCompleteTransformer.PROCESS_INSTANCE_ID);
    String simulationRunId = SimulationRunContext.getSimulationRunId();
    ProcessInstance processInstance = SimulationRunContext.getRuntimeService().createProcessInstanceQuery().
        variableValueEquals(StartReplayLogEventHandler.PROCESS_INSTANCE_ID, processInstanceId).
        variableValueEquals(StartReplayLogEventHandler.SIMULATION_RUN_ID, simulationRunId).
        singleResult();
    
    Task task = SimulationRunContext.getTaskService().createTaskQuery().
        processInstanceId(processInstance.getId()).
        taskDefinitionKey(taskDefinitionKey).
        singleResult();

		@SuppressWarnings("unchecked")
		Map<String, Object> variables = (Map<String, Object>) event.getProperty(EventLogUserTaskCompleteTransformer.TASK_VARIABLES);		
		if (variables != null) {
		  if (event.getProperty(EventLogUserTaskCompleteTransformer.VARIABLES_LOCAL_SCOPE) != null && 
		      ((Boolean) event.getProperty(EventLogUserTaskCompleteTransformer.VARIABLES_LOCAL_SCOPE)).booleanValue()) {
		  
		    SimulationRunContext.getTaskService().complete(task.getId(), variables, true);
		      
		  } else {
		    SimulationRunContext.getTaskService().complete(task.getId(), variables);
		  }
		  log.debug( "completed {}, {}, {}", task, task.getName(), variables);
		} else {
		  SimulationRunContext.getTaskService().complete(task.getId());
      log.debug( "completed {}, {}", task, task.getName());
		}
	}

	@Override
	public void init() {
		
	}
}
