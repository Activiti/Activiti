package org.activiti.engine.test.api.event;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to historic process instances.
 * 
 * @author Daisuke Yoshimoto
 */
public class HistoricProcessInstanceEventsTest extends PluggableActivitiTestCase{

	private TestActivitiEntityEventListener listener;
	
	/**
	 * Test create, update and delete events of historic process instances.
	 */
	@Deployment(resources= {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
	public void testHistoricProcessInstanceEvents() throws Exception {
		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
			
			// Check create-event
			assertEquals(1, listener.getEventsReceived().size());
			assertEquals(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_CREATED, listener.getEventsReceived().get(0).getType());
			listener.clearEventsReceived();
			
			Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
			taskService.complete(task.getId());
			
			// Check end-event
			assertEquals(1, listener.getEventsReceived().size());
			assertEquals(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_ENDED, listener.getEventsReceived().get(0).getType());
			listener.clearEventsReceived();
			
			historyService.deleteHistoricProcessInstance(processInstance.getId());
			
			// Check delete-event
			assertEquals(1, listener.getEventsReceived().size());
			assertEquals(ActivitiEventType.ENTITY_DELETED, listener.getEventsReceived().get(0).getType());
		}
	}
	
	@Override
	protected void initializeServices() {
		super.initializeServices();
	
		listener = new TestActivitiEntityEventListener(HistoricProcessInstance.class);
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
