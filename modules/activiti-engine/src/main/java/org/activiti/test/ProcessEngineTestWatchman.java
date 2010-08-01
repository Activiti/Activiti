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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.HistoricDataService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineBuilder;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.JobHandlers;
import org.junit.Assert;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

/**
 * A JUnit &#64;Rule that bootstraps a {@link ProcessEngine} and makes it
 * available to test methods.
 * 
 * @author Tom Baeyens
 * @author Dave Syer
 */
public class ProcessEngineTestWatchman extends TestWatchman {

  private static final List<String> EXCLUDED_TABLES = Arrays.asList(
          "ACT_PROPERTY",
          "ACT_H_PROCINST",
          "ACT_H_ACTINST"
  );

  private static Logger log = Logger.getLogger(ProcessEngineTestWatchman.class.getName());

  private final String configurationResource;

  private ProcessEngine processEngine;

  private boolean succeeded = false;

  public ProcessEngineTestWatchman() {
    this("activiti.properties");
  }

  public ProcessEngineTestWatchman(String configurationResource) {
    this.configurationResource = configurationResource;
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public RepositoryService getRepositoryService() {
    return processEngine == null ? null : processEngine.getRepositoryService();
  }

  public RuntimeService getProcessService() {
    return processEngine == null ? null : processEngine.getRuntimeService();
  }

  public HistoricDataService getHistoricDataService() {
    return processEngine == null ? null : processEngine.getHistoricDataService();  
  }

  public IdentityService getIdentityService() {
    return processEngine == null ? null : processEngine.getIdentityService();
  }

  public TaskService getTaskService() {
    return processEngine == null ? null : processEngine.getTaskService();
  }

  public ManagementService getManagementService() {
    return processEngine == null ? null : processEngine.getManagementService();
  }

  @Override
  public void starting(FrameworkMethod method) {
    // Create a process engine if we don't have one
    if (processEngine == null) {
      buildProcessEngine();
    }
  }

  public void buildProcessEngine() {
    log.fine("Creating process engine: " + configurationResource);
    processEngine = new ProcessEngineBuilder().configureFromPropertiesResource(configurationResource).buildProcessEngine();
  }

  @Override
  public void succeeded(FrameworkMethod method) {
    succeeded = true;
  }

  @Override
  public void finished(FrameworkMethod method) {
    try {
      assertDatabaseIsClean();
    } catch (AssertionError e) {
      if (succeeded) {
        throw e;
      } else {
        log.log(Level.SEVERE, "Assertion failed in clean up after unsuccessful test", e);
      }
    }
    if (processEngine != null) {
      processEngine.close();
      processEngine = null;
    }
  }

  public void assertProcessEnded(final String processInstanceId) {
    assertThat("An active execution with id " + processInstanceId + " was found when expecting none (it should have ended and been removed).", processEngine
            .getRuntimeService().findProcessInstanceById(processInstanceId), nullValue());
  }

  /**
   * asserts that the database is clean after a test. Normally called
   * automatically, but exposed as a public method in case it is needed as a
   * manual check.
   */
  public void assertDatabaseIsClean() {
    Map<String, Long> tableCounts = processEngine.getManagementService().getTableCount();
    StringBuilder outputMessage = new StringBuilder();
    for (String table : tableCounts.keySet()) {
      Long count = tableCounts.get(table);

      if (!EXCLUDED_TABLES.contains(table) && count != 0L) {
        outputMessage.append(table + ":" + count + " record(s) ");
      }
    }

    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "Database not clean! ");
      Assert.fail(outputMessage.toString());
    }
  }

  public void deleteTasks(Collection<String> taskIds) {
    for (String id : taskIds) {
      processEngine.getTaskService().deleteTask(id);
    }
  }

  public CommandExecutor getCommandExecutor() {
    // FIXME: downcast
    return ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutor();
  }

  public JobExecutor getJobExecutor() {
    // FIXME: downcast
    return ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getJobExecutor();
  }

  public JobHandlers getJobHandlers() {
    // FIXME: downcast
    return ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getJobHandlers();
  }

}
