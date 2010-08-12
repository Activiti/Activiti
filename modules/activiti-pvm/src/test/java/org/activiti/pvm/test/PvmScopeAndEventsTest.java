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

package org.activiti.pvm.test;

import java.util.ArrayList;
import java.util.List;

import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.runtime.PvmExecution;
import org.activiti.pvm.runtime.PvmProcessInstance;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.End;
import org.activiti.test.pvm.activities.WaitState;


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
      .variable("processVar1", "one")
      .variable("processVar2", "two")
      .eventListener(EventListener.EVENTNAME_START, eventCollector)
      .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .createActivity("mostOuterNestedActivity")
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .createActivity("outerScope")
          .variable("outerVar", "outerValue")
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .createActivity("firstInnerScope")
            .variable("firstInnerVar", "firstInnerValue")
            .eventListener(EventListener.EVENTNAME_START, eventCollector)
            .eventListener(EventListener.EVENTNAME_END, eventCollector)
            .createActivity("firstMostInnerNestedActivity")
              .eventListener(EventListener.EVENTNAME_START, eventCollector)
              .eventListener(EventListener.EVENTNAME_END, eventCollector)
              .createActivity("start")
                .initial()
                .behavior(new Automatic())
                .eventListener(EventListener.EVENTNAME_START, eventCollector)
                .eventListener(EventListener.EVENTNAME_END, eventCollector)
                .transition("waitInFirst")
              .endActivity()
              .createActivity("waitInFirst")
                .behavior(new WaitState())
                .eventListener(EventListener.EVENTNAME_START, eventCollector)
                .eventListener(EventListener.EVENTNAME_END, eventCollector)
                .transition("waitInSecond")
              .endActivity()
            .endActivity()
          .endActivity()
          .createActivity("secondInnerScope")
            .variable("secondInnerVar", "secondInnerValue")
            .eventListener(EventListener.EVENTNAME_START, eventCollector)
            .eventListener(EventListener.EVENTNAME_END, eventCollector)
            .createActivity("secondMostInnerNestedActivity")
              .eventListener(EventListener.EVENTNAME_START, eventCollector)
              .eventListener(EventListener.EVENTNAME_END, eventCollector)
              .createActivity("waitInSecond")
                .behavior(new WaitState())
                .eventListener(EventListener.EVENTNAME_START, eventCollector)
                .eventListener(EventListener.EVENTNAME_END, eventCollector)
                .transition("end")
              .endActivity()
              .createActivity("end")
                .behavior(new End())
                .eventListener(EventListener.EVENTNAME_START, eventCollector)
                .eventListener(EventListener.EVENTNAME_END, eventCollector)
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

    assertEquals(expectedEvents, eventCollector.events);
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
    eventCollector.events.clear();
  }
}
