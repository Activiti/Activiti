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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  public void testSkipExpression() {
    for (int i = 1; i <= 3; i++) {
      Map<String,Object> variables = new HashMap<String,Object>();
      variables.put("_ACTIVITI_SKIP_EXPRESSION_ENABLED", true);
      variables.put("input", -i);
      
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("exclusiveGwDivergingSkipExpression", variables);
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
  
  @Deployment
  public void testDefaultSequenceFlow() {
    
    // Input == 1 -> default is not selected
    String procId = runtimeService.startProcessInstanceByKey("exclusiveGwDefaultSequenceFlow", 
            CollectionUtil.singletonMap("input", 1)).getId();
    Task task = taskService.createTaskQuery().singleResult();
    assertEquals("Input is one", task.getName());
    runtimeService.deleteProcessInstance(procId, null);
    
    runtimeService.startProcessInstanceByKey("exclusiveGwDefaultSequenceFlow",
            CollectionUtil.singletonMap("input", 5)).getId();
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Default input", task.getName());
  }

  public void testInvalidProcessDefinition() {
    String defaultFlowWithCondition = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<definitions id='definitions' xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:activiti='http://activiti.org/bpmn' targetNamespace='Examples'>" +
            "  <process id='exclusiveGwDefaultSequenceFlow'> " + 
            "    <startEvent id='theStart' /> " + 
            "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='exclusiveGw' /> " + 
            
            "    <exclusiveGateway id='exclusiveGw' name='Exclusive Gateway' default='flow3' /> " + 
            "    <sequenceFlow id='flow2' sourceRef='exclusiveGw' targetRef='theTask1'> " + 
            "      <conditionExpression xsi:type='tFormalExpression'>${input == 1}</conditionExpression> " + 
            "    </sequenceFlow> " + 
            "    <sequenceFlow id='flow3' sourceRef='exclusiveGw' targetRef='theTask2'> " + 
            "      <conditionExpression xsi:type='tFormalExpression'>${input == 3}</conditionExpression> " + 
            "    </sequenceFlow> " + 
    
            "    <userTask id='theTask1' name='Input is one' /> " + 
            "    <userTask id='theTask2' name='Default input' /> " + 
            "  </process>" + 
            "</definitions>";    
    
    try {
    	repositoryService.createDeployment().addString("myprocess.bpmn20.xml", defaultFlowWithCondition).deploy();
    	fail();
    } catch (Exception e) {}

    String noOutgoingFlow = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<definitions id='definitions' xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:activiti='http://activiti.org/bpmn' targetNamespace='Examples'>" +
            "  <process id='exclusiveGwDefaultSequenceFlow'> " + 
            "    <startEvent id='theStart' /> " + 
            "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='exclusiveGw' /> " + 
            "    <exclusiveGateway id='exclusiveGw' name='Exclusive Gateway' /> " + 
            "  </process>" + 
            "</definitions>";    
    try {
      repositoryService.createDeployment().addString("myprocess.bpmn20.xml", noOutgoingFlow).deploy();
      fail("Could deploy a process definition with a XOR Gateway without outgoing sequence flows.");
    }
    catch (ActivitiException ex) {
    }

  }

  @Deployment
  public void testExclusiveDirectlyToEnd() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", 1);
    ProcessInstance startProcessInstanceByKey = runtimeService.startProcessInstanceByKey("exclusiveGateway", variables);
    long count = historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(startProcessInstanceByKey.getId()).unfinished()
        .count();
    assertEquals(0, count);
  }
  
}
