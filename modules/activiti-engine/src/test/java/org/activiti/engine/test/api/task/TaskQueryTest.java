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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
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
    } catch (ActivitiException e) {
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
    } catch (ActivitiException e) {
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
    TaskQuery query = taskService.createTaskQuery().taskName("1");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().taskName(null).singleResult();
      fail();
    } catch (ActivitiException e) { }
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
    } catch (ActivitiException e) {
      
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
    } catch (ActivitiException e) {
      
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
  }
  
  public void testQueryByInvalidPriority() {
    try {
      taskService.createTaskQuery().taskPriority(null);
      fail("expected exception");
    } catch (ActivitiException e) {
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
  
  public void testQueryByNullAssignee() {
    try {
      taskService.createTaskQuery().taskAssignee(null).list();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }
  }

  public void testQueryByUnassigned() {
    TaskQuery query = taskService.createTaskQuery().taskUnnassigned();
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
    } catch(ActivitiException e) {}
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
  
  public void testQueryByNullCandidateGroup() {
    try {
      taskService.createTaskQuery().taskCandidateGroup(null).list();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }
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
    
    // Test query for other values on exixting variables
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("longVar", 999L).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("shortVar",  (short) 999).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("integerVar", 999).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("stringVar", "999").count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("booleanVar", false).count());
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("dateVar", otherDate.getTime()).count());
    assertEquals(0, taskService.createTaskQuery().taskVariableValueEquals("nullVar", "999").count());
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
  
  public void testQueryPaging() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateUser("kermit");

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

    assertEquals(12, taskService.createTaskQuery().orderByTaskId().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskName().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskPriority().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskAssignee().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskDescription().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByExecutionId().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByTaskCreateTime().desc().list().size());
    
    assertEquals(6, taskService.createTaskQuery().orderByTaskId().taskName("testTask").asc().list().size());
    assertEquals(6, taskService.createTaskQuery().orderByTaskId().taskName("testTask").desc().list().size());
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
    ClockUtil.setCurrentTime(sdf.parse("01/01/2001 01:01:01.000"));
    for (int i = 0; i < 6; i++) {
      Task task = taskService.newTask();
      task.setName("testTask");
      task.setDescription("testTask description");
      task.setPriority(3);
      taskService.saveTask(task);
      ids.add(task.getId());
      taskService.addCandidateUser(task.getId(), "kermit");
    }

    ClockUtil.setCurrentTime(sdf.parse("02/02/2002 02:02:02.000"));
    // 1 task for gonzo
    Task task = taskService.newTask();
    task.setName("gonzoTask");
    task.setDescription("gonzo description");
    task.setPriority(4);
    taskService.saveTask(task);
    taskService.setAssignee(task.getId(), "gonzo");
    ids.add(task.getId());

    ClockUtil.setCurrentTime(sdf.parse("03/03/2003 03:03:03.000"));
    // 2 tasks for management group
    for (int i = 0; i < 2; i++) {
      task = taskService.newTask();
      task.setName("managementTask");
      task.setPriority(10);
      taskService.saveTask(task);
      taskService.addCandidateGroup(task.getId(), "management");
      ids.add(task.getId());
    }

    ClockUtil.setCurrentTime(sdf.parse("04/04/2004 04:04:04.000"));
    // 2 tasks for accountancy group
    for (int i = 0; i < 2; i++) {
      task = taskService.newTask();
      task.setName("accountancyTask");
      task.setName("accountancy description");
      taskService.saveTask(task);
      taskService.addCandidateGroup(task.getId(), "accountancy");
      ids.add(task.getId());
    }

    ClockUtil.setCurrentTime(sdf.parse("05/05/2005 05:05:05.000"));
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
