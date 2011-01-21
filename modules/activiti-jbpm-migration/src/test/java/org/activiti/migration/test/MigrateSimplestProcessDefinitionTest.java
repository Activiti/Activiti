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
package org.activiti.migration.test;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.Execution;
import org.activiti.migration.Jbpm3ToActivitiMigrator;
import org.activiti.migration.util.XmlUtil;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.junit.Test;
import org.w3c.dom.Document;


/**
 * Testcase for migrating the process definition file of the simplest case possible:
 * 
 * start -> wait -> end
 * 
 * @author Joram Barrez
 */
public class MigrateSimplestProcessDefinitionTest extends PluggableActivitiTestCase {
  
  @Test
  public void testStartProcessInstanceOfMigratedProcess() throws Exception {
    generateJbpmData();
    Jbpm3ToActivitiMigrator migrator = createMigrator();
    migrator.migrate();
    
    Map<String, Document> migratedProcessDefinitions = migrator.getMigratedProcessDefinitions();
    assertEquals(1, migratedProcessDefinitions.size());
    
    String migratedBpmn20Xml = XmlUtil.toString(migratedProcessDefinitions.get("simplest"));
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
  
  protected Jbpm3ToActivitiMigrator createMigrator() throws IOException {
    Properties jbpm3DbProperties = new Properties();
    jbpm3DbProperties.load(this.getClass().getClassLoader().getResourceAsStream("jbpm3.db.properties"));

    Properties activitiDbProperties = new Properties();
    activitiDbProperties.load(this.getClass().getClassLoader().getResourceAsStream("activiti.db.properties"));
    
    Jbpm3ToActivitiMigrator migrator = new Jbpm3ToActivitiMigrator();
    migrator.configureFromProperties(jbpm3DbProperties, activitiDbProperties);
    return migrator;
  }
  
  public void generateJbpmData() {
    JbpmConfiguration config = JbpmConfiguration.getInstance("jbpm.test.cfg.xml");
    JbpmContext jbpmContext = config.createJbpmContext();
    try {
      ProcessDefinition processDefinition = ProcessDefinition
        .parseXmlResource("org/activiti/migration/test/simplest/processdefinition.xml");
      jbpmContext.deployProcessDefinition(processDefinition);
      jbpmContext.newProcessInstance("simplest");
    } finally {
      jbpmContext.close();
    }
  }

}
