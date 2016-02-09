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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.EventSubscriptionQueryImpl;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class MessageStartEventTest extends PluggableActivitiTestCase {
  
  public void testDeploymentCreatesSubscriptions() {
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
      .deploy()
      .getId();
    
    List<EventSubscriptionEntity> eventSubscriptions = new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor())
      .list();
    
    assertEquals(1, eventSubscriptions.size());
    
    repositoryService.deleteDeployment(deploymentId);    
  }
  
  public void testSameMessageNameFails() {
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
      .deploy()
      .getId();
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/message/otherProcessWithNewInvoiceMessage.bpmn20.xml")
        .deploy();
      fail("exception expected");
    }catch (ActivitiException e) {
      assertTrue(e.getMessage().contains("there already is a message event subscription for the message with name"));
    }
    
    // clean db:
    repositoryService.deleteDeployment(deploymentId);
    
  }
  
  public void testSameMessageNameInSameProcessFails() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/event/message/testSameMessageNameInSameProcessFails.bpmn20.xml")
        .deploy();
      fail("exception expected: Cannot have more than one message event subscription with name 'newInvoiceMessage' for scope");
    }catch (ActivitiException e) {
      e.printStackTrace();
    }        
  }
  
  public void testUpdateProcessVersionCancelsSubscriptions() {
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
      .deploy()
      .getId();
    
    List<EventSubscriptionEntity> eventSubscriptions = new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor()).list();
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        
    assertEquals(1, eventSubscriptions.size());
    assertEquals(1, processDefinitions.size());
    
    String newDeploymentId  = repositoryService
      .createDeployment()
      .addClasspathResource("org/activiti/engine/test/bpmn/event/message/MessageStartEventTest.testSingleMessageStartEvent.bpmn20.xml")
      .deploy()
      .getId();
    
    List<EventSubscriptionEntity> newEventSubscriptions = new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutor()).list();
    List<ProcessDefinition> newProcessDefinitions = repositoryService.createProcessDefinitionQuery().list();
        
    assertEquals(1, newEventSubscriptions.size());
    
    assertEquals(2, newProcessDefinitions.size());
    int version1Count = 0;
    int version2Count = 0;
    for (ProcessDefinition processDefinition : newProcessDefinitions) {
      if(processDefinition.getVersion() == 1) {
        for (EventSubscriptionEntity subscription : newEventSubscriptions) {
          if (subscription.getConfiguration().equals(processDefinition.getId())) {
            version1Count++;
          }
        }
      } else {
        for (EventSubscriptionEntity subscription : newEventSubscriptions) {
          if (subscription.getConfiguration().equals(processDefinition.getId())) {
            version2Count++;
          }
        }
      }
    }
    assertEquals(0, version1Count);
    assertEquals(1, version2Count);
    assertFalse(eventSubscriptions.equals(newEventSubscriptions));
    
    repositoryService.deleteDeployment(deploymentId);   
    repositoryService.deleteDeployment(newDeploymentId);
  }
  
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
    
    // starting the process using startProcessInstanceByKey is possible, the first message start event will be the default:
    processInstance = runtimeService.startProcessInstanceByKey("testProcess");
    assertFalse(processInstance.isEnded());
    task = taskService.createTaskQuery().taskDefinitionKey("taskAfterMessageStart").singleResult();
    assertNotNull(task);
    taskService.complete(task.getId()); 
    assertProcessEnded(processInstance.getId());
  }
  
}
