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
package org.activiti.engine.test.api.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Falko Menge
 */
public class TaskQueryTest extends PluggableActivitiTestCase {

  private List<String> taskIds;

  public void setUp() throws Exception {

    identityService.saveUser(identityService.newUser("kermit"));
    identityService.saveUser(identityService.newUser("gonzo"));
    identityService.saveUser(identityService.newUser("fozzie"));

    identityService.saveGroup(identityService.newGroup("management"));
    identityService.saveGroup(identityService.newGroup("accountancy"));

    identityService.createMembership("kermit", "management");
    identityService.createMembership("kermit", "accountancy");
    identityService.createMembership("fozzie", "management");

    taskIds = generateTestTasks();
  }

  public void tearDown() throws Exception {
    identityService.deleteGroup("accountancy");
    identityService.deleteGroup("management");
    identityService.deleteUser("fozzie");
    identityService.deleteUser("gonzo");
    identityService.deleteUser("kermit");
    taskService.deleteTasks(taskIds, true);
  }
  
  public void tesBasicTaskPropertiesNotNull() {
    Task task = taskService.createTaskQuery().taskId(taskIds.get(0)).singleResult();
    assertNotNull(task.getDescription());
    assertNotNull(task.getId());
    assertNotNull(task.getName());
    assertNotNull(task.getCreateTime());
  }
  
  public void testQueryNoCriteria() {
    TaskQuery query = taskService.createTaskQuery();
    assertEquals(12, query.count());
    assertEquals(12, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }
  }

  public void testQueryByTaskId() {
    TaskQuery query = taskService.createTaskQuery().taskId(taskIds.get(0));
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidTaskId() {
    TaskQuery query = taskService.createTaskQuery().taskId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().taskId(null);
      fail("expected exception");
    } catch (ActivitiIllegalArgumentException e) {
      // OK
    }
  }
  
  public void testQueryByName() {
    TaskQuery query = taskService.createTaskQuery().taskName("testTask");
    assertEquals(6, query.list().size());
    assertEquals(6, query.count());
    
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }
  }
  
  public void testQueryByInvalidName() {
    TaskQuery query = taskService.createTaskQuery().taskName("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().taskName(null).singleResult();
      fail("expected exception");
    } catch (ActivitiIllegalArgumentException e) {
      // OK
    }
  }
  
  public void testQueryByNameLike() {
    TaskQuery query = taskService.createTaskQuery().taskNameLike("gonzo%");
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidNameLike() {
    TaskQuery query = taskService.createTaskQuery().taskNameLike("1");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().taskNameLike(null).singleResult();
      fail();
    } catch (ActivitiIllegalArgumentException e) { }
  }
  
  public void testQueryByDescription() {
    TaskQuery query = taskService.createTaskQuery().taskDescription("testTask description");
    assertEquals(6, query.list().size());
    assertEquals(6, query.count());
    
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByInvalidDescription() {
    TaskQuery query = taskService.createTaskQuery().taskDescription("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().taskDescription(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
  }
  
  public void testQueryByDescriptionLike() {
    TaskQuery query = taskService.createTaskQuery().taskDescriptionLike("%gonzo%");
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidDescriptionLike() {
    TaskQuery query = taskService.createTaskQuery().taskDescriptionLike("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().taskDescriptionLike(null).list();
      fail();
    } catch (ActivitiIllegalArgumentException e) {
      
    }
  }
  
  public void testQueryByPriority() {
    TaskQuery query = taskService.createTaskQuery().taskPriority(10);
    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
    
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
    
    query = taskService.createTaskQuery().taskPriority(100);
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    query = taskService.createTaskQuery().taskMinPriority(50);
    assertEquals(3, query.list().size());

    query = taskService.createTaskQuery().taskMinPriority(10);
    assertEquals(5, query.list().size());

    query = taskService.createTaskQuery().taskMaxPriority(10);
    assertEquals(9, query.list().size());

    query = taskService.createTaskQuery().taskMaxPriority(3);
    assertEquals(6, query.list().size());
  }
  
  public void testQueryByInvalidPriority() {
    try {
      taskService.createTaskQuery().taskPriority(null);
      fail("expected exception");
    } catch (ActivitiIllegalArgumentException e) {
      // OK
    }
  }
  
  public void testQueryByAssignee() {
    TaskQuery query = taskService.createTaskQuery().taskAssignee("gonzo");
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    query = taskService.createTaskQuery().taskAssignee("kermit");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());
  }
  
  public void testQueryByInvolvedUser() {
    try {
      Task adhocTask = taskService.newTask();
      adhocTask.setAssignee("kermit");
      adhocTask.setOwner("fozzie");
      taskService.saveTask(adhocTask);
      taskService.addUserIdentityLink(adhocTask.getId(), "gonzo", "customType");
      
      assertEquals(3, taskService.getIdentityLinksForTask(adhocTask.getId()).size());
      
      assertEquals(1, taskService.createTaskQuery().taskId(adhocTask.getId()).taskInvolvedUser("gonzo").count());
      assertEquals(1, taskService.createTaskQuery().taskId(adhocTask.getId()).taskInvolvedUser("kermit").count());
      assertEquals(1, taskService.createTaskQuery().taskId(adhocTask.getId()).taskInvolvedUser("fozzie").count());
      
    } finally {
      List<Task> allTasks = taskService.createTaskQuery().list();
      for(Task task : allTasks) {
        if(task.getExecutionId() == null) {
          taskService.deleteTask(task.getId());
          if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(task.getId());
          }
        }
      }
    }
  }
  
  public void testQueryByNullAssignee() {
    try {
      taskService.createTaskQuery().taskAssignee(null).list();
      fail("expected exception");
    } catch (ActivitiIllegalArgumentException e) {
      // OK
    }
  }

  public void testQueryByUnassigned() {
    TaskQuery query = taskService.createTaskQuery().taskUnassigned();
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());
  }
  
  public void testQueryByCandidateUser() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateUser("kermit");
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }

    query = taskService.createTaskQuery().taskCandidateUser("fozzie");
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }
  }
  
  public void testQueryByNullCandidateUser() {
    try {
      taskService.createTaskQuery().taskCandidateUser(null).list();
      fail();
    } catch(ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByCandidateGroup() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateGroup("management");
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }

    query = taskService.createTaskQuery().taskCandidateGroup("sales");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }

  public void testQueryByCandidateOrAssigneed() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned("kermit");
    assertEquals(11, query.count());
    List<Task> tasks = query.list();
    assertEquals(11, tasks.size());

    // claim a task
    taskService.claim(tasks.get(0).getId(), "kermit");
    query = taskService.createTaskQuery().taskCandidateOrAssigned("kermit");
    assertEquals(11, query.count());

    taskService.claim(tasks.get(1).getId(), "fozzie");
    query = taskService.createTaskQuery().taskCandidateOrAssigned("kermit");
    assertEquals(10, query.count());

    query = taskService.createTaskQuery().taskCandidateOrAssigned("fozzie");
    assertEquals(4, query.count());
    assertEquals(4, query.list().size());
  }
  
  public void testQueryByNullCandidateGroup() {
    try {
      taskService.createTaskQuery().taskCandidateGroup(null).list();
      fail("expected exception");
    } catch (ActivitiIllegalArgumentException e) {
      // OK
    }
  }
  
  public void testQueryByCandidateGroupIn() {
    List<String> groups = Arrays.asList("management", "accountancy");
    TaskQuery query = taskService.createTaskQuery().taskCandidateGroupIn(groups);
    assertEquals(5, query.count());
    assertEquals(5, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }

    // Unexisting groups or groups that don't have candidate tasks shouldn't influence other results
    groups = Arrays.asList("management", "accountancy", "sales", "unexising");
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups);
    assertEquals(5, query.count());
    assertEquals(5, query.list().size());
  }
  
  public void testQueryByNullCandidateGroupIn() {
    try {
      taskService.createTaskQuery().taskCandidateGroupIn(null).list();
      fail("expected exception");
    } catch (ActivitiIllegalArgumentException e) {
      // OK
    }
    try {
      taskService.createTaskQuery().taskCandidateGroupIn(new ArrayList<String>()).list();
      fail("expected exception");
    } catch (ActivitiIllegalArgumentException e) {
      // OK
    }
  }

  public void testQueryByDelegationState() {
    TaskQuery query = taskService.createTaskQuery().taskDelegationState(null);
    assertEquals(12, query.count());
    assertEquals(12, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.PENDING);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    String taskId= taskService.createTaskQuery().taskAssignee("gonzo").singleResult().getId();
    taskService.delegateTask(taskId, "kermit");

    query = taskService.createTaskQuery().taskDelegationState(null);
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.PENDING);
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());

    taskService.resolveTask(taskId);

    query = taskService.createTaskQuery().taskDelegationState(null);
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.PENDING);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED);
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
  }

  public void testQueryCreatedOn() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    
    // Exact matching of createTime, should result in 6 tasks
    Date createTime = sdf.parse("01/01/2001 01:01:01.000");
    
    TaskQuery query = taskService.createTaskQuery().taskCreatedOn(createTime);
    assertEquals(6, query.count());
    assertEquals(6, query.list().size());
  }
  
  public void testQueryCreatedBefore() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    
    // Should result in 7 tasks
    Date before = sdf.parse("03/02/2002 02:02:02.000");
    
    TaskQuery query = taskService.createTaskQuery().taskCreatedBefore(before);
    assertEquals(7, query.count());
    assertEquals(7, query.list().size());
    
    before = sdf.parse("01/01/2001 01:01:01.000");
    query = taskService.createTaskQuery().taskCreatedBefore(before);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }
  
  public void testQueryCreatedAfter() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    
    // Should result in 3 tasks
    Date after = sdf.parse("03/03/2003 03:03:03.000");
    
    TaskQuery query = taskService.createTaskQuery().taskCreatedAfter(after);
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());
    
    after = sdf.parse("05/05/2005 05:05:05.000");
    query = taskService.createTaskQuery().taskCreatedAfter(after);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }
  
  @Deployment(resources="org/activiti/engine/test/api/task/taskDefinitionProcess.bpmn20.xml")
  public void testTaskDefinitionKey() throws Exception {
    
    // Start process instance, 2 tasks will be available
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");
    
    List<Task> tasks2 = taskService.createTaskQuery().list();
    for (Task task : tasks2) {
      System.out.println("task " + task.getId() + " " + task.getTaskDefinitionKey() + " " + task.getName());
    }
    
    // 1 task should exist with key "taskKey1"
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("taskKey1").list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());

    assertEquals("taskKey1", tasks.get(0).getTaskDefinitionKey());

    // No task should be found with unexisting key
    Long count = taskService.createTaskQuery().taskDefinitionKey("unexistingKey").count();
    assertEquals(0L, count.longValue());
  }
  
  @Deployment(resources="org/activiti/engine/test/api/task/taskDefinitionProcess.bpmn20.xml")
  public void testTaskDefinitionKeyLike() throws Exception {
    
    // Start process instance, 2 tasks will be available
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");
    
    // Ends with matching, TaskKey1 and TaskKey123 match
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKeyLike("taskKey1%").orderByTaskName().asc().list();
    assertNotNull(tasks);
    assertEquals(2, tasks.size());

    assertEquals("taskKey1", tasks.get(0).getTaskDefinitionKey());
    assertEquals("taskKey123", tasks.get(1).getTaskDefinitionKey());
    
    // Starts with matching, TaskKey123 matches
    tasks = taskService.createTaskQuery().taskDefinitionKeyLike("%123").orderByTaskName().asc().list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());
    
    assertEquals("taskKey123", tasks.get(0).getTaskDefinitionKey());
    
    // Contains matching, TaskKey123 matches
    tasks = taskService.createTaskQuery().taskDefinitionKeyLike("%Key12%").orderByTaskName().asc().list();
    assertNotNull(tasks);
    assertEquals(1, tasks.size());
    
    assertEquals("taskKey123", tasks.get(0).getTaskDefinitionKey());
    

    // No task should be found with unexisting key
    Long count = taskService.createTaskQuery().taskDefinitionKeyLike("%unexistingKey%").count();
    assertEquals(0L, count.longValue());
  }
  
  @Deployment
  public void testTaskVariableValueEquals() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // No task should be found for an unexisting var
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("unexistingVar", "value").count());
    
    // Create a map with a variable for all default types
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);
    
    taskService.setVariablesLocal(task.getId(), variables);
    
    // Test query matches
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("longVar", 928374L).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("shortVar",  (short) 123).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("integerVar", 1234).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("stringVar", "stringValue").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("booleanVar", true).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("dateVar", date).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("nullVar", null).count());
    
    // Test query for other values on existing variables
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("longVar", 999L).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("shortVar",  (short) 999).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("integerVar", 999).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("stringVar", "999").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("booleanVar", false).count());
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("dateVar", otherDate.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("nullVar", "999").count());
    
    // Test query for not equals
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("longVar", 999L).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("shortVar",  (short) 999).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("integerVar", 999).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("stringVar", "999").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEquals("booleanVar", false).count());
    
    // Test value-only variable equals
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals(928374L).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals((short) 123).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals(1234).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals("stringValue").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals(true).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals(date).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEquals(null).count());
    
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals(999999L).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals((short) 999).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals(9999).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("unexistingstringvalue").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals(false).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals(otherDate.getTime()).count());
    
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLike("stringVar", "string%").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLike("stringVar", "String%").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLike("stringVar", "%Value").count());
    
    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThan("integerVar", 1000).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThan("integerVar", 1234).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThan("integerVar", 1240).count());
    
    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThanOrEqual("integerVar", 1000).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueGreaterThanOrEqual("integerVar", 1234).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueGreaterThanOrEqual("integerVar", 1240).count());
    
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThan("integerVar", 1240).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThan("integerVar", 1234).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThan("integerVar", 1000).count());
    
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThanOrEqual("integerVar", 1240).count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueLessThanOrEqual("integerVar", 1234).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueLessThanOrEqual("integerVar", 1000).count());
  }
  
  @Deployment
  public void testProcessVariableValueEquals() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);
    
    // Start process-instance with all types of variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    // Test query matches
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("longVar", 928374L).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("shortVar",  (short) 123).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("integerVar", 1234).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("stringVar", "stringValue").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("booleanVar", true).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("dateVar", date).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("nullVar", null).count());
    
    // Test query for other values on existing variables
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("longVar", 999L).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("shortVar",  (short) 999).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("integerVar", 999).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("stringVar", "999").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("booleanVar", false).count());
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("dateVar", otherDate.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("nullVar", "999").count());
    
    // Test querying for task variables don't match the process-variables 
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("longVar", 928374L).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("shortVar",  (short) 123).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("integerVar", 1234).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("stringVar", "stringValue").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("booleanVar", true).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("dateVar", date).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("nullVar", null).count());
    
    // Test querying for task variables not equals
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("longVar", 999L).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("shortVar",  (short) 999).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("integerVar", 999).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("stringVar", "999").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueNotEquals("booleanVar", false).count());
    
    // and query for the existing variable with NOT shoudl result in nothing found:
    assertEquals(0, taskService.createTaskQuery().processVariableValueNotEquals("longVar", 928374L).count());
    
    // Test value-only variable equals
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals(928374L).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals((short) 123).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals(1234).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals("stringValue").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals(true).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals(date).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEquals(null).count());
    
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals(999999L).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals((short) 999).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals(9999).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals("unexistingstringvalue").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals(false).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEquals(otherDate.getTime()).count());
    
    // Test combination of task-variable and process-variable
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "taskVar", "theValue");
    taskService.setVariableLocal(task.getId(), "longVar", 928374L);
    
    assertEquals(1, taskService.createTaskQuery()
            .processVariableValueEquals("longVar", 928374L)
            .taskVariableValueEquals("taskVar", "theValue")
            .count());
    
    assertEquals(1, taskService.createTaskQuery()
            .processVariableValueEquals("longVar", 928374L)
            .taskVariableValueEquals("longVar", 928374L)
            .count());
    
    assertEquals(1, taskService.createTaskQuery()
            .processVariableValueEquals(928374L)
            .taskVariableValueEquals("theValue")
            .count());
    
    assertEquals(1, taskService.createTaskQuery()
            .processVariableValueEquals(928374L)
            .taskVariableValueEquals(928374L)
            .count());
  }
  
  @Deployment(resources="org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testVariableValueEqualsIgnoreCase() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("mixed", "AzerTY");
    variables.put("upper", "AZERTY");
    variables.put("lower", "azerty");
    taskService.setVariablesLocal(task.getId(), variables);
    
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("mixed", "azerTY").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("mixed", "azerty").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("mixed", "uiop").count());
    
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("upper", "azerTY").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("upper", "azerty").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("upper", "uiop").count());
    
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("lower", "azerTY").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("lower", "azerty").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("lower", "uiop").count());
    
    // Test not-equals
    assertEquals(0, taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("mixed", "azerTY").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("mixed", "azerty").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("mixed", "uiop").count());
    
    assertEquals(0, taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("upper", "azerTY").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("upper", "azerty").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("upper", "uiop").count());
    
    assertEquals(0, taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("lower", "azerTY").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("lower", "azerty").count());
    assertEquals(1, taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("lower", "uiop").count());
    
  }
  
  
  @Deployment(resources="org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueEqualsIgnoreCase() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("mixed", "AzerTY");
    variables.put("upper", "AZERTY");
    variables.put("lower", "azerty");
    
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    assertEquals(1, taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("mixed", "azerTY").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("mixed", "azerty").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("mixed", "uiop").count());
    
    assertEquals(1, taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("upper", "azerTY").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("upper", "azerty").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("upper", "uiop").count());
    
    assertEquals(1, taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("lower", "azerTY").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("lower", "azerty").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("lower", "uiop").count());
  }
  
  @Deployment(resources="org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueLike() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("mixed", "AzerTY");
    
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    assertEquals(1, taskService.createTaskQuery().processVariableValueLike("mixed", "Azer%").count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueLike("mixed", "A%").count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLike("mixed", "a%").count());
  }
  
  @Deployment(resources="org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueGreaterThan() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", 10);
    
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThan("number", 5).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThan("number", 10).count());
  }
  
  @Deployment(resources="org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueGreaterThanOrEquals() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", 10);
    
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThanOrEqual("number", 5).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueGreaterThanOrEqual("number", 10).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueGreaterThanOrEqual("number", 11).count());
  }
  
  @Deployment(resources="org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueLessThan() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", 10);
    
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThan("number", 12).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThan("number", 10).count());
  }
  
  @Deployment(resources="org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueLessThanOrEquals() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", 10);
    
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
    
    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThanOrEqual("number", 12).count());
    assertEquals(1, taskService.createTaskQuery().processVariableValueLessThanOrEqual("number", 10).count());
    assertEquals(0, taskService.createTaskQuery().processVariableValueLessThanOrEqual("number", 8).count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testProcessDefinitionId() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    List<Task> tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list();
    assertEquals(1, tasks.size());
    assertEquals(processInstance.getId(), tasks.get(0).getProcessInstanceId());
    
    assertEquals(0, taskService.createTaskQuery().processDefinitionId("unexisting").count());
  }
  
  
  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testProcessDefinitionKey() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess").list();
    assertEquals(1, tasks.size());
    assertEquals(processInstance.getId(), tasks.get(0).getProcessInstanceId());
    
    assertEquals(0, taskService.createTaskQuery().processDefinitionKey("unexisting").count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testProcessDefinitionName() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    List<Task> tasks = taskService.createTaskQuery().processDefinitionName("The One Task Process").list();
    assertEquals(1, tasks.size());
    assertEquals(processInstance.getId(), tasks.get(0).getProcessInstanceId());
    
    assertEquals(0, taskService.createTaskQuery().processDefinitionName("unexisting").count());
  }
 
  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testProcessInstanceBusinessKey() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-1");
    
    assertEquals(1, taskService.createTaskQuery().processDefinitionName("The One Task Process").processInstanceBusinessKey("BUSINESS-KEY-1").list().size());
    assertEquals(1, taskService.createTaskQuery().processInstanceBusinessKey("BUSINESS-KEY-1").list().size());    
    assertEquals(0, taskService.createTaskQuery().processInstanceBusinessKey("NON-EXISTING").count());
  }
 
  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testTaskDueDate() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // Set due-date on task
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setDueDate(dueDate);
    taskService.saveTask(task);

    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueDate(dueDate).count());
    
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueDate(otherDate.getTime()).count());

    Calendar priorDate = Calendar.getInstance();
    priorDate.setTime(dueDate);
    priorDate.roll(Calendar.YEAR, -1);
    
    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueAfter(priorDate.getTime())
        .count());

    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId())
        .dueBefore(otherDate.getTime()).count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testTaskDueBefore() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // Set due-date on task
    Calendar dueDateCal = Calendar.getInstance();
    task.setDueDate(dueDateCal.getTime());
    taskService.saveTask(task);
    
    Calendar oneHourAgo = Calendar.getInstance();
    oneHourAgo.setTime(dueDateCal.getTime());
    oneHourAgo.add(Calendar.HOUR, -1);
    
    Calendar oneHourLater = Calendar.getInstance();
    oneHourLater.setTime(dueDateCal.getTime());
    oneHourLater.add(Calendar.HOUR, 1);

    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueBefore(oneHourLater.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueBefore(oneHourAgo.getTime()).count());
    
    // Update due-date to null, shouldn't show up anymore in query that matched before
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDueDate(null);
    taskService.saveTask(task);
    
    assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueBefore(oneHourLater.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueBefore(oneHourAgo.getTime()).count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testTaskDueAfter() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // Set due-date on task
    Calendar dueDateCal = Calendar.getInstance();
    task.setDueDate(dueDateCal.getTime());
    taskService.saveTask(task);
    
    Calendar oneHourAgo = Calendar.getInstance();
    oneHourAgo.setTime(dueDateCal.getTime());
    oneHourAgo.add(Calendar.HOUR, -1);
    
    Calendar oneHourLater = Calendar.getInstance();
    oneHourLater.setTime(dueDateCal.getTime());
    oneHourLater.add(Calendar.HOUR, 1);

    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueAfter(oneHourAgo.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueAfter(oneHourLater.getTime()).count());
    
    // Update due-date to null, shouldn't show up anymore in query that matched before
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDueDate(null);
    taskService.saveTask(task);
    
    assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueAfter(oneHourLater.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).dueAfter(oneHourAgo.getTime()).count());
  }
  
  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testTaskWithoutDueDate() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).withoutDueDate().singleResult();
    
    // Set due-date on task
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setDueDate(dueDate);
    taskService.saveTask(task);

    assertEquals(0, taskService.createTaskQuery().processInstanceId(processInstance.getId()).withoutDueDate().count());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    
    // Clear due-date on task
    task.setDueDate(null);
    taskService.saveTask(task);
    
    assertEquals(1, taskService.createTaskQuery().processInstanceId(processInstance.getId()).withoutDueDate().count());
  }
  
  public void testQueryPaging() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateUser("kermit");
    
    assertEquals(11, query.listPage(0, Integer.MAX_VALUE).size());

    // Verifying the un-paged results
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());

    // Verifying paged results
    assertEquals(2, query.listPage(0, 2).size());
    assertEquals(2, query.listPage(2, 2).size());
    assertEquals(3, query.listPage(4, 3).size());
    assertEquals(1, query.listPage(10, 3).size());
    assertEquals(1, query.listPage(10, 1).size());

    // Verifying odd usages
    assertEquals(0, query.listPage(-1, -1).size());
    assertEquals(0, query.listPage(11, 2).size()); // 10 is the last index with a result
    assertEquals(11, query.listPage(0, 15).size()); // there are only 11 tasks
  }
  
  public void testQuerySorting() {
    assertEquals(12, taskService.createTaskQuery().orderByTaskId().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskName().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskPriority().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskAssignee().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskDescription().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByExecutionId().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskCreateTime().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByDueDate().asc().list().size());

    assertEquals(12, taskService.createTaskQuery().orderByTaskId().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskName().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskPriority().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskAssignee().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskDescription().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByExecutionId().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskCreateTime().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByDueDate().desc().list().size());
    
    assertEquals(6, taskService.createTaskQuery().orderByTaskId().taskName("testTask").asc().list().size());
    assertEquals(6, taskService.createTaskQuery().orderByTaskId().taskName("testTask").desc().list().size());
  }
  
  public void testNativeQueryPaging() {
    assertEquals("ACT_RU_TASK", managementService.getTableName(Task.class));
    assertEquals("ACT_RU_TASK", managementService.getTableName(TaskEntity.class));
    assertEquals(5, taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class)).listPage(0, 5).size());
    assertEquals(2, taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class)).listPage(10, 12).size());
  }
  
  public void testNativeQuery() {
    assertEquals("ACT_RU_TASK", managementService.getTableName(Task.class));
    assertEquals("ACT_RU_TASK", managementService.getTableName(TaskEntity.class));
    assertEquals(12, taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class)).list().size());
    assertEquals(12, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class)).count());
    
    assertEquals(144, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM ACT_RU_TASK T1, ACT_RU_TASK T2").count());
    
    // join task and variable instances
    assertEquals(1, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T1, " + managementService.getTableName(VariableInstanceEntity.class)+" V1 WHERE V1.TASK_ID_ = T1.ID_").count());    
    List<Task> tasks = taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class) + " T1, " + managementService.getTableName(VariableInstanceEntity.class)+" V1 WHERE V1.TASK_ID_ = T1.ID_").list();    
    assertEquals(1, tasks.size());
    assertEquals("gonzoTask", tasks.get(0).getName());    
    
    // select with distinct
    assertEquals(12, taskService.createNativeTaskQuery().sql("SELECT DISTINCT T1.* FROM ACT_RU_TASK T1").list().size());    
    
    assertEquals(1, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = 'gonzoTask'").count());
    assertEquals(1, taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = 'gonzoTask'").list().size());
    
    // use parameters
    assertEquals(1, taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = #{taskName}").parameter("taskName", "gonzoTask").count());
  }
  
  /**
   * Test confirming fix for ACT-1731
   */
  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testIncludeBinaryVariables() throws Exception {
    // Start process with a binary variable
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", 
            Collections.singletonMap("binaryVariable", (Object)"It is I, le binary".getBytes()));
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.setVariableLocal(task.getId(), "binaryTaskVariable", (Object)"It is I, le binary".getBytes());
    
    // Query task, including processVariables
    task = taskService.createTaskQuery().taskId(task.getId()).includeProcessVariables().singleResult();
    assertNotNull(task);
    assertNotNull(task.getProcessVariables());
    byte[] bytes = (byte[]) task.getProcessVariables().get("binaryVariable");
    assertEquals("It is I, le binary", new String(bytes));
    
    // Query task, including taskVariables
    task = taskService.createTaskQuery().taskId(task.getId()).includeTaskLocalVariables().singleResult();
    assertNotNull(task);
    assertNotNull(task.getTaskLocalVariables());
    bytes = (byte[]) task.getTaskLocalVariables().get("binaryTaskVariable");
    assertEquals("It is I, le binary", new String(bytes));
  }
  
  /**
   * Generates some test tasks. - 6 tasks where kermit is a candidate - 1 tasks
   * where gonzo is assignee - 2 tasks assigned to management group - 2 tasks
   * assigned to accountancy group - 1 task assigned to both the management and
   * accountancy group
   */
  private List<String> generateTestTasks() throws Exception {
    List<String> ids = new ArrayList<String>();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    // 6 tasks for kermit
    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("01/01/2001 01:01:01.000"));
    for (int i = 0; i < 6; i++) {
      Task task = taskService.newTask();
      task.setName("testTask");
      task.setDescription("testTask description");
      task.setPriority(3);
      taskService.saveTask(task);
      ids.add(task.getId());
      taskService.addCandidateUser(task.getId(), "kermit");
    }

    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("02/02/2002 02:02:02.000"));
    // 1 task for gonzo
    Task task = taskService.newTask();
    task.setName("gonzoTask");
    task.setDescription("gonzo description");
    task.setPriority(4);    
    taskService.saveTask(task);
    taskService.setAssignee(task.getId(), "gonzo");
    taskService.setVariable(task.getId(), "testVar", "someVariable");
    ids.add(task.getId());

    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("03/03/2003 03:03:03.000"));
    // 2 tasks for management group
    for (int i = 0; i < 2; i++) {
      task = taskService.newTask();
      task.setName("managementTask");
      task.setPriority(10);
      taskService.saveTask(task);
      taskService.addCandidateGroup(task.getId(), "management");
      ids.add(task.getId());
    }

    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("04/04/2004 04:04:04.000"));
    // 2 tasks for accountancy group
    for (int i = 0; i < 2; i++) {
      task = taskService.newTask();
      task.setName("accountancyTask");
      task.setName("accountancy description");
      taskService.saveTask(task);
      taskService.addCandidateGroup(task.getId(), "accountancy");
      ids.add(task.getId());
    }

    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("05/05/2005 05:05:05.000"));
    // 1 task assigned to management and accountancy group
    task = taskService.newTask();
    task.setName("managementAndAccountancyTask");
    taskService.saveTask(task);
    taskService.addCandidateGroup(task.getId(), "management");
    taskService.addCandidateGroup(task.getId(), "accountancy");
    ids.add(task.getId());

    return ids;
  }

}
