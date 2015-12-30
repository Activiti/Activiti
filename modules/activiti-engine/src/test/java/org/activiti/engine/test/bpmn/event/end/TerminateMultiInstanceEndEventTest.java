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
package org.activiti.engine.test.bpmn.event.end;

import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class TerminateMultiInstanceEndEventTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testMultiInstanceEmbeddedSubprocess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMi");
    
    Task aTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(aTask.getId());
    
    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(8, bTasks.size());
    
    // Complete 2 tasks by going to task C. The 3th tasks goes to the MI terminate end and shuts down the MI.
    for (int i=0; i<2; i++) {
      Task bTask = bTasks.get(i);
      taskService.complete(bTask.getId(), CollectionUtil.singletonMap("myVar", "toC"));
    }
    
    bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertEquals(6, bTasks.size());
    
    taskService.complete(bTasks.get(0).getId(), CollectionUtil.singletonMap("myVar", "toEnd"));
    
    Task afterMiTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("AfterMi", afterMiTask.getName());
    taskService.complete(afterMiTask.getId());
    
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }
  
  @Deployment
  public void testMultiInstanceEmbeddedSubprocessSequential() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMi");
    
    Task aTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(aTask.getId());
    
    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, bTasks.size());
    taskService.complete(bTasks.get(0).getId(), CollectionUtil.singletonMap("myVar", "toC"));
    
    List<Task> cTasks = taskService.createTaskQuery().taskName("C").list();
    assertEquals(1, cTasks.size());
    taskService.complete(cTasks.get(0).getId());
    
    bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskName("B").list();
    assertEquals(1, bTasks.size());
    taskService.complete(bTasks.get(0).getId(), CollectionUtil.singletonMap("myVar", "toEnd"));
    
    Task afterMiTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("AfterMi", afterMiTask.getName());
    taskService.complete(afterMiTask.getId());
    
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }
  
  @Deployment
  public void testMultiInstanceEmbeddedSubprocess2() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMi");
    
    Task aTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(aTask.getId());
    
    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(5, bTasks.size());
    
    // Complete one b task to get one C and D
    taskService.complete(bTasks.get(0).getId());
    
    // C and D should now be active
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(6, tasks.size());
    // 0-3 are B tasks
    assertEquals("C", tasks.get(4).getName());
    assertEquals("D", tasks.get(5).getName());
    
    // Completing C should terminate the multi instance
    taskService.complete(tasks.get(4).getId());
    
    Task afterMiTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("AfterMi", afterMiTask.getName());
    taskService.complete(afterMiTask.getId());
    
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }
  
  @Deployment
  public void testMultiInstanceEmbeddedSubprocess2Sequential() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMi");
    
    Task aTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(aTask.getId());
    
    List<Task> bTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, bTasks.size());
    
    // Complete one b task to get one C and D
    taskService.complete(bTasks.get(0).getId());
    
    // C and D should now be active
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(2, tasks.size());
    assertEquals("C", tasks.get(0).getName());
    assertEquals("D", tasks.get(1).getName());
    
    // Completing C should terminate the multi instance
    taskService.complete(tasks.get(0).getId());
    
    Task afterMiTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("AfterMi", afterMiTask.getName());
    taskService.complete(afterMiTask.getId());
    
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }
  
  @Deployment(resources = {
      "org/activiti/engine/test/bpmn/event/end/TerminateMultiInstanceEndEventTest.testTerminateMiCallactivity-parentProcess.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/end/TerminateMultiInstanceEndEventTest.testTerminateMiCallactivity-calledProcess.bpmn20.xml"
  })
  public void testTerminateMiCallactivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMiCallActivity");
    
    Task taskA = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("A", taskA.getName());
    taskService.complete(taskA.getId());
    
    // After completing A, four B's should be active (due to the call activity)
    List<Task> bTasks = taskService.createTaskQuery().taskName("B").list();
    assertEquals(4, bTasks.size());
    
    // Compelting 3 B tasks, giving 3 C's and D's
    for (int i=0; i<3; i++) {
      taskService.complete(bTasks.get(i).getId());
    }
    
    List<Task> cTasks = taskService.createTaskQuery().taskName("C").list();
    assertEquals(3, cTasks.size());
    List<Task> dTasks = taskService.createTaskQuery().taskName("D").list();
    assertEquals(3, dTasks.size());
    
    // Completing one of the C tasks should terminate the whole multi instance
    taskService.complete(cTasks.get(0).getId());
    
    List<Task> afterMiTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(2, afterMiTasks.size());
    assertEquals("AfterMi", afterMiTasks.get(0).getName());
    assertEquals("Parallel task", afterMiTasks.get(1).getName());
  }
  
  @Deployment(resources = {
      "org/activiti/engine/test/bpmn/event/end/TerminateMultiInstanceEndEventTest.testTerminateMiCallactivity-parentProcessSequential.bpmn20.xml",
      "org/activiti/engine/test/bpmn/event/end/TerminateMultiInstanceEndEventTest.testTerminateMiCallactivity-calledProcess.bpmn20.xml"
  })
  public void testTerminateMiCallactivitySequential() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("terminateMiCallActivity");
    
    Task taskA = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("A", taskA.getName());
    taskService.complete(taskA.getId());
    
    List<Task> bTasks = taskService.createTaskQuery().taskName("B").list();
    assertEquals(1, bTasks.size());
    taskService.complete(bTasks.get(0).getId());
    
    List<Task> cTasks = taskService.createTaskQuery().taskName("C").list();
    assertEquals(1, cTasks.size());
    List<Task> dTasks = taskService.createTaskQuery().taskName("D").list();
    assertEquals(1, dTasks.size());
    
    // Completing one of the C tasks should terminate the whole multi instance
    taskService.complete(cTasks.get(0).getId());
    
    List<Task> afterMiTasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderByTaskName().asc().list();
    assertEquals(2, afterMiTasks.size());
    assertEquals("AfterMi", afterMiTasks.get(0).getName());
    assertEquals("Parallel task", afterMiTasks.get(1).getName());
  }

}
