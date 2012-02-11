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
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.test.pvm.activities.Automatic;
import org.activiti.engine.test.pvm.activities.EmbeddedSubProcess;
import org.activiti.engine.test.pvm.activities.End;
import org.activiti.engine.test.pvm.activities.ParallelGateway;
import org.activiti.engine.test.pvm.activities.WaitState;


/**
 * @author Tom Baeyens
 */
public class PvmEmbeddedSubProcessTest extends PvmTestCase {

  /** 
   *           +------------------------------+
   *           | embedded subprocess          |
   * +-----+   |  +-----------+   +---------+ |   +---+
   * |start|-->|  |startInside|-->|endInside| |-->|end|
   * +-----+   |  +-----------+   +---------+ |   +---+
   *           +------------------------------+
   */
  public void testEmbeddedSubProcess() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .createActivity("startInside")
          .behavior(new Automatic())
          .transition("endInside")
        .endActivity()
        .createActivity("endInside")
          .behavior(new End())
        .endActivity()
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    List<String> expectedActiveActivityIds = new ArrayList<String>();
    expectedActiveActivityIds.add("end");
      
    assertEquals(expectedActiveActivityIds, processInstance.findActiveActivityIds());
  }

  /** 
   *           +----------------------------------------+
   *           | embeddedsubprocess        +----------+ |
   *           |                     +---->|endInside1| |
   *           |                     |     +----------+ |
   *           |                     |                  |
   * +-----+   |  +-----------+   +----+   +----------+ |   +---+
   * |start|-->|  |startInside|-->|fork|-->|endInside2| |-->|end|
   * +-----+   |  +-----------+   +----+   +----------+ |   +---+
   *           |                     |                  |
   *           |                     |     +----------+ |
   *           |                     +---->|endInside3| |
   *           |                           +----------+ |
   *           +----------------------------------------+
   */
  public void testMultipleConcurrentEndsInsideEmbeddedSubProcess() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .createActivity("startInside")
          .behavior(new Automatic())
          .transition("fork")
        .endActivity()
        .createActivity("fork")
          .behavior(new ParallelGateway())
          .transition("endInside1")
          .transition("endInside2")
          .transition("endInside3")
        .endActivity()
        .createActivity("endInside1")
          .behavior(new End())
        .endActivity()
        .createActivity("endInside2")
          .behavior(new End())
        .endActivity()
        .createActivity("endInside3")
          .behavior(new End())
        .endActivity()
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertTrue(processInstance.isEnded());
  }

  /** 
   *           +-------------------------------------------------+
   *           | embeddedsubprocess        +----------+          |
   *           |                     +---->|endInside1|          |
   *           |                     |     +----------+          |
   *           |                     |                           |
   * +-----+   |  +-----------+   +----+   +----+   +----------+ |   +---+
   * |start|-->|  |startInside|-->|fork|-->|wait|-->|endInside2| |-->|end|
   * +-----+   |  +-----------+   +----+   +----+   +----------+ |   +---+
   *           |                     |                           |
   *           |                     |     +----------+          |
   *           |                     +---->|endInside3|          |
   *           |                           +----------+          |
   *           +-------------------------------------------------+
   */
  public void testMultipleConcurrentEndsInsideEmbeddedSubProcessWithWaitState() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .createActivity("startInside")
          .behavior(new Automatic())
          .transition("fork")
        .endActivity()
        .createActivity("fork")
          .behavior(new ParallelGateway())
          .transition("endInside1")
          .transition("wait")
          .transition("endInside3")
        .endActivity()
        .createActivity("endInside1")
          .behavior(new End())
        .endActivity()
        .createActivity("wait")
          .behavior(new WaitState())
          .transition("endInside2")
        .endActivity()
        .createActivity("endInside2")
          .behavior(new End())
        .endActivity()
        .createActivity("endInside3")
          .behavior(new End())
        .endActivity()
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new End())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertFalse(processInstance.isEnded());
    PvmExecution execution = processInstance.findExecution("wait");
    execution.signal(null, null);
    
    assertTrue(processInstance.isEnded());
  }
  
  /** 
   *           +-------------------------------------------------------+
   *           | embedded subprocess                                   |
   *           |                  +--------------------------------+   |
   *           |                  | nested embedded subprocess     |   |
   * +-----+   | +-----------+    |  +-----------+   +---------+   |   |   +---+
   * |start|-->| |startInside|--> |  |startInside|-->|endInside|   |   |-->|end|
   * +-----+   | +-----------+    |  +-----------+   +---------+   |   |   +---+
   *           |                  +--------------------------------+   |
   *           |                                                       |
   *           +-------------------------------------------------------+
   */
  public void testNestedSubProcessNoEnd() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .createActivity("startInside")
          .behavior(new Automatic())
          .transition("nestedSubProcess")
        .endActivity()
          .createActivity("nestedSubProcess")
          .scope()
          .behavior(new EmbeddedSubProcess())
          .createActivity("startNestedInside")
            .behavior(new Automatic())
            .transition("endInside")
            .endActivity()
          .createActivity("endInside")
            .behavior(new End())
            .endActivity()
        .endActivity()
      .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    List<String> expectedActiveActivityIds = new ArrayList<String>();
    expectedActiveActivityIds.add("end");
      
    assertEquals(expectedActiveActivityIds, processInstance.findActiveActivityIds());
  }
  
  /** 
   *           +------------------------------+
   *           | embedded subprocess          |
   * +-----+   |  +-----------+               |
   * |start|-->|  |startInside|               |
   * +-----+   |  +-----------+               |
   *           +------------------------------+
   */
  public void testEmbeddedSubProcessWithoutEndEvents() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()   
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .createActivity("startInside")
          .behavior(new Automatic())
        .endActivity()       
      .endActivity()      
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertTrue(processInstance.isEnded());
  }
  
  /** 
   *           +-------------------------------------------------------+
   *           | embedded subprocess                                   |
   *           |                  +--------------------------------+   |
   *           |                  | nested embedded subprocess     |   |
   * +-----+   | +-----------+    |  +-----------+                 |   |
   * |start|-->| |startInside|--> |  |startInside|                 |   |
   * +-----+   | +-----------+    |  +-----------+                 |   |
   *           |                  +--------------------------------+   |
   *           |                                                       |
   *           +-------------------------------------------------------+
   */
  public void testNestedSubProcessBothNoEnd() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .createActivity("startInside")
          .behavior(new Automatic())
          .transition("nestedSubProcess")
        .endActivity()
          .createActivity("nestedSubProcess")
          .scope()
          .behavior(new EmbeddedSubProcess())
          .createActivity("startNestedInside")
            .behavior(new Automatic())            
            .endActivity()        
        .endActivity()
      .endActivity()  
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertTrue(processInstance.isEnded());
  }
  
  
  /** 
   *           +------------------------------+
   *           | embedded subprocess          |
   * +-----+   |  +-----------+   +---------+ |
   * |start|-->|  |startInside|-->|endInside| |
   * +-----+   |  +-----------+   +---------+ |
   *           +------------------------------+
   */
  public void testEmbeddedSubProcessNoEnd() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()   
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("embeddedsubprocess")
      .endActivity()
      .createActivity("embeddedsubprocess")
        .scope()
        .behavior(new EmbeddedSubProcess())
        .createActivity("startInside")
          .behavior(new Automatic())
          .transition("endInside")
        .endActivity()
        .createActivity("endInside")
          .behavior(new End())
        .endActivity()
      .endActivity()      
    .buildProcessDefinition();
    
    PvmProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertTrue(processInstance.isEnded());
  }
    
}
