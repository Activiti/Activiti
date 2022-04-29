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

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.impl.event.logger.EventLogger;
import org.activiti.engine.impl.event.logger.handler.Fields;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 */
public class DatabaseEventLoggerTest extends PluggableActivitiTestCase {

  protected EventLogger databaseEventLogger;

  protected ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Database event logger setup
    databaseEventLogger = new EventLogger(processEngineConfiguration.getClock(), processEngineConfiguration.getObjectMapper());
    runtimeService.addEventListener(databaseEventLogger);
  }

  @Override
  protected void tearDown() throws Exception {

    // Database event logger teardown
    runtimeService.removeEventListener(databaseEventLogger);

    super.tearDown();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/event/DatabaseEventLoggerProcess.bpmn20.xml" })
  public void testDatabaseEvents() throws IOException {

    String testTenant = "testTenant";

    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/api/event/DatabaseEventLoggerProcess.bpmn20.xml")
        .tenantId(testTenant)
        .deploy().getId();

    // Run process to gather data
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("DatabaseEventLoggerProcess",
        singletonMap("testVar", "helloWorld"), testTenant);

    // Verify event log entries
    List<EventLogEntry> eventLogEntries = managementService.getEventLogEntries(null, null);

    String processDefinitionId = processInstance.getProcessDefinitionId();
    Iterator<EventLogEntry> iterator = eventLogEntries.iterator();
    while (iterator.hasNext()) {
      EventLogEntry entry = iterator.next();
      if (entry.getProcessDefinitionId() != null && !entry.getProcessDefinitionId().equals(processDefinitionId)) {
        iterator.remove();
      }
    }

    assertThat(eventLogEntries).hasSize(15);

    long lastLogNr = -1;
    for (int i = 0; i < eventLogEntries.size(); i++) {

      EventLogEntry entry = eventLogEntries.get(i);

      if (i == 0) {

        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED.name());
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getTaskId()).isNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {});
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_INSTANCE_ID)).isNotNull();
        assertThat(data.get(Fields.VALUE_STRING)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);
      }

      // process instance start
      if (i == 1) {

        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo("PROCESSINSTANCE_START");
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getExecutionId()).isNull();
        assertThat(entry.getTaskId()).isNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);

        Map<String, Object> variableMap = (Map<String, Object>) data.get(Fields.VARIABLES);
        assertThat(variableMap).hasSize(1);
        assertThat(variableMap.get("testVar")).isEqualTo("helloWorld");

        assertThat(data.containsKey(Fields.NAME)).isFalse();
        assertThat(data.containsKey(Fields.BUSINESS_KEY)).isFalse();
      }

      // Activity started
      if (i == 2 || i == 5 || i == 9 || i == 12) {
        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo(ActivitiEventType.ACTIVITY_STARTED.name());
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {});
        assertThat(data.get(Fields.ACTIVITY_ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_INSTANCE_ID)).isNotNull();
        assertThat(data.get(Fields.EXECUTION_ID)).isNotNull();
        assertThat(data.get(Fields.ACTIVITY_TYPE)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);
      }

      // Leaving start
      if (i == 3) {

        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED.name());
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {});
        assertThat(data.get(Fields.ACTIVITY_ID)).isNotNull();
        assertThat(data.get(Fields.ACTIVITY_ID)).isEqualTo("startEvent1");
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_INSTANCE_ID)).isNotNull();
        assertThat(data.get(Fields.EXECUTION_ID)).isNotNull();
        assertThat(data.get(Fields.ACTIVITY_TYPE)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);

      }

      // Sequence flow taken
      if (i == 4 || i == 7 || i == 8) {
        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo(ActivitiEventType.SEQUENCEFLOW_TAKEN.name());
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {});
        assertThat(data.get(Fields.ID)).isNotNull();
        assertThat(data.get(Fields.SOURCE_ACTIVITY_ID)).isNotNull();
        assertThat(data.get(Fields.SOURCE_ACTIVITY_NAME)).isNotNull();
        assertThat(data.get(Fields.SOURCE_ACTIVITY_TYPE)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);
      }

      // Leaving parallel gateway
      if (i == 6) {

        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED.name());
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {});
        assertThat(data.get(Fields.ACTIVITY_ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_INSTANCE_ID)).isNotNull();
        assertThat(data.get(Fields.EXECUTION_ID)).isNotNull();
        assertThat(data.get(Fields.ACTIVITY_TYPE)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);

      }

      // Tasks
      if (i == 11 || i == 14) {

        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo(ActivitiEventType.TASK_ASSIGNED.name());
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNotNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {});
        assertThat(data.get(Fields.ID)).isNotNull();
        assertThat(data.get(Fields.NAME)).isNotNull();
        assertThat(data.get(Fields.ASSIGNEE)).isNotNull();
        assertThat(data.get(Fields.CREATE_TIME)).isNotNull();
        assertThat(data.get(Fields.PRIORITY)).isNotNull();
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.EXECUTION_ID)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isNotNull();

        assertThat(data.containsKey(Fields.DESCRIPTION)).isFalse();
        assertThat(data.containsKey(Fields.CATEGORY)).isFalse();
        assertThat(data.containsKey(Fields.OWNER)).isFalse();
        assertThat(data.containsKey(Fields.DUE_DATE)).isFalse();
        assertThat(data.containsKey(Fields.FORM_KEY)).isFalse();
        assertThat(data.containsKey(Fields.USER_ID)).isFalse();

        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);

      }

      if (i == 10 || i == 13) {

        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo(ActivitiEventType.TASK_CREATED.name());
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNotNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {});
        assertThat(data.get(Fields.ID)).isNotNull();
        assertThat(data.get(Fields.NAME)).isNotNull();
        assertThat(data.get(Fields.ASSIGNEE)).isNotNull();
        assertThat(data.get(Fields.CREATE_TIME)).isNotNull();
        assertThat(data.get(Fields.PRIORITY)).isNotNull();
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.EXECUTION_ID)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isNotNull();

        assertThat(data.containsKey(Fields.DESCRIPTION)).isFalse();
        assertThat(data.containsKey(Fields.CATEGORY)).isFalse();
        assertThat(data.containsKey(Fields.OWNER)).isFalse();
        assertThat(data.containsKey(Fields.DUE_DATE)).isFalse();
        assertThat(data.containsKey(Fields.FORM_KEY)).isFalse();
        assertThat(data.containsKey(Fields.USER_ID)).isFalse();

        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);

      }

      lastLogNr = entry.getLogNumber();
    }

    // Completing two tasks
    for (Task task : taskService.createTaskQuery().list()) {
      Authentication.setAuthenticatedUserId(task.getAssignee());
      taskService.complete(task.getId(), singletonMap("test", "test"));
      Authentication.setAuthenticatedUserId(null);
    }

    // Verify events
    eventLogEntries = managementService.getEventLogEntries(lastLogNr, 100L);
    assertThat(eventLogEntries).hasSize(17);

    for (int i = 0; i < eventLogEntries.size(); i++) {

      EventLogEntry entry = eventLogEntries.get(i);

      // Task completion
      if (i == 1 || i == 6) {
        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo(ActivitiEventType.TASK_COMPLETED.name());
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNotNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.ID)).isNotNull();
        assertThat(data.get(Fields.NAME)).isNotNull();
        assertThat(data.get(Fields.ASSIGNEE)).isNotNull();
        assertThat(data.get(Fields.CREATE_TIME)).isNotNull();
        assertThat(data.get(Fields.PRIORITY)).isNotNull();
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.EXECUTION_ID)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isNotNull();
        assertThat(data.get(Fields.USER_ID)).isNotNull();

        Map<String, Object> variableMap = (Map<String, Object>) data.get(Fields.VARIABLES);
        assertThat(variableMap).hasSize(1);
        assertThat(variableMap.get("test")).isEqualTo("test");

        assertThat(data.containsKey(Fields.DESCRIPTION)).isFalse();
        assertThat(data.containsKey(Fields.CATEGORY)).isFalse();
        assertThat(data.containsKey(Fields.OWNER)).isFalse();
        assertThat(data.containsKey(Fields.DUE_DATE)).isFalse();
        assertThat(data.containsKey(Fields.FORM_KEY)).isFalse();

        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);

      }

      // Activity Completed
      if (i == 2 || i == 7 || i == 10 || i == 13) {
        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo(ActivitiEventType.ACTIVITY_COMPLETED.name());
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.ACTIVITY_ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_INSTANCE_ID)).isNotNull();
        assertThat(data.get(Fields.EXECUTION_ID)).isNotNull();
        assertThat(data.get(Fields.ACTIVITY_TYPE)).isNotNull();
        assertThat(data.get(Fields.BEHAVIOR_CLASS)).isNotNull();

        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);

        if (i == 2) {
          assertThat(data.get(Fields.ACTIVITY_TYPE)).isEqualTo("userTask");
        } else if (i == 7) {
          assertThat(data.get(Fields.ACTIVITY_TYPE)).isEqualTo("userTask");
        } else if (i == 10) {
          assertThat(data.get(Fields.ACTIVITY_TYPE)).isEqualTo("parallelGateway");
        } else if (i == 13) {
          assertThat(data.get(Fields.ACTIVITY_TYPE)).isEqualTo("endEvent");
        }

      }

      // Sequence flow taken
      if (i == 3 || i == 8 || i == 11) {
        assertThat(entry.getType()).isNotNull();
        assertThat(ActivitiEventType.SEQUENCEFLOW_TAKEN.name()).isEqualTo(entry.getType());
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {});
        assertThat(data.get(Fields.ID)).isNotNull();
        assertThat(data.get(Fields.SOURCE_ACTIVITY_ID)).isNotNull();
        assertThat(data.get(Fields.SOURCE_ACTIVITY_TYPE)).isNotNull();
        assertThat(data.get(Fields.SOURCE_ACTIVITY_BEHAVIOR_CLASS)).isNotNull();
        assertThat(data.get(Fields.TARGET_ACTIVITY_ID)).isNotNull();
        assertThat(data.get(Fields.TARGET_ACTIVITY_TYPE)).isNotNull();
        assertThat(data.get(Fields.TARGET_ACTIVITY_BEHAVIOR_CLASS)).isNotNull();

        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);
      }

      if (i == 14 || i == 15) {
        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo("VARIABLE_DELETED");
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getExecutionId()).isNotNull();
        assertThat(entry.getTaskId()).isNull();
      }

      if (i == 16) {
        assertThat(entry.getType()).isNotNull();
        assertThat(entry.getType()).isEqualTo("PROCESSINSTANCE_END");
        assertThat(entry.getProcessDefinitionId()).isNotNull();
        assertThat(entry.getProcessInstanceId()).isNotNull();
        assertThat(entry.getTimeStamp()).isNotNull();
        assertThat(entry.getExecutionId()).isNull();
        assertThat(entry.getTaskId()).isNull();

        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {});
        assertThat(data.get(Fields.ID)).isNotNull();
        assertThat(data.get(Fields.PROCESS_DEFINITION_ID)).isNotNull();
        assertThat(data.get(Fields.TENANT_ID)).isNotNull();

        assertThat(data.containsKey(Fields.NAME)).isFalse();
        assertThat(data.containsKey(Fields.BUSINESS_KEY)).isFalse();

        assertThat(data.get(Fields.TENANT_ID)).isEqualTo(testTenant);
      }
    }

    // Cleanup
    for (EventLogEntry eventLogEntry : managementService.getEventLogEntries(null, null)) {
      managementService.deleteEventLogEntry(eventLogEntry.getLogNumber());
    }

    repositoryService.deleteDeployment(deploymentId, true);

  }

  public void testDatabaseEventsNoTenant() throws IOException {

    String deploymentId = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/event/DatabaseEventLoggerProcess.bpmn20.xml").deploy().getId();

    // Run process to gather data
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("DatabaseEventLoggerProcess", singletonMap("testVar", "helloWorld"));

    // Verify event log entries
    List<EventLogEntry> eventLogEntries = managementService.getEventLogEntries(null, null);

    String processDefinitionId = processInstance.getProcessDefinitionId();
    Iterator<EventLogEntry> iterator = eventLogEntries.iterator();
    while (iterator.hasNext()) {
      EventLogEntry entry = iterator.next();
      if (entry.getProcessDefinitionId() != null && !entry.getProcessDefinitionId().equals(processDefinitionId)) {
        iterator.remove();
      }
    }

    assertThat(eventLogEntries).hasSize(15);

    for (int i = 0; i < eventLogEntries.size(); i++) {

      EventLogEntry entry = eventLogEntries.get(i);

      if (i == 0) {
        assertThat(ActivitiEventType.VARIABLE_CREATED.name()).isEqualTo(entry.getType());
        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.TENANT_ID)).isNull();
      }

      // process instance start
      if (i == 1) {
        assertThat(entry.getType()).isEqualTo("PROCESSINSTANCE_START");
        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.TENANT_ID)).isNull();
      }

      // Activity started
      if (i == 2 || i == 5 || i == 9 || i == 12) {
        assertThat(ActivitiEventType.ACTIVITY_STARTED.name()).isEqualTo(entry.getType());
        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.TENANT_ID)).isNull();
      }

      // Leaving start
      if (i == 3) {
        assertThat(ActivitiEventType.ACTIVITY_COMPLETED.name()).isEqualTo(entry.getType());
        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.TENANT_ID)).isNull();
      }

      // Sequence flow taken
      if (i == 4 || i == 7 || i == 8) {
        assertThat(ActivitiEventType.SEQUENCEFLOW_TAKEN.name()).isEqualTo(entry.getType());
        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.TENANT_ID)).isNull();
      }

      // Leaving parallel gateway
      if (i == 6) {
        assertThat(ActivitiEventType.ACTIVITY_COMPLETED.name()).isEqualTo(entry.getType());
        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.TENANT_ID)).isNull();
      }

      // Tasks
      if (i == 11 || i == 14) {
        assertThat(ActivitiEventType.TASK_ASSIGNED.name()).isEqualTo(entry.getType());
        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.TENANT_ID)).isNull();
      }

      if (i == 10 || i == 13) {
        assertThat(ActivitiEventType.TASK_CREATED.name()).isEqualTo(entry.getType());
        Map<String, Object> data = objectMapper.readValue(entry.getData(), new TypeReference<HashMap<String, Object>>() {
        });
        assertThat(data.get(Fields.TENANT_ID)).isNull();
      }

    }

    repositoryService.deleteDeployment(deploymentId, true);

    // Cleanup
    for (EventLogEntry eventLogEntry : managementService.getEventLogEntries(null, null)) {
      managementService.deleteEventLogEntry(eventLogEntry.getLogNumber());
    }

  }

  public void testStandaloneTaskEvents() throws JsonParseException, JsonMappingException, IOException {

    Task task = taskService.newTask();
    task.setAssignee("kermit");
    task.setTenantId("myTenant");
    taskService.saveTask(task);

    taskService.setAssignee(task.getId(), "gonzo");

    task = taskService.createTaskQuery().taskId(task.getId()).singleResult();
    task.setAssignee("kermit");
    taskService.saveTask(task);

    List<EventLogEntry> events = managementService.getEventLogEntries(null, null);
    assertThat(events).hasSize(4);
    assertThat(events.get(0).getType()).isEqualTo("TASK_CREATED");
    assertThat(events.get(1).getType()).isEqualTo("TASK_ASSIGNED");
    assertThat(events.get(2).getType()).isEqualTo("TASK_ASSIGNED");
    assertThat(events.get(3).getType()).isEqualTo("TASK_ASSIGNED");

    for (EventLogEntry eventLogEntry : events) {
      Map<String, Object> data = objectMapper.readValue(eventLogEntry.getData(), new TypeReference<HashMap<String, Object>>() {
      });
      assertThat(data.get(Fields.TENANT_ID)).isEqualTo("myTenant");
    }

    // Cleanup
    taskService.deleteTask(task.getId(), true);
    for (EventLogEntry eventLogEntry : managementService.getEventLogEntries(null, null)) {
      managementService.deleteEventLogEntry(eventLogEntry.getLogNumber());
    }

  }

}
