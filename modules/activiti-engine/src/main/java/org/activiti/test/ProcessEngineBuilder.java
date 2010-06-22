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
import org.activiti.ProcessEngine;
import org.activiti.ProcessService;
import org.activiti.TaskService;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

/**
 * @author Tom Baeyens
 * @author Dave Syer
 */
public class ProcessEngineBuilder extends TestWatchman {

  private static Logger log = Logger.getLogger(ProcessEngineBuilder.class.getName());

  private final String configurationResource;

  private ProcessEngine processEngine;

  public ProcessEngineBuilder() {
    this("activiti.properties");
  }

  public ProcessEngineBuilder(String configurationResource) {
    this.configurationResource = configurationResource;
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public ProcessService getProcessService() {
    return processEngine==null ? null : processEngine.getProcessService();
  }

  public IdentityService getIdentityService() {
    return processEngine==null ? null : processEngine.getIdentityService();
  }

  public TaskService getTaskService() {
    return processEngine==null ? null : processEngine.getTaskService();
  }

  @Override
  public void starting(FrameworkMethod method) {
    buildProcessEngine();
  }
  @Override
  public void finished(FrameworkMethod method) {
    closeProcessEngine();
  }

  private void buildProcessEngine() {

    // Create a process engine if we don't have one
    if (processEngine == null) {
      log.fine("Creating process engine: " + configurationResource);
      processEngine = new DbProcessEngineBuilder().configureFromPropertiesResource(configurationResource).buildProcessEngine();
    }
  }

  private void closeProcessEngine() {
    if (processEngine != null) {
      processEngine.close();
      processEngine = null;
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
