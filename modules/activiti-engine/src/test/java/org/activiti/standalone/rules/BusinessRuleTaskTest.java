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
package org.activiti.standalone.rules;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


/**
 * @author Tijs Rademakers
 */
public class BusinessRuleTaskTest extends PluggableActivitiTestCase {

  @Deployment
  public void testBusinessRuleTask() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("customRule");
    assertEquals("test2", runtimeService.getVariable(processInstance.getId(), "test"));
    
    assertTrue(CustomBusinessRuleTask.ruleInputVariables.size() == 1);
    assertEquals("order", CustomBusinessRuleTask.ruleInputVariables.get(0).getExpressionText());
    
    assertTrue(CustomBusinessRuleTask.ruleIds.size() == 2);
    assertEquals("rule1", CustomBusinessRuleTask.ruleIds.get(0).getExpressionText());
    assertEquals("rule2", CustomBusinessRuleTask.ruleIds.get(1).getExpressionText());
    
    assertTrue(CustomBusinessRuleTask.exclude);
    assertEquals("rulesOutput", CustomBusinessRuleTask.resultVariableName);
    
    runtimeService.signal(runtimeService.createExecutionQuery()
        .processInstanceId(processInstance.getId())
        .singleResult()
        .getId());
    assertProcessEnded(processInstance.getId());
  }
}
