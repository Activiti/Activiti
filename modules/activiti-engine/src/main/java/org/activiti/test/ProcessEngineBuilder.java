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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.DbProcessEngineBuilder;
import org.activiti.IdentityService;
import org.activiti.ManagementService;
import org.activiti.ProcessEngine;
import org.activiti.ProcessService;
import org.activiti.TaskService;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

/**
 * A JUnit &#64;Rule that bootstraps a {@link ProcessEngine} and makes it
 * available to test methods.
 * 
 * @author Tom Baeyens
 * @author Dave Syer
 */
public class ProcessEngineBuilder extends TestWatchman {

  private static Logger log = Logger.getLogger(ProcessEngineBuilder.class.getName());

  private final String configurationResource;

  private ProcessEngine processEngine;

  private List<Runnable> verifiers = new ArrayList<Runnable>();

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
    return processEngine == null ? null : processEngine.getProcessService();
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
    processEngine = new DbProcessEngineBuilder().configureFromPropertiesResource(configurationResource).buildProcessEngine();
  }

  @Override
  public void succeeded(FrameworkMethod method) {
    for (Runnable verifier : verifiers) {
      verifier.run();
    }
  }

  @Override
  public void finished(FrameworkMethod method) {
    assertDatabaseIsClean();
    if (processEngine != null) {
      processEngine.close();
      processEngine = null;
    }
  }

  public void assertProcessEnded(final String processInstanceId) {
    assertThat("An active execution with id " + processInstanceId + " was found when expecting none (it should have ended and been removed).", processEngine
            .getProcessService().findProcessInstanceById(processInstanceId), nullValue());
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
      if (!table.equals("ACT_PROPERTY") && count != 0L) {
        outputMessage.append(table + ":" + count + " record(s) ");
      }
    }

    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "Database not clean! ");
      throw new ActivitiException(outputMessage.toString());
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

}
