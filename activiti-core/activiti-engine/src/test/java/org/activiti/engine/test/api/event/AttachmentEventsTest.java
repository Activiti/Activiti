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

import java.io.ByteArrayInputStream;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to attachments.
 *
 */
public class AttachmentEventsTest extends PluggableActivitiTestCase {

  private TestActivitiEntityEventListener listener;

  /**
   * Test create, update and delete events of attachments on a task/process.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testAttachmentEntityEvents() throws Exception {

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

      Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      assertThat(task).isNotNull();

      // Create link-attachment
      Attachment attachment = taskService.createAttachment("test", task.getId(), processInstance.getId(), "attachment name", "description", "http://activiti.org");
      assertThat(attachment.getUserId()).isNull();
      assertThat(listener.getEventsReceived()).hasSize(2);
      ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
      assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
      assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
      assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
      Attachment attachmentFromEvent = (Attachment) event.getEntity();
      assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
      assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
      assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
      assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
      attachmentFromEvent = (Attachment) event.getEntity();
      assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());
      listener.clearEventsReceived();

      // Create binary attachment
      Authentication.setAuthenticatedUserId("testuser");
      attachment = taskService.createAttachment("test", task.getId(), processInstance.getId(), "attachment name", "description", new ByteArrayInputStream("test".getBytes()));
      assertThat(attachment.getUserId()).isNotNull();
      assertThat(attachment.getUserId()).isEqualTo("testuser");
      assertThat(listener.getEventsReceived()).hasSize(2);
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
      assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
      assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
      assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
      attachmentFromEvent = (Attachment) event.getEntity();
      assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());

      event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
      listener.clearEventsReceived();

      // Update attachment
      attachment = taskService.getAttachment(attachment.getId());
      attachment.setDescription("Description");
      taskService.saveAttachment(attachment);

      assertThat(listener.getEventsReceived()).hasSize(1);
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
      assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
      assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
      assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
      attachmentFromEvent = (Attachment) event.getEntity();
      assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());
      assertThat(attachmentFromEvent.getDescription()).isEqualTo("Description");
      listener.clearEventsReceived();

      // Finally, delete attachment
      taskService.deleteAttachment(attachment.getId());
      assertThat(listener.getEventsReceived()).hasSize(1);
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
      assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
      assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
      assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
      attachmentFromEvent = (Attachment) event.getEntity();
      assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());
    }
  }

  /**
   * Test create, update and delete events of users.
   */
  public void testAttachmentEntityEventsStandaloneTask() throws Exception {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      Task task = null;
      try {
        task = taskService.newTask();
        taskService.saveTask(task);
        assertThat(task).isNotNull();

        // Create link-attachment
        Attachment attachment = taskService.createAttachment("test", task.getId(), null, "attachment name", "description", "http://activiti.org");
        assertThat(listener.getEventsReceived()).hasSize(2);
        ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
        assertThat(event.getProcessInstanceId()).isNull();
        assertThat(event.getExecutionId()).isNull();
        assertThat(event.getProcessDefinitionId()).isNull();
        Attachment attachmentFromEvent = (Attachment) event.getEntity();
        assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());
        event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
        listener.clearEventsReceived();

        // Create binary attachment
        attachment = taskService.createAttachment("test", task.getId(), null, "attachment name", "description", new ByteArrayInputStream("test".getBytes()));
        assertThat(listener.getEventsReceived()).hasSize(2);
        event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
        assertThat(event.getProcessInstanceId()).isNull();
        assertThat(event.getExecutionId()).isNull();
        assertThat(event.getProcessDefinitionId()).isNull();
        attachmentFromEvent = (Attachment) event.getEntity();
        assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());

        event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
        listener.clearEventsReceived();

        // Update attachment
        attachment = taskService.getAttachment(attachment.getId());
        attachment.setDescription("Description");
        taskService.saveAttachment(attachment);

        assertThat(listener.getEventsReceived()).hasSize(1);
        event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
        assertThat(event.getProcessInstanceId()).isNull();
        assertThat(event.getExecutionId()).isNull();
        assertThat(event.getProcessDefinitionId()).isNull();
        attachmentFromEvent = (Attachment) event.getEntity();
        assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());
        assertThat(attachmentFromEvent.getDescription()).isEqualTo("Description");
        listener.clearEventsReceived();

        // Finally, delete attachment
        taskService.deleteAttachment(attachment.getId());
        assertThat(listener.getEventsReceived()).hasSize(1);
        event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
        assertThat(event.getProcessInstanceId()).isNull();
        assertThat(event.getExecutionId()).isNull();
        assertThat(event.getProcessDefinitionId()).isNull();
        attachmentFromEvent = (Attachment) event.getEntity();
        assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());

      } finally {
        if (task != null && task.getId() != null) {
          taskService.deleteTask(task.getId());
          historyService.deleteHistoricTaskInstance(task.getId());
        }
      }
    }
  }

  public void testAttachmentEntityEventsOnHistoricTaskDelete() throws Exception {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      Task task = null;
      try {
        task = taskService.newTask();
        taskService.saveTask(task);
        assertThat(task).isNotNull();

        // Create link-attachment
        Attachment attachment = taskService.createAttachment("test", task.getId(), null, "attachment name", "description", "http://activiti.org");
        listener.clearEventsReceived();

        // Delete task and historic task
        taskService.deleteTask(task.getId());
        historyService.deleteHistoricTaskInstance(task.getId());

        assertThat(listener.getEventsReceived()).hasSize(1);
        ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
        assertThat(event.getProcessInstanceId()).isNull();
        assertThat(event.getExecutionId()).isNull();
        assertThat(event.getProcessDefinitionId()).isNull();
        Attachment attachmentFromEvent = (Attachment) event.getEntity();
        assertThat(attachmentFromEvent.getId()).isEqualTo(attachment.getId());

      } finally {
        if (task != null && task.getId() != null) {
          taskService.deleteTask(task.getId());
          historyService.deleteHistoricTaskInstance(task.getId());
        }
      }
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    listener = new TestActivitiEntityEventListener(Attachment.class);
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    if (listener != null) {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }
}
