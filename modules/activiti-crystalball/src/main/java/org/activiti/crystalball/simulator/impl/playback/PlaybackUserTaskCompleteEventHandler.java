package org.activiti.crystalball.simulator.impl.playback;

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
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * complete user task handler for playback purposes
 *
 * @author martin.grofcik
 */
public class PlaybackUserTaskCompleteEventHandler implements SimulationEventHandler {

	private static Logger log = LoggerFactory.getLogger(PlaybackUserTaskCompleteEventHandler.class);

	@Override
	public void handle(SimulationEvent event) {
		String taskId = (String) event.getProperty("taskId");
		Task task = SimulationRunContext.getTaskService().createTaskQuery().taskId( taskId ).singleResult();		
		String assignee = task.getAssignee();
		
		@SuppressWarnings("unchecked")
		Map<String, Object> variables = (Map<String, Object>) event.getProperty("variables");		

		SimulationRunContext.getTaskService().complete( taskId, variables );
		log.debug( "completed {}, {}, {}, {}", task, task.getName(), assignee, variables);
	}

	@Override
	public void init() {
		
	}
}
