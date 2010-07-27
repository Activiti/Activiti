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
package org.activiti.test.pvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.activiti.pvm.ObjectExecution;
import org.activiti.pvm.ObjectProcessDefinition;
import org.activiti.pvm.ObjectProcessInstance;
import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.test.LogInitializer;
import org.activiti.test.pvm.activities.Automatic;
import org.activiti.test.pvm.activities.ParallelGateway;
import org.activiti.test.pvm.activities.WaitState;
import org.junit.Rule;
import org.junit.Test;



/**
 * @author Tom Baeyens
 */
public class PvmConcurrencyTest {

  @Rule 
  public LogInitializer logInitializer = new LogInitializer();
  
  @Test
  public void testSimpleAutmaticConcurrency() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .build();
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertNotNull(processInstance.findExecution("end"));
  }

  @Test
  public void testSimpleWaitStateConcurrency() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("c1")
        .behavior(new WaitState())
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new WaitState())
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .build();
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    ObjectExecution executionC1 = processInstance.findExecution("c1");
    assertNotNull(executionC1);
    
    ObjectExecution executionC2 = processInstance.findExecution("c2");
    assertNotNull(executionC2);
    
    executionC1.event(null);
    executionC2.event(null);
    
    List<String> activityNames = processInstance.getActivityNames();
    List<String> expectedActivityNames = new ArrayList<String>();
    expectedActivityNames.add("end");
    
    assertEquals(expectedActivityNames, activityNames);
  }

  @Test
  public void testUnstructuredConcurrencyTwoJoins() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
        .transition("c3")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .transition("join1")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join1")
      .endActivity()
      .createActivity("c3")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("join1")
        .behavior(new ParallelGateway())
        .transition("c4")
      .endActivity()
      .createActivity("c4")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("join2")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .build();
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertNotNull(processInstance.findExecution("end"));
  }

  @Test
  public void testUnstructuredConcurrencyTwoForks() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork1")
      .endActivity()
      .createActivity("fork1")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
        .transition("fork2")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("fork2")
        .behavior(new ParallelGateway())
        .transition("c3")
        .transition("c4")
      .endActivity()
      .createActivity("c3")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("c4")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .build();
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertNotNull(processInstance.findExecution("end"));
  }

  @Test
  public void testJoinForkCombinedInOneParallelGateway() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
        .transition("c3")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .transition("join1")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join1")
      .endActivity()
      .createActivity("c3")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("join1")
        .behavior(new ParallelGateway())
        .transition("c4")
        .transition("c5")
        .transition("c6")
      .endActivity()
      .createActivity("c4")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("c5")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("c6")
        .behavior(new Automatic())
        .transition("join2")
      .endActivity()
      .createActivity("join2")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .build();
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertNotNull(processInstance.findExecution("end"));
  }

  @Test
  public void testSimpleAutmaticConcurrencyWithNestedScope() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("fork")
        .behavior(new ParallelGateway())
        .transition("c1")
        .transition("c2")
      .endActivity()
      .createActivity("c1")
        .behavior(new Automatic())
        .scope()
        .transition("join")
      .endActivity()
      .createActivity("c2")
        .behavior(new Automatic())
        .transition("join")
      .endActivity()
      .createActivity("join")
        .behavior(new ParallelGateway())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .build();
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertNotNull(processInstance.findExecution("end"));
  }

  @Test
  public void testSimpleAutmaticConcurrencyInsideScope() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder
    .createProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("fork")
      .endActivity()
      .createActivity("subprocess")
        .scope()
        .createActivity("fork")
          .behavior(new ParallelGateway())
          .transition("c1")
          .transition("c2")
        .endActivity()
        .createActivity("c1")
          .behavior(new Automatic())
          .transition("join")
        .endActivity()
        .createActivity("c2")
          .behavior(new Automatic())
          .transition("join")
        .endActivity()
        .createActivity("join")
          .behavior(new ParallelGateway())
          .transition("end")
        .endActivity()
      .endActivity()
      .createActivity("end")
        .behavior(new WaitState())
      .endActivity()
    .build();
    
    ObjectProcessInstance processInstance = processDefinition.createProcessInstance(); 
    processInstance.start();
    
    assertNotNull(processInstance.findExecution("end"));
  }

}
