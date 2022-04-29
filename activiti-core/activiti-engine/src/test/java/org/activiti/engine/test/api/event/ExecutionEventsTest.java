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
import static org.assertj.core.groups.Tuple.tuple;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to executions.
 *
 */
public class ExecutionEventsTest extends PluggableActivitiTestCase {

  private TestActivitiEntityEventListener listener;

  /**
   * Test create, update and delete events of process instances.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testExecutionEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertThat(processInstance).isNotNull();

    // Check create-event
    assertThat(listener.getEventsReceived()).hasSize(6);
    assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiEntityEvent.class);

    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());
    listener.clearEventsReceived();

    // Check update event when suspended/activated
    runtimeService.suspendProcessInstanceById(processInstance.getId());
    runtimeService.activateProcessInstanceById(processInstance.getId());

    assertThat(listener.getEventsReceived()).hasSize(4);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_SUSPENDED);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_SUSPENDED);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_ACTIVATED);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_ACTIVATED);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());

    listener.clearEventsReceived();

    // Check update event when process-definition is supended (should
    // cascade suspend/activate all process instances)
    repositoryService.suspendProcessDefinitionById(processInstance.getProcessDefinitionId(), true, null);
    repositoryService.activateProcessDefinitionById(processInstance.getProcessDefinitionId(), true, null);

    assertThat(listener.getEventsReceived()).hasSize(4);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_SUSPENDED);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_SUSPENDED);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_ACTIVATED);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_ACTIVATED);
    assertThat(((Execution) event.getEntity()).getProcessInstanceId()).isEqualTo(processInstance.getId());

    listener.clearEventsReceived();

    // Check update-event when business-key is updated
    runtimeService.updateBusinessKey(processInstance.getId(), "thekey");
    assertThat(listener.getEventsReceived()).hasSize(1);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(((Execution) event.getEntity()).getId()).isEqualTo(processInstance.getId());
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
    listener.clearEventsReceived();

    runtimeService.deleteProcessInstance(processInstance.getId(), "Testing events");

    assertThat(listener.getEventsReceived())
        .extracting(ActivitiEvent::getType, ActivitiEvent::getProcessInstanceId)
        .contains(
            tuple(ActivitiEventType.PROCESS_CANCELLED, processInstance.getId()),
            tuple(ActivitiEventType.ENTITY_DELETED, processInstance.getId())
            );
    listener.clearEventsReceived();
  }

  @Override
  protected void initializeServices() {
    super.initializeServices();

    listener = new TestActivitiEntityEventListener(Execution.class);
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
