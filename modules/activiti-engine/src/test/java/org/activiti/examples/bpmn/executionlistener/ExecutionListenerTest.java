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
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.examples.bpmn.executionlistener.CurrentActivityExecutionListener.CurrentActivity;
import org.activiti.examples.bpmn.executionlistener.RecorderExecutionListener.RecordedEvent;

/**
 * @author Frederik Heremans
 */
public class ExecutionListenerTest extends PluggableActivitiTestCase {

  
  @Deployment(resources = {"org/activiti/examples/bpmn/executionlistener/ExecutionListenersProcess.bpmn20.xml"})
  public void testExecutionListenersOnAllPossibleElements() {

    // Process start executionListener will have executionListener class that sets 2 variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess", "businessKey123");
    
    String varSetInExecutionListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");
    assertNotNull(varSetInExecutionListener);
    assertEquals("firstValue", varSetInExecutionListener);
    
    // Check if business key was available in execution listener
    String businessKey = (String) runtimeService.getVariable(processInstance.getId(), "businessKeyInExecution");
    assertNotNull(businessKey);
    assertEquals("businessKey123", businessKey);
    
    // Transition take executionListener will set 2 variables
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertNotNull(task);
    taskService.complete(task.getId());
    
    varSetInExecutionListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");
    
    assertNotNull(varSetInExecutionListener);
    assertEquals("secondValue", varSetInExecutionListener);

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
  
  @Deployment(resources = {"org/activiti/examples/bpmn/executionlistener/ExecutionListenersStartEndEvent.bpmn20.xml"})
  public void testExecutionListenersOnStartEndEvents() {
    RecorderExecutionListener.clear();
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess");
    assertProcessEnded(processInstance.getId());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertEquals(4, recordedEvents.size());
    
    assertEquals("theStart", recordedEvents.get(0).getActivityId());
    assertEquals("Start Event", recordedEvents.get(0).getActivityName());
    assertEquals("Start Event Listener", recordedEvents.get(0).getParameter());
    assertEquals("end", recordedEvents.get(0).getEventName());

    assertEquals("noneEvent", recordedEvents.get(1).getActivityId());
    assertEquals("None Event", recordedEvents.get(1).getActivityName());
    assertEquals("Intermediate Catch Event Listener", recordedEvents.get(1).getParameter());
    assertEquals("end", recordedEvents.get(1).getEventName());
    
    assertEquals("signalEvent", recordedEvents.get(2).getActivityId());
    assertEquals("Signal Event", recordedEvents.get(2).getActivityName());
    assertEquals("Intermediate Throw Event Listener", recordedEvents.get(2).getParameter());
    assertEquals("start", recordedEvents.get(2).getEventName());
    
    assertEquals("theEnd", recordedEvents.get(3).getActivityId());
    assertEquals("End Event", recordedEvents.get(3).getActivityName());
    assertEquals("End Event Listener", recordedEvents.get(3).getParameter());
    assertEquals("start", recordedEvents.get(3).getEventName());
    
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
  
  @Deployment(resources = {"org/activiti/examples/bpmn/executionlistener/ExecutionListenersCurrentActivity.bpmn20.xml"})
  public void testExecutionListenerCurrentActivity() {
    
    CurrentActivityExecutionListener.clear();
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess");
    assertProcessEnded(processInstance.getId());

    List<CurrentActivity> currentActivities = CurrentActivityExecutionListener.getCurrentActivities();
    assertEquals(3, currentActivities.size());
    
    assertEquals("theStart", currentActivities.get(0).getActivityId());
    assertEquals("Start Event", currentActivities.get(0).getActivityName());

    assertEquals("noneEvent", currentActivities.get(1).getActivityId());
    assertEquals("None Event", currentActivities.get(1).getActivityName());
    
    assertEquals("theEnd", currentActivities.get(2).getActivityId());
    assertEquals("End Event", currentActivities.get(2).getActivityName());
  }
}
