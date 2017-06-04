package org.activiti.engine.test.api.event;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to historic task instances.
 * 
 * @author Daisuke Yoshimoto
 */
public class HistoricTaskInstanceEventsTest extends PluggableActivitiTestCase{

	private TestActivitiEntityEventListener listener;
	
	/**
	 * Test delete events of historic task instances.
	 */
	@Deployment(resources= {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testHistoricTaskInstanceEvents() throws Exception {
		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
			
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
			taskService.complete(task.getId());
			
			historyService.deleteHistoricTaskInstance(task.getId());
			
			// Check delete-event
			assertEquals(1, listener.getEventsReceived().size());
			assertEquals(ActivitiEventType.ENTITY_DELETED, listener.getEventsReceived().get(0).getType());
		}
	}
	
	@Override
	protected void initializeServices() {
		super.initializeServices();
	
		listener = new TestActivitiEntityEventListener(HistoricTaskInstance.class);
		processEngineConfiguration.getEventDispatcher().addEventListener(listener);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	
		if(listener != null) {
			listener.clearEventsReceived();
			processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
		}
	}
}
