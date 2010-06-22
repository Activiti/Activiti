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
package org.activiti.test.processinstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.activiti.ActivitiException;
import org.activiti.ProcessInstanceQuery;
import org.activiti.test.ActivitiTestCase;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeclared;
import org.activiti.test.ProcessDeployer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Joram Barrez
 */
public class ProcessInstanceQueryTest extends ActivitiTestCase {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Rule
  public LogInitializer logSetup = new LogInitializer();

  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  private static String PROCESS_KEY = "oneTaskProcess";

  private static String PROCESS_KEY_2 = "oneTaskProcess2";

  @Before
  public void start() {
    for (int i = 0; i < 4; i++) {
      deployer.getProcessService().startProcessInstanceByKey(PROCESS_KEY);
    }
    deployer.getProcessService().startProcessInstanceByKey(PROCESS_KEY_2);
  }

  @Test
  @ProcessDeclared(resources = { "oneTaskProcess.bpmn20.xml", "oneTaskProcess2.bpmn20.xml" })
  public void testQueryNoSpecifics() {
    ProcessInstanceQuery query = deployer.getProcessService().createProcessInstanceQuery();
    assertEquals(5, query.count());
    assertEquals(5, query.list().size());
    exception.expect(ActivitiException.class);
    query.singleResult();
  }

  @Test
  @ProcessDeclared(resources = { "oneTaskProcess.bpmn20.xml", "oneTaskProcess2.bpmn20.xml" })
  public void testQueryByProcessDefinitionKeyUniqueResult() {
    ProcessInstanceQuery query = deployer.getProcessService().createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY);
    assertEquals(4, query.count());
    assertEquals(4, query.list().size());
    exception.expect(ActivitiException.class);
    query.singleResult();
  }

  @Test
  @ProcessDeclared(resources = { "oneTaskProcess.bpmn20.xml", "oneTaskProcess2.bpmn20.xml" })
  public void testQueryByProcessDefinitionKeySunnyDay() {
    ProcessInstanceQuery query = deployer.getProcessService().createProcessInstanceQuery().processDefinitionKey(PROCESS_KEY_2);
    assertEquals(1, query.count());
    assertEquals(1, query.list().size());
    assertNotNull(query.singleResult());
  }

}
