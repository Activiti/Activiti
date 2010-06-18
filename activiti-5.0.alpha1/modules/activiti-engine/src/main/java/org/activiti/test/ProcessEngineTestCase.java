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

import org.activiti.ActivitiException;
import org.activiti.Configuration;
import org.activiti.IdentityService;
import org.activiti.ManagementService;
import org.activiti.ProcessEngine;
import org.activiti.ProcessService;
import org.activiti.TaskService;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.repository.ProcessCache;


/**
 * @author Tom Baeyens
 */
public class ProcessEngineTestCase extends LogTestCase {

  protected static ProcessEngine processEngine;
  
  protected static ProcessService processService;
  
  protected static TaskService taskService;
  
  protected static ManagementService managementService;
  
  protected static IdentityService identityService;
  
  String configurationResource = "activiti.cfg.xml";

  /** allows for tests to configure another configuration resource then the default activiti.cfg.xml. 
   * Tests should call this method in the constructor. */
  protected void setConfigurationResource(String configurationResource) {
    this.configurationResource = configurationResource;
  }

  protected void setUp() throws Exception {
    buildProcessEngine();
    super.setUp();
  }

  public void buildProcessEngine() {
    if (processEngine==null) {
      
      processEngine = new Configuration()
        .configurationResource(configurationResource)
        .buildProcessEngine();
      
      processService = processEngine.getProcessService();
      taskService = processEngine.getTaskService();
      managementService = processEngine.getManagementService();
      identityService = processEngine.getIdentityService();
    }
  }
  
  public static void closeProcessEngine() {
    if (processEngine!=null) {
      processEngine.close();
      processEngine = null;
      processService = null;
      taskService = null;
      identityService = null;
      managementService = null;
    }
  }
  
  protected void tearDown() throws Exception {
    checkDbIsClean();
    super.tearDown();
  }
  
  protected void checkDbIsClean() {
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
  
  protected void resetProcessCache() {
    ProcessEngineImpl processEngineImpl = (ProcessEngineImpl) processEngine;
    ProcessCache processCache = processEngineImpl.getConfigurationObject(
            Configuration.NAME_PROCESSCACHE, ProcessCache.class);
    processCache.reset();
  }

}
