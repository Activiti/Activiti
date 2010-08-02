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
package org.activiti.examples.pojo;

import junit.framework.TestCase;

import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.pvm.ProcessDefinitionBuilder;
import org.activiti.pvm.process.PvmProcessDefinition;
import org.activiti.pvm.runtime.PvmActivityInstance;
import org.activiti.pvm.runtime.PvmProcessInstance;

/**
 * @author Tom Baeyens
 */
public class PojoTest extends TestCase {

  public void testPojoWaitState() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("a")
        .initial()
        .behavior(new WaitState())
        .transition("b")
      .endActivity()
      .createActivity("b")
        .behavior(new WaitState())
        .transition("c")
      .endActivity()
      .createActivity("c")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    PvmActivityInstance activityInstance = processInstance.findActivityInstance("a");
    assertNotNull(activityInstance);

    activityInstance.signal(null, null);
    
    activityInstance = processInstance.findActivityInstance("b");
    assertNotNull(activityInstance);

    activityInstance.signal(null, null);
    
    activityInstance = processInstance.findActivityInstance("c");
    assertNotNull(activityInstance);
  }

  public void testPojoAutomatic() {
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("a")
        .initial()
        .behavior(new Automatic())
        .transition("b")
      .endActivity()
      .createActivity("b")
        .behavior(new Automatic())
        .transition("c")
      .endActivity()
        .createActivity("c")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertNotNull(processInstance.findActivityInstance("c"));
  }

  public void testDecision() {
    ExpressionManager expressionManager = new ExpressionManager();
    PvmProcessDefinition processDefinition = new ProcessDefinitionBuilder()
      .createActivity("start")
        .initial()
        .behavior(new Automatic())
        .transition("checkCredit")
      .endActivity()
      .createActivity("checkCredit")
        .behavior(new Decision())
        .startTransition("takeToGolf")
          .property(Decision.KEY_CONDITION, expressionManager.createValueExpression("${creditRating=='Aaa-'}"))
        .endTransition()
        .startTransition("askDaughterOut")
          .property(Decision.KEY_CONDITION, expressionManager.createValueExpression("${creditRating=='AAA+'}"))
        .endTransition()
        .transition("ignore")
      .endActivity()
      .createActivity("takeToGolf")
        .behavior(new WaitState())
      .endActivity()
      .createActivity("askDaughterOut")
        .behavior(new WaitState())
      .endActivity()
      .createActivity("ignore")
        .behavior(new WaitState())
      .endActivity()
    .buildProcessDefinition();

    PvmProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.setVariable("creditRating", "Aaa-");
    processInstance.start();
    assertNotNull(processInstance.findActivityInstance("takeToGolf"));

    processInstance = processDefinition.createProcessInstance();
    processInstance.setVariable("creditRating", "AAA+");
    processInstance.start();
    assertNotNull(processInstance.findActivityInstance("askDaughterOut"));
    
    processInstance = processDefinition.createProcessInstance();
    processInstance.setVariable("creditRating", "bb-");
    processInstance.start();
    assertNotNull(processInstance.findActivityInstance("ignore"));
  }
}
