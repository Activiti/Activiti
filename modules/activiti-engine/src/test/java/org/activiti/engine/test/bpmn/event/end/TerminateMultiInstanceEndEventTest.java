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
  public void testSimpleSubprocess() {
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

}
