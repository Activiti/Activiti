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
package org.activiti.compatibility.test;

import java.util.List;

import org.activiti.engine.task.Task;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParseHandlerTest extends AbstractActiviti6CompatibilityTest {

  @Test
  public void testActiviti5ParseHandlersApplied() {
      Task task = taskService.createTaskQuery().processDefinitionKey("parseHandlerTestProcess").singleResult();
      assertNotNull(task);
      assertEquals("The task-activiti 5", task.getName());
      
      runtimeService.startProcessInstanceByKey("parseHandlerTestProcess");
      List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("parseHandlerTestProcess").list();
      assertEquals(2, tasks.size());
      for (Task t : tasks) {
        assertEquals("The task-activiti 5", t.getName());
        taskService.complete(t.getId());
      }
      
      
      // Redeploy, the parse handler should NOT have been reapplied
      repositoryService.createDeployment().addClasspathResource("parseHandlerProcess.bpmn20.xml").deploy();
      runtimeService.startProcessInstanceByKey("parseHandlerTestProcess");
      task = taskService.createTaskQuery().processDefinitionKey("parseHandlerTestProcess").singleResult();
      assertEquals("The task", task.getName());
  }

}
