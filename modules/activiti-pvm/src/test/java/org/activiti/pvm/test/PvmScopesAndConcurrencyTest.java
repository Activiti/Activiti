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
import org.activiti.pvm.PvmException;
import org.activiti.pvm.event.EventListener;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.runtime.PvmExecution;
import org.activiti.pvm.runtime.PvmProcessInstance;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.End;
import org.activiti.test.pvm.activities.ParallelGateway;
import org.activiti.test.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class PvmScopesAndConcurrencyTest extends PvmTestCase {

  /**
   *                      +---------+
   *                      | +----+  |
   *                  +---->| c1 |------+
   *                  |   | +----+  |   v
   * +-------+   +------+ |         | +------+   +-----+
   * | start |-->| fork | | noscope | | join |-->| end |
   * +-------+   +------+ |         | +------+   +-----+
   *                  |   | +----+  |   ^
   *                  +---->| c2 |------+
   *                      | +----+  |
   *                      +---------+
   */
  public void testConcurrentPathsThroughNonScopeNestedActivity() {
    EventCollector eventCollector = new EventCollector();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("scopes and concurrency")
      .eventListener(EventListener.EVENTNAME_START, eventCollector)
      .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("noscope")
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .createActivity("c1")
          .behavior(new WaitState())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("join")
        .endActivity()
        .createActivity("c2")
          .behavior(new WaitState())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("join")
        .endActivity()
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on Activity(start)");
    expectedEvents.add("end on Activity(start)");
    expectedEvents.add("start on Activity(fork)");
    expectedEvents.add("end on Activity(fork)");
    expectedEvents.add("start on Activity(noscope)");
    expectedEvents.add("start on Activity(c1)");
    expectedEvents.add("end on Activity(fork)");
    expectedEvents.add("start on Activity(noscope)");
    expectedEvents.add("start on Activity(c2)");
    
    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    PvmExecution execution = processInstance.findExecution("c1");
    execution.signal(null, null);

    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(c1)");
    expectedEvents.add("end on Activity(noscope)");
    expectedEvents.add("start on Activity(join)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    execution = processInstance.findExecution("c2");
    execution.signal(null, null);

    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(c2)");
    expectedEvents.add("end on Activity(noscope)");
    expectedEvents.add("start on Activity(join)");
    expectedEvents.add("end on Activity(join)");
    expectedEvents.add("start on Activity(end)");
    expectedEvents.add("end on Activity(end)");
    expectedEvents.add("end on ProcessDefinition(scopes and concurrency)");
    
    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    assertTrue(processInstance.isEnded());
  }

  /**
   *                      +---------+
   *                      | +----+  |
   *                  +---->| c1 |------+
   *                  |   | +----+  |   v
   * +-------+   +------+ |         | +------+   +-----+
   * | start |-->| fork | |  scope  | | join |-->| end |
   * +-------+   +------+ |         | +------+   +-----+
   *                  |   | +----+  |   ^
   *                  +---->| c2 |------+
   *                      | +----+  |
   *                      +---------+
   */
  public void testConcurrentPathsThroughScope() {
    EventCollector eventCollector = new EventCollector();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("scopes and concurrency")
      .eventListener(EventListener.EVENTNAME_START, eventCollector)
      .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("scope")
        .scope()
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .createActivity("c1")
          .behavior(new WaitState())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("join")
        .endActivity()
        .createActivity("c2")
          .behavior(new WaitState())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("join")
        .endActivity()
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on Activity(start)");
    expectedEvents.add("end on Activity(start)");
    expectedEvents.add("start on Activity(fork)");
    expectedEvents.add("end on Activity(fork)");
    expectedEvents.add("start on Activity(scope)");
    expectedEvents.add("start on Activity(c1)");
    expectedEvents.add("end on Activity(fork)");
    expectedEvents.add("start on Activity(scope)");
    expectedEvents.add("start on Activity(c2)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    PvmExecution execution = processInstance.findExecution("c1");
    execution.signal(null, null);

    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(c1)");
    expectedEvents.add("end on Activity(scope)");
    expectedEvents.add("start on Activity(join)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    execution = processInstance.findExecution("c2");
    execution.signal(null, null);

    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(c2)");
    expectedEvents.add("end on Activity(scope)");
    expectedEvents.add("start on Activity(join)");
    expectedEvents.add("end on Activity(join)");
    expectedEvents.add("start on Activity(end)");
    expectedEvents.add("end on Activity(end)");
    expectedEvents.add("end on ProcessDefinition(scopes and concurrency)");
    
    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    assertTrue(processInstance.isEnded());
  }

  /**
   *           +--------------------+
   *           |            +----+  |
   *           |      +---->| c1 |------+
   *           |      |     +----+  |   v
   * +-------+ | +------+           | +------+   +-----+
   * | start |-->| fork |    scope  | | join |-->| end |
   * +-------+ | +------+           | +------+   +-----+
   *           |      |     +----+  |   ^
   *           |      +---->| c2 |------+
   *           |            +----+  |
   *           +--------------------+
   */
  public void testConcurrentPathsGoingOutOfScope() {
    EventCollector eventCollector = new EventCollector();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("scopes and concurrency")
      .eventListener(EventListener.EVENTNAME_START, eventCollector)
      .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("fork")
      .endActivity()
      .createActivity("scope")
        .scope()
        .createActivity("fork")
          .behavior(new ParallelGateway())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("c1")
          .transition("c2")
        .endActivity()
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .createActivity("c1")
          .behavior(new WaitState())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("join")
        .endActivity()
        .createActivity("c2")
          .behavior(new WaitState())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("join")
        .endActivity()
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on Activity(start)");
    expectedEvents.add("end on Activity(start)");
    expectedEvents.add("start on Activity(scope)");
    expectedEvents.add("start on Activity(fork)");
    expectedEvents.add("end on Activity(fork)");
    expectedEvents.add("start on Activity(c1)");
    expectedEvents.add("end on Activity(fork)");
    expectedEvents.add("start on Activity(c2)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    PvmExecution execution = processInstance.findExecution("c1");
    execution.signal(null, null);

    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(c1)");
    expectedEvents.add("end on Activity(scope)");
    expectedEvents.add("start on Activity(join)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    execution = processInstance.findExecution("c2");
    execution.signal(null, null);

    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(c2)");
    expectedEvents.add("end on Activity(scope)");
    expectedEvents.add("start on Activity(join)");
    expectedEvents.add("end on Activity(join)");
    expectedEvents.add("start on Activity(end)");
    expectedEvents.add("end on Activity(end)");
    expectedEvents.add("end on ProcessDefinition(scopes and concurrency)");
    
    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    assertTrue(processInstance.isEnded());
  }

  /**
   *                      +--------------------+
   *                      | +----+             |
   *                  +---->| c1 |------+      |
   *                  |   | +----+      v      |
   * +-------+   +------+ |           +------+ | +-----+
   * | start |-->| fork | |  scope    | join |-->| end |
   * +-------+   +------+ |           +------+ | +-----+
   *                  |   | +----+      ^      |
   *                  +---->| c2 |------+      |
   *                      | +----+             |
   *                      +--------------------+
   */
  public void testConcurrentPathsGoingIntoScope() {
    EventCollector eventCollector = new EventCollector();
    
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder("scopes and concurrency")
      .eventListener(EventListener.EVENTNAME_START, eventCollector)
      .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("scope")
        .scope()
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
        .createActivity("c1")
          .behavior(new WaitState())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("join")
        .endActivity()
        .createActivity("c2")
          .behavior(new WaitState())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("join")
        .endActivity()
        .createActivity("join")
          .behavior(new ParallelGateway())
          .eventListener(EventListener.EVENTNAME_START, eventCollector)
          .eventListener(EventListener.EVENTNAME_END, eventCollector)
          .transition("end")
        .endActivity()
      .endActivity()
      .createActivity("end")
        .behavior(new End())
        .eventListener(EventListener.EVENTNAME_START, eventCollector)
        .eventListener(EventListener.EVENTNAME_END, eventCollector)
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("start on Activity(start)");
    expectedEvents.add("end on Activity(start)");
    expectedEvents.add("start on Activity(fork)");
    expectedEvents.add("end on Activity(fork)");
    expectedEvents.add("start on Activity(scope)");
    expectedEvents.add("start on Activity(c1)");
    expectedEvents.add("end on Activity(fork)");
    expectedEvents.add("start on Activity(scope)");
    expectedEvents.add("start on Activity(c2)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    PvmExecution execution = processInstance.findExecution("c1");
    execution.signal(null, null);

    expectedEvents = new ArrayList<String>();
    expectedEvents.add("end on Activity(c1)");
    expectedEvents.add("start on Activity(join)");

    assertEquals("expected "+expectedEvents+", but was \n"+eventCollector+"\n", expectedEvents, eventCollector.events);
    eventCollector.events.clear();
    
    execution = processInstance.findExecution("c2");
    try {
      execution.signal(null, null);
      fail("expected exception");
    } catch (PvmException e) {
      // OK
      assertTextPresent("joining scope executions is not allowed", e.getMessage());
    }
  }
}
