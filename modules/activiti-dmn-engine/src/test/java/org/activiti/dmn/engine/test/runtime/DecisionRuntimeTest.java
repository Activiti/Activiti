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

package org.activiti.dmn.engine.test.runtime;

import java.util.HashMap;
import java.util.Map;

import org.activiti.dmn.api.DmnRuleService;
import org.activiti.dmn.api.RuleEngineExecutionResult;
import org.activiti.dmn.engine.DmnEngine;
import org.activiti.dmn.engine.test.ActivitiDmnRule;
import org.activiti.dmn.engine.test.DmnDeploymentAnnotation;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;



public class DecisionRuntimeTest {

  @Rule
  public ActivitiDmnRule activitiDmnRule = new ActivitiDmnRule();
    
    @Test
    @DmnDeploymentAnnotation
    public void ruleUsageExample() {
      DmnEngine dmnEngine = activitiDmnRule.getDmnEngine();
      DmnRuleService dmnRuleService = dmnEngine.getDmnRuleService();
    
      Map<String, Object> inputMap = new HashMap<>();
      inputMap.put("inputVariable1", 2);
      inputMap.put("inputVariable2", "inputval2");
    
      RuleEngineExecutionResult result = dmnRuleService.executeDecisionByKey("decision1", inputMap);
    
      Assert.assertEquals("outputval2", result.getResultVariables().get("outputVariable1"));
      Assert.assertEquals("result2", result.getResultVariables().get("outputVariable2"));
      }

}
