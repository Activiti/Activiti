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
package org.activiti.engine.test.api.event;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;

/**
 * Test case for all {@link ActivitiEvent}s related to variables.
 * 
 * @author Frederik Heremans
 */
public class VariableEventsStoreTest extends PluggableActivitiTestCase {

	private TestVariableEventListenerStore listener;

	/*@Deployment(resources = {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testStartEndProcessInstanceVariableEvents() throws Exception {
	  Map<String, Object> variables = new HashMap<String, Object>();
	  variables.put("var1", "value1");
	  ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

	  assertEquals(1, listener.getEventsReceived().size());
	  assertEquals(ActivitiEventType.VARIABLE_CREATED, listener.getEventsReceived().get(0).getType());

	  Task task = taskService.createTaskQuery().processInstanceId( processInstance.getId()).singleResult();
	  taskService.complete(task.getId());

	  assertEquals(2, listener.getEventsReceived().size());
	  assertEquals(ActivitiEventType.VARIABLE_DELETED, listener.getEventsReceived().get(1).getType());
	}*/
	
	
	public void testTaskInstanceVariableEvents() throws Exception {
	  Task task = taskService.newTask();
	  taskService.saveTask(task);
	  
	  taskService.setVariableLocal(task.getId(), "myVar", "value");
	  
	  assertEquals(1, listener.getEventsReceived().size());
    assertEquals(ActivitiEventType.VARIABLE_CREATED, listener.getEventsReceived().get(0).getType());
    
    taskService.removeVariableLocal(task.getId(), "myVar");
    
    assertEquals(2, listener.getEventsReceived().size());
    assertEquals(ActivitiEventType.VARIABLE_DELETED, listener.getEventsReceived().get(1).getType());
    
	}

	@Override
	protected void initializeServices() {
		super.initializeServices();

		listener = new TestVariableEventListenerStore();
		processEngineConfiguration.getEventDispatcher().addEventListener(listener);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		if (listener != null) {
			listener.clearEventsReceived();
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
}
