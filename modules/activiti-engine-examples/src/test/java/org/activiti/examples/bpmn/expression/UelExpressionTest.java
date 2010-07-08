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

import static org.junit.Assert.assertEquals;

import org.activiti.ProcessInstance;
import org.activiti.ProcessService;
import org.activiti.Task;
import org.activiti.TaskService;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.activiti.util.CollectionUtil;
import org.junit.Rule;
import org.junit.Test;


/**
 * @author Joram Barrez
 */
public class UelExpressionTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();
  
  @Test
  @ProcessDeclared
  public void testValueAndMethodExpression() {
    ProcessService processService = deployer.getProcessService();
    TaskService taskService = deployer.getTaskService();
    
    // An order of price 150 is a standard order (goes through an UEL value expression)
    UelExpressionTestOrder order = new UelExpressionTestOrder(150);
    ProcessInstance processInstance = processService.startProcessInstanceByKey("uelExpressions", 
            CollectionUtil.singletonMap("order",  order));
    Task task = taskService.createTaskQuery().processInstance(processInstance.getId()).singleResult();
    assertEquals("Standard service", task.getName());
    
    // While an order of 300, gives us a premium service (goes through an UEL method expression)
    order = new UelExpressionTestOrder(300);
    processInstance = processService.startProcessInstanceByKey("uelExpressions",
            CollectionUtil.singletonMap("order",  order));
    task = taskService.createTaskQuery().processInstance(processInstance.getId()).singleResult();
    assertEquals("Premium service", task.getName());
    
  }

}
