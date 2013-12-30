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

import org.activiti.engine.delegate.event.ActivitiErrorEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * Test case for {@link ActivitiEvent} thrown when a BPMNError is not caught
 * in the process.
 * 
 * @author Frederik Heremans
 */
public class UncaughtErrorEventTest extends PluggableActivitiTestCase {

	private TestActivitiEventListener listener;

	/**
	 * Test events related to error-events, thrown from within process-execution (eg. service-task).
	 */
	@Deployment
	public void testUncaughtError() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("errorProcess");
		assertNotNull(processInstance);
		
		// Error-handling should have ended the process
		ProcessInstance afterErrorInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
				.singleResult();
		assertNull(afterErrorInstance);
		
		ActivitiEvent errorEvent = null;
		
		for(ActivitiEvent event : listener.getEventsReceived()) {
			if(ActivitiEventType.UNCAUGHT_BPMN_ERROR.equals(event.getType())) {
				if(errorEvent == null) {
					errorEvent = event;
				} else {
					fail("Only one ActivityErrorEvent expected");
				}
			}
		}
		
		assertNotNull(errorEvent);
		assertEquals(ActivitiEventType.UNCAUGHT_BPMN_ERROR, errorEvent.getType());
		assertTrue(errorEvent instanceof ActivitiErrorEvent);
		assertEquals("123", ((ActivitiErrorEvent) errorEvent).getErrorCode());
		assertNull(((ActivitiErrorEvent) errorEvent).getActivityId());
		assertEquals(processInstance.getId(), errorEvent.getProcessInstanceId());
		assertEquals(processInstance.getProcessDefinitionId(), errorEvent.getProcessDefinitionId());
		assertFalse(processInstance.getId().equals(errorEvent.getExecutionId()));
	}
	
	/**
	 * Test events related to error-events, thrown from within process-execution (eg. service-task).
	 */
	@Deployment
	public void testUncaughtErrorFromBPMNError() throws Exception {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("errorProcess");
		assertNotNull(processInstance);
		
		// Error-handling should have ended the process
		ProcessInstance afterErrorInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
				.singleResult();
		assertNull(afterErrorInstance);
		
		ActivitiEvent errorEvent = null;
		
		for(ActivitiEvent event : listener.getEventsReceived()) {
			if(ActivitiEventType.UNCAUGHT_BPMN_ERROR.equals(event.getType())) {
				if(errorEvent == null) {
					errorEvent = event;
				} else {
					fail("Only one ActivityErrorEvent expected");
				}
			}
		}
		
		assertNotNull(errorEvent);
		assertEquals(ActivitiEventType.UNCAUGHT_BPMN_ERROR, errorEvent.getType());
		assertTrue(errorEvent instanceof ActivitiErrorEvent);
		assertEquals("23", ((ActivitiErrorEvent) errorEvent).getErrorCode());
		assertNull(((ActivitiErrorEvent) errorEvent).getActivityId());
		assertEquals(processInstance.getId(), errorEvent.getProcessInstanceId());
		assertEquals(processInstance.getProcessDefinitionId(), errorEvent.getProcessDefinitionId());
		assertFalse(processInstance.getId().equals(errorEvent.getExecutionId()));
	}
	
	

	@Override
	protected void initializeServices() {
		super.initializeServices();

		listener = new TestActivitiEventListener();
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
