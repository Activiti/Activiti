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

package org.activiti.engine.test.history;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.apache.commons.lang.time.DateUtils;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class HistoricProcessInstanceTest extends PluggableActivitiTestCase {
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricDataCreatedForProcessExecution() {

    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2010);
    calendar.set(Calendar.MONTH, 8);
    calendar.set(Calendar.DAY_OF_MONTH, 30);
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date noon = calendar.getTime();
    
    ClockUtil.setCurrentTime(noon);
    final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "myBusinessKey");

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().unfinished().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finished().count());
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(historicProcessInstance);
    assertEquals(processInstance.getId(), historicProcessInstance.getId());
    assertEquals(processInstance.getBusinessKey(), historicProcessInstance.getBusinessKey());
    assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
    assertEquals(noon, historicProcessInstance.getStartTime());
    assertNull(historicProcessInstance.getEndTime());
    assertNull(historicProcessInstance.getDurationInMillis());

    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

    assertEquals(1, tasks.size());

    // in this test scenario we assume that 25 seconds after the process start, the 
    // user completes the task (yes! he must be almost as fast as me)
    Date twentyFiveSecsAfterNoon = new Date(noon.getTime() + 25*1000);
    ClockUtil.setCurrentTime(twentyFiveSecsAfterNoon);
    taskService.complete(tasks.get(0).getId());

    historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

    assertNotNull(historicProcessInstance);
    assertEquals(processInstance.getId(), historicProcessInstance.getId());
    assertEquals(processInstance.getProcessDefinitionId(), historicProcessInstance.getProcessDefinitionId());
    assertEquals(noon, historicProcessInstance.getStartTime());
    assertEquals(twentyFiveSecsAfterNoon, historicProcessInstance.getEndTime());
    assertEquals(new Long(25*1000), historicProcessInstance.getDurationInMillis());
    
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().unfinished().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testDeleteProcessInstanceHistoryCreated() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertNotNull(processInstance);
    
    // delete process instance should not delete the history
    runtimeService.deleteProcessInstance(processInstance.getId(), "cancel");
    HistoricProcessInstance historicProcessInstance = 
      historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(historicProcessInstance.getEndTime());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceStartDate() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    Date date = new Date();

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startDateOn(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startDateBy(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startDateBy(DateUtils.addDays(date, -1)).count());
    
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startDateBy(DateUtils.addDays(date, 1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startDateOn(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startDateOn(DateUtils.addDays(date, 1)).count());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceFinishDateUnfinished() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    Date date = new Date();
    
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(date).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateBy(date).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateBy(DateUtils.addDays(date, 1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateBy(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(DateUtils.addDays(date, 1)).count());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceFinishDateFinished() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    Date date = new Date();
    
    runtimeService.deleteProcessInstance(pi.getId(), "cancel");

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishDateOn(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishDateBy(date).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishDateBy(DateUtils.addDays(date, 1)).count());
    
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateBy(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(DateUtils.addDays(date, -1)).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishDateOn(DateUtils.addDays(date, 1)).count());
  }
  
  /*@Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceVariables() {
  	Map<String,Object> vars = new HashMap<String,Object>();
  	vars.put("foo", "bar");
  	vars.put("baz", "boo");
	
    runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processVariableEquals("foo", "bar").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processVariableEquals("baz", "boo").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processVariableEquals("foo", "bar").processVariableEquals("baz", "boo").count());
  }*/

  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceQuery() {
    Calendar startTime = Calendar.getInstance();
    
    ClockUtil.setCurrentTime(startTime.getTime());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "businessKey123");
    
    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);
    
    // Start/end dates
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedBefore(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedBefore(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedAfter(hourFromNow.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startedBefore(hourFromNow.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startedBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().startedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().startedAfter(hourFromNow.getTime()).count());

    // General fields
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finished().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionId(processInstance.getProcessDefinitionId()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKey("oneTaskProcess").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey("businessKey123").count()); 
    
    List<String> exludeIds = new ArrayList<String>();
    exludeIds.add("unexistingProcessDefinition");
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(exludeIds).count());
    
    exludeIds.add("oneTaskProcess");
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKeyNotIn(exludeIds).count()); 
    
    // After finishing process
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finished().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedBefore(hourAgo.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishedBefore(hourFromNow.getTime()).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().finishedAfter(hourAgo.getTime()).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().finishedAfter(hourFromNow.getTime()).count());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceSorting() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().list().size());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().list().size());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().count());

    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().count());
  }
  
  public void testInvalidSorting() {
    try {
      historyService.createHistoricProcessInstanceQuery().asc();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricProcessInstanceQuery().desc();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().list();
      fail();
    } catch (ActivitiException e) {
      
    }
  }

  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  // ACT-1098
  public void testDeleteReason() {
    if(!ProcessEngineConfiguration.HISTORY_NONE.equals(processEngineConfiguration.getHistory())) {
      final String deleteReason = "some delete reason";
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.deleteProcessInstance(pi.getId(), deleteReason);
      HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(pi.getId()).singleResult();
      assertEquals(deleteReason, hpi.getDeleteReason());
    }
  }
}
