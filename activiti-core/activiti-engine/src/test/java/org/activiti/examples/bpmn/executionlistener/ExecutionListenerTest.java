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

import static org.assertj.core.api.Assertions.assertThat;

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

 */
public class ExecutionListenerTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/examples/bpmn/executionlistener/ExecutionListenersProcess.bpmn20.xml" })
  public void testExecutionListenersOnAllPossibleElements() {
    RecorderExecutionListener.clear();

    // Process start executionListener will have executionListener class
    // that sets 2 variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess", "businessKey123");

    String varSetInExecutionListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");
    assertThat(varSetInExecutionListener).isNotNull();
    assertThat(varSetInExecutionListener).isEqualTo("firstValue");

    // Check if business key was available in execution listener
    String businessKey = (String) runtimeService.getVariable(processInstance.getId(), "businessKeyInExecution");
    assertThat(businessKey).isNotNull();
    assertThat(businessKey).isEqualTo("businessKey123");

    // Transition take executionListener will set 2 variables
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    varSetInExecutionListener = (String) runtimeService.getVariable(processInstance.getId(), "variableSetInExecutionListener");

    assertThat(varSetInExecutionListener).isNotNull();
    assertThat(varSetInExecutionListener).isEqualTo("secondValue");

    ExampleExecutionListenerPojo myPojo = new ExampleExecutionListenerPojo();
    runtimeService.setVariable(processInstance.getId(), "myPojo", myPojo);

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    // First usertask uses a method-expression as executionListener:
    // ${myPojo.myMethod(execution.eventName)}
    ExampleExecutionListenerPojo pojoVariable = (ExampleExecutionListenerPojo) runtimeService.getVariable(processInstance.getId(), "myPojo");
    assertThat(pojoVariable.getReceivedEventName()).isNotNull();
    assertThat(pojoVariable.getReceivedEventName()).isEqualTo("end");

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    List<RecordedEvent> events = RecorderExecutionListener.getRecordedEvents();
    assertThat(events).hasSize(1);
    RecordedEvent event = events.get(0);
    assertThat(event.getParameter()).isEqualTo("End Process Listener");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/executionlistener/ExecutionListenersStartEndEvent.bpmn20.xml" })
  public void testExecutionListenersOnStartEndEvents() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess");
    assertProcessEnded(processInstance.getId());

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertThat(recordedEvents).hasSize(4);

    assertThat(recordedEvents.get(0).getActivityId()).isEqualTo("theStart");
    assertThat(recordedEvents.get(0).getActivityName()).isEqualTo("Start Event");
    assertThat(recordedEvents.get(0).getParameter()).isEqualTo("Start Event Listener");
    assertThat(recordedEvents.get(0).getEventName()).isEqualTo("end");

    assertThat(recordedEvents.get(1).getActivityId()).isEqualTo("noneEvent");
    assertThat(recordedEvents.get(1).getActivityName()).isEqualTo("None Event");
    assertThat(recordedEvents.get(1).getParameter()).isEqualTo("Intermediate Catch Event Listener");
    assertThat(recordedEvents.get(1).getEventName()).isEqualTo("end");

    assertThat(recordedEvents.get(2).getActivityId()).isEqualTo("signalEvent");
    assertThat(recordedEvents.get(2).getActivityName()).isEqualTo("Signal Event");
    assertThat(recordedEvents.get(2).getParameter()).isEqualTo("Intermediate Throw Event Listener");
    assertThat(recordedEvents.get(2).getEventName()).isEqualTo("start");

    assertThat(recordedEvents.get(3).getActivityId()).isEqualTo("theEnd");
    assertThat(recordedEvents.get(3).getActivityName()).isEqualTo("End Event");
    assertThat(recordedEvents.get(3).getParameter()).isEqualTo("End Event Listener");
    assertThat(recordedEvents.get(3).getEventName()).isEqualTo("start");

  }

  @Deployment(resources = { "org/activiti/examples/bpmn/executionlistener/ExecutionListenersFieldInjectionProcess.bpmn20.xml" })
  public void testExecutionListenerFieldInjection() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("myVar", "listening!");

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess", variables);

    Object varSetByListener = runtimeService.getVariable(processInstance.getId(), "var");
    assertThat(varSetByListener).isNotNull();
    assertThat(varSetByListener).isInstanceOf(String.class);

    // Result is a concatenation of fixed injected field and injected expression
    assertThat(varSetByListener).isEqualTo("Yes, I am listening!");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/executionlistener/ExecutionListenersCurrentActivity.bpmn20.xml" })
  public void testExecutionListenerCurrentActivity() {

    CurrentActivityExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess");
    assertProcessEnded(processInstance.getId());

    List<CurrentActivity> currentActivities = CurrentActivityExecutionListener.getCurrentActivities();
    assertThat(currentActivities).hasSize(3);

    assertThat(currentActivities.get(0).getActivityId()).isEqualTo("theStart");
    assertThat(currentActivities.get(0).getActivityName()).isEqualTo("Start Event");

    assertThat(currentActivities.get(1).getActivityId()).isEqualTo("noneEvent");
    assertThat(currentActivities.get(1).getActivityName()).isEqualTo("None Event");

    assertThat(currentActivities.get(2).getActivityId()).isEqualTo("theEnd");
    assertThat(currentActivities.get(2).getActivityName()).isEqualTo("End Event");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/executionlistener/ExecutionListenersForSubprocessStartEndEvent.bpmn20.xml" })
  public void testExecutionListenersForSubprocessStartEndEvents() {
    RecorderExecutionListener.clear();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess");

    List<RecordedEvent> recordedEvents = RecorderExecutionListener.getRecordedEvents();
    assertThat(recordedEvents).hasSize(1);
    assertThat(recordedEvents.get(0).getParameter()).isEqualTo("Process Start");

    RecorderExecutionListener.clear();

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    assertProcessEnded(processInstance.getId());

    recordedEvents = RecorderExecutionListener.getRecordedEvents();

    assertThat(recordedEvents).hasSize(3);
    assertThat(recordedEvents.get(0).getParameter()).isEqualTo("Subprocess Start");
    assertThat(recordedEvents.get(1).getParameter()).isEqualTo("Subprocess End");
    assertThat(recordedEvents.get(2).getParameter()).isEqualTo("Process End");
  }
}
