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

package org.activiti.engine.test.el;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * @author Frederik Heremans
 */
public class ExpressionManagerTest extends ActivitiInternalTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }


  @Deployment
  public void testResolveUserUsingMethodExpressionOnValueExpressionField() {
    // This test will check that, even when we use a methodExpression in a field
    // where a value expression is expected, resolving the value still works.
    // In this case, we sey the assignee in the processdefinition xml to
    // ${userstring.substring(3,7)}
    // which should result in a task that is assigned to user with id 'user'.
    User user = identityService.newUser("user");
    identityService.saveUser(user);

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("userstring", "XXXuserXXX");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("resolveUserProcess", vars);

    // Assignee should be resolved to user, evaluating
    // ${userstring.substring(3,7)}
    Task task = taskService.createTaskQuery().assignee(user.getId()).singleResult();
    assertNotNull(task);

    runtimeService.deleteProcessInstance(pi.getId(), null);
    identityService.deleteUser(user.getId());
  }
  
  @Deployment
  public void testMethodExpressions() {
    // Process contains 2 service tasks. one containing a method with no params, the other
    // contains a method with 2 params. When the process completes without exception,
    // test passed.
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("aString", "abcdefgh");
    runtimeService.startProcessInstanceByKey("methodExpressionProcess", vars);
    
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey("methodExpressionProcess").count());
  }
  
  @Deployment
  public void testExecutionAvailable() {
    Map<String, Object> vars = new HashMap<String, Object>();
   
    vars.put("myVar", new ExecutionTestVariable());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testExecutionAvailableProcess", vars);
    
    // Check of the testMethod has been called with the current execution
    String value = (String) runtimeService.getVariable(processInstance.getId(), "testVar");
    assertNotNull(value);
    assertEquals("myValue", value);
  }
}
