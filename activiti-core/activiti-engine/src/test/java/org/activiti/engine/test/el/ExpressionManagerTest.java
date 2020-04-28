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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**

 */
public class ExpressionManagerTest extends PluggableActivitiTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Deployment
  public void testMethodExpressions() {
    // Process contains 2 service tasks. one containing a method with no
    // params, the other
    // contains a method with 2 params. When the process completes without
    // exception,
    // test passed.
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("aString", "abcdefgh");
    runtimeService.startProcessInstanceByKey("methodExpressionProcess", vars);

    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("methodExpressionProcess").count()).isEqualTo(0);
  }

  @Deployment
  public void testExecutionAvailable() {
    Map<String, Object> vars = new HashMap<String, Object>();

    vars.put("myVar", new ExecutionTestVariable());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testExecutionAvailableProcess", vars);

    // Check of the testMethod has been called with the current execution
    String value = (String) runtimeService.getVariable(processInstance.getId(), "testVar");
    assertThat(value).isNotNull();
    assertThat(value).isEqualTo("myValue");
  }

  @Deployment
  public void testAuthenticatedUserIdAvailable() {
    try {
      // Setup authentication
      Authentication.setAuthenticatedUserId("frederik");
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testAuthenticatedUserIdAvailableProcess");

      // Check if the variable that has been set in service-task is the
      // authenticated user
      String value = (String) runtimeService.getVariable(processInstance.getId(), "theUser");
      assertThat(value).isNotNull();
      assertThat(value).isEqualTo("frederik");
    } finally {
      // Cleanup
      Authentication.setAuthenticatedUserId(null);
    }
  }
}
