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

package org.activiti.examples.bpmn.eventlistener;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Frederik Heremans
 */
public class EventListenerTest extends ActivitiInternalTestCase {

  
  @Deployment(resources = {"org/activiti/examples/bpmn/eventlistener/EventListenersProcess.bpmn20.xml"})
  public void testEventListenersOnAllPossibleElements() {

    // Process start event-listener will have event-listener class that sets 2 variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("eventListenersProcess");
    
    String varSetInEventListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInEventListener");
    String eventNameReceived = (String) runtimeService.getVariable(processInstance.getId(), "eventNameReceived");
    
    assertNotNull(varSetInEventListener);
    assertEquals("firstValue", varSetInEventListener);
    assertNotNull(eventNameReceived);
    assertEquals("start", eventNameReceived);
    
    // Transition take event-listener will set 2 variables
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    varSetInEventListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInEventListener");
    eventNameReceived = (String) runtimeService.getVariable(processInstance.getId(), "eventNameReceived");
    
    assertNotNull(varSetInEventListener);
    assertEquals("secondValue", varSetInEventListener);
    assertNotNull(eventNameReceived);
    assertEquals("take", eventNameReceived);

    ExampleEventListenerPojo myPojo = new ExampleEventListenerPojo();
    runtimeService.setVariable(processInstance.getId(), "myPojo", myPojo);
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    // First usertask uses a method-expression as event-listener: ${myPojo.myMethod(execution.eventName)}
    ExampleEventListenerPojo pojoVariable = (ExampleEventListenerPojo) runtimeService.getVariable(processInstance.getId(), "myPojo");
    assertNotNull(pojoVariable.getReceivedEventName());
    assertEquals("end", pojoVariable.getReceivedEventName());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment(resources = {"org/activiti/examples/bpmn/eventlistener/EventListenersFieldInjectionProcess.bpmn20.xml"})
  public void testEventListenerFieldInjection() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("myVar", "listening!");
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("eventListenersProcess", variables);
    
    Object varSetByListener = runtimeService.getVariable(processInstance.getId(), "var");
    assertNotNull(varSetByListener);
    assertTrue(varSetByListener instanceof String);
    
    // Result is a concatenation of fixed injected field and injected value-expression
    assertEquals("Yes, I am listening!", varSetByListener);
  }
}
