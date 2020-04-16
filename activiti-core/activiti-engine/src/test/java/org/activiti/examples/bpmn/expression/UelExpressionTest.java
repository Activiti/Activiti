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

package org.activiti.examples.bpmn.expression;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 */
public class UelExpressionTest extends PluggableActivitiTestCase {

  @Deployment
  public void testValueAndMethodExpression() {
    // An order of price 150 is a standard order (goes through an UEL value
    // expression)
    UelExpressionTestOrder order = new UelExpressionTestOrder(150);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("uelExpressions", singletonMap("order", order));
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task.getName()).isEqualTo("Standard service");

    // While an order of 300, gives us a premium service (goes through an
    // UEL method expression)
    order = new UelExpressionTestOrder(300);
    processInstance = runtimeService.startProcessInstanceByKey("uelExpressions", singletonMap("order", order));
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task.getName()).isEqualTo("Premium service");

  }

}
