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
package org.activiti.compatibility.test;

import static org.junit.Assert.*;

import java.util.Map;

import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;

public class JavaDelegateTest extends AbstractActiviti6CompatibilityTest {

  @Test
  public void testActiviti5JavaDelegate() {
    
    // Check data for existing process
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processDefinitionKey("javaDelegateTestProcess").singleResult();
    assertNotNull(processInstance);
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(1, variables.size());
    assertNotNull(variables.get("testVar"));
    
    // Redploying it. Note that we have a new delegate now!
    repositoryService.createDeployment().addClasspathResource("javaDelegateProcess-activiti6.bpmn20.xml").deploy();
    processInstance = runtimeService.startProcessInstanceByKey("javaDelegateTestProcess");
    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(1, variables.size());
    assertNotNull(variables.get("testVarFromActiviti6"));
    assertNull(variables.get("testVar"));
  }

}
