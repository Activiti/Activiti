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


package org.activiti.engine.test.api.runtime;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.DeleteReason;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

public class RuntimeServiceTest extends PluggableActivitiTestCase {

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testStartProcessInstanceWithVariables() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("basicType", new DummySerializable());
        runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                 vars);
        Task task = taskService.createTaskQuery().includeProcessVariables().singleResult();
        assertThat(task.getProcessVariables()).isNotNull();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testStartProcessInstanceWithLongStringVariable() {
        Map<String, Object> vars = new HashMap<String, Object>();
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 4001; i++) {
            longString.append("c");
        }
        vars.put("longString", longString.toString());
        runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
        Task task = taskService.createTaskQuery().includeProcessVariables().singleResult();
        assertThat(task.getProcessVariables()).isNotNull();
        assertThat(task.getProcessVariables().get("longString")).isEqualTo(longString.toString());
    }

    public void testStartProcessInstanceByKeyNullKey() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.startProcessInstanceByKey(null));
    }

    public void testStartProcessInstanceByKeyUnexistingKey() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.startProcessInstanceByKey("unexistingkey"))
            .withMessageContaining("no processes deployed with key")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(ProcessDefinition.class));
    }

    public void testStartProcessInstanceByIdNullId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.startProcessInstanceById(null));
    }

    public void testStartProcessInstanceByIdUnexistingId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.startProcessInstanceById("unexistingId"))
            .withMessageContaining("no deployed process definition found with id")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(ProcessDefinition.class));
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testStartProcessInstanceByIdNullVariables() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess", (Map<String, Object>) null);
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(1);
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testStartProcessInstanceWithBusinessKey() {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        // by key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "123");
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getBusinessKey()).isEqualTo("123");
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(1);

        // by key with variables
        processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                   "456",
                                                                   singletonMap("var", "value"));
        assertThat(processInstance).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(2);
        assertThat(runtimeService.getVariable(processInstance.getId(), "var")).isEqualTo("value");

        // by id
        processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), "789");
        assertThat(processInstance).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(3);

        // by id with variables
        processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), "101123", singletonMap("var", "value2"));
        assertThat(processInstance).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(4);
        assertThat(runtimeService.getVariable(processInstance.getId(), "var")).isEqualTo("value2");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testStartProcessInstanceByProcessInstanceBuilder() {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder();

        // by key
        ProcessInstance processInstance = processInstanceBuilder.processDefinitionKey("oneTaskProcess").businessKey("123").start();
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getBusinessKey()).isEqualTo("123");
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(1);

        processInstanceBuilder = runtimeService.createProcessInstanceBuilder();

        // by key, with processInstance name with variables
        processInstance = processInstanceBuilder.processDefinitionKey("oneTaskProcess").businessKey("456").variable("var",
                                                                                                                    "value").name("processName1").start();
        assertThat(processInstance).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(2);
        assertThat(processInstance.getName()).isEqualTo("processName1");
        assertThat(processInstance.getBusinessKey()).isEqualTo("456");
        assertThat(runtimeService.getVariable(processInstance.getId(), "var")).isEqualTo("value");

        processInstanceBuilder = runtimeService.createProcessInstanceBuilder();

        // by id
        processInstance = processInstanceBuilder.processDefinitionId(processDefinition.getId()).businessKey("789").start();
        assertThat(processInstance).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(3);
        assertThat(processInstance.getBusinessKey()).isEqualTo("789");

        processInstanceBuilder = runtimeService.createProcessInstanceBuilder();
        // by id with variables
        processInstance = processInstanceBuilder.processDefinitionId(processDefinition.getId()).businessKey("101123").variable("var",
                                                                                                                               "value2").start();
        assertThat(processInstance).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(4);
        assertThat(runtimeService.getVariable(processInstance.getId(), "var")).isEqualTo("value2");
        assertThat(processInstance.getBusinessKey()).isEqualTo("101123");

        processInstanceBuilder = runtimeService.createProcessInstanceBuilder();
        // by id and processInstance name
        processInstance = processInstanceBuilder.processDefinitionId(processDefinition.getId()).businessKey("101124").name("processName2").start();
        assertThat(processInstance).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(5);
        assertThat(processInstance.getName()).isEqualTo("processName2");
        assertThat(processInstance.getBusinessKey()).isEqualTo("101124");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testProcessInstanceStartTimeUsingRuntimeService() {
        ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
            .processDefinitionKey("oneTaskProcess")
            .create();
        assertThat(processInstance.getStartTime()).isNull();

        runtimeService.startCreatedProcessInstance(processInstance, new HashMap<>());
        processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstance.getProcessInstanceId())
            .singleResult();
        assertThat(processInstance.getStartTime()).isNotNull();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testNonUniqueBusinessKey() {
        runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                 "123");

        // Behaviour changed: https://activiti.atlassian.net/browse/ACT-1860
        runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                 "123");
        assertThat(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("123").count()).isEqualTo(2);
    }

    // some databases might react strange on having multiple times null for the
    // business key
    // when the unique constraint is {processDefinitionId, businessKey}
    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testMultipleNullBusinessKeys() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        assertThat(processInstance.getBusinessKey()).isNull();

        runtimeService.startProcessInstanceByKey("oneTaskProcess");
        runtimeService.startProcessInstanceByKey("oneTaskProcess");

        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(3);
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testDeleteProcessInstance() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(1);

        String deleteReason = "testing instance deletion";
        runtimeService.deleteProcessInstance(processInstance.getId(), deleteReason);
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(0);

        // test that the delete reason of the process instance shows up as delete reason of the task in history ACT-848
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {

            HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

            assertThat(historicTaskInstance.getDeleteReason()).isEqualTo(deleteReason);

            HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

            assertThat(historicInstance).isNotNull();
            assertThat(historicInstance.getDeleteReason()).isEqualTo(deleteReason);
            assertThat(historicInstance.getEndTime()).isNotNull();
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testDeleteProcessInstanceNullReason() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(1);

        // Deleting without a reason should be possible
        runtimeService.deleteProcessInstance(processInstance.getId(), null);
        assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count()).isEqualTo(0);

        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
            HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

            assertThat(historicInstance).isNotNull();
            assertThat(historicInstance.getDeleteReason()).isEqualTo(DeleteReason.PROCESS_INSTANCE_DELETED);
        }
    }

    public void testDeleteProcessInstanceUnexistingId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.deleteProcessInstance("enexistingInstanceId", null))
            .withMessageContaining("No process instance found for id")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(ProcessInstance.class));
    }

    public void testDeleteProcessInstanceNullId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.deleteProcessInstance(null, "test null id delete"))
            .withMessageContaining("processInstanceId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testFindActiveActivityIds() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        List<String> activities = runtimeService.getActiveActivityIds(processInstance.getId());
        assertThat(activities).isNotNull();
        assertThat(activities).hasSize(1);
    }

    public void testFindActiveActivityIdsUnexistingExecututionId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.getActiveActivityIds("unexistingExecutionId"))
            .withMessageContaining("execution unexistingExecutionId doesn't exist")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Execution.class));
    }

    public void testFindActiveActivityIdsNullExecututionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.getActiveActivityIds(null))
            .withMessageContaining("executionId is null");
    }

    /**
     * Testcase to reproduce ACT-950 (https://jira.codehaus.org/browse/ACT-950)
     */
    @Deployment
    public void testFindActiveActivityIdProcessWithErrorEventAndSubProcess() {
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("errorEventSubprocess");

        List<String> activeActivities = runtimeService.getActiveActivityIds(processInstance.getId());
        assertThat(activeActivities).hasSize(5);

        List<Task> tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(2);

        Task parallelUserTask = null;
        for (Task task : tasks) {
            assertThat(task.getName())
                .as("Expected: <ParallelUserTask> or <MainUserTask> but was <" + task.getName() + ">.")
                .isIn("ParallelUserTask", "MainUserTask");
            if (task.getName().equals("ParallelUserTask")) {
                parallelUserTask = task;
            }
        }
        assertThat(parallelUserTask).isNotNull();

        taskService.complete(parallelUserTask.getId());

        Execution execution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("subprocess1WaitBeforeError").singleResult();
        runtimeService.trigger(execution.getId());

        activeActivities = runtimeService.getActiveActivityIds(processInstance.getId());
        assertThat(activeActivities).hasSize(4);

        tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(2);

        Task beforeErrorUserTask = null;
        for (Task task : tasks) {
            assertThat(task.getName())
                .as("Expected: <BeforeError> or <MainUserTask> but was <" + task.getName() + ">.")
                .isIn("BeforeError", "MainUserTask");
            if (task.getName().equals("BeforeError")) {
                beforeErrorUserTask = task;
            }
        }
        assertThat(beforeErrorUserTask).isNotNull();

        taskService.complete(beforeErrorUserTask.getId());

        activeActivities = runtimeService.getActiveActivityIds(processInstance.getId());
        assertThat(activeActivities).hasSize(2);

        tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(2);

        Task afterErrorUserTask = null;
        for (Task task : tasks) {
            assertThat(task.getName())
                .as("Expected: <AfterError> or <MainUserTask> but was <" + task.getName() + ">.")
                .isIn("AfterError", "MainUserTask");
            if (task.getName().equals("AfterError")) {
                afterErrorUserTask = task;
            }
        }
        assertThat(afterErrorUserTask).isNotNull();

        taskService.complete(afterErrorUserTask.getId());

        tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getName()).isEqualTo("MainUserTask");

        activeActivities = runtimeService.getActiveActivityIds(processInstance.getId());
        assertThat(activeActivities).hasSize(1);
        assertThat(activeActivities.get(0)).isEqualTo("MainUserTask");

        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());
    }

    public void testSignalUnexistingExecututionId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.trigger("unexistingExecutionId"))
            .withMessageContaining("execution unexistingExecutionId doesn't exist")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Execution.class));
    }

    public void testSignalNullExecutionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.trigger(null))
            .withMessageContaining("executionId is null");
    }

    @Deployment
    public void testSignalWithProcessVariables() {

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testSignalWithProcessVariables");
        Map<String, Object> processVariables = new HashMap<String, Object>();
        processVariables.put("variable",
                             "value");

        // signal the execution while passing in the variables
        Execution execution = runtimeService.createExecutionQuery().activityId("receiveMessage").singleResult();
        runtimeService.trigger(execution.getId(),
                               processVariables);

        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        assertThat(processVariables).isEqualTo(variables);
    }

    public void testGetVariablesUnexistingExecutionId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.getVariables("unexistingExecutionId"))
            .withMessageContaining("execution unexistingExecutionId doesn't exist")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Execution.class));
    }

    public void testGetVariablesNullExecutionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.getVariables(null))
            .withMessageContaining("executionId is null");
    }

    public void testGetVariableUnexistingExecutionId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.getVariables("unexistingExecutionId"))
            .withMessageContaining("execution unexistingExecutionId doesn't exist")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Execution.class));
    }

    public void testGetVariableNullExecutionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.getVariables(null))
            .withMessageContaining("executionId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableUnexistingVariableName() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        Object variableValue = runtimeService.getVariable(processInstance.getId(), "unexistingVariable");
        assertThat(variableValue).isNull();
    }

    public void testSetVariableUnexistingExecutionId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.setVariable("unexistingExecutionId", "variableName", "value"))
            .withMessageContaining("execution unexistingExecutionId doesn't exist")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Execution.class));
    }

    public void testSetVariableNullExecutionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.setVariable(null, "variableName", "variableValue"))
            .withMessageContaining("executionId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testSetVariableNullVariableName() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> {
                ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
                runtimeService.setVariable(processInstance.getId(), null, "variableValue");
            })
            .withMessageContaining("variableName is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testSetVariables() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        runtimeService.setVariables(processInstance.getId(), map(
            "variable1", "value1",
            "variable2", "value2"
        ));

        assertThat(runtimeService.getVariable(processInstance.getId(),"variable1")).isEqualTo("value1");
        assertThat(runtimeService.getVariable(processInstance.getId(),"variable2")).isEqualTo("value2");
    }

    public void testSetVariablesUnexistingExecutionId() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.setVariables("unexistingexecution", emptyMap()))
            .withMessageContaining("execution unexistingexecution doesn't exist")
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Execution.class));
    }

    public void testSetVariablesNullExecutionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.setVariables(null, emptyMap()))
            .withMessageContaining("executionId is null");
    }

    private void checkHistoricVariableUpdateEntity(String variableName,
                                                   String processInstanceId) {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.FULL)) {
            boolean deletedVariableUpdateFound = false;

            List<HistoricDetail> resultSet = historyService.createHistoricDetailQuery().processInstanceId(processInstanceId).list();
            for (HistoricDetail currentHistoricDetail : resultSet) {
                assertThat(currentHistoricDetail).isInstanceOf(HistoricDetailVariableInstanceUpdateEntity.class);
                HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = (HistoricDetailVariableInstanceUpdateEntity) currentHistoricDetail;

                if (historicVariableUpdate.getName().equals(variableName)) {
                    if (historicVariableUpdate.getValue() == null) {
                        if (deletedVariableUpdateFound) {
                            fail("Mismatch: A HistoricVariableUpdateEntity with a null value already found");
                        } else {
                            deletedVariableUpdateFound = true;
                        }
                    }
                }
            }

            assertThat(deletedVariableUpdateFound).isTrue();
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariable() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("variable1", "value1");
        vars.put("variable2", "value2");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        runtimeService.setVariables(processInstance.getId(), vars);

        runtimeService.removeVariable(processInstance.getId(), "variable1");

        assertThat(runtimeService.getVariable(processInstance.getId(), "variable1")).isNull();
        assertThat(runtimeService.getVariableLocal(processInstance.getId(), "variable1")).isNull();
        assertThat(runtimeService.getVariable(processInstance.getId(), "variable2")).isEqualTo("value2");

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneSubProcess.bpmn20.xml"})
    public void testRemoveVariableInParentScope() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("variable1", "value1");
        vars.put("variable2", "value2");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", vars);
        Task currentTask = taskService.createTaskQuery().singleResult();

        runtimeService.removeVariable(currentTask.getExecutionId(), "variable1");

        assertThat(runtimeService.getVariable(processInstance.getId(), "variable1")).isNull();
        assertThat(runtimeService.getVariable(processInstance.getId(), "variable2")).isEqualTo("value2");

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    }

    public void testRemoveVariableNullExecutionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.removeVariable(null, "variable"))
            .withMessageContaining("executionId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariableLocal() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("variable1", "value1");
        vars.put("variable2", "value2");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
        runtimeService.removeVariableLocal(processInstance.getId(), "variable1");

        assertThat(runtimeService.getVariable(processInstance.getId(), "variable1")).isNull();
        assertThat(runtimeService.getVariableLocal(processInstance.getId(), "variable1")).isNull();
        assertThat(runtimeService.getVariable(processInstance.getId(), "variable2")).isEqualTo("value2");

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneSubProcess.bpmn20.xml"})
    public void testRemoveVariableLocalWithParentScope() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("variable1", "value1");
        vars.put("variable2", "value2");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", vars);
        Task currentTask = taskService.createTaskQuery().singleResult();
        runtimeService.setVariableLocal(currentTask.getExecutionId(), "localVariable", "local value");

        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(),"localVariable")).isEqualTo("local value");

        runtimeService.removeVariableLocal(currentTask.getExecutionId(), "localVariable");

        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "localVariable")).isNull();
        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(), "localVariable")).isNull();

        assertThat(runtimeService.getVariable(processInstance.getId(),"variable1")).isEqualTo("value1");
        assertThat(runtimeService.getVariable(processInstance.getId(),"variable2")).isEqualTo("value2");

        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable1")).isEqualTo("value1");
        assertThat(runtimeService.getVariable(currentTask.getExecutionId(),"variable2")).isEqualTo("value2");

        checkHistoricVariableUpdateEntity("localVariable", processInstance.getId());
    }

    public void testRemoveLocalVariableNullExecutionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.removeVariableLocal(null, "variable"))
            .withMessageContaining("executionId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testRemoveVariables() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("variable1", "value1");
        vars.put("variable2", "value2");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
        runtimeService.setVariable(processInstance.getId(), "variable3", "value3");

        runtimeService.removeVariables(processInstance.getId(), vars.keySet());

        assertThat(runtimeService.getVariable(processInstance.getId(), "variable1")).isNull();
        assertThat(runtimeService.getVariableLocal(processInstance.getId(), "variable1")).isNull();
        assertThat(runtimeService.getVariable(processInstance.getId(), "variable2")).isNull();
        assertThat(runtimeService.getVariableLocal(processInstance.getId(), "variable2")).isNull();

        assertThat(runtimeService.getVariable(processInstance.getId(),"variable3")).isEqualTo("value3");
        assertThat(runtimeService.getVariableLocal(processInstance.getId(),"variable3")).isEqualTo("value3");

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
        checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneSubProcess.bpmn20.xml"})
    public void testRemoveVariablesWithParentScope() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("variable1", "value1");
        vars.put("variable2", "value2");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", vars);
        runtimeService.setVariable(processInstance.getId(), "variable3", "value3");

        Task currentTask = taskService.createTaskQuery().singleResult();

        runtimeService.removeVariables(currentTask.getExecutionId(), vars.keySet());

        assertThat(runtimeService.getVariable(processInstance.getId(), "variable1")).isNull();
        assertThat(runtimeService.getVariableLocal(processInstance.getId(), "variable1")).isNull();
        assertThat(runtimeService.getVariable(processInstance.getId(), "variable2")).isNull();
        assertThat(runtimeService.getVariableLocal(processInstance.getId(), "variable2")).isNull();

        assertThat(runtimeService.getVariable(processInstance.getId(),"variable3")).isEqualTo("value3");
        assertThat(runtimeService.getVariableLocal(processInstance.getId(),"variable3")).isEqualTo("value3");

        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable1")).isNull();
        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable2")).isNull();

        assertThat(runtimeService.getVariable(currentTask.getExecutionId(),"variable3")).isEqualTo("value3");

        checkHistoricVariableUpdateEntity("variable1", processInstance.getId());
        checkHistoricVariableUpdateEntity("variable2", processInstance.getId());
    }

    public void testRemoveVariablesNullExecutionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.removeVariables(null, emptyList()))
            .withMessageContaining("executionId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneSubProcess.bpmn20.xml"})
    public void testRemoveVariablesLocalWithParentScope() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("variable1", "value1");
        vars.put("variable2", "value2");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess", vars);

        Task currentTask = taskService.createTaskQuery().singleResult();
        Map<String, Object> varsToDelete = new HashMap<String, Object>();
        varsToDelete.put("variable3", "value3");
        varsToDelete.put("variable4", "value4");
        varsToDelete.put("variable5", "value5");
        runtimeService.setVariablesLocal(currentTask.getExecutionId(), varsToDelete);
        runtimeService.setVariableLocal(currentTask.getExecutionId(), "variable6", "value6");

        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable3")).isEqualTo("value3");
        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable3")).isEqualTo("value3");
        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable4")).isEqualTo("value4");
        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable4")).isEqualTo("value4");
        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable5")).isEqualTo("value5");
        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable5")).isEqualTo("value5");
        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable6")).isEqualTo("value6");
        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable6")).isEqualTo("value6");

        runtimeService.removeVariablesLocal(currentTask.getExecutionId(), varsToDelete.keySet());

        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable1")).isEqualTo("value1");
        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable2")).isEqualTo("value2");

        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable3")).isNull();
        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable3")).isNull();
        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable4")).isNull();
        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable4")).isNull();
        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable5")).isNull();
        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable5")).isNull();

        assertThat(runtimeService.getVariable(currentTask.getExecutionId(), "variable6")).isEqualTo("value6");
        assertThat(runtimeService.getVariableLocal(currentTask.getExecutionId(), "variable6")).isEqualTo("value6");

        checkHistoricVariableUpdateEntity("variable3", processInstance.getId());
        checkHistoricVariableUpdateEntity("variable4", processInstance.getId());
        checkHistoricVariableUpdateEntity("variable5", processInstance.getId());
    }

    public void testRemoveVariablesLocalNullExecutionId() {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> runtimeService.removeVariablesLocal(null, emptyList()))
            .withMessageContaining("executionId is null");
    }

    @Deployment(resources = {"org/activiti/engine/test/api/runtime/RuntimeServiceTest.catchAlertSignal.bpmn20.xml",
            "org/activiti/engine/test/api/runtime/RuntimeServiceTest.catchPanicSignal.bpmn20.xml"})
    public void testSignalEventReceived() {

        startSignalCatchProcesses();
        // 15, because the signal catch is a scope
        assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(15);
        runtimeService.signalEventReceived("alert");
        assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(9);
        runtimeService.signalEventReceived("panic");
        assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);

        startSignalCatchProcesses();

        // signal the executions one at a time:
        for (int executions = 3; executions > 0; executions--) {
            List<Execution> page = runtimeService.createExecutionQuery().signalEventSubscriptionName("alert").listPage(0, 1);
            runtimeService.signalEventReceived("alert", page.get(0).getId());

            assertThat(runtimeService.createExecutionQuery().signalEventSubscriptionName("alert").count()).isEqualTo(executions - 1);
        }

        for (int executions = 3; executions > 0; executions--) {
            List<Execution> page = runtimeService.createExecutionQuery().signalEventSubscriptionName("panic").listPage(0, 1);
            runtimeService.signalEventReceived("panic", page.get(0).getId());

            assertThat(runtimeService.createExecutionQuery().signalEventSubscriptionName("panic").count()).isEqualTo(executions - 1);
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/runtime/RuntimeServiceTest.catchAlertMessage.bpmn20.xml",
            "org/activiti/engine/test/api/runtime/RuntimeServiceTest.catchPanicMessage.bpmn20.xml"})
    public void testMessageEventReceived() {

        startMessageCatchProcesses();
        // 12, because the signal catch is a scope
        assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(12);

        // signal the executions one at a time:
        for (int executions = 3; executions > 0; executions--) {
            List<Execution> page = runtimeService.createExecutionQuery().messageEventSubscriptionName("alert").listPage(0, 1);
            runtimeService.messageEventReceived("alert", page.get(0).getId());

            assertThat(runtimeService.createExecutionQuery().messageEventSubscriptionName("alert").count()).isEqualTo(executions - 1);
        }

        for (int executions = 3; executions > 0; executions--) {
            List<Execution> page = runtimeService.createExecutionQuery().messageEventSubscriptionName("panic").listPage(0, 1);
            runtimeService.messageEventReceived("panic", page.get(0).getId());

            assertThat(runtimeService.createExecutionQuery().messageEventSubscriptionName("panic").count()).isEqualTo(executions - 1);
        }
    }

    public void testSignalEventReceivedNonExistingExecution() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.signalEventReceived("alert", "nonexistingExecution"))
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Execution.class));
    }

    public void testMessageEventReceivedNonExistingExecution() {
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.messageEventReceived("alert", "nonexistingExecution"))
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(Execution.class));
    }

    @Deployment(resources = {"org/activiti/engine/test/api/runtime/RuntimeServiceTest.catchAlertSignal.bpmn20.xml"})
    public void testExecutionWaitingForDifferentSignal() {
        runtimeService.startProcessInstanceByKey("catchAlertSignal");
        Execution execution = runtimeService.createExecutionQuery().signalEventSubscriptionName("alert").singleResult();
        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> runtimeService.signalEventReceived("bogusSignal", execution.getId()));
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testSetProcessInstanceName() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getName()).isNull();

        // Set the name
        runtimeService.setProcessInstanceName(processInstance.getId(), "New name");
        processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getName()).isEqualTo("New name");

        // Set the name to null
        runtimeService.setProcessInstanceName(processInstance.getId(), null);
        processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getName()).isNull();

        // Set name for unexisting process instance, should fail
        assertThatExceptionOfType(ActivitiObjectNotFoundException.class)
            .isThrownBy(() -> runtimeService.setProcessInstanceName("unexisting", null))
            .satisfies(ae -> assertThat(ae.getObjectClass()).isEqualTo(ProcessInstance.class));

        processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getName()).isNull();

        // Set name for suspended process instance, should fail
        runtimeService.suspendProcessInstanceById(processInstance.getId());
        String processInstanceId = processInstance.getId();
        assertThatExceptionOfType(ActivitiException.class)
            .isThrownBy(() -> runtimeService.setProcessInstanceName(processInstanceId, null))
            .withMessage("process instance " + processInstanceId + " is suspended, cannot set name");

        processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getName()).isNull();
    }

    private void startSignalCatchProcesses() {
        for (int i = 0; i < 3; i++) {
            runtimeService.startProcessInstanceByKey("catchAlertSignal");
            runtimeService.startProcessInstanceByKey("catchPanicSignal");
        }
    }

    private void startMessageCatchProcesses() {
        for (int i = 0; i < 3; i++) {
            runtimeService.startProcessInstanceByKey("catchAlertMessage");
            runtimeService.startProcessInstanceByKey("catchPanicMessage");
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableUnexistingVariableNameWithCast() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        String variableValue = runtimeService.getVariable(processInstance.getId(),
                                                          "unexistingVariable",
                                                          String.class);
        assertThat(variableValue).isNull();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableExistingVariableNameWithCast() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("var1",
                   true);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   params);
        Boolean variableValue = runtimeService.getVariable(processInstance.getId(),
                                                           "var1",
                                                           Boolean.class);
        assertThat(variableValue).isTrue();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableExistingVariableNameWithInvalidCast() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("var1",
                   true);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   params);
        assertThatExceptionOfType(ClassCastException.class)
            .isThrownBy(() -> runtimeService.getVariable(processInstance.getId(), "var1", String.class));
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableLocalUnexistingVariableNameWithCast() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
        String variableValue = runtimeService.getVariableLocal(processInstance.getId(),
                                                               "var1",
                                                               String.class);
        assertThat(variableValue).isNull();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableLocalExistingVariableNameWithCast() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("var1",
                   true);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   params);
        Boolean variableValue = runtimeService.getVariableLocal(processInstance.getId(),
                                                                "var1",
                                                                Boolean.class);
        assertThat(variableValue).isTrue();
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testGetVariableLocalExistingVariableNameWithInvalidCast() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("var1",
                   true);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   params);

        assertThatExceptionOfType(ClassCastException.class)
            .isThrownBy(() -> runtimeService.getVariableLocal(processInstance.getId(), "var1", String.class));
    }

    // Test for https://activiti.atlassian.net/browse/ACT-2186
    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testHistoricVariableRemovedWhenRuntimeVariableIsRemoved() {
        if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            Map<String, Object> vars = new HashMap<String, Object>();
            vars.put("var1", "Hello");
            vars.put("var2", "World");
            vars.put("var3", "!");
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

            // Verify runtime
            assertThat(runtimeService.getVariables(processInstance.getId())).hasSize(3);
            assertThat(runtimeService.getVariables(processInstance.getId(), asList("var1", "var2", "var3"))).hasSize(3);
            assertThat(runtimeService.getVariable(processInstance.getId(),"var2")).isNotNull();

            // Verify history
            assertThat(historyService.createHistoricVariableInstanceQuery().list()).hasSize(3);
            assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("var2").singleResult()).isNotNull();

            // Remove one variable
            runtimeService.removeVariable(processInstance.getId(), "var2");

            // Verify runtime
            assertThat(runtimeService.getVariables(processInstance.getId())).hasSize(2);
            assertThat(runtimeService.getVariables(processInstance.getId(), asList("var1", "var2", "var3"))).hasSize(2);
            assertThat(runtimeService.getVariable(processInstance.getId(), "var2")).isNull();

            // Verify history
            assertThat(historyService.createHistoricVariableInstanceQuery().list()).hasSize(2);
            assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("var2").singleResult()).isNull();
        }
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testStartTimeProcessInstance() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, 8);
        calendar.set(Calendar.DAY_OF_MONTH, 30);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date noon = calendar.getTime();

        processEngineConfiguration.getClock().setCurrentTime(noon);
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        assertThat(processInstance.getStartTime()).isEqualTo(noon);
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testAuthenticatedStartUserProcessInstance() {
        final String authenticatedUser = "user1";
        Authentication.setAuthenticatedUserId(authenticatedUser);
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        assertThat(processInstance.getStartUserId()).isEqualTo(authenticatedUser);
    }

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testNoAuthenticatedStartUserProcessInstance() {
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

        assertThat(processInstance.getStartUserId()).isNull();
    }
}
