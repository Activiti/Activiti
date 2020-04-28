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

import static java.util.Collections.singletonMap;
import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to variables.
 */
public class VariableEventsTest extends PluggableActivitiTestCase {

    private TestVariableEventListener listener;

    /**
     * Test create, update and delete variables on a process-instance, using the API.
     */
    @Deployment(resources = {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
    public void testProcessInstanceVariableEvents() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        assertThat(processInstance).isNotNull();

        // Check create event
        runtimeService.setVariable(processInstance.getId(), "testVariable", "The value");
        assertThat(listener.getEventsReceived()).hasSize(1);
        ActivitiVariableEvent event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isNull();
        assertThat(event.getVariableName()).isEqualTo("testVariable");
        assertThat(event.getVariableValue()).isEqualTo("The value");
        listener.clearEventsReceived();

        // Update variable
        runtimeService.setVariable(processInstance.getId(), "testVariable", "Updated value");
        assertThat(listener.getEventsReceived()).hasSize(1);
        event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_UPDATED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isNull();
        assertThat(event.getVariableName()).isEqualTo("testVariable");
        assertThat(event.getVariableValue()).isEqualTo("Updated value");
        listener.clearEventsReceived();

        // Delete variable
        runtimeService.removeVariable(processInstance.getId(), "testVariable");
        assertThat(listener.getEventsReceived()).hasSize(1);
        event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_DELETED);
        // process definition Id can't be recognized in DB flush
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isNull();
        assertThat(event.getVariableName()).isEqualTo("testVariable");
        // deleted variable value should be present now
        assertThat(event.getVariableValue()).isEqualTo("Updated value");
        listener.clearEventsReceived();

        // Create, update and delete multiple variables
        Map<String, Object> vars = map(
            "test", 123,
            "test2", 456);
        runtimeService.setVariables(processInstance.getId(), vars);
        runtimeService.setVariables(processInstance.getId(), vars);
        runtimeService.removeVariables(processInstance.getId(), vars.keySet());

        assertThat(listener.getEventsReceived()).hasSize(6);
        assertThat(listener.getEventsReceived())
            .extracting(ActivitiEvent::getType)
            .containsExactly(
                ActivitiEventType.VARIABLE_CREATED,
                ActivitiEventType.VARIABLE_CREATED,
                ActivitiEventType.VARIABLE_UPDATED,
                ActivitiEventType.VARIABLE_UPDATED,
                ActivitiEventType.VARIABLE_DELETED,
                ActivitiEventType.VARIABLE_DELETED
            );
        listener.clearEventsReceived();

        // Delete nonexistent variable should not dispatch event
        runtimeService.removeVariable(processInstance.getId(), "unexistingVariable");
        assertThat(listener.getEventsReceived().isEmpty()).isTrue();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
    public void testStartEndProcessInstanceVariableEvents() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   singletonMap("var1", "value1"));

        assertThat(listener.getEventsReceived()).hasSize(1);
        assertThat(listener.getEventsReceived().get(0).getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.complete(task.getId());

        assertThat(listener.getEventsReceived()).hasSize(2);
        assertThat(listener.getEventsReceived().get(1).getType()).isEqualTo(ActivitiEventType.VARIABLE_DELETED);
    }

    /**
     * Test create event of variables when process is started with variables passed in.
     */
    @Deployment(resources = {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
    public void testProcessInstanceVariableEventsOnStart() throws Exception {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   singletonMap("testVariable", "The value"));
        assertThat(processInstance).isNotNull();

        // Check create event
        assertThat(listener.getEventsReceived()).hasSize(1);
        ActivitiVariableEvent event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isNull();
        assertThat(event.getVariableName()).isEqualTo("testVariable");
        assertThat(event.getVariableValue()).isEqualTo("The value");
        listener.clearEventsReceived();
    }

    /**
     * Test create, update and delete variables locally on a child-execution of the process instance.
     */
    @Deployment
    public void testProcessInstanceVariableEventsOnChildExecution() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("variableProcess");
        assertThat(processInstance).isNotNull();

        Execution child = runtimeService.createExecutionQuery().parentId(processInstance.getId()).singleResult();
        assertThat(child).isNotNull();

        runtimeService.setVariableLocal(child.getId(), "test", 1234567);

        assertThat(listener.getEventsReceived()).hasSize(1);
        ActivitiVariableEvent event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);

        // Execution and process-id should differ
        assertThat(event.getExecutionId()).isEqualTo(child.getId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    }

    /**
     * Test variable events when done within a process (eg. execution-listener)
     */
    @Deployment
    public void ActivitiEventType() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("variableProcess");
        assertThat(processInstance).isNotNull();

        assertThat(listener.getEventsReceived()).hasSize(3);

        // Check create event
        ActivitiVariableEvent event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isNull();
        assertThat(event.getVariableName()).isEqualTo("variable");
        assertThat(event.getVariableValue()).isEqualTo(123);

        // Check update event
        event = (ActivitiVariableEvent) listener.getEventsReceived().get(1);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_UPDATED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isNull();
        assertThat(event.getVariableName()).isEqualTo("variable");
        assertThat(event.getVariableValue()).isEqualTo(456);

        // Check delete event
        event = (ActivitiVariableEvent) listener.getEventsReceived().get(2);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_DELETED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isNull();
        assertThat(event.getVariableName()).isEqualTo("variable");
        assertThat(event.getVariableValue()).isEqualTo(456);
    }

    @Deployment
    public void testProcessInstanceVariableEventsOnCallActivity() throws Exception {
      ProcessInstance processInstance = runtimeService
          .startProcessInstanceByKey("callVariableProcess",
              singletonMap("parentVar1", "parentVar1Value"));
      assertThat(processInstance).isNotNull();

      assertThat(listener.getEventsReceived()).hasSize(2);

      ActivitiVariableEvent event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);

      event = (ActivitiVariableEvent) listener.getEventsReceived().get(1);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_DELETED);
    }

    /**
     * Test create, update and delete of task-local variables.
     */
    @Deployment(resources = {"org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml"})
    public void testTaskVariableEvents() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        assertThat(processInstance).isNotNull();

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task).isNotNull();

        taskService.setVariableLocal(task.getId(), "testVariable", "The value");
        taskService.setVariableLocal(task.getId(), "testVariable", "Updated value");
        taskService.removeVariableLocal(task.getId(), "testVariable");

        // Check create event
        assertThat(listener.getEventsReceived()).hasSize(3);
        ActivitiVariableEvent event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isEqualTo(task.getId());
        assertThat(event.getVariableName()).isEqualTo("testVariable");
        assertThat(event.getVariableValue()).isEqualTo("The value");

        event = (ActivitiVariableEvent) listener.getEventsReceived().get(1);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_UPDATED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isEqualTo(task.getId());
        assertThat(event.getVariableName()).isEqualTo("testVariable");
        assertThat(event.getVariableValue()).isEqualTo("Updated value");

        event = (ActivitiVariableEvent) listener.getEventsReceived().get(2);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_DELETED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getTaskId()).isEqualTo(task.getId());
        assertThat(event.getVariableName()).isEqualTo("testVariable");
        // deleted values is returned
        assertThat(event.getVariableValue()).isEqualTo("Updated value");
        listener.clearEventsReceived();
    }

    /**
     * Test variable events when done within a process (eg. execution-listener)
     */
    @Deployment
    public void testTaskVariableEventsWithinProcess() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("variableProcess");
        assertThat(processInstance).isNotNull();

        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task).isNotNull();

        assertThat(listener.getEventsReceived()).hasSize(3);

        // Check create event
        ActivitiVariableEvent event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isEqualTo(task.getId());
        assertThat(event.getVariableName()).isEqualTo("variable");
        assertThat(event.getVariableValue()).isEqualTo(123);

        // Check update event
        event = (ActivitiVariableEvent) listener.getEventsReceived().get(1);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_UPDATED);
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isEqualTo(task.getId());
        assertThat(event.getVariableName()).isEqualTo("variable");
        assertThat(event.getVariableValue()).isEqualTo(456);

        // Check delete event
        event = (ActivitiVariableEvent) listener.getEventsReceived().get(2);
        assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_DELETED);
        // process definition Id can't be recognized in DB flush
        assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(event.getTaskId()).isEqualTo(task.getId());
        assertThat(event.getVariableName()).isEqualTo("variable");
        // deleted variable value is returned
        assertThat(event.getVariableValue()).isEqualTo(456);
    }

    /**
     * Test to check create, update an delete behavior for variables on a task not related to a process.
     */
    public void testTaskVariableStandalone() throws Exception {
        Task newTask = taskService.newTask();
        try {
            taskService.saveTask(newTask);

            taskService.setVariable(newTask.getId(),
                                    "testVariable",
                                    123);
            taskService.setVariable(newTask.getId(),
                                    "testVariable",
                                    456);
            taskService.removeVariable(newTask.getId(),
                                       "testVariable");

            assertThat(listener.getEventsReceived()).hasSize(3);
            ActivitiVariableEvent event = (ActivitiVariableEvent) listener.getEventsReceived().get(0);
            assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_CREATED);
            assertThat(event.getProcessDefinitionId()).isNull();
            assertThat(event.getExecutionId()).isNull();
            assertThat(event.getProcessInstanceId()).isNull();
            assertThat(event.getTaskId()).isEqualTo(newTask.getId());
            assertThat(event.getVariableName()).isEqualTo("testVariable");
            assertThat(event.getVariableValue()).isEqualTo(123);

            event = (ActivitiVariableEvent) listener.getEventsReceived().get(1);
            assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_UPDATED);
            assertThat(event.getProcessDefinitionId()).isNull();
            assertThat(event.getExecutionId()).isNull();
            assertThat(event.getProcessInstanceId()).isNull();
            assertThat(event.getTaskId()).isEqualTo(newTask.getId());
            assertThat(event.getVariableName()).isEqualTo("testVariable");
            assertThat(event.getVariableValue()).isEqualTo(456);

            event = (ActivitiVariableEvent) listener.getEventsReceived().get(2);
            assertThat(event.getType()).isEqualTo(ActivitiEventType.VARIABLE_DELETED);
            assertThat(event.getProcessDefinitionId()).isNull();
            assertThat(event.getExecutionId()).isNull();
            assertThat(event.getProcessInstanceId()).isNull();
            assertThat(event.getTaskId()).isEqualTo(newTask.getId());
            assertThat(event.getVariableName()).isEqualTo("testVariable");
            // deleted variable value is returned now
            assertThat(event.getVariableValue()).isEqualTo(456);
        } finally {

            // Cleanup task and history to ensure a clean DB after test
            // success/failure
            if (newTask.getId() != null) {
                taskService.deleteTask(newTask.getId());
                if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
                    historyService.deleteHistoricTaskInstance(newTask.getId());
                }
            }
        }
    }

    @Override
    protected void initializeServices() {
        super.initializeServices();

        listener = new TestVariableEventListener();
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
