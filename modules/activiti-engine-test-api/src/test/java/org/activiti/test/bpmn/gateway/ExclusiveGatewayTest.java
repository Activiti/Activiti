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
package org.activiti.test.bpmn.gateway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.activiti.ActivitiException;
import org.activiti.ProcessInstance;
import org.activiti.Task;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.activiti.util.CollectionUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Joram Barrez
 */
public class ExclusiveGatewayTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  @ProcessDeclared
  public void testDivergingExclusiveGateway() {
    for (int i = 1; i <= 3; i++) {
      ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("exclusiveGwDiverging", CollectionUtil.singletonMap("input", i));
      assertEquals("Task " + i, deployer.getTaskService().createTaskQuery().singleResult().getName());
      deployer.getProcessService().deleteProcessInstance(pi.getId());
    }
  }

  @Test
  @ProcessDeclared
  public void testMergingExclusiveGateway() {
    deployer.getProcessService().startProcessInstanceByKey("exclusiveGwMerging");
    assertEquals(3, deployer.getTaskService().createTaskQuery().count());
  }

  // If there are multiple outgoing seqFlow with valid conditions, the first
  // defined one should be chosen.
  @Test
  @ProcessDeclared
  public void testMultipleValidConditions() {
    deployer.getProcessService().startProcessInstanceByKey("exclusiveGwMultipleValidConditions", CollectionUtil.singletonMap("input", 5));
    assertEquals("Task 2", deployer.getTaskService().createTaskQuery().singleResult().getName());
  }

  @Test
  @ProcessDeclared
  public void testNoSequenceFlowSelected() {
    exception.expect(ActivitiException.class);
    exception.expectMessage("No outgoing sequence flow of the exclusive gateway " + "'exclusiveGw' could be selected for continuing the process");
    deployer.getProcessService().startProcessInstanceByKey("exclusiveGwNoSeqFlowSelected", CollectionUtil.singletonMap("input", 4));
  }
  
  /**
   * Test for bug ACT-10: whitespaces/newlines in expressions lead to exceptions 
   */
  @Test
  @ProcessDeclared
  public void testWhitespaceInExpression() {
    // Starting a process instance will lead to an exception if whitespace are incorrectly handled
    deployer.getProcessService().startProcessInstanceByKey("whiteSpaceInExpression",
            CollectionUtil.singletonMap("input", 1));
  }
  
  @Test
  @ProcessDeclared(resources = {"/org/activiti/test/bpmn/gateway/ExclusiveGatewayTest.testDivergingExclusiveGateway.bpmn20.xml"})
  public void testUnknownVariableInExpression() {
    // Instead of 'input' we're starting a process instance with the name 'iinput' (ie. a typo)
    exception.expect(ActivitiException.class);
    exception.expectMessage("Unknown property used in expression");
    deployer.getProcessService().startProcessInstanceByKey(
            "exclusiveGwDiverging", CollectionUtil.singletonMap("iinput", 1));
  }
  
  @Test
  @ProcessDeclared
  public void testDecideBasedOnBeanProperty() {
    deployer.getProcessService().startProcessInstanceByKey("decisionBasedOnBeanProperty", 
            CollectionUtil.singletonMap("order", new ExclusiveGatewayTestOrder(150)));
    
    Task task = deployer.getTaskService().createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Standard service", task.getName());
  }
  
  @Test
  @ProcessDeclared
  public void testDecideBasedOnListOrArrayOfBeans() {
    List<ExclusiveGatewayTestOrder> orders = new ArrayList<ExclusiveGatewayTestOrder>();
    orders.add(new ExclusiveGatewayTestOrder(50));
    orders.add(new ExclusiveGatewayTestOrder(300));
    orders.add(new ExclusiveGatewayTestOrder(175));
    
    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey(
            "decisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orders));
    
    Task task = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertNotNull(task);
    assertEquals("Gold Member service", task.getName());
    
    
    // Arrays are usable in exactly the same way
    ExclusiveGatewayTestOrder[] orderArray = orders.toArray(new ExclusiveGatewayTestOrder[orders.size()]);
    orderArray[1].setPrice(10);
    pi = deployer.getProcessService().startProcessInstanceByKey(
            "decisionBasedOnListOrArrayOfBeans", CollectionUtil.singletonMap("orders", orderArray));
    
    task = deployer.getTaskService().createTaskQuery().processInstance(pi.getId()).singleResult();
    assertNotNull(task);
    assertEquals("Basic service", task.getName());
  }
  
  @Test
  @ProcessDeclared
  public void testDecideBasedOnBeanMethod() {
    deployer.getProcessService().startProcessInstanceByKey("decisionBasedOnBeanMethod", 
            CollectionUtil.singletonMap("order", new ExclusiveGatewayTestOrder(300)));
    
    Task task = deployer.getTaskService().createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("Gold Member service", task.getName());
  }
  
  @Test
  @ProcessDeclared
  public void testInvalidMethodExpression() {
    exception.expect(ActivitiException.class);
    exception.expectMessage("Unknown method used in expression");
    deployer.getProcessService().startProcessInstanceByKey("invalidMethodExpression", 
            CollectionUtil.singletonMap("order", new ExclusiveGatewayTestOrder(50)));
  }
  
}
