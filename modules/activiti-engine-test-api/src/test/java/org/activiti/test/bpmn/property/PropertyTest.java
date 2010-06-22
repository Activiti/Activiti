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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.Execution;
import org.activiti.ProcessInstance;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.ProcessDeclared;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Joram Barrez
 */
public class PropertyTest extends ActivitiTestCase {

  @Test
  @ProcessDeclared
  public void testUserTaskSrcProperty() {

    // Start the process -> waits in usertask
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("inputVar", "test");
    ProcessInstance pi = processEngineBuilder.getProcessService().startProcessInstanceByKey("testUserTaskSrcProperty", vars);

    // 1 task should be active, and since the task is scoped 1 child execution
    // should exist
    assertNotNull(processEngineBuilder.getTaskService().createTaskQuery().singleResult());
    List<Execution> childExecutions = processEngineBuilder.getProcessService().findChildExecutions(pi.getId());
    assertEquals(1, childExecutions.size());

    // The scope at the task should be able to see the 'myVar' variable,
    // but the process instance shouldn't be able to see it
    Execution childExecution = childExecutions.get(0);
    assertEquals("test", processEngineBuilder.getProcessService().getVariable(childExecution.getId(), "myVar"));
    assertNull(processEngineBuilder.getProcessService().getVariable(pi.getId(), "myVar"));

    // The variable 'inputVar' should be visible for both
    assertEquals("test", processEngineBuilder.getProcessService().getVariable(childExecution.getId(), "inputVar"));
    assertEquals("test", processEngineBuilder.getProcessService().getVariable(pi.getId(), "inputVar"));

    // Change the value of variable 'myVar' on the task scope
    processEngineBuilder.getProcessService().setVariable(childExecution.getId(), "myVar", "new_value");
    assertEquals("new_value", processEngineBuilder.getProcessService().getVariable(childExecution.getId(), "myVar"));
    assertEquals("test", processEngineBuilder.getProcessService().getVariable(childExecution.getId(), "inputVar"));
    assertNull(processEngineBuilder.getProcessService().getVariable(pi.getId(), "myVar"));

    // When the task completes, the variable 'myVar' is destroyed
    processEngineBuilder.getTaskService().complete(processEngineBuilder.getTaskService().createTaskQuery().singleResult().getId());
    for (Execution execution : processEngineBuilder.getProcessService().findChildExecutions(pi.getId())) {
      assertNull(processEngineBuilder.getProcessService().getVariable(execution.getId(), "myVar"));
    }
  }

  @Test
  @ProcessDeclared
  public void testUserTaskSrcExprProperty() {

    // Start the process -> waits in usertask
    final String address = "TestStreet 123 90210 Beverly-Hills";
    Order order = new Order(address);
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("order", order);
    ProcessInstance pi = processEngineBuilder.getProcessService().startProcessInstanceByKey("testUserTaskSrcExprProperty", vars);

    // The execution at the task should be able to see the 'orderAddress'
    // variable,
    // but the process instance shouldn't be able to see it
    List<Execution> childExecutions = processEngineBuilder.getProcessService().findChildExecutions(pi.getId());
    String childExecutionId = childExecutions.get(0).getId();
    assertEquals(address, processEngineBuilder.getProcessService().getVariable(childExecutionId, "orderAddress"));
    assertNull(processEngineBuilder.getProcessService().getVariable(pi.getId(), "orderAddress"));

    // Completing the task removes the 'orderAddress' variable
    processEngineBuilder.getTaskService().complete(processEngineBuilder.getTaskService().createTaskQuery().singleResult().getId());
    assertNull(processEngineBuilder.getProcessService().getVariable(pi.getId(), "orderAddress"));
    assertNotNull(processEngineBuilder.getProcessService().getVariable(pi.getId(), "order"));
  }

  @Test
  @ProcessDeclared
  public void testUserTaskDstProperty() {

    ProcessInstance pi = processEngineBuilder.getProcessService().startProcessInstanceByKey("testUserTaskDstProperty");
    List<Execution> childExecutions = processEngineBuilder.getProcessService().findChildExecutions(pi.getId());
    String childExecutionId = childExecutions.get(0).getId();

    // The execution at the task should be able to see the 'taskVar' variable,
    Map<String, Object> vars = processEngineBuilder.getProcessService().getVariables(childExecutionId);
    assertEquals(1, vars.size());
    assertTrue(vars.containsKey("taskVar"));

    // but the process instance shouldn't be able to see it
    assertTrue(processEngineBuilder.getProcessService().getVariables(pi.getId()).isEmpty());

    // Setting the 'taskVar' value and completing the task should push the value
    // into 'processVar'
    processEngineBuilder.getProcessService().setVariable(childExecutionId, "taskVar", "myValue");
    processEngineBuilder.getTaskService().complete(processEngineBuilder.getTaskService().createTaskQuery().singleResult().getId());
    vars = processEngineBuilder.getProcessService().getVariables(pi.getId());
    assertEquals(1, vars.size());
    assertTrue(vars.containsKey("processVar"));
  }

  @Test
  @ProcessDeclared
  @Ignore // NOT YET IMPLEMENTED
  public void testUserTaskDstExprProperty() {

    Order order = new Order();
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("order", order);
    ProcessInstance pi = processEngineBuilder.getProcessService().startProcessInstanceByKey("testUserTaskDstExprProperty", vars);

    List<Execution> childExecutions = processEngineBuilder.getProcessService().findChildExecutions(pi.getId());
    String childExecutionId = childExecutions.get(0).getId();

    // The execution at the task should be able to see the 'orderAddress'
    // variable,
    vars = processEngineBuilder.getProcessService().getVariables(childExecutionId);
    assertEquals(1, vars.size());
    assertTrue(vars.containsKey("orderAddress"));

    // but the process instance shouldn't be able to see it
    vars = processEngineBuilder.getProcessService().getVariables(pi.getId());
    assertEquals(1, vars.size());
    assertTrue(vars.containsKey("order"));

    // Setting the 'orderAddress' value and completing the task should push the
    // value into order object
    processEngineBuilder.getProcessService().setVariable(childExecutionId, "orderAddress", "testAddress");
    processEngineBuilder.getTaskService().complete(processEngineBuilder.getTaskService().createTaskQuery().singleResult().getId());
    assertEquals(1, processEngineBuilder.getProcessService().getVariables(pi.getId()).size());

    Order orderAfterComplete = (Order) processEngineBuilder.getProcessService().getVariable(pi.getId(), "order");
    assertEquals("testAddress", orderAfterComplete.getAddress());
  }

  @Test
  @ProcessDeclared
  public void testUserTaskLinkProperty() {

    // Start the process -> waits in usertask
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("inputVar", "test");
    ProcessInstance pi = processEngineBuilder.getProcessService().startProcessInstanceByKey("testUserTaskLinkProperty", vars);

    // Variable 'taskVar' should only be visible for the task scoped execution
    Execution childExecution = processEngineBuilder.getProcessService().findChildExecutions(pi.getId()).get(0);
    assertEquals("test", processEngineBuilder.getProcessService().getVariable(childExecution.getId(), "taskVar"));
    assertEquals("test", processEngineBuilder.getProcessService().getVariable(childExecution.getId(), "inputVar"));

    // Change the value of variable 'taskVar' on the task scope
    String taskScopedExecutionId = childExecution.getId();
    processEngineBuilder.getProcessService().setVariable(taskScopedExecutionId, "taskVar", "new_value");
    assertEquals("new_value", processEngineBuilder.getProcessService().getVariable(taskScopedExecutionId, "taskVar"));
    assertEquals("test", processEngineBuilder.getProcessService().getVariable(taskScopedExecutionId, "inputVar"));
    assertNull(processEngineBuilder.getProcessService().getVariable(pi.getId(), "taskVar"));

    // Completing the task copies the value of 'taskVar' into 'inputVar'
    processEngineBuilder.getTaskService().complete(processEngineBuilder.getTaskService().createTaskQuery().singleResult().getId());
    assertTrue(processEngineBuilder.getProcessService().findChildExecutions(pi.getId()).isEmpty()); // second
                                                                          // task
                                                                          // is
                                                                          // not
                                                                          // scoped
    assertNull(processEngineBuilder.getProcessService().findExecutionById(taskScopedExecutionId));
    assertNull(processEngineBuilder.getProcessService().getVariable(pi.getId(), "taskVar"));
    assertEquals("new_value", processEngineBuilder.getProcessService().getVariable(pi.getId(), "inputVar"));

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
