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

package org.activiti.engine.test.bpmn.event.message;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Daniel Meyer
 */
public class MessageBoundaryEventTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testSingleBoundaryMessageEvent() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
    
    assertEquals(2, runtimeService.createExecutionQuery().count());
    
    Task userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    
    Execution execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNotNull(execution);
    
    // 1. case: message received cancels the task
    
    runtimeService.messageEventReceived("messageName", execution.getId());
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterMessage", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
    // 2nd. case: complete the user task cancels the message subscription
    
    processInstance = runtimeService.startProcessInstanceByKey("process");

    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    taskService.complete(userTask.getId());
    
    execution = runtimeService.createExecutionQuery()
      .messageEventSubscriptionName("messageName")
      .singleResult();
    assertNull(execution);    
    
    userTask = taskService.createTaskQuery().singleResult();
    assertNotNull(userTask);
    assertEquals("taskAfterTask", userTask.getTaskDefinitionKey());
    taskService.complete(userTask.getId());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
    
  }

}
