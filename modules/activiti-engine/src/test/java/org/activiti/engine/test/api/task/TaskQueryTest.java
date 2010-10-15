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
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class TaskQueryTest extends ActivitiInternalTestCase {

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
    taskService.deleteTasks(taskIds);
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
    TaskQuery query = taskService.createTaskQuery().name("testTask");
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
    TaskQuery query = taskService.createTaskQuery().name("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().name(null).singleResult();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }
  }
  
  public void testQueryByNameLike() {
    TaskQuery query = taskService.createTaskQuery().nameLike("gonzo%");
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidNameLike() {
    TaskQuery query = taskService.createTaskQuery().name("1");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().name(null).singleResult();
      fail();
    } catch (ActivitiException e) { }
  }
  
  public void testQueryByDescription() {
    TaskQuery query = taskService.createTaskQuery().description("testTask description");
    assertEquals(6, query.list().size());
    assertEquals(6, query.count());
    
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByInvalidDescription() {
    TaskQuery query = taskService.createTaskQuery().description("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().description(null).list();
      fail();
    } catch (ActivitiException e) {
      
    }
  }
  
  public void testQueryByDescriptionLike() {
    TaskQuery query = taskService.createTaskQuery().descriptionLike("%gonzo%");
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidDescriptionLike() {
    TaskQuery query = taskService.createTaskQuery().descriptionLike("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      taskService.createTaskQuery().descriptionLike(null).list();
      fail();
    } catch (ActivitiException e) {
      
    }
  }
  
  public void testQueryByPriority() {
    TaskQuery query = taskService.createTaskQuery().priority(10);
    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
    
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
    
    query = taskService.createTaskQuery().priority(100);
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  public void testQueryByInvalidPriority() {
    try {
      taskService.createTaskQuery().priority(null);
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }
  }
  
  public void testQueryByAssignee() {
    TaskQuery query = taskService.createTaskQuery().assignee("gonzo");
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());

    query = taskService.createTaskQuery().assignee("kermit");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
    assertNull(query.singleResult());
  }
  
  public void testQueryByNullAssignee() {
    try {
      taskService.createTaskQuery().assignee(null).list();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }
  }

  public void testQueryByUnassigned() {
    TaskQuery query = taskService.createTaskQuery().unnassigned();
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());
  }
  
  public void testQueryByCandidateUser() {
    TaskQuery query = taskService.createTaskQuery().candidateUser("kermit");
    assertEquals(11, query.count());
    assertEquals(11, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }

    query = taskService.createTaskQuery().candidateUser("fozzie");
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
      taskService.createTaskQuery().candidateUser(null).list();
      fail();
    } catch(ActivitiException e) {}
  }
  
  public void testQueryByCandidateGroup() {
    TaskQuery query = taskService.createTaskQuery().candidateGroup("management");
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());
    try {
      query.singleResult();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }

    query = taskService.createTaskQuery().candidateGroup("sales");
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }
  
  public void testQueryByNullCandidateGroup() {
    try {
      taskService.createTaskQuery().candidateGroup(null).list();
      fail("expected exception");
    } catch (ActivitiException e) {
      // OK
    }
  }
  
  public void testQueryCreatedOn() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    
    // Exact matching of createTime, should result in 6 tasks
    Date createTime = sdf.parse("01/01/2001 01:01:01.000");
    
    TaskQuery query = taskService.createTaskQuery().createdOn(createTime);
    assertEquals(6, query.count());
    assertEquals(6, query.list().size());
  }
  
  public void testQueryCreatedBefore() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    
    // Should result in 7 tasks
    Date before = sdf.parse("03/02/2002 02:02:02.000");
    
    TaskQuery query = taskService.createTaskQuery().createdBefore(before);
    assertEquals(7, query.count());
    assertEquals(7, query.list().size());
    
    before = sdf.parse("01/01/2001 01:01:01.000");
    query = taskService.createTaskQuery().createdBefore(before);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }
  
  public void testQueryCreatedAfter() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    
    // Should result in 3 tasks
    Date after = sdf.parse("03/03/2003 03:03:03.000");
    
    TaskQuery query = taskService.createTaskQuery().createdAfter(after);
    assertEquals(3, query.count());
    assertEquals(3, query.list().size());
    
    after = sdf.parse("05/05/2005 05:05:05.000");
    query = taskService.createTaskQuery().createdAfter(after);
    assertEquals(0, query.count());
    assertEquals(0, query.list().size());
  }
  
  public void testQueryPaging() {
    TaskQuery query = taskService.createTaskQuery().candidateUser("kermit");

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
    assertEquals(12, taskService.createTaskQuery().orderByName().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByPriority().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByAssignee().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByDescription().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByProcessInstanceId().asc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByExecutionId().asc().list().size());

    assertEquals(12, taskService.createTaskQuery().orderByTaskId().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByName().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByPriority().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByAssignee().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByDescription().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByProcessInstanceId().desc().list().size());
    assertEquals(12, taskService.createTaskQuery().orderByExecutionId().desc().list().size());
    
    assertEquals(6, taskService.createTaskQuery().orderByTaskId().name("testTask").asc().list().size());
    assertEquals(6, taskService.createTaskQuery().orderByTaskId().name("testTask").desc().list().size());
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
