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
package org.activiti.examples.bpmn.executionlistener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * @author Tijs Rademakers
 */
public class ScriptExecutionListenerTest extends PluggableActivitiTestCase {

	@Deployment(resources = { "org/activiti/examples/bpmn/executionlistener/ScriptExecutionListenerTest.bpmn20.xml" })
	public void testScriptExecutionListener() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("scriptExecutionListenerProcess");     

		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
  		List<HistoricVariableInstance> historicVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list();
  		Map<String, Object> varMap = new HashMap<String, Object>();
  		for (HistoricVariableInstance historicVariableInstance : historicVariables) {
  		  varMap.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
      }
  		
  		assertTrue(varMap.containsKey("foo"));
  		assertEquals("FOO", varMap.get("foo"));
  		assertTrue(varMap.containsKey("var1"));
      assertEquals("test", varMap.get("var1"));
      assertFalse(varMap.containsKey("bar"));
      assertTrue(varMap.containsKey("myVar"));
      assertEquals("BAR", varMap.get("myVar"));
		}
	}

}
