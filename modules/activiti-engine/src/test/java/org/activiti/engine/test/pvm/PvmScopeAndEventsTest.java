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

package org.activiti.engine.test.pvm;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.ProcessDefinitionBuilder;
import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.test.pvm.activities.Automatic;
import org.activiti.engine.test.pvm.activities.End;
import org.activiti.engine.test.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class PvmScopeAndEventsTest extends PvmTestCase {

  /**
   * +--------------------------------------------------------------------------------+
   * | mostOuterNestedActivity                                                        |                  
   * | +----------------------------------------------------------------------------+ |
   * | | outerScope (scope)                                                         | |
   * | | +----------------------------------+ +-----------------------------------+ | |
   * | | | firstInnerScope (scope)          | | secondInnerScope (scope)          | | |
   * | | | +------------------------------+ | | +-------------------------------+ | | |
   * | | | | firstMostInnerNestedActivity | | | | secondMostInnerNestedActivity | | | |
   * | | | | +-------+   +-------------+  | | | | +--------------+    +-----+   | | | |
   * | | | | | start |-->| waitInFirst |--------->| waitInSecond |--> | end |   | | | |
   * | | | | +-------+   +-------------+  | | | | +--------------+    +-----+   | | | |
   * | | | +------------------------------+ | | +-------------------------------+ | | |
   * | | +----------------------------------+ +-----------------------------------+ | |
   * | +----------------------------------------------------------------------------+ |
   * +--------------------------------------------------------------------------------+
   * 
   */
  public void testStartEndWithScopesAndNestedActivities() {
    EventCollector eventCollector = new EventCollector();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("scopes and events")
      .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
      .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .createActivity("mostOuterNestedActivity")
        .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .createActivity("outerScope")
          .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
          .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
          .createActivity("firstInnerScope")
            .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
            .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
            .createActivity("firstMostInnerNestedActivity")
              .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
              .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
              .createActivity("start")
                .initial()
                .behavior(new Automatic())
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
                .transition("waitInFirst")
              .endActivity()
              .createActivity("waitInFirst")
                .behavior(new WaitState())
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
                .transition("waitInSecond")
              .endActivity()
            .endActivity()
          .endActivity()
          .createActivity("secondInnerScope")
            .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
            .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
            .createActivity("secondMostInnerNestedActivity")
              .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
              .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
              .createActivity("waitInSecond")
                .behavior(new WaitState())
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
                .transition("end")
              .endActivity()
              .createActivity("end")
                .behavior(new End())
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
              .endActivity()
            .endActivity()
          .endActivity()
        .endActivity()
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on ProcessDefinition(scopes and events)");
    expectedEvents.add("start on Activity(mostOuterNestedActivity)");
    expectedEvents.add("start on Activity(outerScope)");
    expectedEvents.add("start on Activity(firstInnerScope)");
    expectedEvents.add("start on Activity(firstMostInnerNestedActivity)");
    expectedEvents.add("start on Activity(start)");
    expectedEvents.add("end on Activity(start)");
    expectedEvents.add("start on Activity(waitInFirst)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events); 
    eventCollector.events.clear();
    
    PvmExecution execution = processInstance.findExecution("waitInFirst");
    execution.signal(null, null);
    
    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(waitInFirst)");
    expectedEvents.add("end on Activity(firstMostInnerNestedActivity)");
    expectedEvents.add("end on Activity(firstInnerScope)");
    expectedEvents.add("start on Activity(secondInnerScope)");
    expectedEvents.add("start on Activity(secondMostInnerNestedActivity)");
    expectedEvents.add("start on Activity(waitInSecond)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events); 
    eventCollector.events.clear();
    
    execution = processInstance.findExecution("waitInSecond");
    execution.signal(null, null);
    
    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(waitInSecond)");
    expectedEvents.add("start on Activity(end)");
    expectedEvents.add("end on Activity(end)");
    expectedEvents.add("end on Activity(secondMostInnerNestedActivity)");
    expectedEvents.add("end on Activity(secondInnerScope)");
    expectedEvents.add("end on Activity(outerScope)");
    expectedEvents.add("end on Activity(mostOuterNestedActivity)");
    expectedEvents.add("end on ProcessDefinition(scopes and events)");
    
    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events); 
    eventCollector.events.clear();
  }
  
  /**
   * +--------------------------------------------------------------------------------+
   * | mostOuterNestedActivity                                                        |                  
   * | +----------------------------------------------------------------------------+ |
   * | | outerScope (scope)                                                         | |
   * | | +----------------------------------+ +-----------------------------------+ | |
   * | | | firstInnerScope (scope)          | | secondInnerScope (scope)          | | |
   * | | | +------------------------------+ | | +-------------------------------+ | | |
   * | | | | firstMostInnerNestedActivity | | | | secondMostInnerNestedActivity | | | |
   * | | | | +-------+   +-------------+  | | | | +--------------+    +-----+   | | | |
   * | | | | | start |-->| waitInFirst |--------->| waitInSecond |--> | end |   | | | |
   * | | | | +-------+   +-------------+  | | | | +--------------+    +-----+   | | | |
   * | | | +------------------------------+ | | +-------------------------------+ | | |
   * | | +----------------------------------+ +-----------------------------------+ | |
   * | +----------------------------------------------------------------------------+ |
   * +--------------------------------------------------------------------------------+
   * 
   */
  // this test does not start the process at the initial (the activity with id 'start'), but at 
  // 'waitInFirst'
  public void testStartEndWithScopesAndNestedActivitiesNotAtInitial() {
    EventCollector eventCollector = new EventCollector();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("scopes and events")
      .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
      .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
      .createActivity("mostOuterNestedActivity")
        .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
        .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
        .createActivity("outerScope")
          .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
          .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
          .createActivity("firstInnerScope")
            .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
            .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
            .createActivity("firstMostInnerNestedActivity")
              .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
              .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
              .createActivity("start")
                .initial()
                .behavior(new Automatic())
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
                .transition("waitInFirst")
              .endActivity()
              .createActivity("waitInFirst")
                .behavior(new WaitState())
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
                .transition("waitInSecond")
              .endActivity()
            .endActivity()
          .endActivity()
          .createActivity("secondInnerScope")
            .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
            .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
            .createActivity("secondMostInnerNestedActivity")
              .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
              .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
              .createActivity("waitInSecond")
                .behavior(new WaitState())
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
                .transition("end")
              .endActivity()
              .createActivity("end")
                .behavior(new End())
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_START, eventCollector)
                .executionListener(org.activiti.engine.impl.pvm.PvmEvent.EVENTNAME_END, eventCollector)
              .endActivity()
            .endActivity()
          .endActivity()
        .endActivity()
      .endActivity()
    .buildProcessDefinition();
    
    ActivityImpl alternativeInitial = (ActivityImpl) processDefinition.findActivity("waitInFirst");
    PvmProcessInstance processInstance = ((ProcessDefinitionImpl)processDefinition).createProcessInstanceForInitial(alternativeInitial);
    processInstance.start();
    
    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on ProcessDefinition(scopes and events)");
    expectedEvents.add("start on Activity(mostOuterNestedActivity)");
    expectedEvents.add("start on Activity(outerScope)");
    expectedEvents.add("start on Activity(firstInnerScope)");
    expectedEvents.add("start on Activity(firstMostInnerNestedActivity)");
    expectedEvents.add("start on Activity(waitInFirst)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events); 
    eventCollector.events.clear();
    
    PvmExecution execution = processInstance.findExecution("waitInFirst");
    execution.signal(null, null);
    
    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(waitInFirst)");
    expectedEvents.add("end on Activity(firstMostInnerNestedActivity)");
    expectedEvents.add("end on Activity(firstInnerScope)");
    expectedEvents.add("start on Activity(secondInnerScope)");
    expectedEvents.add("start on Activity(secondMostInnerNestedActivity)");
    expectedEvents.add("start on Activity(waitInSecond)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events); 
    eventCollector.events.clear();
    
    execution = processInstance.findExecution("waitInSecond");
    execution.signal(null, null);
    
    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(waitInSecond)");
    expectedEvents.add("start on Activity(end)");
    expectedEvents.add("end on Activity(end)");
    expectedEvents.add("end on Activity(secondMostInnerNestedActivity)");
    expectedEvents.add("end on Activity(secondInnerScope)");
    expectedEvents.add("end on Activity(outerScope)");
    expectedEvents.add("end on Activity(mostOuterNestedActivity)");
    expectedEvents.add("end on ProcessDefinition(scopes and events)");
    
    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events); 
    eventCollector.events.clear();
  }
}
