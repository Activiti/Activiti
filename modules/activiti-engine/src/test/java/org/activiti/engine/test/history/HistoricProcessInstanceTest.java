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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


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
    
    processEngineConfiguration.getClock().setCurrentTime(noon);
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
    processEngineConfiguration.getClock().setCurrentTime(twentyFiveSecsAfterNoon);
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
    
    processEngineConfiguration.getClock().setCurrentTime(startTime.getTime());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "businessKey123");
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "someType");
    runtimeService.setProcessInstanceName(processInstance.getId(), "The name");
    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);
    
    // Name and name like
    assertEquals("The name", historyService.createHistoricProcessInstanceQuery().processInstanceName("The name").singleResult().getName());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceName("The name").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processInstanceName("Other name").count());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("% name").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("%nope").count());
    
    
    // Query after update name
    runtimeService.setProcessInstanceName(processInstance.getId(), "New name");
    assertEquals("New name", historyService.createHistoricProcessInstanceQuery().processInstanceName("New name").singleResult().getName());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceName("New name").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processInstanceName("The name").count());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("New %").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processInstanceNameLike("The %").count());
    
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
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKeyIn(Arrays.asList("oneTaskProcess")).count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().processDefinitionKeyIn(Arrays.asList("undefined", "oneTaskProcess")).count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().processDefinitionKeyIn(Arrays.asList("undefined1", "undefined2")).count());
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
    
    // Check identity links
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().involvedUser("kermit").count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().involvedUser("gonzo").count());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceOrQuery() {
    Calendar startTime = Calendar.getInstance();
    
    processEngineConfiguration.getClock().setCurrentTime(startTime.getTime());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", "businessKey123");
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "someType");
    runtimeService.setProcessInstanceName(processInstance.getId(), "The name");
    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    Calendar hourFromNow = Calendar.getInstance();
    hourFromNow.add(Calendar.HOUR_OF_DAY, 1);
    
    // Name and name like
    assertEquals("The name", historyService.createHistoricProcessInstanceQuery().or().processInstanceName("The name").processDefinitionId("undefined").endOr().singleResult().getName());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processInstanceName("The name").processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().processInstanceName("Other name").processDefinitionId("undefined").endOr().count());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("% name").processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("%nope").processDefinitionId("undefined").endOr().count());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery()
        .or()
          .processInstanceName("The name")
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processInstanceNameLike("% name")
          .processDefinitionId("undefined")
        .endOr()
        .count());
    
    assertEquals(0, historyService.createHistoricProcessInstanceQuery()
        .or()
          .processInstanceName("The name")
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processInstanceNameLike("undefined")
          .processDefinitionId("undefined")
        .endOr()
        .count());
    
    // Query after update name
    runtimeService.setProcessInstanceName(processInstance.getId(), "New name");
    assertEquals("New name", historyService.createHistoricProcessInstanceQuery().or().processInstanceName("New name").processDefinitionId("undefined").endOr().singleResult().getName());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processInstanceName("New name").processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().processInstanceName("The name").processDefinitionId("undefined").endOr().count());
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("New %").processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLike("The %").processDefinitionId("undefined").endOr().count());
    
    // Start/end dates
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().finishedBefore(hourAgo.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().finishedBefore(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().finishedAfter(hourAgo.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().finishedAfter(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().startedBefore(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().startedBefore(hourAgo.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().startedAfter(hourAgo.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().startedAfter(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count());

    // General fields
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().finished().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processInstanceId(processInstance.getId()).processDefinitionId("undefined").endOr().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processDefinitionId(processInstance.getProcessDefinitionId()).processDefinitionKey("undefined").count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processDefinitionId("undefined").processDefinitionKeyIn(Arrays.asList("undefined", "oneTaskProcess")).endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().processDefinitionId("undefined").processDefinitionKeyIn(Arrays.asList("undefined1", "undefined2")).endOr().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processDefinitionKey("oneTaskProcess").processDefinitionId("undefined").endOr().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processInstanceBusinessKey("businessKey123").processDefinitionId("undefined").endOr().count()); 
    
    List<String> exludeIds = new ArrayList<String>();
    exludeIds.add("unexistingProcessDefinition");
    
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().processDefinitionKeyNotIn(exludeIds).processDefinitionId("undefined").endOr().count());
    
    exludeIds.add("oneTaskProcess");
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().processDefinitionKeyNotIn(exludeIds).processDefinitionId("undefined").endOr().count()); 
    
    // After finishing process
    taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().finished().processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().finishedBefore(hourAgo.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().finishedBefore(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().finishedAfter(hourAgo.getTime()).processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().finishedAfter(hourFromNow.getTime()).processDefinitionId("undefined").endOr().count());
    
    // Check identity links
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().or().involvedUser("kermit").processDefinitionId("undefined").endOr().count());
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().or().involvedUser("gonzo").processDefinitionId("undefined").endOr().count());
  }
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceSorting() {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

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
    
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    // First complete process instance 2
    for (Task task : taskService.createTaskQuery().processInstanceId(processInstance2.getId()).list()) {
    	taskService.complete(task.getId());
    }
    
    // Then process instance 1
    for (Task task : taskService.createTaskQuery().processInstanceId(processInstance1.getId()).list()) {
    	taskService.complete(task.getId());
    }
    
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().list().size());

    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().list().size());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().list().size());
    
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().asc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().asc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().asc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().asc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().asc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().asc().count());

    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().desc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceStartTime().desc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceDuration().desc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessDefinitionId().desc().count());
    assertEquals(2, historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceBusinessKey().desc().count());
    
    // Verify orderByProcessInstanceEndTime
    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().list();
    assertEquals(processInstance1.getId(), historicProcessInstances.get(0).getId());
    assertEquals(processInstance2.getId(), historicProcessInstances.get(1).getId());
    
    // Verify again, with variables included (bug reported on that)
    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceEndTime().desc().includeProcessVariables().list();
    assertEquals(processInstance1.getId(), historicProcessInstances.get(0).getId());
    assertEquals(processInstance2.getId(), historicProcessInstances.get(1).getId());
  }
  
  public void testInvalidSorting() {
    try {
      historyService.createHistoricProcessInstanceQuery().asc();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricProcessInstanceQuery().desc();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
    
    try {
      historyService.createHistoricProcessInstanceQuery().orderByProcessInstanceId().list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
  }

  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  // ACT-1098
  public void testDeleteReason() {
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      final String deleteReason = "some delete reason";
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.deleteProcessInstance(pi.getId(), deleteReason);
      HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(pi.getId()).singleResult();
      assertEquals(deleteReason, hpi.getDeleteReason());
    }
  }
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricIdenityLinksOnProcessInstance() {
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("oneTaskProcess");
      runtimeService.addUserIdentityLink(pi.getId(), "kermit", "myType");
      
      // Check historic links
      List<HistoricIdentityLink> historicLinks = historyService.getHistoricIdentityLinksForProcessInstance(pi.getId());
      assertEquals(1, historicLinks.size());
      
      assertEquals("myType", historicLinks.get(0).getType());
      assertEquals("kermit", historicLinks.get(0).getUserId());
      assertNull(historicLinks.get(0).getGroupId());
      assertEquals(pi.getId(), historicLinks.get(0).getProcessInstanceId());
      
      // When process is ended, link should remain
      taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
      assertNull(runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).singleResult());
      
      assertEquals(1, historyService.getHistoricIdentityLinksForProcessInstance(pi.getId()).size());
      
      // When process is deleted, identitylinks shouldn't exist anymore
      historyService.deleteHistoricProcessInstance(pi.getId());
      assertEquals(0, historyService.getHistoricIdentityLinksForProcessInstance(pi.getId()).size());
    }
  }
  
  
  /**
   * Validation for ACT-821
   */
  @Deployment(resources= {
  		"org/activiti/engine/test/history/HistoricProcessInstanceTest.testDeleteHistoricProcessInstanceWithCallActivity.bpmn20.xml",
  		"org/activiti/engine/test/history/HistoricProcessInstanceTest.testDeleteHistoricProcessInstanceWithCallActivity-subprocess.bpmn20.xml"
  })
  public void testDeleteHistoricProcessInstanceWithCallActivity() {
    if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
    	 ProcessInstance pi = runtimeService.startProcessInstanceByKey("callSimpleSubProcess");
    	 
    	 runtimeService.deleteProcessInstance(pi.getId(), "testing");
    	 
    	 // The parent and child process should be present in history
    	 assertEquals(2L, historyService.createHistoricProcessInstanceQuery().count());
    	 
    	 // Deleting the parent process should cascade the child-process
    	 historyService.deleteHistoricProcessInstance(pi.getId());
    	 assertEquals(0L, historyService.createHistoricProcessInstanceQuery().count());
    }
  }
  
  @Deployment(resources = {"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricProcessInstanceName() {
	  String piName = "Customized Process Instance Name";
	  ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder();
	  builder.processDefinitionKey("oneTaskProcess");
	  builder.processInstanceName(piName);
	  ProcessInstance processInstance1 = builder.start();
	
	  HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance1.getProcessInstanceId()).singleResult();
	  assertEquals(piName, historicProcessInstance.getName());
	  assertEquals(1, historyService.createHistoricProcessInstanceQuery().processInstanceName(piName).list().size());
  }
  
  /**
   * Validation for https://activiti.atlassian.net/browse/ACT-2182
   */
  public void testNameAndTenantIdSetWhenFetchingVariables() {
  	
  	String tenantId = "testTenantId";
  	String processInstanceName = "myProcessInstance";
  	
  	String deploymentId = repositoryService.createDeployment()
  		.addClasspathResource("org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml")
  		.tenantId(tenantId)
  		.deploy()
  		.getId();
  	
  	Map<String, Object> vars = new HashMap<String, Object>();
  	vars.put("name", "Kermit");
  	vars.put("age", 60);
  	ProcessInstance processInstance = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", vars, tenantId);
  	runtimeService.setProcessInstanceName(processInstance.getId(), processInstanceName);
  	
  	// Verify name and tenant id (didnt work on mssql and db2) on process instance
  	List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().includeProcessVariables().list();
  	assertEquals(1, processInstances.size());
  	processInstance = processInstances.get(0);
  	
  	assertEquals(processInstanceName, processInstance.getName());
  	assertEquals(tenantId, processInstance.getTenantId());
  	
  	Map<String, Object> processInstanceVars = processInstance.getProcessVariables();
  	assertEquals(2, processInstanceVars.size());
  	assertEquals("Kermit", processInstanceVars.get("name"));
  	assertEquals(60, processInstanceVars.get("age"));
  	
  	
  	// Verify name and tenant id (didnt work on mssql and db2) on historic process instance
  	List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().list();
  	assertEquals(1, historicProcessInstances.size());
  	HistoricProcessInstance historicProcessInstance = historicProcessInstances.get(0);
  	
  	// Verify name and tenant id (didnt work on mssql and db2) on process instance
  	assertEquals(processInstanceName, historicProcessInstance.getName());
  	assertEquals(tenantId, historicProcessInstance.getTenantId());
  	
  	Map<String, Object> historicProcessInstanceVars = historicProcessInstance.getProcessVariables();
  	assertEquals(2, historicProcessInstanceVars.size());
  	assertEquals("Kermit", historicProcessInstanceVars.get("name"));
  	assertEquals(60, historicProcessInstanceVars.get("age"));
  	
  	// cleanup
  	repositoryService.deleteDeployment(deploymentId, true);
  }
  
}
