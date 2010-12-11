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

package org.activiti.engine.test.bpmn.parse;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;


/**
 * Test to validate that processes like
 * 
 * &lt;bpmn:process ..
 *   &lt;startEvent ...
 *   
 * are parseable.
 * 
 * @author Joram Barrez
 */
public class BpmnParseTest extends PluggableActivitiTestCase {
  
  public void testParseWithBpmnNamespacePrefix() {
      repositoryService.createDeployment()
        .addClasspathResource("org/activiti/engine/test/bpmn/parse/BpmnParseTest.testParseWithBpmnNamespacePrefix.bpmn20.xml")
        .deploy();
      assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
      
      repositoryService.deleteDeploymentCascade(repositoryService.createDeploymentQuery().singleResult().getId());
  }

}
