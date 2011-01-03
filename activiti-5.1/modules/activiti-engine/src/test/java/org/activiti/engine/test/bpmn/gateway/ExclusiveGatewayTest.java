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
package org.activiti.engine.test.bpmn.gateway;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Joram Barrez
 */
public class ExclusiveGatewayTest extends PluggableActivitiTestCase {

  @Deployment
  public void testDivergingExclusiveGateway() {
    for (int i = 1; i <= 3; i++) {
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("exclusiveGwDiverging", CollectionUtil.singletonMap("input", i));
      assertEquals("Task " + i, taskService.createTaskQuery().singleResult().getName());
      runtimeService.deleteProcessInstance(pi.getId(), "testing deletion");
    }
  }

  @Deployment
  public void testMergingExclusiveGateway() {
    runtimeService.startProcessInstanceByKey("exclusiveGwMerging");
    assertEquals(3, taskService.createTaskQuery().count());
  }

  // If there are multiple outgoing seqFlow with valid conditions, the first
  // defined one should be chosen.
  @Deployment
  public void testMultipleValidConditions() {
    runtimeService.startProcessInstanceByKey("exclusiveGwMultipleValidConditions", CollectionUtil.singletonMap("input", 5));
    assertEquals("Task 2", taskService.createTaskQuery().singleResult().getName());
  }

  @Deployment
  public void testNoSequenceFlowSelected() {
    try {
      runtimeService.startProcessInstanceByKey("exclusiveGwNoSeqFlowSelected", CollectionUtil.singletonMap("input", 4));
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("No outgoing sequence flow of the exclusive gateway " + "'exclusiveGw' could be selected for continuing the process", e.getMessage());
    }
  }
  
  /**
   * Test for bug ACT-10: whitespaces/newlines in expressions lead to exceptions 
   */
  @Deployment
  public void testWhitespaceInExpression() {
    // Starting a process instance will lead to an exception if whitespace are incorrectly handled
    runtimeService.startProcessInstanceByKey("whiteSpaceInExpression",
            CollectionUtil.singletonMap("input", 1));
  }
  
  @Deployment(resources = {"org/activiti/engine/test/bpmn/gateway/ExclusiveGatewayTest.testDivergingExclusiveGateway.bpmn20.xml"})
  public void testUnknownVariableInExpression() {
    // Instead of 'input' we're starting a process instance with the name 'iinput' (ie. a typo)
    try {
      runtimeService.startProcessInstanceByKey(
            "exclusiveGwDiverging", CollectionUtil.singletonMap("iinput", 1));
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("Unknown property used in expression", e.getMessage());
    }
  }
  
  @Deployment
  public void testDecideBasedOnBeanProperty() {
    runtimeService.startProcessInstanceByKey("decisionBasedOnBeanProperty", 
            CollectionUtil.singletonMap("order", new ExclusiveGatewayTestOrder(150)));
    
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Standard service", task.getName());
  }
  
  @Deployment
  public void testDecideBasedOnListOrArrayOfBeans() {
    List<ExclusiveGatewayTestOrder> orders = new ArrayList<ExclusiveGatewayTestOrder>();
    orders.add(new ExclusiveGatewayTestOrder(50));
    orders.add(new ExclusiveGatewayTestOrder(300));
    orders.add(new ExclusiveGatewayTestOrder(175));
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey(
            "decisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orders));
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertNotNull(task);
    assertEquals("Gold Member service", task.getName());
    
    
    // Arrays are usable in exactly the same way
    ExclusiveGatewayTestOrder[] orderArray = orders.toArray(new ExclusiveGatewayTestOrder[orders.size()]);
    orderArray[1].setPrice(10);
    pi = runtimeService.startProcessInstanceByKey(
            "decisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orderArray));
    
    task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
    assertNotNull(task);
    assertEquals("Basic service", task.getName());
  }
  
  @Deployment
  public void testDecideBasedOnBeanMethod() {
    runtimeService.startProcessInstanceByKey("decisionBasedOnBeanMethod", 
            CollectionUtil.singletonMap("order", new ExclusiveGatewayTestOrder(300)));
    
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Gold Member service", task.getName());
  }
  
  @Deployment
  public void testInvalidMethodExpression() {
    try {
      runtimeService.startProcessInstanceByKey("invalidMethodExpression", 
            CollectionUtil.singletonMap("order", new ExclusiveGatewayTestOrder(50)));
      fail();
    } catch (ActivitiException e) {
      assertTextPresent("Unknown method used in expression", e.getMessage());
    }
  }
  
}
