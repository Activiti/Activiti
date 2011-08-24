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
package org.activiti.examples.bpmn.gateway;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Example of using the exclusive gateway.
 * 
 * @author Joram Barrez
 * @author Tom Van Buskirk
 */
public class InclusiveGatewayTest extends PluggableActivitiTestCase {

  private static final String TASK1_NAME = "Send e-mail for more information";
  private static final String TASK2_NAME = "Check account balance";
  private static final String TASK3_NAME = "Call customer";

  /**
   * The test process has an OR gateway where, the 'input' variable is used to
   * select the expected outgoing sequence flow.
   */
  @Deployment
  public void testDecisionFunctionality() {

    Map<String, Object> variables = new HashMap<String, Object>();

    // Test with input == 1
    variables.put("input", 1);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("inclusiveGateway", variables);
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(3, tasks.size());
    Map<String, String> expectedMessages = new HashMap<String, String>();
    expectedMessages.put(TASK1_NAME, TASK1_NAME);
    expectedMessages.put(TASK2_NAME, TASK2_NAME);
    expectedMessages.put(TASK3_NAME, TASK3_NAME);
    for (Task task : tasks) {
      expectedMessages.remove(task.getName());
    }
    assertEquals(0, expectedMessages.size());

    // Test with input == 2
    variables.put("input", 2);
    pi = runtimeService.startProcessInstanceByKey("inclusiveGateway", variables);
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(2, tasks.size());
    expectedMessages = new HashMap<String, String>();
    expectedMessages.put(TASK2_NAME, TASK2_NAME);
    expectedMessages.put(TASK3_NAME, TASK3_NAME);
    for (Task task : tasks) {
      expectedMessages.remove(task.getName());
    }
    assertEquals(0, expectedMessages.size());

    // Test with input == 3
    variables.put("input", 3);
    pi = runtimeService.startProcessInstanceByKey("inclusiveGateway", variables);
    tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
    assertEquals(1, tasks.size());
    expectedMessages = new HashMap<String, String>();
    expectedMessages.put(TASK3_NAME, TASK3_NAME);
    for (Task task : tasks) {
      expectedMessages.remove(task.getName());
    }
    assertEquals(0, expectedMessages.size());

    // Test with input == 4
    variables.put("input", 4);
    try {
      runtimeService.startProcessInstanceByKey("inclusiveGateway", variables);
      fail();
    } catch (ActivitiException e) {
      // Exception is expected since no outgoing sequence flow matches
    }

  }

}
