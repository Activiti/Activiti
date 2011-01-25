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
import org.activiti.migration.Jbpm3ToActivitiMigrator;
import org.activiti.migration.util.XmlUtil;
import org.jbpm.JbpmConfiguration;
import org.w3c.dom.Document;


/**
 * @author Joram Barrez
 */
public class MigrationTestCase extends PluggableActivitiTestCase {
  
  protected static JbpmConfiguration jbpmConfiguration;

  protected Jbpm3ToActivitiMigrator migrator;
  
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    if (jbpmConfiguration == null) {
      jbpmConfiguration = JbpmConfiguration.getInstance("jbpm.test.cfg.xml");
    }
    
    this.migrator = createMigrator();
  }
  
  @Override
  protected void tearDown() throws Exception {
    this.migrator = null;
    super.tearDown();
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
  
  protected String migrateProcess(String processName) {
    migrator.migrate();
    
    Map<String, Document> migratedProcessDefinitions = migrator.getMigratedProcessDefinitions();
    assertEquals(1, migratedProcessDefinitions.size());
    
    return XmlUtil.toString(migratedProcessDefinitions.get(processName));
  }

}
