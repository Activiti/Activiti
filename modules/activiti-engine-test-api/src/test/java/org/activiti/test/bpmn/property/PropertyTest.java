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
package org.activiti.test.bpmn.property;

import org.activiti.engine.test.Deployment;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class PropertyTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  @Test
  @Deployment
  public void testUserTaskSrcProperty() {

//    // Start the process -> waits in usertask
//    Map<String, Object> vars = new HashMap<String, Object>();
//    vars.put("inputVar", "test");
//    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("testUserTaskSrcProperty", vars);
//
//    // 1 task should be active, and since the task is scoped 1 child execution
//    // should exist
//    assertNotNull(deployer.getTaskService().createTaskQuery().singleResult());
//    List<Execution> childExecutions = deployer.getProcessService().findChildExecutions(pi.getId());
//    assertEquals(1, childExecutions.size());
//
//    // The scope at the task should be able to see the 'myVar' variable,
//    // but the process instance shouldn't be able to see it
//    Execution childExecution = childExecutions.get(0);
//    assertEquals("test", deployer.getProcessService().getVariable(childExecution.getId(), "myVar"));
//    assertNull(deployer.getProcessService().getVariable(pi.getId(), "myVar"));
//
//    // The variable 'inputVar' should be visible for both
//    assertEquals("test", deployer.getProcessService().getVariable(childExecution.getId(), "inputVar"));
//    assertEquals("test", deployer.getProcessService().getVariable(pi.getId(), "inputVar"));
//
//    // Change the value of variable 'myVar' on the task scope
//    deployer.getProcessService().setVariable(childExecution.getId(), "myVar", "new_value");
//    assertEquals("new_value", deployer.getProcessService().getVariable(childExecution.getId(), "myVar"));
//    assertEquals("test", deployer.getProcessService().getVariable(childExecution.getId(), "inputVar"));
//    assertNull(deployer.getProcessService().getVariable(pi.getId(), "myVar"));
//
//    // When the task completes, the variable 'myVar' is destroyed
//    deployer.getTaskService().complete(deployer.getTaskService().createTaskQuery().singleResult().getId());
//    for (Execution execution : deployer.getProcessService().findChildExecutions(pi.getId())) {
//      assertNull(deployer.getProcessService().getVariable(execution.getId(), "myVar"));
//    }
  }

  @Test
  @Deployment
  public void testUserTaskSrcExprProperty() {

//    // Start the process -> waits in usertask
//    final String address = "TestStreet 123 90210 Beverly-Hills";
//    Order order = new Order(address);
//    Map<String, Object> vars = new HashMap<String, Object>();
//    vars.put("order", order);
//    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("testUserTaskSrcExprProperty", vars);
//
//    // The execution at the task should be able to see the 'orderAddress'
//    // variable,
//    // but the process instance shouldn't be able to see it
//    List<Execution> childExecutions = deployer.getProcessService().findChildExecutions(pi.getId());
//    String childExecutionId = childExecutions.get(0).getId();
//    assertEquals(address, deployer.getProcessService().getVariable(childExecutionId, "orderAddress"));
//    assertNull(deployer.getProcessService().getVariable(pi.getId(), "orderAddress"));
//
//    // Completing the task removes the 'orderAddress' variable
//    deployer.getTaskService().complete(deployer.getTaskService().createTaskQuery().singleResult().getId());
//    assertNull(deployer.getProcessService().getVariable(pi.getId(), "orderAddress"));
//    assertNotNull(deployer.getProcessService().getVariable(pi.getId(), "order"));
  }

  @Test
  @Deployment
  public void testUserTaskDstProperty() {

//    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("testUserTaskDstProperty");
//    List<Execution> childExecutions = deployer.getProcessService().findChildExecutions(pi.getId());
//    String childExecutionId = childExecutions.get(0).getId();
//
//    // The execution at the task should be able to see the 'taskVar' variable,
//    Map<String, Object> vars = deployer.getProcessService().getVariables(childExecutionId);
//    assertEquals(1, vars.size());
//    assertTrue(vars.containsKey("taskVar"));
//
//    // but the process instance shouldn't be able to see it
//    assertTrue(deployer.getProcessService().getVariables(pi.getId()).isEmpty());
//
//    // Setting the 'taskVar' value and completing the task should push the value
//    // into 'processVar'
//    deployer.getProcessService().setVariable(childExecutionId, "taskVar", "myValue");
//    deployer.getTaskService().complete(deployer.getTaskService().createTaskQuery().singleResult().getId());
//    vars = deployer.getProcessService().getVariables(pi.getId());
//    assertEquals(1, vars.size());
//    assertTrue(vars.containsKey("processVar"));
  }

  @Test
  @Deployment
  @Ignore // NOT YET IMPLEMENTED
  public void testUserTaskDstExprProperty() {

//    Order order = new Order();
//    Map<String, Object> vars = new HashMap<String, Object>();
//    vars.put("order", order);
//    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("testUserTaskDstExprProperty", vars);
//
//    List<Execution> childExecutions = deployer.getProcessService().findChildExecutions(pi.getId());
//    String childExecutionId = childExecutions.get(0).getId();
//
//    // The execution at the task should be able to see the 'orderAddress'
//    // variable,
//    vars = deployer.getProcessService().getVariables(childExecutionId);
//    assertEquals(1, vars.size());
//    assertTrue(vars.containsKey("orderAddress"));
//
//    // but the process instance shouldn't be able to see it
//    vars = deployer.getProcessService().getVariables(pi.getId());
//    assertEquals(1, vars.size());
//    assertTrue(vars.containsKey("order"));
//
//    // Setting the 'orderAddress' value and completing the task should push the
//    // value into order object
//    deployer.getProcessService().setVariable(childExecutionId, "orderAddress", "testAddress");
//    deployer.getTaskService().complete(deployer.getTaskService().createTaskQuery().singleResult().getId());
//    assertEquals(1, deployer.getProcessService().getVariables(pi.getId()).size());
//
//    Order orderAfterComplete = (Order) deployer.getProcessService().getVariable(pi.getId(), "order");
//    assertEquals("testAddress", orderAfterComplete.getAddress());
  }

  @Test
  @Deployment
  public void testUserTaskLinkProperty() {

//    // Start the process -> waits in usertask
//    Map<String, Object> vars = new HashMap<String, Object>();
//    vars.put("inputVar", "test");
//    ProcessInstance pi = deployer.getProcessService().startProcessInstanceByKey("testUserTaskLinkProperty", vars);
//
//    // Variable 'taskVar' should only be visible for the task scoped execution
//    Execution childExecution = deployer.getProcessService().findChildExecutions(pi.getId()).get(0);
//    assertEquals("test", deployer.getProcessService().getVariable(childExecution.getId(), "taskVar"));
//    assertEquals("test", deployer.getProcessService().getVariable(childExecution.getId(), "inputVar"));
//
//    // Change the value of variable 'taskVar' on the task scope
//    String taskScopedExecutionId = childExecution.getId();
//    deployer.getProcessService().setVariable(taskScopedExecutionId, "taskVar", "new_value");
//    assertEquals("new_value", deployer.getProcessService().getVariable(taskScopedExecutionId, "taskVar"));
//    assertEquals("test", deployer.getProcessService().getVariable(taskScopedExecutionId, "inputVar"));
//    assertNull(deployer.getProcessService().getVariable(pi.getId(), "taskVar"));
//
//    // Completing the task copies the value of 'taskVar' into 'inputVar'
//    deployer.getTaskService().complete(deployer.getTaskService().createTaskQuery().singleResult().getId());
//    assertTrue(deployer.getProcessService().findChildExecutions(pi.getId()).isEmpty()); // second
//                                                                          // task
//                                                                          // is
//                                                                          // not
//                                                                          // scoped
//    assertNull(deployer.getProcessService().findExecutionById(taskScopedExecutionId));
//    assertNull(deployer.getProcessService().getVariable(pi.getId(), "taskVar"));
//    assertEquals("new_value", deployer.getProcessService().getVariable(pi.getId(), "inputVar"));

  }

  // @Test public void testUserTaskLinkExprProperty() {
  // deployProcessForThisTestMethod();
  //    
  // // Start the process -> waits in usertask
  // Map<String, Object> address = new HashMap<String, Object>();
  // address.put("Street", "Broadway");
  // address.put("City", "New York");
  //    
  // Map<String, Object> variables = new HashMap<String, Object>();
  // variables.put("address", address);
  // ProcessInstance pi =
  // processService.startProcessInstanceByKey("testUserTaskLinkExprProperty",
  // variables);
  //    
  // // Variable 'taskVar' should only be visible for the task scoped execution
  // Execution childExecution =
  // processService.findChildExecutions(pi.getId()).get(0);
  // assertEquals("test", processService.getVariable(childExecution.getId(),
  // "taskVar"));
  // assertEquals("test", processService.getVariable(childExecution.getId(),
  // "inputVar"));
  //    
  // // Change the value of variable 'taskVar' on the task scope
  // String taskScopedExecutionId = childExecution.getId();
  // processService.setVariable(taskScopedExecutionId, "taskVar", "new_value");
  // assertEquals("new_value", processService.getVariable(taskScopedExecutionId,
  // "taskVar"));
  // assertEquals("test", processService.getVariable(taskScopedExecutionId,
  // "inputVar"));
  // assertNull(processService.getVariable(pi.getId(), "taskVar"));
  //    
  // // Completing the task copies the value of 'taskVar' into 'inputVar'
  // taskService.complete(taskService.createTaskQuery().singleResult().getId());
  // assertTrue(processService.findChildExecutions(pi.getId()).isEmpty()); //
  // second task is not scoped
  // assertNull(processService.findExecutionById(taskScopedExecutionId));
  // assertNull(processService.getVariable(pi.getId(), "taskVar"));
  // assertEquals("new_value", processService.getVariable(pi.getId(),
  // "inputVar"));
  // }

}
