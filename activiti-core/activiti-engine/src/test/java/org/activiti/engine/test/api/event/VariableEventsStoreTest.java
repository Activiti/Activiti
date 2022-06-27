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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test variables delete events with storing events {@link EventLogEntryEntity}.
 *
 */
public class VariableEventsStoreTest extends PluggableActivitiTestCase {

  private TestVariableEventListenerStore listener;

  @Deployment(resources = {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
  public void testStartEndProcessInstanceVariableEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("var1", "value1"));

    assertThat(listener.getEventsReceived()).hasSize(1);
    assertThat(listener.getEventsReceived().get(0).getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
    assertThat(managementService.getEventLogEntries(null, null)).hasSize(1);

    Task task = taskService.createTaskQuery().processInstanceId( processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    assertThat(listener.getEventsReceived()).hasSize(2);
    assertThat(listener.getEventsReceived().get(1).getType()).isEqualTo(ActivitiEventType.VARIABLE_DELETED);
    assertThat(managementService.getEventLogEntries(null, null)).hasSize(2);

  }

  public void testTaskInstanceVariableEvents() throws Exception {
    Task task = taskService.newTask();
    taskService.saveTask(task);

    taskService.setVariableLocal(task.getId(), "myVar", "value");

    assertThat(listener.getEventsReceived()).hasSize(1);
    assertThat(listener.getEventsReceived().get(0).getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
    assertThat(managementService.getEventLogEntries(null, null)).hasSize(1);

    taskService.removeVariableLocal(task.getId(), "myVar");

    assertThat(listener.getEventsReceived()).hasSize(2);
    assertThat(listener.getEventsReceived().get(1).getType()).isEqualTo(ActivitiEventType.VARIABLE_DELETED);
    assertThat(managementService.getEventLogEntries(null, null)).hasSize(2);

    // bulk insert delete var test
    taskService.setVariablesLocal(task.getId(), map(
        "myVar", "value",
        "myVar2", "value"
        ));
    taskService.removeVariablesLocal(task.getId(), asList("myVar", "myVar2"));

    assertThat(listener.getEventsReceived()).hasSize(6);
    assertThat(managementService.getEventLogEntries(null, null)).hasSize(6);

    taskService.complete(task.getId());
    historyService.deleteHistoricTaskInstance(task.getId());

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

      // cleanup
      for (EventLogEntry eventLogEntry : managementService.getEventLogEntries(null, null)) {
        managementService.deleteEventLogEntry(eventLogEntry.getLogNumber());
      }
    }
  }
}
