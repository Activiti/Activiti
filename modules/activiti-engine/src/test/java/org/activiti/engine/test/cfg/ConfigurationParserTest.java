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

package org.activiti.engine.test.cfg;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.ConfigurationParse;
import org.activiti.engine.impl.cfg.ConfigurationParser;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.PvmTestCase;


/**
 * @author Joram Barrez
 */
public class ConfigurationParserTest extends PvmTestCase {
  
  protected ConfigurationParser configurationParser;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.configurationParser = new ConfigurationParser();
  }
  
  public void testInvalidXml() {
    try {
      configurationParser.createParse()
        .sourceResource("org/activiti/engine/test/cfg/invalid.activiti.cfg.xml")
        .execute();
      fail("Invalid config xml should not parse");
    } catch (ActivitiException e) {
      assertTextPresent("Invalid root element", e.getMessage());
    }
  }
  
  public void testMissingDbConfiguration() {
    try {
      configurationParser.createParse()
        .sourceResource("org/activiti/engine/test/cfg/no-db-config.activiti.cfg.xml")
        .execute();
      fail("Invalid config xml should not parse");
    } catch (ActivitiException e) {
      assertTextPresent("Could not find required element 'database'", e.getMessage());
    }
  }
  
  public void testOnlyDbConfiguration() {
    ConfigurationParse parse = 
      configurationParser.createParse()
      .sourceResource("org/activiti/engine/test/cfg/only-db-config.activiti.cfg.xml")
      .execute();
    assertNotNull(parse);
    assertEquals("only-db", parse.getProcessEngineName());
  }
  
  public void testMultipleDbConfigurations() {
    try {
      configurationParser.createParse()
        .sourceResource("org/activiti/engine/test/cfg/multiple-db-config.activiti.cfg.xml")
        .execute();
      fail("Invalid config xml should not parse");
    } catch (ActivitiException e) {
      assertTextPresent("multiple elements with tag name database found", e.getMessage());
    }
  }
  
  public void testCompleteConfiguration() {
    ConfigurationParse parse = 
      configurationParser.createParse()
      .sourceResource("org/activiti/engine/test/cfg/complete.activiti.cfg.xml")
      .execute();

    assertNotNull(parse);
    assertEquals("complete-cfg", parse.getProcessEngineName());
    
    assertEquals("localhost", parse.getMailServerHost());
    assertEquals(new Integer(5025), parse.getMailServerPort());
    assertNull(parse.getMailDefaultFrom());
    assertNull(parse.getMailServerUsername());
    assertNull(parse.getMailServerPassword());
    
    assertEquals("h2", parse.getDatabaseType());
    assertEquals("create-if-necessary", parse.getDatabaseSchemaStrategy());
    assertEquals("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000", parse.getJdbcUrl());
    assertEquals("org.h2.Driver", parse.getJdbcDriver());
    assertEquals("sa", parse.getJdbcUsername());
    assertEquals("", parse.getJdbcPassword());
    
    assertTrue(parse.getJobExecutorActivate());
    
    assertEquals(ProcessEngineConfigurationImpl.parseHistoryLevel("audit"), parse.getHistoryLevel());
  }
  
  public void testConfigurationWithSchemaLocations() {
    ConfigurationParse parse = 
      configurationParser.createParse()
      .sourceResource("org/activiti/engine/test/cfg/activiti.cfg.with.schemalocation.xml")
      .execute();
    assertNotNull(parse);
    assertEquals("with-schema", parse.getProcessEngineName());
  }

}
