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

package org.activiti.standalone.history;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.api.runtime.DummySerializable;
import org.activiti.engine.test.history.SerializableVariable;
import org.activiti.standalone.jpa.FieldAccessJPAEntity;


/**
 * @author Tom Baeyens
 * @author Frederik Heremans
 * @author Joram Barrez
 * @author Christian Lipphardt (camunda)
 */
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
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveTask", variables);
    runtimeService.setVariable(processInstance.getId(), "number", "two");
    runtimeService.setVariable(processInstance.getId(), "bytes", ":-)".getBytes());
    
    // Start-task should be added to history
    HistoricActivityInstance historicStartEvent = historyService.createHistoricActivityInstanceQuery()
      .processInstanceId(processInstance.getId())
      .activityId("theStart")
      .singleResult();
    assertNotNull(historicStartEvent);
    
    HistoricActivityInstance waitStateActivity = historyService.createHistoricActivityInstanceQuery()
      .processInstanceId(processInstance.getId())
      .activityId("waitState")
      .singleResult();
    assertNotNull(waitStateActivity);
    
    HistoricActivityInstance serviceTaskActivity = historyService.createHistoricActivityInstanceQuery()
      .processInstanceId(processInstance.getId())
      .activityId("serviceTask")
      .singleResult();
    assertNotNull(serviceTaskActivity);
    
    List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
      .orderByVariableName().asc()
      .orderByVariableRevision().asc()
      .list();
    
    assertEquals(10, historicDetails.size());
    
    HistoricVariableUpdate historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(0);
    assertEquals("bytes", historicVariableUpdate.getVariableName());
    assertEquals(":-(", new String((byte[])historicVariableUpdate.getValue()));
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(historicStartEvent.getId(), historicVariableUpdate.getActivityInstanceId());
    
    // Variable is updated when process was in waitstate
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(1);
    assertEquals("bytes", historicVariableUpdate.getVariableName());
    assertEquals(":-)", new String((byte[])historicVariableUpdate.getValue()));
    assertEquals(1, historicVariableUpdate.getRevision());
    assertEquals(waitStateActivity.getId(), historicVariableUpdate.getActivityInstanceId());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(2);
    assertEquals("character", historicVariableUpdate.getVariableName());
    assertEquals("a", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(historicStartEvent.getId(), historicVariableUpdate.getActivityInstanceId());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(3);
    assertEquals("number", historicVariableUpdate.getVariableName());
    assertEquals("one", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(historicStartEvent.getId(), historicVariableUpdate.getActivityInstanceId());
    
    // Variable is updated when process was in waitstate
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(4);
    assertEquals("number", historicVariableUpdate.getVariableName());
    assertEquals("two", historicVariableUpdate.getValue());
    assertEquals(1, historicVariableUpdate.getRevision());
    assertEquals(waitStateActivity.getId(), historicVariableUpdate.getActivityInstanceId());
    
    // Variable set from process-start execution listener
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(5);
    assertEquals("zVar1", historicVariableUpdate.getVariableName());
    assertEquals("Event: start", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(historicStartEvent.getId(), historicVariableUpdate.getActivityInstanceId());
    
    // Variable set from transition take execution listener
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(6);
    assertEquals("zVar2", historicVariableUpdate.getVariableName());
    assertEquals("Event: take", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertNull(historicVariableUpdate.getActivityInstanceId());
    
    // Variable set from activity start execution listener on the servicetask
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(7);
    assertEquals("zVar3", historicVariableUpdate.getVariableName());
    assertEquals("Event: start", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(serviceTaskActivity.getId(), historicVariableUpdate.getActivityInstanceId());
    
    // Variable set from activity end execution listener on the servicetask
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(8);
    assertEquals("zVar4", historicVariableUpdate.getVariableName());
    assertEquals("Event: end", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(serviceTaskActivity.getId(), historicVariableUpdate.getActivityInstanceId());
    
    // Variable set from service-task
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(9);
    assertEquals("zzz", historicVariableUpdate.getVariableName());
    assertEquals(123456789L, historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    assertEquals(serviceTaskActivity.getId(), historicVariableUpdate.getActivityInstanceId());
    
    // trigger receive task
    runtimeService.signal(processInstance.getId());
    assertProcessEnded(processInstance.getId());
    
    // check for historic process variables set
    HistoricVariableInstanceQuery historicProcessVariableQuery = historyService
            .createHistoricVariableInstanceQuery()
            .orderByVariableName().asc();
    
    assertEquals(8, historicProcessVariableQuery.count());
    
    List<HistoricVariableInstance> historicVariables = historicProcessVariableQuery.list();
    
    // Variable status when process is finished
    HistoricVariableInstance historicVariable = historicVariables.get(0);
    assertEquals("bytes", historicVariable.getVariableName());
    assertEquals(":-)", new String((byte[])historicVariable.getValue()));
    assertNotNull(historicVariable.getCreateTime());
    assertNotNull(historicVariable.getLastUpdatedTime());
    
    historicVariable = historicVariables.get(1);
    assertEquals("character", historicVariable.getVariableName());
    assertEquals("a", historicVariable.getValue());
    assertNotNull(historicVariable.getCreateTime());
    assertNotNull(historicVariable.getLastUpdatedTime());
    
    historicVariable = historicVariables.get(2);
    assertEquals("number", historicVariable.getVariableName());
    assertEquals("two", historicVariable.getValue());
    assertNotNull(historicVariable.getCreateTime());
    assertNotNull(historicVariable.getLastUpdatedTime());
    assertNotSame(historicVariable.getCreateTime(), historicVariable.getLastUpdatedTime());
    
    historicVariable = historicVariables.get(3);
    assertEquals("zVar1", historicVariable.getVariableName());
    assertEquals("Event: start", historicVariable.getValue());
    assertNotNull(historicVariable.getCreateTime());
    assertNotNull(historicVariable.getLastUpdatedTime());
    
    historicVariable = historicVariables.get(4);
    assertEquals("zVar2", historicVariable.getVariableName());
    assertEquals("Event: take", historicVariable.getValue());
    assertNotNull(historicVariable.getCreateTime());
    assertNotNull(historicVariable.getLastUpdatedTime());
    
    historicVariable = historicVariables.get(5);
    assertEquals("zVar3", historicVariable.getVariableName());
    assertEquals("Event: start", historicVariable.getValue());
    assertNotNull(historicVariable.getCreateTime());
    assertNotNull(historicVariable.getLastUpdatedTime());
    
    historicVariable = historicVariables.get(6);
    assertEquals("zVar4", historicVariable.getVariableName());
    assertEquals("Event: end", historicVariable.getValue());
    assertNotNull(historicVariable.getCreateTime());
    assertNotNull(historicVariable.getLastUpdatedTime());
    
    historicVariable = historicVariables.get(7);
    assertEquals("zzz", historicVariable.getVariableName());
    assertEquals(123456789L, historicVariable.getValue());
    assertNotNull(historicVariable.getCreateTime());
    assertNotNull(historicVariable.getLastUpdatedTime());
    
    historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLike("number", "tw%").singleResult();
    assertNotNull(historicVariable);
    assertEquals("number", historicVariable.getVariableName());
    assertEquals("two", historicVariable.getValue());
    
    historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("number", "TW%").singleResult();
    assertNotNull(historicVariable);
    assertEquals("number", historicVariable.getVariableName());
    assertEquals("two", historicVariable.getValue());
    
    historicVariable = historyService.createHistoricVariableInstanceQuery().variableValueLikeIgnoreCase("number", "TW2%").singleResult();
    assertNull(historicVariable);
  }
  
  @Deployment(resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableInstanceQueryTaskVariables() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("variable", "setFromProcess");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());
    
    Task activeTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(activeTask);
    taskService.setVariableLocal(activeTask.getId(), "variable", "setFromTask");

    // Check if additional variable is available in history, task-local
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().taskId(activeTask.getId()).count());
    assertEquals("setFromTask", historyService.createHistoricVariableInstanceQuery().taskId(activeTask.getId()).singleResult().getValue());
    assertEquals(activeTask.getId(), historyService.createHistoricVariableInstanceQuery().taskId(activeTask.getId()).singleResult().getTaskId());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().excludeTaskVariables().count());
    
    // Test null task-id
    try 
    {
      historyService.createHistoricVariableInstanceQuery().taskId(null).singleResult();
      fail("Exception expected");
    }
    catch(ActivitiIllegalArgumentException ae)
    {
      assertEquals("taskId is null", ae.getMessage());
    }
    
    // Test invalid usage of taskId together with excludeTaskVariables
    try 
    {
      historyService.createHistoricVariableInstanceQuery().taskId("123").excludeTaskVariables().singleResult();
      fail("Exception expected");
    }
    catch(ActivitiIllegalArgumentException ae)
    {
      assertEquals("Cannot use taskId together with excludeTaskVariables", ae.getMessage());
    }
    
    try 
    {
      historyService.createHistoricVariableInstanceQuery().excludeTaskVariables().taskId("123").singleResult();
      fail("Exception expected");
    }
    catch(ActivitiIllegalArgumentException ae)
    {
      assertEquals("Cannot use taskId together with excludeTaskVariables", ae.getMessage());
    }
  }
  
  @Deployment(resources="org/activiti/standalone/history/FullHistoryTest.testVariableUpdates.bpmn20.xml")
  public void testHistoricVariableInstanceQuery() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("process", "one");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveTask", variables);
    runtimeService.signal(processInstance.getProcessInstanceId());
    
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableName("process").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "one").count());    
    
    Map<String, Object> variables2 = new HashMap<String, Object>();
    variables2.put("process", "two");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("receiveTask", variables2);
    runtimeService.signal(processInstance2.getProcessInstanceId());
    
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().variableName("process").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "one").count());    
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "two").count());        
    
    HistoricVariableInstance historicProcessVariable = historyService.createHistoricVariableInstanceQuery().variableValueEquals("process", "one").singleResult();
    assertEquals("process", historicProcessVariable.getVariableName());
    assertEquals("one", historicProcessVariable.getValue());
    
    Map<String, Object> variables3 = new HashMap<String, Object>();
    variables3.put("long", 1000l);
    variables3.put("double", 25.43d);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("receiveTask", variables3);
    runtimeService.signal(processInstance3.getProcessInstanceId());
    
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableName("long").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("long", 1000l).count());    
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableName("double").count());
    assertEquals(1, historyService.createHistoricVariableInstanceQuery().variableValueEquals("double",  25.43d).count());    

  }
 
  @Deployment
  public void testHistoricVariableUpdatesAllTypes() throws Exception {
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS");
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "initial value");
    
    Date startedDate = sdf.parse("01/01/2001 01:23:45 000");
    
    // In the javaDelegate, the current time is manipulated
    Date updatedDate = sdf.parse("01/01/2001 01:23:46 000");
    
    processEngineConfiguration.getClock().setCurrentTime(startedDate);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricVariableUpdateProcess", variables);
    
    List<HistoricDetail> details = historyService.createHistoricDetailQuery()
      .variableUpdates()
      .processInstanceId(processInstance.getId())
      .orderByVariableName().asc()
      .orderByTime().asc()
      .list();
    
    // 8 variable updates should be present, one performed when starting process
    // the other 7 are set in VariableSetter serviceTask
    assertEquals(9, details.size());
    
    // Since we order by varName, first entry should be aVariable update from startTask
    HistoricVariableUpdate startVarUpdate = (HistoricVariableUpdate) details.get(0);
    assertEquals("aVariable", startVarUpdate.getVariableName());
    assertEquals("initial value", startVarUpdate.getValue());
    assertEquals(0, startVarUpdate.getRevision());
    assertEquals(processInstance.getId(), startVarUpdate.getProcessInstanceId());
    // Date should the the one set when starting
    assertEquals(startedDate, startVarUpdate.getTime());
    
    HistoricVariableUpdate updatedStringVariable = (HistoricVariableUpdate) details.get(1);
    assertEquals("aVariable", updatedStringVariable.getVariableName());
    assertEquals("updated value", updatedStringVariable.getValue());
    assertEquals(processInstance.getId(), updatedStringVariable.getProcessInstanceId());
    // Date should be the updated date
    assertEquals(updatedDate, updatedStringVariable.getTime());
    
    HistoricVariableUpdate intVariable = (HistoricVariableUpdate) details.get(2);
    assertEquals("bVariable", intVariable.getVariableName());
    assertEquals(123, intVariable.getValue());
    assertEquals(processInstance.getId(), intVariable.getProcessInstanceId());
    assertEquals(updatedDate, intVariable.getTime());
    
    HistoricVariableUpdate longVariable = (HistoricVariableUpdate) details.get(3);
    assertEquals("cVariable", longVariable.getVariableName());
    assertEquals(12345L, longVariable.getValue());
    assertEquals(processInstance.getId(), longVariable.getProcessInstanceId());
    assertEquals(updatedDate, longVariable.getTime());
    
    HistoricVariableUpdate doubleVariable = (HistoricVariableUpdate) details.get(4);
    assertEquals("dVariable", doubleVariable.getVariableName());
    assertEquals(1234.567, doubleVariable.getValue());
    assertEquals(processInstance.getId(), doubleVariable.getProcessInstanceId());
    assertEquals(updatedDate, doubleVariable.getTime());
    
    HistoricVariableUpdate shortVariable = (HistoricVariableUpdate) details.get(5);
    assertEquals("eVariable", shortVariable.getVariableName());
    assertEquals((short)12, shortVariable.getValue());
    assertEquals(processInstance.getId(), shortVariable.getProcessInstanceId());
    assertEquals(updatedDate, shortVariable.getTime());
    
    HistoricVariableUpdate dateVariable = (HistoricVariableUpdate) details.get(6);
    assertEquals("fVariable", dateVariable.getVariableName());
    assertEquals(sdf.parse("01/01/2001 01:23:45 678"), dateVariable.getValue());
    assertEquals(processInstance.getId(), dateVariable.getProcessInstanceId());
    assertEquals(updatedDate, dateVariable.getTime());
    
    HistoricVariableUpdate serializableVariable = (HistoricVariableUpdate) details.get(7);
    assertEquals("gVariable", serializableVariable.getVariableName());
    assertEquals(new SerializableVariable("hello hello"), serializableVariable.getValue());
    assertEquals(processInstance.getId(), serializableVariable.getProcessInstanceId());
    assertEquals(updatedDate, serializableVariable.getTime());
    
    HistoricVariableUpdate byteArrayVariable = (HistoricVariableUpdate) details.get(8);
    assertEquals("hVariable", byteArrayVariable.getVariableName());
    assertEquals(";-)", new String((byte[])byteArrayVariable.getValue()));
    assertEquals(processInstance.getId(), byteArrayVariable.getProcessInstanceId());
    assertEquals(updatedDate, byteArrayVariable.getTime());
    
    // end process instance
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    assertProcessEnded(processInstance.getId());
    
    // check for historic process variables set
    HistoricVariableInstanceQuery historicProcessVariableQuery = historyService
            .createHistoricVariableInstanceQuery()
            .orderByVariableName().asc();
    
    assertEquals(8, historicProcessVariableQuery.count());
    
    List<HistoricVariableInstance> historicVariables = historicProcessVariableQuery.list();
    
 // Variable status when process is finished
    HistoricVariableInstance historicVariable = historicVariables.get(0);
    assertEquals("aVariable", historicVariable.getVariableName());
    assertEquals("updated value", historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());
    
    historicVariable = historicVariables.get(1);
    assertEquals("bVariable", historicVariable.getVariableName());
    assertEquals(123, historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());
    
    historicVariable = historicVariables.get(2);
    assertEquals("cVariable", historicVariable.getVariableName());
    assertEquals(12345L, historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());
    
    historicVariable = historicVariables.get(3);
    assertEquals("dVariable", historicVariable.getVariableName());
    assertEquals(1234.567, historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());
    
    historicVariable = historicVariables.get(4);
    assertEquals("eVariable", historicVariable.getVariableName());
    assertEquals((short) 12, historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());
    
    historicVariable = historicVariables.get(5);
    assertEquals("fVariable", historicVariable.getVariableName());
    assertEquals(sdf.parse("01/01/2001 01:23:45 678"), historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());
    
    historicVariable = historicVariables.get(6);
    assertEquals("gVariable", historicVariable.getVariableName());
    assertEquals(new SerializableVariable("hello hello"), historicVariable.getValue());
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());
    
    historicVariable = historicVariables.get(7);
    assertEquals("hVariable", historicVariable.getVariableName());
    assertEquals(";-)", ";-)", new String((byte[])historicVariable.getValue()));
    assertEquals(processInstance.getId(), historicVariable.getProcessInstanceId());
  }
  
  @Deployment
  public void testHistoricFormProperties() throws Exception {
    Date startedDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS").parse("01/01/2001 01:23:46 000");
    
    processEngineConfiguration.getClock().setCurrentTime(startedDate);
    
    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("formProp1", "Activiti rocks");
    formProperties.put("formProp2", "12345");
    
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("historicFormPropertiesProcess").singleResult();
    
    ProcessInstance processInstance = formService.submitStartFormData(procDef.getId() , formProperties);
    
    // Submit form-properties on the created task
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    
    // Out execution only has a single activity waiting, the task
    List<String> activityIds = runtimeService.getActiveActivityIds(task.getExecutionId());
    assertNotNull(activityIds);
    assertEquals(1, activityIds.size());
    
    String taskActivityId = activityIds.get(0);
   
    // Submit form properties
    formProperties = new HashMap<String, String>();
    formProperties.put("formProp3", "Activiti still rocks!!!");
    formProperties.put("formProp4", "54321");
    formService.submitTaskFormData(task.getId(), formProperties);
    
    // 4 historic form properties should be created. 2 when process started, 2 when task completed
    List<HistoricDetail> props = historyService.createHistoricDetailQuery()
      .formProperties()
      .processInstanceId(processInstance.getId())
      .orderByFormPropertyId().asc()
      .list();
    
    HistoricFormProperty historicProperty1 = (HistoricFormProperty) props.get(0);
    assertEquals("formProp1", historicProperty1.getPropertyId());
    assertEquals("Activiti rocks", historicProperty1.getPropertyValue());
    assertEquals(startedDate, historicProperty1.getTime());
    assertEquals(processInstance.getId(), historicProperty1.getProcessInstanceId());
    assertNull(historicProperty1.getTaskId());
    
    assertNotNull(historicProperty1.getActivityInstanceId());
    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityInstanceId(historicProperty1.getActivityInstanceId()).singleResult();
    assertNotNull(historicActivityInstance);
    assertEquals("start", historicActivityInstance.getActivityId());
    
    HistoricFormProperty historicProperty2 = (HistoricFormProperty) props.get(1);
    assertEquals("formProp2", historicProperty2.getPropertyId());
    assertEquals("12345", historicProperty2.getPropertyValue());
    assertEquals(startedDate, historicProperty2.getTime());
    assertEquals(processInstance.getId(), historicProperty2.getProcessInstanceId());
    assertNull(historicProperty2.getTaskId());
    
    assertNotNull(historicProperty2.getActivityInstanceId());
    historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityInstanceId(historicProperty2.getActivityInstanceId()).singleResult();
    assertNotNull(historicActivityInstance);
    assertEquals("start", historicActivityInstance.getActivityId());
    
    HistoricFormProperty historicProperty3 = (HistoricFormProperty) props.get(2);
    assertEquals("formProp3", historicProperty3.getPropertyId());
    assertEquals("Activiti still rocks!!!", historicProperty3.getPropertyValue());
    assertEquals(startedDate, historicProperty3.getTime());
    assertEquals(processInstance.getId(), historicProperty3.getProcessInstanceId());
    String activityInstanceId = historicProperty3.getActivityInstanceId();
    historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityInstanceId(activityInstanceId).singleResult();
    assertNotNull(historicActivityInstance);
    assertEquals(taskActivityId, historicActivityInstance.getActivityId());
    assertNotNull(historicProperty3.getTaskId());
    
    HistoricFormProperty historicProperty4 = (HistoricFormProperty) props.get(3);
    assertEquals("formProp4", historicProperty4.getPropertyId());
    assertEquals("54321", historicProperty4.getPropertyValue());
    assertEquals(startedDate, historicProperty4.getTime());
    assertEquals(processInstance.getId(), historicProperty4.getProcessInstanceId());
    activityInstanceId = historicProperty4.getActivityInstanceId();
    historicActivityInstance = historyService.createHistoricActivityInstanceQuery().activityInstanceId(activityInstanceId).singleResult();
    assertNotNull(historicActivityInstance);
    assertEquals(taskActivityId, historicActivityInstance.getActivityId());
    assertNotNull(historicProperty4.getTaskId());

    assertEquals(4, props.size());
  }
  
  @Deployment(
    resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableQuery() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "activiti rocks!");
    variables.put("longVar", 12345L);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
   
    // Query on activity-instance, activity instance null will return all vars set when starting process
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().activityInstanceId(null).count());
    assertEquals(0, historyService.createHistoricDetailQuery().variableUpdates().activityInstanceId("unexisting").count());
    
    // Query on process-instance
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId("unexisting").count());

    // Query both process-instance and activity-instance
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates()
            .activityInstanceId(null)
            .processInstanceId(processInstance.getId()).count());
    
    // end process instance
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(1, tasks.size());
    taskService.complete(tasks.get(0).getId());
    assertProcessEnded(processInstance.getId());
    
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());
       
    // Query on process-instance
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().processInstanceId("unexisting").count());
  }
  
  @Deployment(
    resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableQueryExcludeTaskRelatedDetails() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "activiti rocks!");
    variables.put("longVar", 12345L);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // Set a local task-variable
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.setVariableLocal(task.getId(), "taskVar", "It is I, le Variable");

    // Query on process-instance
    assertEquals(3, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    
    // Query on process-instance, excluding task-details
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId())
      .excludeTaskDetails().count());
    
    // Check task-id precedence on excluding task-details
    assertEquals(1, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId())
            .excludeTaskDetails().taskId(task.getId()).count());
  }
  
  @Deployment(
    resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricFormPropertiesQuery() throws Exception {
    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("stringVar", "activiti rocks!");
    formProperties.put("longVar", "12345");
    
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult();
    ProcessInstance processInstance = formService.submitStartFormData(procDef.getId() , formProperties);
   
    // Query on activity-instance, activity instance null will return all vars set when starting process
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().activityInstanceId(null).count());
    assertEquals(0, historyService.createHistoricDetailQuery().formProperties().activityInstanceId("unexisting").count());
    
    // Query on process-instance
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().formProperties().processInstanceId("unexisting").count());

    // Complete the task by submitting the task properties
    Task task = taskService.createTaskQuery().singleResult();
    formProperties = new HashMap<String, String>();
    formProperties.put("taskVar", "task form property");
    formService.submitTaskFormData(task.getId(), formProperties);

    assertEquals(3, historyService.createHistoricDetailQuery().formProperties().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().formProperties().processInstanceId("unexisting").count());
  }
  
  
  
  @Deployment(
    resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableQuerySorting() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "activiti rocks!");
    variables.put("longVar", 12345L);
    
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
   
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().asc().count());
    
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().desc().count());
    
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().asc().list().size());
    
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().desc().list().size());
  }
  
  @Deployment(
    resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricFormPropertySorting() throws Exception {
    
    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("stringVar", "activiti rocks!");
    formProperties.put("longVar", "12345");
    
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult();
    formService.submitStartFormData(procDef.getId() , formProperties);
   
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().asc().count());
    
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().desc().count());
    
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().asc().list().size());
    
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().desc().list().size());
  }
  
  @Deployment
  public void testHistoricDetailQueryMixed() throws Exception {
    
    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("formProp1", "activiti rocks!");
    formProperties.put("formProp2", "12345");
    
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("historicDetailMixed").singleResult();
    ProcessInstance processInstance = formService.submitStartFormData(procDef.getId() , formProperties);
   
    List<HistoricDetail> details = historyService
      .createHistoricDetailQuery()
      .processInstanceId(processInstance.getId())
      .orderByVariableName().asc()
      .list();
   
    assertEquals(4, details.size());
    
    assertTrue(details.get(0) instanceof HistoricFormProperty);
    HistoricFormProperty formProp1 = (HistoricFormProperty) details.get(0);
    assertEquals("formProp1", formProp1.getPropertyId());
    assertEquals("activiti rocks!", formProp1.getPropertyValue());
    
    assertTrue(details.get(1) instanceof HistoricFormProperty);
    HistoricFormProperty formProp2 = (HistoricFormProperty) details.get(1);
    assertEquals("formProp2", formProp2.getPropertyId());
    assertEquals("12345", formProp2.getPropertyValue());
    
    
    assertTrue(details.get(2) instanceof HistoricVariableUpdate);
    HistoricVariableUpdate varUpdate1 = (HistoricVariableUpdate) details.get(2);
    assertEquals("variable1", varUpdate1.getVariableName());
    assertEquals("activiti rocks!", varUpdate1.getValue());
    
    
    // This variable should be of type LONG since this is defined in the process-definition
    assertTrue(details.get(3) instanceof HistoricVariableUpdate);
    HistoricVariableUpdate varUpdate2 = (HistoricVariableUpdate) details.get(3);
    assertEquals("variable2", varUpdate2.getVariableName());
    assertEquals(12345L, varUpdate2.getValue());
  }
  
  
  
  public void testHistoricDetailQueryInvalidSorting() throws Exception {
    try {
      historyService.createHistoricDetailQuery().asc().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().desc().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByProcessInstanceId().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByTime().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByVariableName().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByVariableRevision().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByVariableType().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
  }
  
  @Deployment
  public void testHistoricTaskInstanceVariableUpdates() {
    String processInstanceId = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest").getId();
    
    String taskId = taskService.createTaskQuery().singleResult().getId();
    
    runtimeService.setVariable(processInstanceId, "deadline", "yesterday");
    
    taskService.setVariableLocal(taskId, "bucket", "23c");
    taskService.setVariableLocal(taskId, "mop", "37i");
    
    taskService.complete(taskId);
    
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().count());

    List<HistoricDetail> historicTaskVariableUpdates = historyService.createHistoricDetailQuery()
      .taskId(taskId)
      .variableUpdates()
      .orderByVariableName().asc()
      .list();
    
    assertEquals(2, historicTaskVariableUpdates.size());

    historyService.deleteHistoricTaskInstance(taskId);
    
    // Check if the variable updates have been removed as well
    historicTaskVariableUpdates = historyService.createHistoricDetailQuery()
      .taskId(taskId)
      .variableUpdates()
      .orderByVariableName().asc()
      .list();
  
     assertEquals(0, historicTaskVariableUpdates.size());
  }
  
  // ACT-592
  @Deployment
  public void testSetVariableOnProcessInstanceWithTimer() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("timerVariablesProcess");
    runtimeService.setVariable(processInstance.getId(), "myVar", 123456L);
    assertEquals(123456L, runtimeService.getVariable(processInstance.getId(), "myVar"));
  }
  
  
  @Deployment
  public void testDeleteHistoricProcessInstance() {
    // Start process-instance with some variables set
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("processVar", 123L);
    vars.put("anotherProcessVar", new DummySerializable());
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest", vars);
    assertNotNull(processInstance);
    
    // Set 2 task properties
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "taskVar", 45678);
    taskService.setVariableLocal(task.getId(), "anotherTaskVar", "value");
 
    // Finish the task, this end the process-instance
    taskService.complete(task.getId());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(3, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(4, historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).count());
    
    // Delete the historic process-instance
    historyService.deleteHistoricProcessInstance(processInstance.getId());
    
    // Verify no traces are left in the history tables
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstance.getId()).count());
    
    try {
      // Delete the historic process-instance, which is still running
      historyService.deleteHistoricProcessInstance("unexisting");
      fail("Exception expected when deleting process-instance that is still running");
    } catch(ActivitiException ae) {
      // Expected exception
      assertTextPresent("No historic process instance found with id: unexisting", ae.getMessage());
    }
  }
  
  @Deployment
  public void testDeleteRunningHistoricProcessInstance() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");
    assertNotNull(processInstance);
    
    try {
      // Delete the historic process-instance, which is still running
      historyService.deleteHistoricProcessInstance(processInstance.getId());
      fail("Exception expected when deleting process-instance that is still running");
    } catch(ActivitiException ae) {
      // Expected exception
      assertTextPresent("Process instance is still running, cannot delete historic process instance", ae.getMessage());
    }
  }
  
  /**
   * Test created to validate ACT-621 fix.
   */
  @Deployment
  public void testHistoricFormPropertiesOnReEnteringActivity() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("comeBack", Boolean.TRUE);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricFormPropertiesProcess", variables);
    assertNotNull(processInstance);
    
    // Submit form on task
    Map<String, String> data = new HashMap<String, String>();
    data.put("formProp1", "Property value");
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    formService.submitTaskFormData(task.getId(), data);
    
    // Historic property should be available
    List<HistoricDetail> details = historyService.createHistoricDetailQuery()
      .formProperties()
      .processInstanceId(processInstance.getId())
      .list();
    assertNotNull(details);
    assertEquals(1, details.size());
    
    // Task should be active in the same activity as the previous one
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    formService.submitTaskFormData(task.getId(), data);
    
    details = historyService.createHistoricDetailQuery()
      .formProperties()
      .processInstanceId(processInstance.getId())
      .list();
    assertNotNull(details);
    assertEquals(2, details.size());
    
    // Should have 2 different historic activity instance ID's, with the same activityId
    assertNotSame(details.get(0).getActivityInstanceId(), details.get(1).getActivityInstanceId());
    
    HistoricActivityInstance historicActInst1 = historyService.createHistoricActivityInstanceQuery()
      .activityInstanceId(details.get(0).getActivityInstanceId())
      .singleResult();
    
    HistoricActivityInstance historicActInst2 = historyService.createHistoricActivityInstanceQuery()
      .activityInstanceId(details.get(1).getActivityInstanceId())
      .singleResult();
    
    assertEquals(historicActInst1.getActivityId(), historicActInst2.getActivityId());
  }
  
  @Deployment
  public void testHistoricTaskInstanceQueryTaskVariableValueEquals() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // Set some variables on the task
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);
    
    taskService.setVariablesLocal(task.getId(), variables);
    
    // Validate all variable-updates are present in DB
    assertEquals(7, historyService.createHistoricDetailQuery().variableUpdates().taskId(task.getId()).count());
    
    // Query Historic task instances based on variable
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("longVar", 12345L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("shortVar", (short) 123).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("integerVar",1234).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("stringVar","stringValue").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("booleanVar", true).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("dateVar", date).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("nullVar", null).count());
    
    // Update the variables
    variables.put("longVar", 67890L);
    variables.put("shortVar", (short) 456);
    variables.put("integerVar", 5678);
    variables.put("stringVar", "updatedStringValue");
    variables.put("booleanVar", false);
    Calendar otherCal = Calendar.getInstance();
    otherCal.add(Calendar.DAY_OF_MONTH, 1);
    Date otherDate = otherCal.getTime();
    variables.put("dateVar", otherDate);
    variables.put("nullVar", null);
    
    taskService.setVariablesLocal(task.getId(), variables);
    
    // Validate all variable-updates are present in DB
    assertEquals(14, historyService.createHistoricDetailQuery().variableUpdates().taskId(task.getId()).count());
    
    // Previous values should NOT match
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("longVar", 12345L).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("shortVar", (short) 123).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("integerVar",1234).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("stringVar","stringValue").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("booleanVar", true).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("dateVar", date).count());
    
    // New values should match
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("longVar", 67890L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("shortVar", (short) 456).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("integerVar",5678).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("stringVar","updatedStringValue").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("booleanVar", false).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("dateVar", otherDate).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("nullVar", null).count());
  }
  
  @Deployment
  public void testHistoricTaskInstanceQueryProcessVariableValueEquals() throws Exception {
    // Set some variables on the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricTaskInstanceTest", variables);
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // Validate all variable-updates are present in DB
    assertEquals(7, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    
    // Query Historic task instances based on process variable
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 12345L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("shortVar", (short) 123).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("integerVar",1234).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("stringVar","stringValue").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("booleanVar", true).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("dateVar", date).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("nullVar", null).count());
    
    // Update the variables
    variables.put("longVar", 67890L);
    variables.put("shortVar", (short) 456);
    variables.put("integerVar", 5678);
    variables.put("stringVar", "updatedStringValue");
    variables.put("booleanVar", false);
    Calendar otherCal = Calendar.getInstance();
    otherCal.add(Calendar.DAY_OF_MONTH, 1);
    Date otherDate = otherCal.getTime();
    variables.put("dateVar", otherDate);
    variables.put("nullVar", null);
    
    runtimeService.setVariables(processInstance.getId(), variables);
    
    // Validate all variable-updates are present in DB
    assertEquals(14, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    
    // Previous values should NOT match
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 12345L).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("shortVar", (short) 123).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("integerVar",1234).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("stringVar","stringValue").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("booleanVar", true).count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("dateVar", date).count());
    
    // New values should match
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 67890L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("shortVar", (short) 456).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("integerVar",5678).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("stringVar","updatedStringValue").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("booleanVar", false).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("dateVar", otherDate).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("nullVar", null).count());
    
    // Set a task-variables, shouldn't affect the process-variable matches
    taskService.setVariableLocal(task.getId(), "longVar", 9999L);
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 9999L).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("longVar", 67890L).count());
  }

  @Deployment
  public void testHistoricProcessInstanceVariableValueEquals() throws Exception {
    // Set some variables on the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricProcessInstanceTest", variables);
    
    // Validate all variable-updates are present in DB
    assertEquals(7, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    
    // Query Historic process instances based on process variable
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("longVar", 12345L).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("shortVar", (short) 123).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("integerVar",1234).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar","stringValue").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("booleanVar", true).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("nullVar", null).count());
    
    // Update the variables
    variables.put("longVar", 67890L);
    variables.put("shortVar", (short) 456);
    variables.put("integerVar", 5678);
    variables.put("stringVar", "updatedStringValue");
    variables.put("booleanVar", false);
    Calendar otherCal = Calendar.getInstance();
    otherCal.add(Calendar.DAY_OF_MONTH, 1);
    Date otherDate = otherCal.getTime();
    variables.put("dateVar", otherDate);
    variables.put("nullVar", null);
    
    runtimeService.setVariables(processInstance.getId(), variables);
    
    // Validate all variable-updates are present in DB
    assertEquals(14, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    
    // Previous values should NOT match
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueEquals("longVar", 12345L).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueEquals("shortVar", (short) 123).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueEquals("integerVar",1234).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar","stringValue").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueEquals("booleanVar", true).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", date).count());
    
    // New values should match
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("longVar", 67890L).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("shortVar", (short) 456).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("integerVar",5678).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("stringVar","updatedStringValue").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("booleanVar", false).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("dateVar", otherDate).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueEquals("nullVar", null).count());
  }
  
  @Deployment(resources={"org/activiti/standalone/history/FullHistoryTest.testHistoricProcessInstanceVariableValueEquals.bpmn20.xml"})
  public void testHistoricProcessInstanceVariableValueNotEquals() throws Exception {
    // Set some variables on the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    Calendar otherCal = Calendar.getInstance();
    otherCal.add(Calendar.DAY_OF_MONTH, 1);
    Date otherDate = otherCal.getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricProcessInstanceTest", variables);
    
    // Validate all variable-updates are present in DB
    assertEquals(7, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    
    // Query Historic process instances based on process variable, shouldn't match
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("longVar", 12345L).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("shortVar", (short) 123).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("integerVar",1234).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar","stringValue").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("booleanVar", true).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar", date).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("nullVar", null).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("nullVar", null).count());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("longVar", 67890L).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("shortVar", (short) 456).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("integerVar",5678).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar","updatedStringValue").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("booleanVar", false).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar", otherDate).count());
    
    // Update the variables
    variables.put("longVar", 67890L);
    variables.put("shortVar", (short) 456);
    variables.put("integerVar", 5678);
    variables.put("stringVar", "updatedStringValue");
    variables.put("booleanVar", false);
    variables.put("dateVar", otherDate);
    variables.put("nullVar", null);
    
    runtimeService.setVariables(processInstance.getId(), variables);
    
    // Validate all variable-updates are present in DB
    assertEquals(14, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("longVar", 12345L).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("shortVar", (short) 123).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("integerVar",1234).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar","stringValue").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("booleanVar", true).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar", date).count());
    
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("longVar", 67890L).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("shortVar", (short) 456).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("integerVar",5678).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("stringVar","updatedStringValue").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("booleanVar", false).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("dateVar", otherDate).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueNotEquals("nullVar", null).count());
  }
  
  @Deployment(resources={"org/activiti/standalone/history/FullHistoryTest.testHistoricProcessInstanceVariableValueEquals.bpmn20.xml"})
  public void testHistoricProcessInstanceVariableValueLessThanAndGreaterThan() throws Exception {
    // Set some variables on the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 12345L);
       
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricProcessInstanceTest", variables);
    
    // Validate all variable-updates are present in DB
    assertEquals(1, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("longVar", 12345L).count());
//    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThan("longVar", 12344L).count());
//    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 12345L).count());
//    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 12344L).count());
//    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 12346L).count());
//    
//    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThan("longVar", 12345L).count());
//    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueLessThan("longVar", 12346L).count());
//    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("longVar", 12345L).count());
//    assertEquals(1, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("longVar", 12346L).count());
//    assertEquals(0, historyService.createHistoricProcessInstanceQuery().variableValueLessThanOrEqual("longVar", 12344L).count());
  }
 
  @Deployment(resources={"org/activiti/standalone/history/FullHistoryTest.testVariableUpdatesAreLinkedToActivity.bpmn20.xml"})
  public void testVariableUpdatesLinkedToActivity() throws Exception {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("ProcessWithSubProcess");
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("test", "1");    
    taskService.complete(task.getId(), variables);
    
    // now we are in the subprocess
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    variables.clear();
    variables.put("test", "2");    
    taskService.complete(task.getId(), variables);
    
    // now we are ended
    assertProcessEnded(pi.getId());
    
    // check history
    List<HistoricDetail> updates = historyService.createHistoricDetailQuery().variableUpdates().list();
    assertEquals(2, updates.size());
    
    Map<String, HistoricVariableUpdate> updatesMap = new HashMap<String, HistoricVariableUpdate>();
    HistoricVariableUpdate update = (HistoricVariableUpdate) updates.get(0);
    updatesMap.put((String)update.getValue(), update);
    update = (HistoricVariableUpdate) updates.get(1);
    updatesMap.put((String)update.getValue(), update);
    
    HistoricVariableUpdate update1 = updatesMap.get("1");
    HistoricVariableUpdate update2 = updatesMap.get("2");

    assertNotNull(update1.getActivityInstanceId());
    assertNotNull(update1.getExecutionId());
    HistoricActivityInstance historicActivityInstance1 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update1.getActivityInstanceId()).singleResult();
    assertEquals(historicActivityInstance1.getExecutionId(), update1.getExecutionId());
    assertEquals("usertask1", historicActivityInstance1.getActivityId());
    
    assertNotNull(update2.getActivityInstanceId());
    HistoricActivityInstance historicActivityInstance2 = historyService.createHistoricActivityInstanceQuery().activityInstanceId(update2.getActivityInstanceId()).singleResult();
    assertEquals("usertask2", historicActivityInstance2.getActivityId());

    /*
     * This is OK! The variable is set on the root execution, on a execution never run through the activity, where the process instances
     * stands when calling the set Variable. But the ActivityId of this flow node is used. So the execution id's doesn't have to be equal.
     * 
     * execution id: On which execution it was set
     * activity id: in which activity was the process instance when setting the variable
     */
    assertFalse(historicActivityInstance2.getExecutionId().equals(update2.getExecutionId()));
  }  
  
  @Deployment(resources = { 
  "org/activiti/standalone/jpa/JPAVariableTest.testQueryJPAVariable.bpmn20.xml" })
    public void testReadJpaVariableValueFromHistoricVariableUpdate() {
    
    EntityManagerSessionFactory entityManagerSessionFactory = (EntityManagerSessionFactory) processEngineConfiguration
            .getSessionFactories()
            .get(EntityManagerSession.class);
          
    EntityManagerFactory  entityManagerFactory = entityManagerSessionFactory.getEntityManagerFactory();
          
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
    
    runtimeService.setVariable(executionId, variableName, entity);
    taskService.complete(task.getId());
    
    List<HistoricDetail> variableUpdates = historyService.createHistoricDetailQuery().processInstanceId(executionId).variableUpdates().list();
    
    assertEquals(1, variableUpdates.size());
    HistoricVariableUpdate update = (HistoricVariableUpdate) variableUpdates.get(0);
    assertNotNull(update.getValue());
    assertTrue(update.getValue() instanceof FieldAccessJPAEntity);
    
    assertEquals(entity.getId(), ((FieldAccessJPAEntity)update.getValue()).getId());
    }
  
  /**
   * Test confirming fix for ACT-1731
   */
   @Deployment(
      resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
    public void testQueryHistoricTaskIncludeBinaryVariable() throws Exception {
     // Start process with a binary variable
     ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", 
             Collections.singletonMap("binaryVariable", (Object)"It is I, le binary".getBytes()));
     Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
     assertNotNull(task);
     taskService.setVariableLocal(task.getId(), "binaryTaskVariable", (Object)"It is I, le binary".getBytes());
     
     // Complete task
     taskService.complete(task.getId());
     
     // Query task, including processVariables
     HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).includeProcessVariables().singleResult();
     assertNotNull(historicTask);
     assertNotNull(historicTask.getProcessVariables());
     byte[] bytes = (byte[]) historicTask.getProcessVariables().get("binaryVariable");
     assertEquals("It is I, le binary", new String(bytes));
     
     // Query task, including taskVariables
     historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.getId()).includeTaskLocalVariables().singleResult();
     assertNotNull(historicTask);
     assertNotNull(historicTask.getTaskLocalVariables());
     bytes = (byte[]) historicTask.getTaskLocalVariables().get("binaryTaskVariable");
     assertEquals("It is I, le binary", new String(bytes));
    }
   
   /**
    * Test confirming fix for ACT-1731
    */
   @Deployment(
     resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
   public void testQueryHistoricProcessInstanceIncludeBinaryVariable() throws Exception {
    // Start process with a binary variable
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", 
            Collections.singletonMap("binaryVariable", (Object)"It is I, le binary".getBytes()));
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    
    // Complete task to end process
    taskService.complete(task.getId());
    
    // Query task, including processVariables
    HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).includeProcessVariables().singleResult();
    assertNotNull(historicProcess);
    assertNotNull(historicProcess.getProcessVariables());
    byte[] bytes = (byte[]) historicProcess.getProcessVariables().get("binaryVariable");
    assertEquals("It is I, le binary", new String(bytes));
    
   }
   
   // Test for https://activiti.atlassian.net/browse/ACT-2186
   @Deployment(resources={
   	"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
   public void testHistoricVariableRemovedWhenRuntimeVariableIsRemoved() {
   	 Map<String, Object> vars = new HashMap<String, Object>();
      vars.put("var1", "Hello");
      vars.put("var2", "World");
      vars.put("var3", "!");
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
      
      // Verify runtime
      assertEquals(3, runtimeService.getVariables(processInstance.getId()).size());
      assertEquals(3, runtimeService.getVariables(processInstance.getId(), Arrays.asList("var1", "var2", "var3")).size());
      assertNotNull(runtimeService.getVariable(processInstance.getId(), "var2"));
      
      // Verify history
      assertEquals(3, historyService.createHistoricVariableInstanceQuery().list().size());
      assertNotNull(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("var2").singleResult());
      
      // Verify historic details
      List<HistoricDetail> details = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).variableUpdates().orderByTime().asc().list();
      assertEquals(3, details.size()); // 3 vars
      for (HistoricDetail historicDetail : details) {
      	assertNotNull( ((HistoricVariableUpdate) historicDetail).getValue());
      }
      
      // Remove one variable
      runtimeService.removeVariable(processInstance.getId(), "var2");
      
      // Verify runtime
      assertEquals(2, runtimeService.getVariables(processInstance.getId()).size());
      assertEquals(2, runtimeService.getVariables(processInstance.getId(), Arrays.asList("var1", "var2", "var3")).size());
      assertNull(runtimeService.getVariable(processInstance.getId(), "var2"));
      
      // Verify history
      assertEquals(2, historyService.createHistoricVariableInstanceQuery().list().size());
      assertNull(historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).variableName("var2").singleResult());
      
      // Verify historic details
      details = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).variableUpdates().orderByTime().asc().list();
      assertEquals(4, details.size()); // 3 vars + 1 delete
      
      // The last entry should be the delete
      for (int i=0; i<details.size(); i++) {
      	if (i != 3) {
      		assertNotNull( ((HistoricVariableUpdate) details.get(i)).getValue());
      	} else if (i == 3) {
      		assertNull( ((HistoricVariableUpdate) details.get(i)).getValue());
      	}
      }
      
   }
   
}
