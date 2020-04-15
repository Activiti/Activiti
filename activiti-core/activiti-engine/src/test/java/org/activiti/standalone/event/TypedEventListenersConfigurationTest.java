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
package org.activiti.standalone.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventImpl;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.test.api.event.TestActivitiEventListener;

/**
 * Test to verify event-listeners, which are configured in the cfg.xml, are notified.
 *

 */
public class TypedEventListenersConfigurationTest extends ResourceActivitiTestCase {

  public TypedEventListenersConfigurationTest() {
    super("org/activiti/standalone/event/activiti-typed-eventlistener.cfg.xml");
  }

  public void testEventListenerConfiguration() {
    // Fetch the listener to check received events
    TestActivitiEventListener listener = (TestActivitiEventListener) processEngineConfiguration.getBeans().get("eventListener");
    assertThat(listener).isNotNull();

    // Clear any events received (eg. engine initialisation)
    listener.clearEventsReceived();

    // Dispath a custom event
    ActivitiEvent event = new ActivitiEventImpl(ActivitiEventType.CUSTOM);
    processEngineConfiguration.getEventDispatcher().dispatchEvent(event);

    assertThat(listener.getEventsReceived()).hasSize(1);
    assertThat(listener.getEventsReceived().get(0)).isEqualTo(event);
    listener.clearEventsReceived();

    // Dispatch another event the listener is registered for
    event = new ActivitiEventImpl(ActivitiEventType.ENTITY_DELETED);
    processEngineConfiguration.getEventDispatcher().dispatchEvent(event);
    event = new ActivitiEventImpl(ActivitiEventType.ENTITY_UPDATED);
    processEngineConfiguration.getEventDispatcher().dispatchEvent(event);

    assertThat(listener.getEventsReceived()).hasSize(2);
    assertThat(listener.getEventsReceived().get(0).getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    assertThat(listener.getEventsReceived().get(1).getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    listener.clearEventsReceived();

    // Dispatch an event that is NOT part of the types configured
    event = new ActivitiEventImpl(ActivitiEventType.ENTITY_CREATED);
    processEngineConfiguration.getEventDispatcher().dispatchEvent(event);
    assertThat(listener.getEventsReceived().isEmpty()).isTrue();
  }
}
