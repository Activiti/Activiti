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
package org.activiti.test;

import java.util.Map;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.DbProcessEngineBuilder;
import org.activiti.IdentityService;
import org.activiti.ManagementService;
import org.activiti.ProcessEngine;
import org.activiti.ProcessService;
import org.activiti.TaskService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatchman;


/**
 * @author Tom Baeyens
 */
public abstract class ProcessEngineTestCase {
  /** 
   * The configurationResource at the time that the
   *  current {@link #processEngine} was created
   */
  protected static String processEngineConfigurationResource;
  
  protected static ProcessEngine processEngine;
  
  protected static ProcessService processService;
  
  protected static TaskService taskService;
  
  protected static ManagementService managementService;
  
  protected static IdentityService identityService;
  
  private String configurationResource = "activiti.properties";
  
  private static Logger log = Logger.getLogger(ProcessEngineTestCase.class.getName());

  @Rule
  public ProcessEngineBuilder builder = new ProcessEngineBuilder();
  
  public class ProcessEngineBuilder extends TestWatchman {
  }

  /** allows for tests to configure another configuration resource then the default activiti.properties 
   * Tests should call this method in the constructor. */
  protected void setConfigurationResource(String configurationResource) {
    this.configurationResource = configurationResource;
  }

  @Before
  public void buildProcessEngine() {
    
    // Create a process engine if we don't have one
    if (processEngine==null) {
      log.fine("Creating process engine: " + configurationResource);
      
      processEngine = new DbProcessEngineBuilder()
        .configureFromPropertiesResource(configurationResource)
        .buildProcessEngine();
      processEngineConfigurationResource = configurationResource;
      
      processService = processEngine.getProcessService();
      taskService = processEngine.getTaskService();
      managementService = processEngine.getManagementService();
      identityService = processEngine.getIdentityService();
    }
  }
  
  @After
  public void closeProcessEngine() {
    if (processEngine!=null) {
      processEngine.close();
      processEngine = null;
      processEngineConfigurationResource = null;
      processService = null;
      taskService = null;
      identityService = null;
      managementService = null;
    }
  }
    
  public void checkDbIsClean() {
    Map<String, Long> tableCounts = processEngine.getManagementService().getTableCount();
    StringBuilder outputMessage = new StringBuilder();
    for (String table : tableCounts.keySet()) {
      Long count = tableCounts.get(table);
      if (!table.equals("ACT_PROPERTY") && count != 0L) {
        outputMessage.append(table + ":" + count + " record(s) ");
      }
    }
    
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "Database not clean! ");
      throw new ActivitiException(outputMessage.toString());
    }
  }

}
