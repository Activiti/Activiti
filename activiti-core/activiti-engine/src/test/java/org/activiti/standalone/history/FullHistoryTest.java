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


package org.activiti.standalone.history;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.api.runtime.DummySerializable;
import org.activiti.engine.test.history.SerializableVariable;
import org.activiti.standalone.jpa.FieldAccessJPAEntity;

public class FullHistoryTest extends ResourceActivitiTestCase {

    public FullHistoryTest() {
        super("org/activiti/standalone/history/fullhistory.activiti.cfg.xml");
    }

    @Deployment
    public void testVariableUpdates() {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("number", "one");
        variables.put("character", "a");
        variables.put("bytes", ":-(".getBytes());
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveTask",
                                                                                   variables);
        runtimeService.setVariable(processInstance.getId(), "number", "two");
        runtimeService.setVariable(processInstance.getId(), "bytes", ":-)".getBytes());

        // Start-task should be added to history
        HistoricActivityInstance historicStartEvent = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).activityId("theStart").singleResult();
        assertThat(historicStartEvent).isNotNull();

        HistoricActivityInstance waitStateActivity = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).activityId("waitState").singleResult();
        assertThat(waitStateActivity).isNotNull();

        HistoricActivityInstance serviceTaskActivity = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).activityId("serviceTask").singleResult();
        assertThat(serviceTaskActivity).isNotNull();

        List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery().orderByVariableName().asc().orderByVariableRevision().asc().list();

        assertThat(historicDetails).hasSize(10);

        HistoricVariableUpdate historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(0);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("bytes");
        assertThat(new String((byte[]) historicVariableUpdate.getValue())).isEqualTo(":-(");
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(0);

        // Activiti 6: we don't store the start event activityId anymore!
//    assertThat(historicVariableUpdate.getActivityInstanceId()).isEqualTo(historicStartEvent.getId());
        assertThat(historicVariableUpdate.getActivityInstanceId()).isNull();

        // Variable is updated when process was in waitstate
        historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(1);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("bytes");
        assertThat(new String((byte[]) historicVariableUpdate.getValue())).isEqualTo(":-)");
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(1);

//    assertThat(historicVariableUpdate.getActivityInstanceId()).isEqualTo(waitStateActivity.getId());
        assertThat(historicVariableUpdate.getActivityInstanceId()).isNull();

        historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(2);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("character");
        assertThat(historicVariableUpdate.getValue()).isEqualTo("a");
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(0);

//    assertThat(historicVariableUpdate.getActivityInstanceId()).isEqualTo(historicStartEvent.getId());
        assertThat(historicVariableUpdate.getActivityInstanceId()).isNull();

        historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(3);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("number");
        assertThat(historicVariableUpdate.getValue()).isEqualTo("one");
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(0);

//    assertThat(historicVariableUpdate.getActivityInstanceId()).isEqualTo(historicStartEvent.getId());
        assertThat(historicVariableUpdate.getActivityInstanceId()).isNull();

        // Variable is updated when process was in waitstate
        historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(4);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("number");
        assertThat(historicVariableUpdate.getValue()).isEqualTo("two");
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(1);

//    assertThat(historicVariableUpdate.getActivityInstanceId()).isEqualTo(waitStateActivity.getId());
        assertThat(historicVariableUpdate.getActivityInstanceId()).isNull();

        // Variable set from process-start execution listener
        historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(5);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("zVar1");
        assertThat(historicVariableUpdate.getValue()).isEqualTo("Event: start");
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(0);
        assertThat(historicVariableUpdate.getActivityInstanceId()).isNull();

        // Variable set from transition take execution listener
        historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(6);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("zVar2");
        assertThat(historicVariableUpdate.getValue()).isEqualTo("Event: take");
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(0);
        assertThat(historicVariableUpdate.getActivityInstanceId()).isNull();

        // Variable set from activity start execution listener on the servicetask
        historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(7);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("zVar3");
        assertThat(historicVariableUpdate.getValue()).isEqualTo("Event: start");
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(0);
        assertThat(historicVariableUpdate.getActivityInstanceId()).isEqualTo(serviceTaskActivity.getId());

        // Variable set from activity end execution listener on the servicetask
        historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(8);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("zVar4");
        assertThat(historicVariableUpdate.getValue()).isEqualTo("Event: end");
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(0);
        assertThat(historicVariableUpdate.getActivityInstanceId()).isEqualTo(serviceTaskActivity.getId());

        // Variable set from service-task
        historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(9);
        assertThat(historicVariableUpdate.getVariableName()).isEqualTo("zzz");
        assertThat(historicVariableUpdate.getValue()).isEqualTo(123456789L);
        assertThat(historicVariableUpdate.getRevision()).isEqualTo(0);
        assertThat(historicVariableUpdate.getActivityInstanceId()).isEqualTo(serviceTaskActivity.getId());

        // trigger receive task
        runtimeService.trigger(runtimeService.createExecutionQuery().activityId("waitState").singleResult().getId());
        assertProcessEnded(processInstance.getId());

        // check for historic process variables set
        HistoricVariableInstanceQuery historicProcessVariableQuery = historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc();

        assertThat(historicProcessVariableQuery.count()).isEqualTo(8);

        List<HistoricVariableInstance> historicVariables = historicProcessVariableQuery.list();

        // Variable status when process is finished
        HistoricVariableInstance historicVariable = historicVariables.get(0);
        assertThat(historicVariable.getVariableName()).isEqualTo("bytes");
        assertThat(new String((byte[]) historicVariable.getValue())).isEqualTo(":-)");
        assertThat(historicVariable.getCreateTime()).isNotNull();
        assertThat(historicVariable.getLastUpdatedTime()).isNotNull();

        historicVariable = historicVariables.get(1);
        assertThat(historicVariable.getVariableName()).isEqualTo("character");
        assertThat(historicVariable.getValue()).isEqualTo("a");
        assertThat(historicVariable.getCreateTime()).isNotNull();
        assertThat(historicVariable.getLastUpdatedTime()).isNotNull();

        historicVariable = historicVariables.get(2);
        assertThat(historicVariable.getVariableName()).isEqualTo("number");
        assertThat(historicVariable.getValue()).isEqualTo("two");
        assertThat(historicVariable.getCreateTime()).isNotNull();
        assertThat(historicVariable.getLastUpdatedTime()).isNotNull();
        assertThat(historicVariable.getLastUpdatedTime()).isNotSameAs(historicVariable.getCreateTime());

        historicVariable = historicVariables.get(3);
        assertThat(historicVariable.getVariableName()).isEqualTo("zVar1");
        assertThat(historicVariable.getValue()).isEqualTo("Event: start");
        assertThat(historicVariable.getCreateTime()).isNotNull();
        assertThat(historicVariable.getLastUpdatedTime()).isNotNull();

        historicVariable = historicVariables.get(4);
        assertThat(historicVariable.getVariableName()).isEqualTo("zVar2");
        assertThat(historicVariable.getValue()).isEqualTo("Event: take");
        assertThat(historicVariable.getCreateTime()).isNotNull();
        assertThat(historicVariable.getLastUpdatedTime()).isNotNull();

        historicVariable = historicVariables.get(5);
        assertThat(historicVariable.getVariableName()).isEqualTo("zVar3");
        assertThat(historicVariable.getValue()).isEqualTo("Event: start");
        assertThat(historicVariable.getCreateTime()).isNotNull();
        assertThat(historicVariable.getLastUpdatedTime()).isNotNull();

        historicVariable = historicVariables.get(6);
        assertThat(historicVariable.getVariableName()).isEqualTo("zVar4");
        assertThat(historicVariable.getValue()).isEqualTo("Event: end");
        assertThat(historicVariable.getCreateTime()).isNotNull();
        assertThat(historicVariable.getLastUpdatedTime()).isNotNull();

        historicVariable = historicVariables.get(7);
        assertThat(historicVariable.getVariableName()).isEqualTo("zzz");
        assertThat(historicVariable.getValue()).isEqualTo(123456789L);
        assertThat(historicVariable.getCreateTime()).isNotNull();
        assertThat(historicVariable.getLastUpdatedTime()).isNotNull();

        historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLike("number",
                                                                                                  "tw%").singleResult();
        assertThat(historicVariable).isNotNull();
        assertThat(historicVariable.getVariableName()).isEqualTo("number");
        assertThat(historicVariable.getValue()).isEqualTo("two");

        historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("number",
                                                                                                            "TW%").singleResult();
        assertThat(historicVariable).isNotNull();
        assertThat(historicVariable.getVariableName()).isEqualTo("number");
        assertThat(historicVariable.getValue()).isEqualTo("two");

        historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("number",
                                                                                                            "TW2%").singleResult();
        assertThat(historicVariable).isNull();
    }

    @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
    public void testHistoricVariableInstanceQueryTaskVariables() {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("variable", "setFromProcess");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   variables);

        assertThat(historyService.createHistoricVariableInstanceQuery().count()).isEqualTo(1);

        Task activeTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(activeTask).isNotNull();
        taskService.setVariableLocal(activeTask.getId(),
                                     "variable",
                                     "setFromTask");

        // Check if additional variable is available in history, task-local
        assertThat(historyService.createHistoricVariableInstanceQuery().count()).isEqualTo(2);
        assertThat(historyService.createHistoricVariableInstanceQuery().taskId(activeTask.getId()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricVariableInstanceQuery().taskId(activeTask.getId()).singleResult().getValue()).isEqualTo("setFromTask");
        assertThat(historyService.createHistoricVariableInstanceQuery().taskId(activeTask.getId()).singleResult().getTaskId()).isEqualTo(activeTask.getId());
        assertThat(historyService.createHistoricVariableInstanceQuery().excludeTaskVariables().count()).isEqualTo(1);

        // Test null task-id
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricVariableInstanceQuery().taskId(null).singleResult())
            .withMessage("taskId is null");

        // Test invalid usage of taskId together with excludeTaskVariables
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricVariableInstanceQuery().taskId("123").excludeTaskVariables().singleResult())
            .withMessage("Cannot use taskId together with excludeTaskVariables");

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricVariableInstanceQuery().excludeTaskVariables().taskId("123").singleResult())
            .withMessage("Cannot use taskId together with excludeTaskVariables");
    }

    @Deployment(resources = "org/activiti/standalone/history/FullHistoryTest.testVariableUpdates.bpmn20.xml")
    public void testHistoricVariableInstanceQuery() {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("process", "one");
        runtimeService.startProcessInstanceByKey("receiveTask", variables);
        runtimeService.trigger(runtimeService.createExecutionQuery().activityId("waitState").singleResult().getId());

        assertThat(historyService.createHistoricVariableInstanceQuery().variableName("process").count()).isEqualTo(1);
        assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "one").count()).isEqualTo(1);

        Map<String, Object> variables2 = new HashMap<String, Object>();
        variables2.put("process", "two");
        ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("receiveTask",
                                                                                    variables2);
        runtimeService.trigger(runtimeService.createExecutionQuery().activityId("waitState").singleResult().getId());

        assertThat(historyService.createHistoricVariableInstanceQuery().variableName("process").count()).isEqualTo(2);
        assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "one").count()).isEqualTo(1);
        assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "two").count()).isEqualTo(1);

        HistoricVariableInstance historicProcessVariable = historyService.createHistoricVariableInstanceQuery().variableValueEquals("process",
                                                                                                                                    "one").singleResult();
        assertThat(historicProcessVariable.getVariableName()).isEqualTo("process");
        assertThat(historicProcessVariable.getValue()).isEqualTo("one");

        Map<String, Object> variables3 = new HashMap<String, Object>();
        variables3.put("long", 1000l);
        variables3.put("double", 25.43d);
        ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("receiveTask",
                                                                                    variables3);
        runtimeService.trigger(runtimeService.createExecutionQuery().activityId("waitState").singleResult().getId());

        assertThat(historyService.createHistoricVariableInstanceQuery().variableName("long").count()).isEqualTo(1);
        assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("long", 1000l).count()).isEqualTo(1);
        assertThat(historyService.createHistoricVariableInstanceQuery().variableName("double").count()).isEqualTo(1);
        assertThat(historyService.createHistoricVariableInstanceQuery().variableValueEquals("double",
                                                                                              25.43d).count()).isEqualTo(1);
    }

    @Deployment
    public void testHistoricVariableUpdatesAllTypes() throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS");
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("aVariable",
                      "initial value");

        Date startedDate = sdf.parse("01/01/2001 01:23:45 000");

        // In the javaDelegate, the current time is manipulated
        Date updatedDate = sdf.parse("01/01/2001 01:23:46 000");

        processEngineConfiguration.getClock().setCurrentTime(startedDate);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricVariableUpdateProcess",
                                                                                   variables);

        List<HistoricDetail> details = historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).orderByVariableName().asc().orderByTime().asc().list();

        // 8 variable updates should be present, one performed when starting process the other 7 are set in VariableSetter serviceTask
        assertThat(details).hasSize(9);

        // Since we order by varName, first entry should be aVariable update from startTask
        HistoricVariableUpdate startVarUpdate = (HistoricVariableUpdate) details.get(0);
        assertThat(startVarUpdate.getVariableName()).isEqualTo("aVariable");
        assertThat(startVarUpdate.getValue()).isEqualTo("initial value");
        assertThat(startVarUpdate.getRevision()).isEqualTo(0);
        assertThat(startVarUpdate.getProcessInstanceId()).isEqualTo(processInstance.getId());
        // Date should the one set when starting
        assertThat(startVarUpdate.getTime()).isEqualTo(startedDate);

        HistoricVariableUpdate updatedStringVariable = (HistoricVariableUpdate) details.get(1);
        assertThat(updatedStringVariable.getVariableName()).isEqualTo("aVariable");
        assertThat(updatedStringVariable.getValue()).isEqualTo("updated value");
        assertThat(updatedStringVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
        // Date should be the updated date
        assertThat(updatedStringVariable.getTime()).isEqualTo(updatedDate);

        HistoricVariableUpdate intVariable = (HistoricVariableUpdate) details.get(2);
        assertThat(intVariable.getVariableName()).isEqualTo("bVariable");
        assertThat(intVariable.getValue()).isEqualTo(123);
        assertThat(intVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(intVariable.getTime()).isEqualTo(updatedDate);

        HistoricVariableUpdate longVariable = (HistoricVariableUpdate) details.get(3);
        assertThat(longVariable.getVariableName()).isEqualTo("cVariable");
        assertThat(longVariable.getValue()).isEqualTo(12345L);
        assertThat(longVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(longVariable.getTime()).isEqualTo(updatedDate);

        HistoricVariableUpdate doubleVariable = (HistoricVariableUpdate) details.get(4);
        assertThat(doubleVariable.getVariableName()).isEqualTo("dVariable");
        assertThat(doubleVariable.getValue()).isEqualTo(1234.567);
        assertThat(doubleVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(doubleVariable.getTime()).isEqualTo(updatedDate);

        HistoricVariableUpdate shortVariable = (HistoricVariableUpdate) details.get(5);
        assertThat(shortVariable.getVariableName()).isEqualTo("eVariable");
        assertThat(shortVariable.getValue()).isEqualTo((short) 12);
        assertThat(shortVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(shortVariable.getTime()).isEqualTo(updatedDate);

        HistoricVariableUpdate dateVariable = (HistoricVariableUpdate) details.get(6);
        assertThat(dateVariable.getVariableName()).isEqualTo("fVariable");
        assertThat(dateVariable.getValue()).isEqualTo(sdf.parse("01/01/2001 01:23:45 678"));
        assertThat(dateVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(dateVariable.getTime()).isEqualTo(updatedDate);

        HistoricVariableUpdate serializableVariable = (HistoricVariableUpdate) details.get(7);
        assertThat(serializableVariable.getVariableName()).isEqualTo("gVariable");
        assertThat(serializableVariable.getValue()).isEqualTo(new SerializableVariable("hello hello"));
        assertThat(serializableVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(serializableVariable.getTime()).isEqualTo(updatedDate);

        HistoricVariableUpdate byteArrayVariable = (HistoricVariableUpdate) details.get(8);
        assertThat(byteArrayVariable.getVariableName()).isEqualTo("hVariable");
        assertThat(new String((byte[]) byteArrayVariable.getValue())).isEqualTo(";-)");
        assertThat(byteArrayVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
        assertThat(byteArrayVariable.getTime()).isEqualTo(updatedDate);

        // end process instance
        List<Task> tasks = taskService.createTaskQuery().list();
        assertThat(tasks).hasSize(1);
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());

        // check for historic process variables set
        HistoricVariableInstanceQuery historicProcessVariableQuery = historyService.createHistoricVariableInstanceQuery().orderByVariableName().asc();

        assertThat(historicProcessVariableQuery.count()).isEqualTo(8);

        List<HistoricVariableInstance> historicVariables = historicProcessVariableQuery.list();

        // Variable status when process is finished
        HistoricVariableInstance historicVariable = historicVariables.get(0);
        assertThat(historicVariable.getVariableName()).isEqualTo("aVariable");
        assertThat(historicVariable.getValue()).isEqualTo("updated value");
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());

        historicVariable = historicVariables.get(1);
        assertThat(historicVariable.getVariableName()).isEqualTo("bVariable");
        assertThat(historicVariable.getValue()).isEqualTo(123);
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());

        historicVariable = historicVariables.get(2);
        assertThat(historicVariable.getVariableName()).isEqualTo("cVariable");
        assertThat(historicVariable.getValue()).isEqualTo(12345L);
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());

        historicVariable = historicVariables.get(3);
        assertThat(historicVariable.getVariableName()).isEqualTo("dVariable");
        assertThat(historicVariable.getValue()).isEqualTo(1234.567);
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());

        historicVariable = historicVariables.get(4);
        assertThat(historicVariable.getVariableName()).isEqualTo("eVariable");
        assertThat(historicVariable.getValue()).isEqualTo((short) 12);
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());

        historicVariable = historicVariables.get(5);
        assertThat(historicVariable.getVariableName()).isEqualTo("fVariable");
        assertThat(historicVariable.getValue()).isEqualTo(sdf.parse("01/01/2001 01:23:45 678"));
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());

        historicVariable = historicVariables.get(6);
        assertThat(historicVariable.getVariableName()).isEqualTo("gVariable");
        assertThat(historicVariable.getValue()).isEqualTo(new SerializableVariable("hello hello"));
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());

        historicVariable = historicVariables.get(7);
        assertThat(historicVariable.getVariableName()).isEqualTo("hVariable");
        assertThat(new String((byte[]) historicVariable.getValue())).isEqualTo(";-)");
        assertThat(historicVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
    }

    @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
    public void testHistoricVariableQuery() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("stringVar",
                      "activiti rocks!");
        variables.put("longVar",
                      12345L);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   variables);

        // Query on activity-instance, activity instance null will return all
        // vars set when starting process
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().activityInstanceId(null).count()).isEqualTo(2);
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().activityInstanceId("unexisting").count()).isEqualTo(0);

        // Query on process-instance
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count()).isEqualTo(2);
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().processInstanceId("unexisting").count()).isEqualTo(0);

        // Query both process-instance and activity-instance
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().activityInstanceId(null).processInstanceId(processInstance.getId()).count()).isEqualTo(2);

        // end process instance
        List<Task> tasks = taskService.createTaskQuery().list();
        assertThat(tasks.size()).isEqualTo(1);
        taskService.complete(tasks.get(0).getId());
        assertProcessEnded(processInstance.getId());

        assertThat(historyService.createHistoricVariableInstanceQuery().count()).isEqualTo(2);

        // Query on process-instance
        assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(2);
        assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId("unexisting").count()).isEqualTo(0);
    }

    @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
    public void testHistoricVariableQueryExcludeTaskRelatedDetails() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("stringVar",
                      "activiti rocks!");
        variables.put("longVar",
                      12345L);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   variables);

        // Set a local task-variable
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task).isNotNull();
        taskService.setVariableLocal(task.getId(),
                                     "taskVar",
                                     "It is I, le Variable");

        // Query on process-instance
        assertEquals(3,
                     historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());

        // Query on process-instance, excluding task-details
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).excludeTaskDetails().count());

        // Check task-id precedence on excluding task-details
        assertEquals(1,
                     historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).excludeTaskDetails().taskId(task.getId()).count());
    }

    @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
    public void testHistoricVariableQuerySorting() throws Exception {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("stringVar",
                      "activiti rocks!");
        variables.put("longVar",
                      12345L);

        runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                 variables);

        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().asc().count());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByTime().asc().count());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().asc().count());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().asc().count());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().asc().count());

        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().desc().count());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByTime().desc().count());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().desc().count());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().desc().count());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().desc().count());

        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().asc().list().size());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByTime().asc().list().size());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().asc().list().size());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().asc().list().size());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().asc().list().size());

        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().desc().list().size());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByTime().desc().list().size());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().desc().list().size());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().desc().list().size());
        assertEquals(2,
                     historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().desc().list().size());
    }

    public void testHistoricDetailQueryInvalidSorting() throws Exception {
        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricDetailQuery().asc().list());

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricDetailQuery().desc().list());

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricDetailQuery().orderByProcessInstanceId().list());

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricDetailQuery().orderByTime().list());

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricDetailQuery().orderByVariableName().list());

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricDetailQuery().orderByVariableRevision().list());

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
            .isThrownBy(() -> historyService.createHistoricDetailQuery().orderByVariableType().list());
    }

    @Deployment
    public void testHistoricTaskInstanceVariableUpdates() {
        String processInstanceId = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest").getId();

        String taskId = taskService.createTaskQuery().singleResult().getId();

        runtimeService.setVariable(processInstanceId,
                                   "deadline",
                                   "yesterday");

        taskService.setVariableLocal(taskId,
                                     "bucket",
                                     "23c");
        taskService.setVariableLocal(taskId,
                                     "mop",
                                     "37i");

        taskService.complete(taskId);

        assertEquals(1,
                     historyService.createHistoricTaskInstanceQuery().count());

        List<HistoricDetail> historicTaskVariableUpdates = historyService.createHistoricDetailQuery().taskId(taskId).variableUpdates().orderByVariableName().asc().list();

        assertEquals(2,
                     historicTaskVariableUpdates.size());

        historyService.deleteHistoricTaskInstance(taskId);

        // Check if the variable updates have been removed as well
        historicTaskVariableUpdates = historyService.createHistoricDetailQuery().taskId(taskId).variableUpdates().orderByVariableName().asc().list();

        assertThat(historicTaskVariableUpdates.size()).isEqualTo(0);
    }

    // ACT-592
    @Deployment
    public void testSetVariableOnProcessInstanceWithTimer() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerVariablesProcess");
        runtimeService.setVariable(processInstance.getId(),
                                   "myVar",
                                   123456L);
        assertThat(runtimeService.getVariable(processInstance.getId(),
                                                "myVar")).isEqualTo(123456L);
    }

    @Deployment
    public void testDeleteHistoricProcessInstance() {
        // Start process-instance with some variables set
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("processVar",
                 123L);
        vars.put("anotherProcessVar",
                 new DummySerializable());

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest",
                                                                                   vars);
        assertThat(processInstance).isNotNull();

        // Set 2 task properties
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        taskService.setVariableLocal(task.getId(),
                                     "taskVar",
                                     45678);
        taskService.setVariableLocal(task.getId(),
                                     "anotherTaskVar",
                                     "value");

        // Finish the task, this end the process-instance
        taskService.complete(task.getId());

        assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);
        assertThat(historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(3);
        assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(4);
        assertThat(historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(4);
        assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(1);

        // Delete the historic process-instance
        historyService.deleteHistoricProcessInstance(processInstance.getId());

        // Verify no traces are left in the history tables
        assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).count()).isEqualTo(0);

        assertThatExceptionOfType(ActivitiException.class)
            .as("Exception expected when deleting process-instance that is still running")
            .isThrownBy(() -> historyService.deleteHistoricProcessInstance("unexisting"))
            .withMessageContaining("No historic process instance found with id: unexisting");
    }

    @Deployment
    public void testDeleteRunningHistoricProcessInstance() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");
        assertThat(processInstance).isNotNull();

        assertThatExceptionOfType(ActivitiException.class)
            .as("Exception expected when deleting process-instance that is still running")
            .isThrownBy(() -> historyService.deleteHistoricProcessInstance(processInstance.getId()))
            .withMessageContaining("Process instance is still running, cannot delete historic process instance");
    }

    @Deployment
    public void testHistoricTaskInstanceQueryTaskVariableValueEquals() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

        // Set some variables on the task
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("longVar",
                      12345L);
        variables.put("shortVar",
                      (short) 123);
        variables.put("integerVar",
                      1234);
        variables.put("stringVar",
                      "stringValue");
        variables.put("booleanVar",
                      true);
        Date date = Calendar.getInstance().getTime();
        variables.put("dateVar",
                      date);
        variables.put("nullVar",
                      null);

        taskService.setVariablesLocal(task.getId(),
                                      variables);

        // Validate all variable-updates are present in DB
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().taskId(task.getId()).count()).isEqualTo(7);

        // Query Historic task instances based on variable
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("longVar",
                                                                                              12345L).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("shortVar",
                                                                                              (short) 123).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("integerVar",
                                                                                              1234).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("stringVar",
                                                                                              "stringValue").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("booleanVar",
                                                                                              true).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("dateVar",
                                                                                              date).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("nullVar",
                                                                                              null).count()).isEqualTo(1);

        // Update the variables
        variables.put("longVar",
                      67890L);
        variables.put("shortVar",
                      (short) 456);
        variables.put("integerVar",
                      5678);
        variables.put("stringVar",
                      "updatedStringValue");
        variables.put("booleanVar",
                      false);
        Calendar otherCal = Calendar.getInstance();
        otherCal.add(Calendar.DAY_OF_MONTH,
                     1);
        Date otherDate = otherCal.getTime();
        variables.put("dateVar",
                      otherDate);
        variables.put("nullVar",
                      null);

        taskService.setVariablesLocal(task.getId(),
                                      variables);

        // Validate all variable-updates are present in DB
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().taskId(task.getId()).count()).isEqualTo(14);

        // Previous values should NOT match
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("longVar",
                                                                                              12345L).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("shortVar",
                                                                                              (short) 123).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("integerVar",
                                                                                              1234).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("stringVar",
                                                                                              "stringValue").count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("booleanVar",
                                                                                              true).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("dateVar",
                                                                                              date).count()).isEqualTo(0);

        // New values should match
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("longVar",
                                                                                              67890L).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("shortVar",
                                                                                              (short) 456).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("integerVar",
                                                                                              5678).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("stringVar",
                                                                                              "updatedStringValue").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("booleanVar",
                                                                                              false).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("dateVar",
                                                                                              otherDate).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("nullVar",
                                                                                              null).count()).isEqualTo(1);
    }

    @Deployment
    public void testHistoricTaskInstanceQueryProcessVariableValueEquals() throws Exception {
        // Set some variables on the process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("longVar",
                      12345L);
        variables.put("shortVar",
                      (short) 123);
        variables.put("integerVar",
                      1234);
        variables.put("stringVar",
                      "stringValue");
        variables.put("booleanVar",
                      true);
        Date date = Calendar.getInstance().getTime();
        variables.put("dateVar",
                      date);
        variables.put("nullVar",
                      null);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest",
                                                                                   variables);
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

        // Validate all variable-updates are present in DB
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count()).isEqualTo(7);

        // Query Historic task instances based on process variable
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar",
                                                                                                 12345L).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("shortVar",
                                                                                                 (short) 123).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("integerVar",
                                                                                                 1234).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("stringVar",
                                                                                                 "stringValue").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("booleanVar",
                                                                                                 true).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("dateVar",
                                                                                                 date).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("nullVar",
                                                                                                 null).count()).isEqualTo(1);

        // Update the variables
        variables.put("longVar",
                      67890L);
        variables.put("shortVar",
                      (short) 456);
        variables.put("integerVar",
                      5678);
        variables.put("stringVar",
                      "updatedStringValue");
        variables.put("booleanVar",
                      false);
        Calendar otherCal = Calendar.getInstance();
        otherCal.add(Calendar.DAY_OF_MONTH,
                     1);
        Date otherDate = otherCal.getTime();
        variables.put("dateVar",
                      otherDate);
        variables.put("nullVar",
                      null);

        runtimeService.setVariables(processInstance.getId(),
                                    variables);

        // Validate all variable-updates are present in DB
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count()).isEqualTo(14);

        // Previous values should NOT match
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar",
                                                                                                 12345L).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("shortVar",
                                                                                                 (short) 123).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("integerVar",
                                                                                                 1234).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("stringVar",
                                                                                                 "stringValue").count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("booleanVar",
                                                                                                 true).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("dateVar",
                                                                                                 date).count()).isEqualTo(0);

        // New values should match
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar",
                                                                                                 67890L).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("shortVar",
                                                                                                 (short) 456).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("integerVar",
                                                                                                 5678).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("stringVar",
                                                                                                 "updatedStringValue").count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("booleanVar",
                                                                                                 false).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("dateVar",
                                                                                                 otherDate).count()).isEqualTo(1);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("nullVar",
                                                                                                 null).count()).isEqualTo(1);

        // Set a task-variables, shouldn't affect the process-variable matches
        taskService.setVariableLocal(task.getId(),
                                     "longVar",
                                     9999L);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar",
                                                                                                 9999L).count()).isEqualTo(0);
        assertThat(historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar",
                                                                                                 67890L).count()).isEqualTo(1);
    }

    @Deployment
    public void testHistoricProcessInstanceVariableValueEquals() throws Exception {
        // Set some variables on the process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("longVar",
                      12345L);
        variables.put("shortVar",
                      (short) 123);
        variables.put("integerVar",
                      1234);
        variables.put("stringVar",
                      "stringValue");
        variables.put("booleanVar",
                      true);
        Date date = Calendar.getInstance().getTime();
        variables.put("dateVar",
                      date);
        variables.put("nullVar",
                      null);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricProcessInstanceTest",
                                                                                   variables);

        // Validate all variable-updates are present in DB
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count()).isEqualTo(7);

        // Query Historic process instances based on process variable
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("longVar",
                                                                                             12345L).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("shortVar",
                                                                                             (short) 123).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("integerVar",
                                                                                             1234).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar",
                                                                                             "stringValue").count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("booleanVar",
                                                                                             true).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar",
                                                                                             date).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("nullVar",
                                                                                             null).count()).isEqualTo(1);

        // Update the variables
        variables.put("longVar",
                      67890L);
        variables.put("shortVar",
                      (short) 456);
        variables.put("integerVar",
                      5678);
        variables.put("stringVar",
                      "updatedStringValue");
        variables.put("booleanVar",
                      false);
        Calendar otherCal = Calendar.getInstance();
        otherCal.add(Calendar.DAY_OF_MONTH,
                     1);
        Date otherDate = otherCal.getTime();
        variables.put("dateVar",
                      otherDate);
        variables.put("nullVar",
                      null);

        runtimeService.setVariables(processInstance.getId(),
                                    variables);

        // Validate all variable-updates are present in DB
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count()).isEqualTo(14);

        // Previous values should NOT match
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("longVar",
                                                                                             12345L).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("shortVar",
                                                                                             (short) 123).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("integerVar",
                                                                                             1234).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar",
                                                                                             "stringValue").count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("booleanVar",
                                                                                             true).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar",
                                                                                             date).count()).isEqualTo(0);

        // New values should match
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("longVar",
                                                                                             67890L).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("shortVar",
                                                                                             (short) 456).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("integerVar",
                                                                                             5678).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar",
                                                                                             "updatedStringValue").count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("booleanVar",
                                                                                             false).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar",
                                                                                             otherDate).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("nullVar",
                                                                                             null).count()).isEqualTo(1);
    }

    @Deployment(resources = {"org/activiti/standalone/history/FullHistoryTest.testHistoricProcessInstanceVariableValueEquals.bpmn20.xml"})
    public void testHistoricProcessInstanceVariableValueNotEquals() throws Exception {
        // Set some variables on the process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("longVar",
                      12345L);
        variables.put("shortVar",
                      (short) 123);
        variables.put("integerVar",
                      1234);
        variables.put("stringVar",
                      "stringValue");
        variables.put("booleanVar",
                      true);
        Date date = Calendar.getInstance().getTime();
        Calendar otherCal = Calendar.getInstance();
        otherCal.add(Calendar.DAY_OF_MONTH,
                     1);
        Date otherDate = otherCal.getTime();
        variables.put("dateVar",
                      date);
        variables.put("nullVar",
                      null);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricProcessInstanceTest",
                                                                                   variables);

        // Validate all variable-updates are present in DB
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count()).isEqualTo(7);

        // Query Historic process instances based on process variable, shouldn't match
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("longVar",
                                                                                                12345L).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("shortVar",
                                                                                                (short) 123).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("integerVar",
                                                                                                1234).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar",
                                                                                                "stringValue").count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("booleanVar",
                                                                                                true).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar",
                                                                                                date).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("nullVar",
                                                                                                null).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("nullVar",
                                                                                                null).count()).isEqualTo(0);

        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("longVar",
                                                                                                67890L).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("shortVar",
                                                                                                (short) 456).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("integerVar",
                                                                                                5678).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar",
                                                                                                "updatedStringValue").count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("booleanVar",
                                                                                                false).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar",
                                                                                                otherDate).count()).isEqualTo(1);

        // Update the variables
        variables.put("longVar",
                      67890L);
        variables.put("shortVar",
                      (short) 456);
        variables.put("integerVar",
                      5678);
        variables.put("stringVar",
                      "updatedStringValue");
        variables.put("booleanVar",
                      false);
        variables.put("dateVar",
                      otherDate);
        variables.put("nullVar",
                      null);

        runtimeService.setVariables(processInstance.getId(),
                                    variables);

        // Validate all variable-updates are present in DB
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count()).isEqualTo(14);

        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("longVar",
                                                                                                12345L).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("shortVar",
                                                                                                (short) 123).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("integerVar",
                                                                                                1234).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar",
                                                                                                "stringValue").count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("booleanVar",
                                                                                                true).count()).isEqualTo(1);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar",
                                                                                                date).count()).isEqualTo(1);

        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("longVar",
                                                                                                67890L).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("shortVar",
                                                                                                (short) 456).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("integerVar",
                                                                                                5678).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar",
                                                                                                "updatedStringValue").count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("booleanVar",
                                                                                                false).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar",
                                                                                                otherDate).count()).isEqualTo(0);
        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("nullVar",
                                                                                                null).count()).isEqualTo(0);
    }

    @Deployment(resources = {"org/activiti/standalone/history/FullHistoryTest.testHistoricProcessInstanceVariableValueEquals.bpmn20.xml"})
    public void testHistoricProcessInstanceVariableValueLessThanAndGreaterThan() throws Exception {
        // Set some variables on the process instance
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("longVar",
                      12345L);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricProcessInstanceTest",
                                                                                   variables);

        // Validate all variable-updates are present in DB
        assertThat(historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count()).isEqualTo(1);

        assertThat(historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("longVar",
                                                                                                  12345L).count()).isEqualTo(0);
    }

    @Deployment(resources = {"org/activiti/standalone/history/FullHistoryTest.testVariableUpdatesAreLinkedToActivity.bpmn20.xml"})
    public void testVariableUpdatesLinkedToActivity() throws Exception {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("ProcessWithSubProcess");

        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("test",
                      "1");
        taskService.complete(task.getId(),
                             variables);

        // now we are in the subprocess
        task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        variables.clear();
        variables.put("test",
                      "2");
        taskService.complete(task.getId(),
                             variables);

        // now we are ended
        assertProcessEnded(pi.getId());

        // check history
        List<HistoricDetail> updates = historyService.createHistoricDetailQuery().variableUpdates().list();
        assertThat(updates.size()).isEqualTo(2);

        Map<String, HistoricVariableUpdate> updatesMap = new HashMap<String, HistoricVariableUpdate>();
        HistoricVariableUpdate update = (HistoricVariableUpdate) updates.get(0);
        updatesMap.put((String) update.getValue(),
                       update);
        update = (HistoricVariableUpdate) updates.get(1);
        updatesMap.put((String) update.getValue(),
                       update);

        HistoricVariableUpdate update1 = updatesMap.get("1");
        HistoricVariableUpdate update2 = updatesMap.get("2");

        assertThat(update1.getActivityInstanceId()).isNotNull();
        assertThat(update1.getExecutionId()).isNotNull();
        HistoricActivityInstance historicActivityInstance1 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update1.getActivityInstanceId()).singleResult();
        assertThat(historicActivityInstance1.getActivityId()).isEqualTo("usertask1");

        assertThat(update2.getActivityInstanceId()).isNotNull();
        HistoricActivityInstance historicActivityInstance2 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update2.getActivityInstanceId()).singleResult();
        assertThat(historicActivityInstance2.getActivityId()).isEqualTo("usertask2");

    /*
     * This is OK! The variable is set on the root execution, on a execution never run through the activity, where the process instances stands when calling the set Variable. But the ActivityId of
     * this flow node is used. So the execution id's doesn't have to be equal.
     *
     * execution id: On which execution it was set activity id: in which activity was the process instance when setting the variable
     */
        assertThat(historicActivityInstance2.getExecutionId().equals(update2.getExecutionId())).isFalse();
    }

    @Deployment(resources = {"org/activiti/standalone/jpa/JPAVariableTest.testQueryJPAVariable.bpmn20.xml"})
    public void testReadJpaVariableValueFromHistoricVariableUpdate() {

        EntityManagerSessionFactory entityManagerSessionFactory = (EntityManagerSessionFactory) processEngineConfiguration.getSessionFactories().get(EntityManagerSession.class);

        EntityManagerFactory entityManagerFactory = entityManagerSessionFactory.getEntityManagerFactory();

        String executionId = runtimeService.startProcessInstanceByKey("JPAVariableProcess").getProcessInstanceId();
        String variableName = "name";

        FieldAccessJPAEntity entity = new FieldAccessJPAEntity();
        entity.setId(1L);
        entity.setValue("Test");

        EntityManager manager = entityManagerFactory.createEntityManager();
        manager.getTransaction().begin();
        manager.persist(entity);
        manager.flush();
        manager.getTransaction().commit();
        manager.close();

        Task task = taskService.createTaskQuery().processInstanceId(executionId).taskName("my task").singleResult();

        runtimeService.setVariable(executionId,
                                   variableName,
                                   entity);
        taskService.complete(task.getId());

        List<HistoricDetail> variableUpdates = historyService.createHistoricDetailQuery().processInstanceId(executionId).variableUpdates().list();

        assertThat(variableUpdates.size()).isEqualTo(1);
        HistoricVariableUpdate update = (HistoricVariableUpdate) variableUpdates.get(0);
        assertThat(update.getValue()).isNotNull();
        assertThat(update.getValue()).isInstanceOf(FieldAccessJPAEntity.class);

        assertThat(((FieldAccessJPAEntity) update.getValue()).getId()).isEqualTo(entity.getId());
    }

    /**
     * Test confirming fix for ACT-1731
     */
    @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
    public void testQueryHistoricTaskIncludeBinaryVariable() throws Exception {
        // Start process with a binary variable
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   singletonMap("binaryVariable",
                                                                                                            (Object) "It is I, le binary".getBytes()));
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task).isNotNull();
        taskService.setVariableLocal(task.getId(),
                                     "binaryTaskVariable",
                                     (Object) "It is I, le binary".getBytes());

        // Complete task
        taskService.complete(task.getId());

        // Query task, including processVariables
        HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).includeProcessVariables().singleResult();
        assertThat(historicTask).isNotNull();
        assertThat(historicTask.getProcessVariables()).isNotNull();
        byte[] bytes = (byte[]) historicTask.getProcessVariables().get("binaryVariable");
        assertThat(new String(bytes)).isEqualTo("It is I, le binary");

        // Query task, including taskVariables
        historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).includeTaskLocalVariables().singleResult();
        assertThat(historicTask).isNotNull();
        assertThat(historicTask.getTaskLocalVariables()).isNotNull();
        bytes = (byte[]) historicTask.getTaskLocalVariables().get("binaryTaskVariable");
        assertThat(new String(bytes)).isEqualTo("It is I, le binary");
    }

    /**
     * Test confirming fix for ACT-1731
     */
    @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
    public void testQueryHistoricProcessInstanceIncludeBinaryVariable() throws Exception {
        // Start process with a binary variable
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess",
                                                                                   singletonMap("binaryVariable",
                                                                                        "It is I, le binary".getBytes()));
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        assertThat(task).isNotNull();

        // Complete task to end process
        taskService.complete(task.getId());

        // Query task, including processVariables
        HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).includeProcessVariables().singleResult();
        assertThat(historicProcess).isNotNull();
        assertThat(historicProcess.getProcessVariables()).isNotNull();
        byte[] bytes = (byte[]) historicProcess.getProcessVariables().get("binaryVariable");
        assertThat(new String(bytes)).isEqualTo("It is I, le binary");
    }

    // Test for https://activiti.atlassian.net/browse/ACT-2186
    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testHistoricVariableRemovedWhenRuntimeVariableIsRemoved() throws InterruptedException {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("var1", "Hello");
        vars.put("var2", "World");
        vars.put("var3", "!");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

        // Verify runtime
        assertThat(runtimeService.getVariables(processInstance.getId())).hasSize(3);
        assertThat(runtimeService.getVariables(processInstance.getId(), asList("var1", "var2", "var3"))).hasSize(3);
        assertThat(runtimeService.getVariable(processInstance.getId(), "var2")).isNotNull();

        // Verify history
        assertThat(historyService.createHistoricVariableInstanceQuery().list()).hasSize(3);
        assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("var2").singleResult()).isNotNull();

        // Verify historic details
        List<HistoricDetail> details = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).variableUpdates().orderByTime().asc().list();
        assertThat(details).hasSize(3); // 3 vars
        for (HistoricDetail historicDetail : details) {
            assertThat(((HistoricVariableUpdate) historicDetail).getValue()).isNotNull();
        }

        // Remove one variable
        Thread.sleep(800);
        runtimeService.removeVariable(processInstance.getId(), "var2");

        // Verify runtime
        assertThat(runtimeService.getVariables(processInstance.getId())).hasSize(2);
        assertThat(runtimeService.getVariables(processInstance.getId(), asList("var1", "var2", "var3"))).hasSize(2);
        assertThat(runtimeService.getVariable(processInstance.getId(), "var2")).isNull();

        // Verify history
        assertThat(historyService.createHistoricVariableInstanceQuery().list()).hasSize(2);
        assertThat(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("var2").singleResult()).isNull();

        // Verify historic details
        details = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).variableUpdates().orderByTime().asc().list();
        assertThat(details).hasSize(4); // 3 vars + 1 delete

        // The last entry should be the delete
        for (int i = 0; i < details.size(); i++) {
            if (i != 3) {
                assertThat(((HistoricVariableUpdate) details.get(i)).getValue()).isNotNull();
            } else if (i == 3) {
                assertThat(((HistoricVariableUpdate) details.get(i)).getValue()).isNull();
            }
        }
    }
}
