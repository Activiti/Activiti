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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Daniel Meyer
 */
public class MessageStartEventTest extends PluggableActivitiTestCase {
  
  @Deployment
  public void testSingleMessageStartEvent() {
    
    // using startProcessInstanceByMessage triggers the message start event
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");
    
    assertFalse(processInstance.isEnded());
    
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // using startProcessInstanceByKey also triggers the message event, if there is a single start event
    
    processInstance = runtimeService.startProcessInstanceByKey("singleMessageStartEvent");
    
    assertFalse(processInstance.isEnded());
    
    task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
            
  }
 
  
  @Deployment
  public void testMessageStartEventAndNoneStartEvent() {
    
    // using startProcessInstanceByKey triggers the none start event
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    
    assertFalse(processInstance.isEnded());
    
    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterNoneStart").singleResult();
    assertNotNull(task);
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // using startProcessInstanceByMessage triggers the message start event
    
    processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");
    
    assertFalse(processInstance.isEnded());
    
    task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart").singleResult();
    assertNotNull(task);
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
            
  }

  @Deployment
  public void testMultipleMessageStartEvents() {
    
    // sending newInvoiceMessage
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage");
    
    assertFalse(processInstance.isEnded());
    
    Task task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart").singleResult();
    assertNotNull(task);
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // sending newInvoiceMessage2
    
    processInstance = runtimeService.startProcessInstanceByMessage("newInvoiceMessage2");
    
    assertFalse(processInstance.isEnded());
    
    task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart2").singleResult();
    assertNotNull(task);
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // starting the process using startProcessInstanceByKey is not possible:
    try {
      runtimeService.startProcessInstanceByKey("testProcess");
      fail("exception expected");
    }catch (ActivitiException e) {
      assertTrue("different exception expected", e.getMessage().contains("Cannot start process instance, initial is null"));
    }
    
  }
  
}
