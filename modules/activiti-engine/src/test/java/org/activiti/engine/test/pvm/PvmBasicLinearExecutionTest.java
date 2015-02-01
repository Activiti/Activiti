package org.activiti.engine.test.pvm;
import java.util.ArrayList;

import org.activiti.engine.impl.pvm.ProcessDefinitionBuilder;
import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.test.pvm.activities.Automatic;
import org.activiti.engine.test.pvm.activities.End;
import org.activiti.engine.test.pvm.activities.WaitState;
import org.activiti.engine.test.pvm.activities.While;


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

/**
 * @author Tom Baeyens
 */
public class PvmBasicLinearExecutionTest extends PvmTestCase {

  /**
   * +-------+   +-----+
   * | start |-->| end |
   * +-------+   +-----+
   */
  public void testStartEnd() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

  /**
   * +-----+   +-----+   +-------+
   * | one |-->| two |-->| three |
   * +-----+   +-----+   +-------+
   */
  public void testSingleAutomatic() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("one")
        .initial()
        .behavior(new Automatic())
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new Automatic())
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

  /**
   * +-----+   +-----+   +-------+
   * | one |-->| two |-->| three |
   * +-----+   +-----+   +-------+
   */
  public void testSingleWaitState() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("one")
        .initial()
        .behavior(new Automatic())
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new WaitState())
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    PvmExecution activityInstance = processInstance.findExecution("two");
    assertNotNull(activityInstance);
    
    activityInstance.signal(null, null);

    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

  /**
   * +-----+   +-----+   +-------+   +------+    +------+
   * | one |-->| two |-->| three |-->| four |--> | five |
   * +-----+   +-----+   +-------+   +------+    +------+
   */
  public void testCombinationOfWaitStatesAndAutomatics() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("one")
      .endActivity()
      .createActivity("one")
        .behavior(new WaitState())
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new WaitState())
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new Automatic())
        .transition("four")
      .endActivity()
      .createActivity("four")
        .behavior(new Automatic())
        .transition("five")
      .endActivity()
      .createActivity("five")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    PvmExecution activityInstance = processInstance.findExecution("one");
    assertNotNull(activityInstance);
    activityInstance.signal(null, null);

    activityInstance = processInstance.findExecution("two");
    assertNotNull(activityInstance);
    activityInstance.signal(null, null);

    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

  /**
   *                  +----------------------------+
   *                  v                            |
   * +-------+   +------+   +-----+   +-----+    +-------+
   * | start |-->| loop |-->| one |-->| two |--> | three |
   * +-------+   +------+   +-----+   +-----+    +-------+
   *                  |
   *                  |   +-----+
   *                  +-->| end |
   *                      +-----+
   */
  public void testWhileLoop() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("loop")
      .endActivity()
      .createActivity("loop")
        .behavior(new While("count", 0, 500))
        .transition("one", "more")
        .transition("end", "done")
      .endActivity()
      .createActivity("one")
        .behavior(new Automatic())
        .transition("two")
      .endActivity()
      .createActivity("two")
        .behavior(new Automatic())
        .transition("three")
      .endActivity()
      .createActivity("three")
        .behavior(new Automatic())
        .transition("loop")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    assertEquals(new ArrayList<String>(), processInstance.findActiveActivityIds());
    assertTrue(processInstance.isEnded());
  }

}
