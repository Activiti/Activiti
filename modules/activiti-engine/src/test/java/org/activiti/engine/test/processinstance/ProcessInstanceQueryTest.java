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
package org.activiti.engine.test.processinstance;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessInstanceQuery;
import org.activiti.engine.test.ProcessEngineTestCase;

/**
 * @author Joram Barrez
 */
public class ProcessInstanceQueryTest extends ProcessEngineTestCase {

  private static String PROCESS_KEY = "oneTaskProcess";
  private static String PROCESS_KEY_2 = "oneTaskProcess2";

  protected void setUp() throws Exception {
    super.setUp();
    repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/processinstance/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/processinstance/oneTaskProcess2.bpmn20.xml")
      .deploy();
    
    for (int i = 0; i < 4; i++) {
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    }
    runtimeService.startProcessInstanceByKey(PROCESS_KEY_2);
  }

  protected void tearDown() throws Exception {
    for (org.activiti.engine.Deployment deployment : repositoryService.findDeployments()) {
      repositoryService.deleteDeploymentCascade(deployment.getId());
    }
    super.tearDown();
  }

  public void testQueryNoSpecifics() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertEquals(5, query.count());
    assertEquals(5, query.list().size());
    
    try { 
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }
  }

  public void testQueryByProcessDefinitionKeyMultipleResults() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY);
    assertEquals(4, query.count());
    assertEquals(4, query.list().size());

    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {
      // Exception is expected
    }
  }

  public void testQueryByProcessDefinitionKeyUniqueResult() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY_2);
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());
  }

}
