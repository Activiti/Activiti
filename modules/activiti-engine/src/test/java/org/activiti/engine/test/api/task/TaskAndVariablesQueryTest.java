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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Tijs Rademakers
 */
public class TaskAndVariablesQueryTest extends PluggableActivitiTestCase {

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
  
  @Deployment
  public void testQuery() {
    Task task = taskService.createTaskQuery().includeTaskLocalVariables().taskAssignee("gonzo").singleResult();
    Map<String, Object> variableMap = task.getTaskLocalVariables();
    assertEquals(3, variableMap.size());
    assertEquals(0, task.getProcessVariables().size());
    assertNotNull(variableMap.get("testVar"));
    assertEquals("someVariable", variableMap.get("testVar"));
    assertNotNull(variableMap.get("testVar2"));
    assertEquals(123, variableMap.get("testVar2"));
    assertNotNull(variableMap.get("testVarBinary"));
    assertEquals("This is a binary variable", new String((byte[]) variableMap.get("testVarBinary")));
    
    List<Task> tasks = taskService.createTaskQuery().list();
    assertEquals(3, tasks.size());
    
    task = taskService.createTaskQuery().includeProcessVariables().taskAssignee("gonzo").singleResult();
    assertEquals(0, task.getProcessVariables().size());
    assertEquals(0, task.getTaskLocalVariables().size());
    
    Map<String, Object> startMap = new HashMap<String, Object>();
    startMap.put("processVar", true);
    startMap.put("binaryVariable", "This is a binary process variable".getBytes());
    runtimeService.startProcessInstanceByKey("oneTaskProcess", startMap);
    
    task = taskService.createTaskQuery().includeProcessVariables().taskAssignee("kermit").singleResult();
    assertEquals(2, task.getProcessVariables().size());
    assertEquals(0, task.getTaskLocalVariables().size());
    assertTrue((Boolean) task.getProcessVariables().get("processVar"));
    assertEquals("This is a binary process variable", new String((byte[]) task.getProcessVariables().get("binaryVariable")));
    
    taskService.setVariable(task.getId(), "anotherProcessVar", 123);
    taskService.setVariableLocal(task.getId(), "localVar", "test");
    
    task = taskService.createTaskQuery().includeTaskLocalVariables().taskAssignee("kermit").singleResult();
    assertEquals(0, task.getProcessVariables().size());
    assertEquals(1, task.getTaskLocalVariables().size());
    assertEquals("test", task.getTaskLocalVariables().get("localVar"));
    
    task =  taskService.createTaskQuery().includeProcessVariables().taskAssignee("kermit").singleResult();
    assertEquals(3, task.getProcessVariables().size());
    assertEquals(0, task.getTaskLocalVariables().size());
    assertEquals(true, task.getProcessVariables().get("processVar"));
    assertEquals(123, task.getProcessVariables().get("anotherProcessVar"));
    assertEquals("This is a binary process variable", new String((byte[]) task.getProcessVariables().get("binaryVariable")));
    
    tasks = taskService.createTaskQuery().includeTaskLocalVariables().taskCandidateUser("kermit").list();
    assertEquals(2, tasks.size());
    assertEquals(2, tasks.get(0).getTaskLocalVariables().size());
    assertEquals("test", tasks.get(0).getTaskLocalVariables().get("test"));
    assertEquals(0, tasks.get(0).getProcessVariables().size());
    
    tasks = taskService.createTaskQuery().includeProcessVariables().taskCandidateUser("kermit").list();
    assertEquals(2, tasks.size());
    assertEquals(0, tasks.get(0).getProcessVariables().size());
    assertEquals(0, tasks.get(0).getTaskLocalVariables().size());
    
    task = taskService.createTaskQuery().includeTaskLocalVariables().taskAssignee("kermit").taskVariableValueEquals("localVar", "test").singleResult();
    assertEquals(0, task.getProcessVariables().size());
    assertEquals(1, task.getTaskLocalVariables().size());
    assertEquals("test", task.getTaskLocalVariables().get("localVar"));
    
    task = taskService.createTaskQuery().includeProcessVariables().taskAssignee("kermit").taskVariableValueEquals("localVar", "test").singleResult();
    assertEquals(3, task.getProcessVariables().size());
    assertEquals(0, task.getTaskLocalVariables().size());
    assertEquals(true, task.getProcessVariables().get("processVar"));
    assertEquals(123, task.getProcessVariables().get("anotherProcessVar"));
    
    task = taskService.createTaskQuery().includeTaskLocalVariables().includeProcessVariables().taskAssignee("kermit").singleResult();
    assertEquals(3, task.getProcessVariables().size());
    assertEquals(1, task.getTaskLocalVariables().size());
    assertEquals("test", task.getTaskLocalVariables().get("localVar"));
    assertEquals(true, task.getProcessVariables().get("processVar"));
    assertEquals(123, task.getProcessVariables().get("anotherProcessVar"));
    assertEquals("This is a binary process variable", new String((byte[]) task.getProcessVariables().get("binaryVariable")));
  }
  
  public void testQueryWithPagingAndVariables() {
    List<Task> tasks = taskService.createTaskQuery()
        .includeProcessVariables()
        .includeTaskLocalVariables()
        .orderByTaskPriority()
        .desc()
        .listPage(0, 1);
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    Map<String, Object> variableMap = task.getTaskLocalVariables();
    assertEquals(3, variableMap.size());
    assertEquals("someVariable", variableMap.get("testVar"));
    assertEquals(123, variableMap.get("testVar2"));
    assertEquals("This is a binary variable", new String((byte[]) variableMap.get("testVarBinary")));
    
    tasks = taskService.createTaskQuery()
        .includeProcessVariables()
        .includeTaskLocalVariables()
        .orderByTaskPriority()
        .asc()
        .listPage(1, 2);
    assertEquals(2, tasks.size());
    task = tasks.get(1);
    variableMap = task.getTaskLocalVariables();
    assertEquals(3, variableMap.size());
    assertEquals("someVariable", variableMap.get("testVar"));
    assertEquals(123, variableMap.get("testVar2"));
    assertEquals("This is a binary variable", new String((byte[]) variableMap.get("testVarBinary")));
    
    tasks = taskService.createTaskQuery()
        .includeProcessVariables()
        .includeTaskLocalVariables()
        .orderByTaskPriority()
        .asc()
        .listPage(2, 4);
    assertEquals(1, tasks.size());
    task = tasks.get(0);
    variableMap = task.getTaskLocalVariables();
    assertEquals(3, variableMap.size());
    assertEquals("someVariable", variableMap.get("testVar"));
    assertEquals(123, variableMap.get("testVar2"));
    assertEquals("This is a binary variable", new String((byte[]) variableMap.get("testVarBinary")));
    
    tasks = taskService.createTaskQuery()
        .includeProcessVariables()
        .includeTaskLocalVariables()
        .orderByTaskPriority()
        .asc()
        .listPage(4, 2);
    assertEquals(0, tasks.size());
  }
  
  /**
   * Generates some test tasks. - 2 tasks where kermit is a candidate and 1 task
   * where gonzo is assignee
   */
  private List<String> generateTestTasks() throws Exception {
    List<String> ids = new ArrayList<String>();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    // 2 tasks for kermit
    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("01/01/2001 01:01:01.000"));
    for (int i = 0; i < 2; i++) {
      Task task = taskService.newTask();
      task.setName("testTask");
      task.setDescription("testTask description");
      task.setPriority(3);
      taskService.saveTask(task);
      ids.add(task.getId());
      taskService.setVariableLocal(task.getId(), "test", "test");
      taskService.setVariableLocal(task.getId(), "testBinary", "This is a binary variable".getBytes());
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
    taskService.setVariableLocal(task.getId(), "testVar", "someVariable");
    taskService.setVariableLocal(task.getId(), "testVarBinary", "This is a binary variable".getBytes());
    taskService.setVariableLocal(task.getId(), "testVar2", 123);
    ids.add(task.getId());

    return ids;
  }

}
