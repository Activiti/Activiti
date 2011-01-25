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
package org.activiti.migration.test.process.conversion;

import java.util.Map;

import org.activiti.engine.runtime.Execution;
import org.activiti.migration.test.MigrationTestCase;
import org.activiti.migration.util.XmlUtil;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.w3c.dom.Document;


/**
 * Testcase for migrating the process definition file of the simplest case possible:
 * 
 * start -> wait -> end
 * 
 * @author Joram Barrez
 */
public class SimplestProcessDefinitionConversionTest extends MigrationTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    JbpmContext jbpmContext = jbpmConfiguration.createJbpmContext();
    try {
      ProcessDefinition processDefinition = ProcessDefinition
        .parseXmlResource("org/activiti/migration/test/process/conversion/simplest/processdefinition.xml");
      jbpmContext.deployProcessDefinition(processDefinition);
      jbpmContext.newProcessInstance("simplest");
    } finally {
      jbpmContext.close();
    }
  }
  
  public void testStartProcessInstanceOfMigratedProcess() throws Exception {
    String migratedBpmn20Xml = migrateProcess("simplest");
    String deployId = repositoryService.createDeployment()
      .addString("simplest.bpmn20.xml", migratedBpmn20Xml)
      .deploy()
      .getId();
    String procId = runtimeService.startProcessInstanceByKey("simplest").getId();
    
    Execution execution = runtimeService.createExecutionQuery().processInstanceId(procId).singleResult();
    assertEquals("wait", runtimeService.getActiveActivityIds(execution.getId()).get(0));
    
    runtimeService.signal(execution.getId());
    assertProcessEnded(procId);
    
    repositoryService.deleteDeployment(deployId, true);
  }
  
}
