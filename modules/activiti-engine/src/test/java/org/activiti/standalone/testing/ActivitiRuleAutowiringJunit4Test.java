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

package org.activiti.standalone.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test class to check if spring <code><context:annotation-config/></code> works with {@link ActivitiRule}
 * 
 * @author Willem Salembier
 */
public class ActivitiRuleAutowiringJunit4Test {

  @Rule
  public ActivitiRule activitiRule = new ActivitiRule("org/activiti/standalone/testing/activiti.cfg.xml");

  @Test
  @Deployment
  public void ruleUsageExample() {
    RuntimeService runtimeService = activitiRule.getRuntimeService();
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("ruleUsageWithAutowiring");
    assertTrue(instance.isEnded());

    HistoricProcessInstanceQuery query = activitiRule.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(instance.getId())
            .includeProcessVariables();
    HistoricProcessInstance singleResult = query.singleResult();

    assertEquals("HELLO WORLD!", singleResult.getProcessVariables().get("output"));
  }

}
