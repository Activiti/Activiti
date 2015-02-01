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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.BaseEntityEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiEventDispatcherImpl;
import org.activiti.engine.delegate.event.impl.ActivitiEventImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;

/**
 * 
 * @author Frederik Heremans
 */
public abstract class ActivitiEventDispatcherTest extends PluggableActivitiTestCase {

	protected ActivitiEventDispatcher dispatcher;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		dispatcher = new ActivitiEventDispatcherImpl();
	}

	/**
	 * Test adding a listener and check if events are sent to it. Also checks that
	 * after removal, no events are received.
	 */
	public void addAndRemoveEventListenerAllEvents() throws Exception {
		// Create a listener that just adds the events to a list
		TestActivitiEventListener newListener = new TestActivitiEventListener();

		// Add event-listener to dispatcher
		dispatcher.addEventListener(newListener);

		ActivitiEntityEventImpl event1 = new ActivitiEntityEventImpl(new TaskEntity(), ActivitiEventType.ENTITY_CREATED);
		ActivitiEntityEventImpl event2 = new ActivitiEntityEventImpl(new TaskEntity(), ActivitiEventType.ENTITY_CREATED);

		// Dispatch events
		dispatcher.dispatchEvent(event1);
		dispatcher.dispatchEvent(event2);

		assertEquals(2, newListener.getEventsReceived().size());
		assertEquals(event1, newListener.getEventsReceived().get(0));
		assertEquals(event2, newListener.getEventsReceived().get(1));

		// Remove listener and dispatch events again, listener should not be invoked
		dispatcher.removeEventListener(newListener);
		newListener.clearEventsReceived();
		dispatcher.dispatchEvent(event1);
		dispatcher.dispatchEvent(event2);

		assertTrue(newListener.getEventsReceived().isEmpty());
	}

	/**
	 * Test adding a listener and check if events are sent to it, for the types it
	 * was registered for. Also checks that after removal, no events are received.
	 */
	public void addAndRemoveEventListenerTyped() throws Exception {
		// Create a listener that just adds the events to a list
		TestActivitiEventListener newListener = new TestActivitiEventListener();

		// Add event-listener to dispatcher
		dispatcher.addEventListener(newListener, ActivitiEventType.ENTITY_CREATED, ActivitiEventType.ENTITY_DELETED);

		ActivitiEntityEventImpl event1 = new ActivitiEntityEventImpl(new TaskEntity(), ActivitiEventType.ENTITY_CREATED);
		ActivitiEntityEventImpl event2 = new ActivitiEntityEventImpl(new TaskEntity(), ActivitiEventType.ENTITY_DELETED);
		ActivitiEntityEventImpl event3 = new ActivitiEntityEventImpl(new TaskEntity(), ActivitiEventType.ENTITY_UPDATED);

		// Dispatch events, only 2 out of 3 should have entered the listener
		dispatcher.dispatchEvent(event1);
		dispatcher.dispatchEvent(event2);
		dispatcher.dispatchEvent(event3);

		assertEquals(2, newListener.getEventsReceived().size());
		assertEquals(event1, newListener.getEventsReceived().get(0));
		assertEquals(event2, newListener.getEventsReceived().get(1));

		// Remove listener and dispatch events again, listener should not be invoked
		dispatcher.removeEventListener(newListener);
		newListener.clearEventsReceived();
		dispatcher.dispatchEvent(event1);
		dispatcher.dispatchEvent(event2);

		assertTrue(newListener.getEventsReceived().isEmpty());
	}

	/**
	 * Test that adding a listener with a null-type is never called.
	 */
	public void addAndRemoveEventListenerTypedNullType() throws Exception {

		// Create a listener that just adds the events to a list
		TestActivitiEventListener newListener = new TestActivitiEventListener();

		// Add event-listener to dispatcher
		dispatcher.addEventListener(newListener, (ActivitiEventType) null);

		ActivitiEntityEventImpl event1 = new ActivitiEntityEventImpl(new TaskEntity(), ActivitiEventType.ENTITY_CREATED);
		ActivitiEntityEventImpl event2 = new ActivitiEntityEventImpl(new TaskEntity(), ActivitiEventType.ENTITY_DELETED);

		// Dispatch events, all should have entered the listener
		dispatcher.dispatchEvent(event1);
		dispatcher.dispatchEvent(event2);

		assertTrue(newListener.getEventsReceived().isEmpty());
	}

	/**
	 * Test the {@link BaseEntityEventListener} shipped with Activiti.
	 */
	public void baseEntityEventListener() throws Exception {
		TestBaseEntityEventListener listener = new TestBaseEntityEventListener();

		dispatcher.addEventListener(listener);

		ActivitiEntityEventImpl createEvent = new ActivitiEntityEventImpl(new TaskEntity(),
		    ActivitiEventType.ENTITY_CREATED);
		ActivitiEntityEventImpl deleteEvent = new ActivitiEntityEventImpl(new TaskEntity(),
		    ActivitiEventType.ENTITY_DELETED);
		ActivitiEntityEventImpl updateEvent = new ActivitiEntityEventImpl(new TaskEntity(),
		    ActivitiEventType.ENTITY_UPDATED);
		ActivitiEntityEventImpl otherEvent = new ActivitiEntityEventImpl(new TaskEntity(), ActivitiEventType.CUSTOM);

		// Dispatch create event
		dispatcher.dispatchEvent(createEvent);
		assertTrue(listener.isCreateReceived());
		assertFalse(listener.isUpdateReceived());
		assertFalse(listener.isCustomReceived());
		assertFalse(listener.isInitializeReceived());
		assertFalse(listener.isDeleteReceived());
		listener.reset();

		// Dispatch update event
		dispatcher.dispatchEvent(updateEvent);
		assertTrue(listener.isUpdateReceived());
		assertFalse(listener.isCreateReceived());
		assertFalse(listener.isCustomReceived());
		assertFalse(listener.isDeleteReceived());
		listener.reset();

		// Dispatch delete event
		dispatcher.dispatchEvent(deleteEvent);
		assertTrue(listener.isDeleteReceived());
		assertFalse(listener.isCreateReceived());
		assertFalse(listener.isCustomReceived());
		assertFalse(listener.isUpdateReceived());
		listener.reset();

		// Dispatch other event
		dispatcher.dispatchEvent(otherEvent);
		assertTrue(listener.isCustomReceived());
		assertFalse(listener.isCreateReceived());
		assertFalse(listener.isUpdateReceived());
		assertFalse(listener.isDeleteReceived());
		listener.reset();

		// Test typed entity-listener
		listener = new TestBaseEntityEventListener(Task.class);

		// Dispatch event for a task, should be received
		dispatcher.addEventListener(listener);
		dispatcher.dispatchEvent(createEvent);

		assertTrue(listener.isCreateReceived());
		listener.reset();

		// Dispatch event for a execution, should NOT be received
		ActivitiEntityEventImpl createEventForExecution = new ActivitiEntityEventImpl(new ExecutionEntity(),
		    ActivitiEventType.ENTITY_CREATED);

		dispatcher.dispatchEvent(createEventForExecution);
		assertFalse(listener.isCreateReceived());
	}

	/**
	 * Test dispatching behavior when an exception occurs in the listener
	 */
	public void exceptionInListener() throws Exception {
		// Create listener that doesn't force the dispatching to fail
		TestExceptionActivitiEventListener listener = new TestExceptionActivitiEventListener(false);
		TestActivitiEventListener secondListener = new TestActivitiEventListener();

		dispatcher.addEventListener(listener);
		dispatcher.addEventListener(secondListener);

		ActivitiEventImpl event = new ActivitiEventImpl(ActivitiEventType.ENTITY_CREATED);
		try {
			dispatcher.dispatchEvent(event);
			assertEquals(1, secondListener.getEventsReceived().size());
		} catch (Throwable t) {
			fail("No exception expected");
		}

		// Remove listeners
		dispatcher.removeEventListener(listener);
		dispatcher.removeEventListener(secondListener);

		// Create listener that forces the dispatching to fail
		listener = new TestExceptionActivitiEventListener(true);
		secondListener = new TestActivitiEventListener();
		dispatcher.addEventListener(listener);
		dispatcher.addEventListener(secondListener);

		try {
			dispatcher.dispatchEvent(event);
			fail("Exception expected");
		} catch (Throwable t) {
			assertTrue(t instanceof ActivitiException);
			assertTrue(t.getCause() instanceof RuntimeException);
			assertEquals("Test exception", t.getCause().getMessage());

			// Second listener should NOT have been called
			assertEquals(0, secondListener.getEventsReceived().size());
		}
	}

	/**
	 * Test conversion of string-value (and list) in list of
	 * {@link ActivitiEventType}s, used in configuration of process-engine
	 * {@link ProcessEngineConfigurationImpl#setTypedEventListeners(java.util.Map)}
	 * .
	 */
	public void activitiEventTypeParsing() throws Exception {
		// Check with empty null
		ActivitiEventType[] types = ActivitiEventType.getTypesFromString(null);
		assertNotNull(types);
		assertEquals(0, types.length);

		// Check with empty string
		types = ActivitiEventType.getTypesFromString("");
		assertNotNull(types);
		assertEquals(0, types.length);

		// Single value
		types = ActivitiEventType.getTypesFromString("ENTITY_CREATED");
		assertNotNull(types);
		assertEquals(1, types.length);
		assertEquals(ActivitiEventType.ENTITY_CREATED, types[0]);

		// Multiple value
		types = ActivitiEventType.getTypesFromString("ENTITY_CREATED,ENTITY_DELETED");
		assertNotNull(types);
		assertEquals(2, types.length);
		assertEquals(ActivitiEventType.ENTITY_CREATED, types[0]);
		assertEquals(ActivitiEventType.ENTITY_DELETED, types[1]);
		
		// Additional separators should be ignored
		types = ActivitiEventType.getTypesFromString(",ENTITY_CREATED,,ENTITY_DELETED,,,");
		assertNotNull(types);
		assertEquals(2, types.length);
		assertEquals(ActivitiEventType.ENTITY_CREATED, types[0]);
		assertEquals(ActivitiEventType.ENTITY_DELETED, types[1]);

		// Invalid type name
		try {
			ActivitiEventType.getTypesFromString("WHOOPS,ENTITY_DELETED");
			fail("Exception expected");
		} catch(ActivitiIllegalArgumentException expected) {
			// Expected exception
			assertEquals("Invalid event-type: WHOOPS", expected.getMessage());
		}
	}
}