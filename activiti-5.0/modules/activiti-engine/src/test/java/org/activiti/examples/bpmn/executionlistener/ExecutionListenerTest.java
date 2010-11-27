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

package org.activiti.examples.bpmn.executionlistener;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Frederik Heremans
 */
public class ExecutionListenerTest extends PluggableActivitiTestCase {

  
  @Deployment(resources = {"org/activiti/examples/bpmn/executionlistener/ExecutionListenersProcess.bpmn20.xml"})
  public void testExecutionListenersOnAllPossibleElements() {

    // Process start executionListener will have executionListener class that sets 2 variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess");
    
    String varSetInExecutionListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");
    String eventNameReceived = (String) runtimeService.getVariable(processInstance.getId(), "eventNameReceived");
    
    assertNotNull(varSetInExecutionListener);
    assertEquals("firstValue", varSetInExecutionListener);
    assertNotNull(eventNameReceived);
    assertEquals("start", eventNameReceived);
    
    // Transition take executionListener will set 2 variables
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    varSetInExecutionListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");
    eventNameReceived = (String) runtimeService.getVariable(processInstance.getId(), "eventNameReceived");
    
    assertNotNull(varSetInExecutionListener);
    assertEquals("secondValue", varSetInExecutionListener);
    assertNotNull(eventNameReceived);
    assertEquals("take", eventNameReceived);

    ExampleExecutionListenerPojo myPojo = new ExampleExecutionListenerPojo();
    runtimeService.setVariable(processInstance.getId(), "myPojo", myPojo);
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    // First usertask uses a method-expression as executionListener: ${myPojo.myMethod(execution.eventName)}
    ExampleExecutionListenerPojo pojoVariable = (ExampleExecutionListenerPojo) runtimeService.getVariable(processInstance.getId(), "myPojo");
    assertNotNull(pojoVariable.getReceivedEventName());
    assertEquals("end", pojoVariable.getReceivedEventName());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment(resources = {"org/activiti/examples/bpmn/executionlistener/ExecutionListenersFieldInjectionProcess.bpmn20.xml"})
  public void testExecutionListenerFieldInjection() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("myVar", "listening!");
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess", variables);
    
    Object varSetByListener = runtimeService.getVariable(processInstance.getId(), "var");
    assertNotNull(varSetByListener);
    assertTrue(varSetByListener instanceof String);
    
    // Result is a concatenation of fixed injected field and injected expression
    assertEquals("Yes, I am listening!", varSetByListener);
  }
}
