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

import java.util.Collections;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiSignalEvent;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to activities.
 * 
 * @author Frederik Heremans
 */
public class ActivityEventsTest extends PluggableActivitiTestCase {

	private TestActivitiActivityEventListener listener;
	
	/**
	 * Test events related to signalling
	 */
	@Deployment
	public void testActivitySignalEvents() throws Exception {
			// Two paths are active in the process, one receive-task and one intermediate catching signal-event
			ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("signalProcess");
			assertNotNull(processInstance);
			
			// Check regular signal through API
			Execution executionWithSignal = runtimeService.createExecutionQuery()
					.activityId("receivePayment").singleResult();
			assertNotNull(executionWithSignal);
			
			runtimeService.signal(executionWithSignal.getId());
			assertEquals(1, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiSignalEvent);
			ActivitiSignalEvent signalEvent = (ActivitiSignalEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ACTIVITY_SIGNALLED, signalEvent.getType());
			assertEquals("receivePayment", signalEvent.getActivityId());
			assertEquals(executionWithSignal.getId(), signalEvent.getExecutionId());
			assertEquals(executionWithSignal.getProcessInstanceId(), signalEvent.getProcessInstanceId());
			assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
			assertNull(signalEvent.getSignalName());
			assertNull(signalEvent.getSignalData());
			listener.clearEventsReceived();
			
			// Check signal using event, and pass in additional payload
			Execution executionWithSignalEvent = runtimeService.createExecutionQuery()
					.activityId("shipOrder").singleResult();
			runtimeService.signalEventReceived("alert", executionWithSignalEvent.getId(), Collections.singletonMap("test", (Object)"test"));
			assertEquals(1, listener.getEventsReceived().size());
			assertTrue(listener.getEventsReceived().get(0) instanceof ActivitiSignalEvent);
			signalEvent = (ActivitiSignalEvent) listener.getEventsReceived().get(0);
			assertEquals(ActivitiEventType.ACTIVITY_SIGNALLED, signalEvent.getType());
			assertEquals("shipOrder", signalEvent.getActivityId());
			assertEquals(executionWithSignalEvent.getId(), signalEvent.getExecutionId());
			assertEquals(executionWithSignalEvent.getProcessInstanceId(), signalEvent.getProcessInstanceId());
			assertEquals(processInstance.getProcessDefinitionId(), signalEvent.getProcessDefinitionId());
			assertEquals("alert", signalEvent.getSignalName());
			assertNotNull(signalEvent.getSignalData());
			listener.clearEventsReceived();
	}
	
	
	@Override
	protected void initializeServices() {
	  super.initializeServices();
	  
	  listener = new TestActivitiActivityEventListener();
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
