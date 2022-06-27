/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.api.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.BaseEntityEventListener;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.delegate.event.impl.ActivitiEventDispatcherImpl;
import org.activiti.engine.delegate.event.impl.ActivitiEventImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;

/**
 *
 */
public abstract class ActivitiEventDispatcherTest extends PluggableActivitiTestCase {

  protected ActivitiEventDispatcher dispatcher;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    dispatcher = new ActivitiEventDispatcherImpl();
  }

  /**
   * Test adding a listener and check if events are sent to it. Also checks that after removal, no events are received.
   */
  public void addAndRemoveEventListenerAllEvents() throws Exception {
    // Create a listener that just adds the events to a list
    TestActivitiEventListener newListener = new TestActivitiEventListener();

    // Add event-listener to dispatcher
    dispatcher.addEventListener(newListener);

    ActivitiEntityEventImpl event1 = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_CREATED);
    ActivitiEntityEventImpl event2 = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_CREATED);

    // Dispatch events
    dispatcher.dispatchEvent(event1);
    dispatcher.dispatchEvent(event2);

    assertThat(newListener.getEventsReceived()).hasSize(2);
    assertThat(newListener.getEventsReceived().get(0)).isEqualTo(event1);
    assertThat(newListener.getEventsReceived().get(1)).isEqualTo(event2);

    // Remove listener and dispatch events again, listener should not be
    // invoked
    dispatcher.removeEventListener(newListener);
    newListener.clearEventsReceived();
    dispatcher.dispatchEvent(event1);
    dispatcher.dispatchEvent(event2);

    assertThat(newListener.getEventsReceived().isEmpty()).isTrue();
  }

  /**
   * Test adding a listener and check if events are sent to it, for the types it was registered for. Also checks that after removal, no events are received.
   */
  public void addAndRemoveEventListenerTyped() throws Exception {
    // Create a listener that just adds the events to a list
    TestActivitiEventListener newListener = new TestActivitiEventListener();

    // Add event-listener to dispatcher
    dispatcher.addEventListener(newListener, ActivitiEventType.ENTITY_CREATED, ActivitiEventType.ENTITY_DELETED);

    ActivitiEntityEventImpl event1 = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_CREATED);
    ActivitiEntityEventImpl event2 = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_DELETED);
    ActivitiEntityEventImpl event3 = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_UPDATED);

    // Dispatch events, only 2 out of 3 should have entered the listener
    dispatcher.dispatchEvent(event1);
    dispatcher.dispatchEvent(event2);
    dispatcher.dispatchEvent(event3);

    assertThat(newListener.getEventsReceived()).hasSize(2);
    assertThat(newListener.getEventsReceived().get(0)).isEqualTo(event1);
    assertThat(newListener.getEventsReceived().get(1)).isEqualTo(event2);

    // Remove listener and dispatch events again, listener should not be
    // invoked
    dispatcher.removeEventListener(newListener);
    newListener.clearEventsReceived();
    dispatcher.dispatchEvent(event1);
    dispatcher.dispatchEvent(event2);

    assertThat(newListener.getEventsReceived().isEmpty()).isTrue();
  }

  /**
   * Test that adding a listener with a null-type is never called.
   */
  public void addAndRemoveEventListenerTypedNullType() throws Exception {

    // Create a listener that just adds the events to a list
    TestActivitiEventListener newListener = new TestActivitiEventListener();

    // Add event-listener to dispatcher
    dispatcher.addEventListener(newListener, (ActivitiEventType) null);

    ActivitiEntityEventImpl event1 = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_CREATED);
    ActivitiEntityEventImpl event2 = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_DELETED);

    // Dispatch events, all should have entered the listener
    dispatcher.dispatchEvent(event1);
    dispatcher.dispatchEvent(event2);

    assertThat(newListener.getEventsReceived().isEmpty()).isTrue();
  }

  /**
   * Test the {@link BaseEntityEventListener} shipped with Activiti.
   */
  public void baseEntityEventListener() throws Exception {
    TestBaseEntityEventListener listener = new TestBaseEntityEventListener();

    dispatcher.addEventListener(listener);

    ActivitiEntityEventImpl createEvent = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_CREATED);
    ActivitiEntityEventImpl deleteEvent = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_DELETED);
    ActivitiEntityEventImpl updateEvent = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.ENTITY_UPDATED);
    ActivitiEntityEventImpl otherEvent = new ActivitiEntityEventImpl(processEngineConfiguration.getTaskEntityManager().create(), ActivitiEventType.CUSTOM);

    // Dispatch create event
    dispatcher.dispatchEvent(createEvent);
    assertThat(listener.isCreateReceived()).isTrue();
    assertThat(listener.isUpdateReceived()).isFalse();
    assertThat(listener.isCustomReceived()).isFalse();
    assertThat(listener.isInitializeReceived()).isFalse();
    assertThat(listener.isDeleteReceived()).isFalse();
    listener.reset();

    // Dispatch update event
    dispatcher.dispatchEvent(updateEvent);
    assertThat(listener.isUpdateReceived()).isTrue();
    assertThat(listener.isCreateReceived()).isFalse();
    assertThat(listener.isCustomReceived()).isFalse();
    assertThat(listener.isDeleteReceived()).isFalse();
    listener.reset();

    // Dispatch delete event
    dispatcher.dispatchEvent(deleteEvent);
    assertThat(listener.isDeleteReceived()).isTrue();
    assertThat(listener.isCreateReceived()).isFalse();
    assertThat(listener.isCustomReceived()).isFalse();
    assertThat(listener.isUpdateReceived()).isFalse();
    listener.reset();

    // Dispatch other event
    dispatcher.dispatchEvent(otherEvent);
    assertThat(listener.isCustomReceived()).isTrue();
    assertThat(listener.isCreateReceived()).isFalse();
    assertThat(listener.isUpdateReceived()).isFalse();
    assertThat(listener.isDeleteReceived()).isFalse();
    listener.reset();

    // Test typed entity-listener
    listener = new TestBaseEntityEventListener(Task.class);

    // Dispatch event for a task, should be received
    dispatcher.addEventListener(listener);
    dispatcher.dispatchEvent(createEvent);

    assertThat(listener.isCreateReceived()).isTrue();
    listener.reset();

    // Dispatch event for a execution, should NOT be received
    ActivitiEntityEventImpl createEventForExecution = new ActivitiEntityEventImpl(new ExecutionEntityImpl(), ActivitiEventType.ENTITY_CREATED);

    dispatcher.dispatchEvent(createEventForExecution);
    assertThat(listener.isCreateReceived()).isFalse();
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
      assertThat(secondListener.getEventsReceived()).hasSize(1);
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

    assertThatExceptionOfType(ActivitiException.class)
        .isThrownBy(() -> dispatcher.dispatchEvent(event))
        .withCauseInstanceOf(RuntimeException.class)
        .satisfies(ae -> assertThat(ae.getCause()).hasMessage("Test exception"));

    // Second listener should NOT have been called
    assertThat(secondListener.getEventsReceived()).hasSize(0);
  }

  /**
   * Test conversion of string-value (and list) in list of {@link ActivitiEventType}s, used in configuration of process-engine
   * {@link ProcessEngineConfigurationImpl#setTypedEventListeners(java.util.Map)} .
   */
  public void activitiEventTypeParsing() throws Exception {
    // Check with empty null
    ActivitiEventType[] types = ActivitiEventType.getTypesFromString(null);
    assertThat(types).isNotNull();
    assertThat(types.length).isEqualTo(0);

    // Check with empty string
    types = ActivitiEventType.getTypesFromString("");
    assertThat(types).isNotNull();
    assertThat(types.length).isEqualTo(0);

    // Single value
    types = ActivitiEventType.getTypesFromString("ENTITY_CREATED");
    assertThat(types).isNotNull();
    assertThat(types.length).isEqualTo(1);
    assertThat(types[0]).isEqualTo(ActivitiEventType.ENTITY_CREATED);

    // Multiple value
    types = ActivitiEventType.getTypesFromString("ENTITY_CREATED,ENTITY_DELETED");
    assertThat(types).isNotNull();
    assertThat(types.length).isEqualTo(2);
    assertThat(types[0]).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(types[1]).isEqualTo(ActivitiEventType.ENTITY_DELETED);

    // Additional separators should be ignored
    types = ActivitiEventType.getTypesFromString(",ENTITY_CREATED,,ENTITY_DELETED,,,");
    assertThat(types).isNotNull();
    assertThat(types.length).isEqualTo(2);
    assertThat(types[0]).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(types[1]).isEqualTo(ActivitiEventType.ENTITY_DELETED);

    // Invalid type name
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> ActivitiEventType.getTypesFromString("WHOOPS,ENTITY_DELETED"))
      .withMessage("Invalid event-type: WHOOPS");
  }
}
