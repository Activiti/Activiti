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

import static org.junit.Assert.assertTrue;

import org.activiti.engine.impl.scripting.ExpressionCondition;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.pvm.ObjectProcessDefinition;
import org.activiti.pvm.ObjectProcessInstance;
import org.activiti.pvm.ProcessDefinitionBuilder;
import org.junit.Test;

/**
 * @author Tom Baeyens
 */
public class PojoTest {

  @Test
  public void testPojoWaitState() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder.createProcessDefinitionBuilder().createActivity("a").initial().behavior(new WaitState())
            .transition("b").endActivity().createActivity("b").behavior(new WaitState()).transition("c").endActivity().createActivity("c").behavior(
                    new WaitState()).endActivity().build();

    ObjectProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertTrue(processInstance.isActive("a"));

    processInstance.event(null);

    assertTrue(processInstance.isActive("b"));

    processInstance.event(null);

    assertTrue(processInstance.isActive("c"));
  }

  @Test
  public void testPojoAutomatic() {
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder.createProcessDefinitionBuilder().createActivity("a").initial().behavior(new Automatic())
            .transition("b").endActivity().createActivity("b").behavior(new Automatic()).transition("c").endActivity().createActivity("c").behavior(
                    new WaitState()).endActivity().build();

    ObjectProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();

    assertTrue(processInstance.isActive("c"));
  }

  @Test
  public void testDecision() {
    ScriptingEngines scriptingEngines = new ScriptingEngines();
    ObjectProcessDefinition processDefinition = ProcessDefinitionBuilder.createProcessDefinitionBuilder().createActivity("start").initial().behavior(new Automatic())
            .transition("checkCredit").endActivity().createActivity("checkCredit").behavior(new Decision()).transition("takeToGolf",
                    new ExpressionCondition(scriptingEngines, "${creditRating=='Aaa-'}", ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE)).transition("askDaughterOut",
                    new ExpressionCondition(scriptingEngines, "${creditRating=='AAA+'}", ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE)).transition("ignore").endActivity()
            .createActivity("takeToGolf").behavior(new WaitState()).endActivity().createActivity("askDaughterOut").behavior(new WaitState()).endActivity()
            .createActivity("ignore").behavior(new WaitState()).endActivity().build();

    ObjectProcessInstance processInstance = processDefinition.createProcessInstance().variable("creditRating", "Aaa-").start();
    assertTrue(processInstance.isActive("takeToGolf"));

    processInstance = processDefinition.createProcessInstance().variable("creditRating", "AAA+").start();
    assertTrue(processInstance.isActive("askDaughterOut"));

    processInstance = processDefinition.createProcessInstance().variable("creditRating", "bb-").start();
    assertTrue(processInstance.isActive("ignore"));

  }
}
